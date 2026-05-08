package com.dailyfinance.service;

import com.dailyfinance.mapper.MonthlyBudgetMapper;
import com.dailyfinance.mapper.RecordMapper;
import com.dailyfinance.mapper.UserProfileMapper;
import com.dailyfinance.model.MonthlyBudget;
import com.dailyfinance.model.RecordView;
import com.dailyfinance.model.UserProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    private final RecordMapper recordMapper;
    private final MonthlyBudgetMapper budgetMapper;
    private final UserProfileMapper profileMapper;

    public StatisticsService(RecordMapper recordMapper, MonthlyBudgetMapper budgetMapper, UserProfileMapper profileMapper) {
        this.recordMapper = recordMapper;
        this.budgetMapper = budgetMapper;
        this.profileMapper = profileMapper;
    }

    public Map<String, Object> home(Long userId) {
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(today);
        Map<String, Object> todaySum = withBalance(recordMapper.sumBetween(userId, today, today));
        Map<String, Object> monthSum = withBalance(recordMapper.sumBetween(userId, month.atDay(1), month.atEndOfMonth()));
        BigDecimal budget = budgetOf(userId, month);
        BigDecimal expense = number(monthSum.get("expense"));
        List<RecordView> recent = recordMapper.search(userId, null, null, null, null, null, 0, 8);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("today", todaySum);
        result.put("month", monthSum);
        result.put("budget", budget);
        result.put("budgetRemaining", budget.subtract(expense));
        result.put("budgetUsageRate", percent(expense, budget));
        result.put("recentRecords", recent);
        result.put("categoryOverview", recordMapper.categoryStats(userId, month.atDay(1), month.atEndOfMonth()));
        return result;
    }

    public Map<String, Object> monthly(Long userId, String monthText) {
        YearMonth month = monthText == null || monthText.isBlank() ? YearMonth.now() : YearMonth.parse(monthText);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Map<String, Object> sum = withBalance(recordMapper.sumBetween(userId, start, end));
        BigDecimal expense = number(sum.get("expense"));
        BigDecimal budget = budgetOf(userId, month);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", month.toString());
        result.put("summary", sum);
        result.put("budget", budget);
        result.put("budgetUsageRate", percent(expense, budget));
        result.put("overBudget", budget.compareTo(BigDecimal.ZERO) > 0 && expense.compareTo(budget) > 0);
        result.put("categoryStats", recordMapper.categoryStats(userId, start, end));
        result.put("trendStats", recordMapper.trendStats(userId, start, end));
        return result;
    }

    public List<Map<String, Object>> category(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? YearMonth.now().atDay(1) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        return recordMapper.categoryStats(userId, start, end);
    }

    public List<Map<String, Object>> trend(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? YearMonth.now().atDay(1) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        return recordMapper.trendStats(userId, start, end);
    }

    private BigDecimal budgetOf(Long userId, YearMonth month) {
        MonthlyBudget budget = budgetMapper.find(userId, month.toString());
        if (budget != null) {
            return budget.amount;
        }
        UserProfile profile = profileMapper.findByUserId(userId);
        return profile == null || profile.defaultMonthlyBudget == null ? BigDecimal.ZERO : profile.defaultMonthlyBudget;
    }

    private Map<String, Object> withBalance(Map<String, Object> source) {
        BigDecimal income = number(source.get("income"));
        BigDecimal expense = number(source.get("expense"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("income", income);
        result.put("expense", expense);
        result.put("balance", income.subtract(expense));
        return result;
    }

    private BigDecimal number(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.multiply(new BigDecimal("100")).divide(denominator, 2, RoundingMode.HALF_UP);
    }
}

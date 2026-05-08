package com.dailyfinance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MonthlyBudget {
    public Long id;
    public Long userId;
    public String budgetMonth;
    public BigDecimal amount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

package com.dailyfinance.mapper;

import com.dailyfinance.model.MonthlyBudget;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface MonthlyBudgetMapper {
    @Select("SELECT * FROM monthly_budget WHERE user_id = #{userId} AND budget_month = #{month} LIMIT 1")
    MonthlyBudget find(@Param("userId") Long userId, @Param("month") String month);

    @Insert("""
            INSERT INTO monthly_budget (user_id, budget_month, amount)
            VALUES (#{userId}, #{month}, #{amount})
            ON DUPLICATE KEY UPDATE amount = VALUES(amount)
            """)
    int upsert(@Param("userId") Long userId, @Param("month") String month, @Param("amount") BigDecimal amount);
}

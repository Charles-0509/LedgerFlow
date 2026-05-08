package com.dailyfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProfileRequest {
    public String nickname;

    @NotNull(message = "默认月度预算不能为空")
    @DecimalMin(value = "0.00", message = "预算不能为负数")
    public BigDecimal defaultMonthlyBudget;

    public String currency;
    public Long defaultAccountId;
}

package com.dailyfinance.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class AccountRequest {
    @NotBlank(message = "账户名称不能为空")
    public String name;

    public String type;
    public BigDecimal balance;
    public Boolean isDefault;
}

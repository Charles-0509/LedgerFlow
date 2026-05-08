package com.dailyfinance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserProfile {
    public Long id;
    public Long userId;
    public BigDecimal defaultMonthlyBudget;
    public String currency;
    public Long defaultAccountId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

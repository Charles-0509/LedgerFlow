package com.dailyfinance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    public Long id;
    public Long userId;
    public String name;
    public String type;
    public BigDecimal balance;
    public Integer isDefault;
    public Integer isDeleted;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

package com.dailyfinance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionRecord {
    public Long id;
    public Long userId;
    public String type;
    public BigDecimal amount;
    public Long categoryId;
    public Long accountId;
    public LocalDate recordDate;
    public String note;
    public Integer isDeleted;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

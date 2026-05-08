package com.dailyfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecordRequest {
    @NotBlank(message = "收支类型不能为空")
    public String type;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    public BigDecimal amount;

    @NotNull(message = "分类不能为空")
    public Long categoryId;

    public Long accountId;

    @NotNull(message = "记账日期不能为空")
    public LocalDate recordDate;

    public String note;
}

package com.dailyfinance.model;

import java.time.LocalDateTime;

public class Category {
    public Long id;
    public Long userId;
    public String name;
    public String type;
    public String icon;
    public String color;
    public Integer isSystem;
    public Integer isDeleted;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

package com.dailyfinance.model;

import java.time.LocalDateTime;

public class User {
    public Long id;
    public String username;
    public String passwordHash;
    public String nickname;
    public Integer status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

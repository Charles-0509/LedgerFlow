package com.dailyfinance.dto;

public class AuthResponse {
    public String token;
    public UserInfo user;

    public AuthResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }
}

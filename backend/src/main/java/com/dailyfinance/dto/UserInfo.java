package com.dailyfinance.dto;

public class UserInfo {
    public Long id;
    public String username;
    public String nickname;

    public UserInfo(Long id, String username, String nickname) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
    }
}

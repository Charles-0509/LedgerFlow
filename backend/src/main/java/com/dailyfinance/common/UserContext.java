package com.dailyfinance.common;

public class UserContext {
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    public static Long requireUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}

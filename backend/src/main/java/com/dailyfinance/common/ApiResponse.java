package com.dailyfinance.common;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "success", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "success", null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

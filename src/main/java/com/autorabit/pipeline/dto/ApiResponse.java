package com.autorabit.pipeline.dto;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for all REST endpoints.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {}

    private ApiResponse(boolean success, String message, T data) {
        this.success   = success;
        this.message   = message;
        this.data      = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // ---- Getters ----

    public boolean isSuccess()          { return success; }
    public String getMessage()          { return message; }
    public T getData()                  { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setSuccess(boolean s)   { this.success = s; }
    public void setMessage(String m)    { this.message = m; }
    public void setData(T data)         { this.data = data; }
    public void setTimestamp(LocalDateTime t) { this.timestamp = t; }
}

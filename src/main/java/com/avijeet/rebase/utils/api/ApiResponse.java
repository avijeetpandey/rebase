package com.avijeet.rebase.utils.api;

import lombok.Getter;

@Getter
public class ApiResponse <T> {
    private final boolean isError;
    private final String message;
    private final T data;

    public ApiResponse(boolean isError, String message, T data) {
        this.isError = isError;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}

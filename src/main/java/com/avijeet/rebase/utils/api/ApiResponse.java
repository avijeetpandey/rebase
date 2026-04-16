package com.avijeet.rebase.utils.api;

public class ApiResponse <T> {
    private final boolean isError;
    private final String message;
    private final T data;

    public ApiResponse(boolean isError, String message, T data) {
        this.isError = isError;
        this.message = message;
        this.data = data;
    }
}

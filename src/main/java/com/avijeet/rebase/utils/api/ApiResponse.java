package com.avijeet.rebase.utils.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Standard API response wrapper")
public class ApiResponse <T> {
    @Schema(description = "Indicates whether the response represents an error", example = "false")
    @JsonProperty("isError")
    private final boolean isError;

    @Schema(description = "Human-readable response message", example = "Request successful")
    private final String message;

    @Schema(description = "Response payload")
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

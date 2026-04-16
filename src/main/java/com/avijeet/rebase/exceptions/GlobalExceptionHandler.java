package com.avijeet.rebase.exceptions;

import com.avijeet.rebase.dto.ErrorResponse;
import com.avijeet.rebase.utils.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExisitsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserAlreadyExists(UserAlreadyExisitsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), "Conflict while creating user");
    }

    @ExceptionHandler({UserNotFoundException.class, InvalidArgumentsException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid request");
    }

    @ExceptionHandler({AuthenticationFailedException.class, InvalidTokenException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnauthorized(RuntimeException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "Authentication failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnhandled(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> build(HttpStatus status, String message, String details) {
        ErrorResponse errorResponse = new ErrorResponse(message, details, LocalDateTime.now());
        return ResponseEntity.status(status).body(ApiResponse.error(message, errorResponse));
    }
}


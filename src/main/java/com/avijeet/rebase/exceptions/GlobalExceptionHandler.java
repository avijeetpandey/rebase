package com.avijeet.rebase.exceptions;

import com.avijeet.rebase.dto.ErrorResponse;
import com.avijeet.rebase.utils.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExisitsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserAlreadyExists(UserAlreadyExisitsException ex, HttpServletRequest request) {
        log.warn("Conflict error path={} message={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), "Conflict while creating user");
    }

    @ExceptionHandler({UserNotFoundException.class, InvalidArgumentsException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad request path={} message={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid request");
    }

    @ExceptionHandler({AuthenticationFailedException.class, InvalidTokenException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnauthorized(RuntimeException ex, HttpServletRequest request) {
        log.warn("Unauthorized request path={} message={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "Authentication failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed path={} details={}", request.getRequestURI(), details);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint validation failed path={} message={}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request constraints are not satisfied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception path={}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Please contact support if the problem persists");
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> build(HttpStatus status, String message, String details) {
        ErrorResponse errorResponse = new ErrorResponse(message, details, LocalDateTime.now());
        return ResponseEntity.status(status).body(ApiResponse.error(message, errorResponse));
    }
}


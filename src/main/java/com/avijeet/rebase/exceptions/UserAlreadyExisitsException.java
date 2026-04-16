package com.avijeet.rebase.exceptions;

public class UserAlreadyExisitsException extends RuntimeException {
    public UserAlreadyExisitsException(String message) {
        super(message);
    }
}

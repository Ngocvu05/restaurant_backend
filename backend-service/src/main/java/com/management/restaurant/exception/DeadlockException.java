package com.management.restaurant.exception;

// Custom Exception
public class DeadlockException extends RuntimeException {
    public DeadlockException(String message, Throwable cause) {
        super(message, cause);
    }
}

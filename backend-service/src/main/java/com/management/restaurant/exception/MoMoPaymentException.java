package com.management.restaurant.exception;

import lombok.Getter;

@Getter
public class MoMoPaymentException extends RuntimeException {
    private final String errorCode;

    public MoMoPaymentException(String message) {
        super(message);
        this.errorCode = "MOMO_ERROR";
    }

    public MoMoPaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MoMoPaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MOMO_ERROR";
    }

}

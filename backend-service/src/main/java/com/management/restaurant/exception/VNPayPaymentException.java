package com.management.restaurant.exception;

import lombok.Getter;

@Getter
public class VNPayPaymentException extends RuntimeException {
    private final String errorCode;

    public VNPayPaymentException(String message) {
        super(message);
        this.errorCode = "VNPAY_ERROR";
    }

    public VNPayPaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public VNPayPaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VNPAY_ERROR";
    }

}

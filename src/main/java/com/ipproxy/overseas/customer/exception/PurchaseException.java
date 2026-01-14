package com.ipproxy.overseas.customer.exception;

import lombok.Getter;

@Getter
public class PurchaseException extends Exception {
    private int errorCode;

    public PurchaseException(String message) {
        super(message);
    }

    public PurchaseException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PurchaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
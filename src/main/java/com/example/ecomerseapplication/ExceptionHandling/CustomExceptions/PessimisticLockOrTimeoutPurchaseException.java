package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class PessimisticLockOrTimeoutPurchaseException extends RuntimeException {
    public PessimisticLockOrTimeoutPurchaseException(String message) {
        super(message);
    }
}

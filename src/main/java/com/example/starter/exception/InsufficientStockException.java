package com.example.starter.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String componentName, int available, int requested) {
        super(String.format("「%s」庫存不足，剩餘 %d，需要 %d", componentName, available, requested));
    }
}
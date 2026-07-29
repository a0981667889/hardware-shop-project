package com.example.starter.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("找不到訂單，id = " + id);
    }
}
package com.example.starter.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long componentId) {
        super("購物車裡沒有這個零件,componentId = " + componentId);
    }
}
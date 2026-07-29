package com.example.starter.exception;

public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(Long id) {
        super("找不到零件，id = " + id);
    }
}
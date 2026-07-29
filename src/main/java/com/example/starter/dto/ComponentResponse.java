package com.example.starter.dto;

import com.example.starter.entity.Component;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ComponentResponse {
    private final Long id;
    private final String name;
    private final String category;
    private final String brand;
    private final BigDecimal price;
    private final Integer stock;
    private final String socket;
    private final Integer powerWatt;
    private final LocalDateTime createdAt;

    public ComponentResponse(Component c) {
        this.id = c.getId();
        this.name = c.getName();
        this.category = c.getCategory();
        this.brand = c.getBrand();
        this.price = c.getPrice();
        this.stock = c.getStock();
        this.socket = c.getSocket();
        this.powerWatt = c.getPowerWatt();
        this.createdAt = c.getCreatedAt();
    }
}
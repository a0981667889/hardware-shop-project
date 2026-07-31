package com.example.starter.dto;

import com.example.starter.entity.CartItem;
import com.example.starter.entity.Component;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CartResponse {
    private final List<Item> items;
    private final BigDecimal totalAmount;

    public CartResponse(List<Item> items) {
        this.items = items;
        this.totalAmount = items.stream()
                .map(Item::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Getter
    public static class Item {
        private final Long componentId;
        private final String componentName;
        private final BigDecimal unitPrice;
        private final Integer quantity;
        private final BigDecimal subtotal;

        public Item(CartItem cartItem, Component component) {
            this.componentId = component.getId();
            this.componentName = component.getName();
            this.unitPrice = component.getPrice();
            this.quantity = cartItem.getQuantity();
            this.subtotal = component.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        }
    }
}
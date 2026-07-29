package com.example.starter.dto;

import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {
    private final Long id;
    private final String status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final List<Item> items;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream().map(Item::new).toList();
    }

    @Getter
    public static class Item {
        private final Long componentId;
        private final Integer quantity;
        private final BigDecimal unitPrice;

        public Item(OrderItem item) {
            this.componentId = item.getComponentId();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
        }
    }
}
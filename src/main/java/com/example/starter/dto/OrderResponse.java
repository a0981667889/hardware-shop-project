package com.example.starter.dto;

import com.example.starter.entity.Component;
import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
public class OrderResponse {
    private final Long id;
    private final String status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final List<Item> items;

    public OrderResponse(Order order, Map<Long, Component> componentMap) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream()
                .map(item -> new Item(item, componentMap.get(item.getComponentId())))
                .toList();
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)   // 值是 null 的欄位不會出現在 JSON 裡
    public static class Item {
        private final Long componentId;
        private final String componentName;
        private final String category;
        private final String socket;
        private final String memoryType;
        private final Integer powerWatt;
        private final Integer quantity;
        private final BigDecimal unitPrice;

        public Item(OrderItem item, Component component) {
            this.componentId = item.getComponentId();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();

            this.componentName = component != null ? component.getName() : null;
            this.category = component != null ? component.getCategory() : null;
            this.socket = component != null ? component.getSocket() : null;
            this.memoryType = component != null ? component.getMemoryType() : null;
            this.powerWatt = component != null ? component.getPowerWatt() : null;
        }
    }
}
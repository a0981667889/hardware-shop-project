package com.example.starter.dto;

import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
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

    // 多帶一個 componentNames 參數,用來把零件名稱組進每個 Item 裡
    public OrderResponse(Order order, Map<Long, String> componentNames) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream()
                .map(item -> new Item(item, componentNames.get(item.getComponentId())))
                .toList();
    }

    @Getter
    public static class Item {
        private final Long componentId;
        private final String componentName;
        private final Integer quantity;
        private final BigDecimal unitPrice;

        public Item(OrderItem item, String componentName) {
            this.componentId = item.getComponentId();
            this.componentName = componentName;
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
        }
    }
}
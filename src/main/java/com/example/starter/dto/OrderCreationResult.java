package com.example.starter.dto;

import java.util.List;

public class OrderCreationResult {
    private final boolean requiresConfirmation;
    private final List<CompatibilityWarning> warnings;
    private final OrderResponse order;

    private OrderCreationResult(boolean requiresConfirmation, List<CompatibilityWarning> warnings, OrderResponse order) {
        this.requiresConfirmation = requiresConfirmation;
        this.warnings = warnings;
        this.order = order;
    }

    public static OrderCreationResult needsConfirmation(List<CompatibilityWarning> warnings) {
        return new OrderCreationResult(true, warnings, null);
    }

    public static OrderCreationResult success(OrderResponse order) {
        return new OrderCreationResult(false, List.of(), order);
    }

    public boolean isRequiresConfirmation() { return requiresConfirmation; }
    public List<CompatibilityWarning> getWarnings() { return warnings; }
    public OrderResponse getOrder() { return order; }
}
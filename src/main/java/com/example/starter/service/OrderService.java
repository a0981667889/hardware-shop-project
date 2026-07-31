package com.example.starter.service;

import com.example.starter.dto.*;
import com.example.starter.entity.Component;
import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
import com.example.starter.exception.ComponentNotFoundException;
import com.example.starter.exception.InsufficientStockException;
import com.example.starter.exception.OrderNotFoundException;
import com.example.starter.repository.ComponentRepository;
import com.example.starter.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ComponentRepository componentRepository;
    private final CompatibilityService compatibilityService;

    @Transactional
    public OrderCreationResult createOrder(Long userId, OrderRequest request) {

        Map<Long, Component> componentMap = new HashMap<>();
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Component component = componentRepository.findById(itemReq.getComponentId())
                    .orElseThrow(() -> new ComponentNotFoundException(itemReq.getComponentId()));

            if (component.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        component.getName(), component.getStock(), itemReq.getQuantity());
            }
            componentMap.put(component.getId(), component);
        }

        List<CompatibilityWarning> warnings = compatibilityService.check(componentMap.values());

        if (!warnings.isEmpty() && !request.isConfirmIncompatible()) {
            return OrderCreationResult.needsConfirmation(warnings);
        }

        Order order = new Order();
        order.setUserId(userId);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Component component = componentMap.get(itemReq.getComponentId());

            OrderItem item = new OrderItem();
            item.setComponentId(component.getId());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(component.getPrice());
            order.addItem(item);

            total = total.add(component.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));

            component.setStock(component.getStock() - itemReq.getQuantity());
            componentRepository.save(component);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 直接複用剛剛查好的 componentMap,不用再查一次資料庫
        return OrderCreationResult.success(new OrderResponse(saved, componentMap));
    }

    public List<OrderResponse> findMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return buildResponsesWithComponents(orders);
    }

    public List<OrderResponse> findAll() {
        List<Order> orders = orderRepository.findAllWithItems();
        return buildResponsesWithComponents(orders);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        Map<Long, Component> componentMap = buildComponentMap(List.of(saved));
        return new OrderResponse(saved, componentMap);
    }

    private List<OrderResponse> buildResponsesWithComponents(List<Order> orders) {
        Map<Long, Component> componentMap = buildComponentMap(orders);
        return orders.stream()
                .map(order -> new OrderResponse(order, componentMap))
                .toList();
    }

    private Map<Long, Component> buildComponentMap(List<Order> orders) {
        List<Long> componentIds = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getComponentId)
                .distinct()
                .toList();

        return componentRepository.findAllById(componentIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Component::getId, c -> c));
    }
}
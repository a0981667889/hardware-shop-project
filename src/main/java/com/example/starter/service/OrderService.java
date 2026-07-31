package com.example.starter.service;

import com.example.starter.dto.OrderRequest;
import com.example.starter.dto.OrderResponse;
import com.example.starter.entity.Component;
import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
import com.example.starter.exception.CompatibilityException;
import com.example.starter.exception.ComponentNotFoundException;
import com.example.starter.exception.InsufficientStockException;
import com.example.starter.exception.OrderNotFoundException;
import com.example.starter.repository.ComponentRepository;
import com.example.starter.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ComponentRepository componentRepository;

    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {

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

        checkCpuMotherboardCompatibility(componentMap.values());

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

        // 下單當下手上已經有零件資料,直接組 name map,不用再查一次資料庫
        Map<Long, String> nameMap = new HashMap<>();
        componentMap.forEach((id, component) -> nameMap.put(id, component.getName()));

        return new OrderResponse(saved, nameMap);
    }

    public List<OrderResponse> findMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return buildResponsesWithNames(orders);
    }

    public List<OrderResponse> findAll() {
        List<Order> orders = orderRepository.findAllWithItems();
        return buildResponsesWithNames(orders);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        Map<Long, String> nameMap = buildNameMap(List.of(saved));
        return new OrderResponse(saved, nameMap);
    }

    /**
     * 把一批訂單轉成 OrderResponse,並且只查一次資料庫拿到所有零件名稱(避免 N+1)
     */
    private List<OrderResponse> buildResponsesWithNames(List<Order> orders) {
        Map<Long, String> nameMap = buildNameMap(orders);
        return orders.stream()
                .map(order -> new OrderResponse(order, nameMap))
                .toList();
    }

    private Map<Long, String> buildNameMap(List<Order> orders) {
        List<Long> componentIds = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getComponentId)
                .distinct()
                .toList();

        Map<Long, String> nameMap = new HashMap<>();
        componentRepository.findAllById(componentIds)
                .forEach(component -> nameMap.put(component.getId(), component.getName()));
        return nameMap;
    }

    private void checkCpuMotherboardCompatibility(java.util.Collection<Component> components) {
        Component cpu = components.stream()
                .filter(c -> "CPU".equalsIgnoreCase(c.getCategory()))
                .findFirst().orElse(null);

        Component motherboard = components.stream()
                .filter(c -> "MOTHERBOARD".equalsIgnoreCase(c.getCategory()))
                .findFirst().orElse(null);

        if (cpu != null && motherboard != null) {
            if (cpu.getSocket() == null || motherboard.getSocket() == null
                    || !cpu.getSocket().equalsIgnoreCase(motherboard.getSocket())) {
                throw new CompatibilityException(String.format(
                        "相容性錯誤：CPU「%s」(socket %s) 與主機板「%s」(socket %s) 插槽不相容",
                        cpu.getName(), cpu.getSocket(), motherboard.getName(), motherboard.getSocket()));
            }
        }
    }
}
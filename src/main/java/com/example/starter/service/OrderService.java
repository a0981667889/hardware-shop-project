package com.example.starter.service;

import com.example.starter.dto.OrderRequest;
import com.example.starter.dto.OrderResponse;
import com.example.starter.entity.Component;
import com.example.starter.entity.Order;
import com.example.starter.entity.OrderItem;
import com.example.starter.exception.CompatibilityException;
import com.example.starter.exception.ComponentNotFoundException;
import com.example.starter.exception.InsufficientStockException;
import com.example.starter.repository.ComponentRepository;
import com.example.starter.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.starter.exception.OrderNotFoundException;

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

        // 1. 撈出這次下單牽涉到的所有 Component，並檢查庫存
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

        // 2. 相容性檢查：CPU 的 socket 必須跟主機板的 socket 相同
        checkCpuMotherboardCompatibility(componentMap.values());

        // 3. 建立訂單、計算總價、扣庫存（全部在同一個 transaction）
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

            // 扣庫存
            component.setStock(component.getStock() - itemReq.getQuantity());
            componentRepository.save(component);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        return new OrderResponse(saved);
    }

    public List<OrderResponse> findMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(OrderResponse::new).toList();
    }
    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream().map(OrderResponse::new).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        return new OrderResponse(orderRepository.save(order));
    }

    /**
     * 相容性檢查核心邏輯：
     * 如果這張訂單同時包含 CPU 跟主機板，兩者的 socket 必須相同，否則擋下整張訂單。
     * 只實作這一條規則，足夠展示邏輯判斷能力，不做成通用規則引擎。
     */
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
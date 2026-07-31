package com.example.starter.service;

import com.example.starter.dto.CartItemRequest;
import com.example.starter.dto.CartResponse;
import com.example.starter.entity.CartItem;
import com.example.starter.entity.Component;
import com.example.starter.exception.CartItemNotFoundException;
import com.example.starter.exception.ComponentNotFoundException;
import com.example.starter.repository.CartItemRepository;
import com.example.starter.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ComponentRepository componentRepository;

    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return buildResponse(cartItems);
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Component component = componentRepository.findById(request.getComponentId())
                .orElseThrow(() -> new ComponentNotFoundException(request.getComponentId()));

        CartItem cartItem = cartItemRepository
                .findByUserIdAndComponentId(userId, request.getComponentId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(userId);
                    newItem.setComponentId(component.getId());
                    newItem.setQuantity(0);
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long componentId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByUserIdAndComponentId(userId, componentId)
                .orElseThrow(() -> new CartItemNotFoundException(componentId));
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long componentId) {
        cartItemRepository.deleteByUserIdAndComponentId(userId, componentId);
        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartResponse buildResponse(List<CartItem> cartItems) {
        List<Long> componentIds = cartItems.stream().map(CartItem::getComponentId).toList();
        Map<Long, Component> componentMap = componentRepository.findAllById(componentIds)
                .stream().collect(Collectors.toMap(Component::getId, c -> c));

        List<CartResponse.Item> items = cartItems.stream()
                .map(ci -> new CartResponse.Item(ci, componentMap.get(ci.getComponentId())))
                .toList();

        return new CartResponse(items);
    }
}
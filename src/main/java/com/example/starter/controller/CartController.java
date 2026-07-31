package com.example.starter.controller;

import com.example.starter.dto.CartItemRequest;
import com.example.starter.dto.CartResponse;
import com.example.starter.security.UserPrincipal;
import com.example.starter.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserPrincipal user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(user.getId(), request);
    }

    @PutMapping("/items/{componentId}")
    public CartResponse updateItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long componentId,
            @RequestParam @Min(1) Integer quantity) {
        return cartService.updateItem(user.getId(), componentId, quantity);
    }

    @DeleteMapping("/items/{componentId}")
    public CartResponse removeItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long componentId) {
        return cartService.removeItem(user.getId(), componentId);
    }

    @DeleteMapping
    public void clearCart(@AuthenticationPrincipal UserPrincipal user) {
        cartService.clearCart(user.getId());
    }
}
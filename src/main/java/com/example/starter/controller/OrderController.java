package com.example.starter.controller;

import com.example.starter.dto.OrderRequest;
import com.example.starter.dto.OrderResponse;
import com.example.starter.security.UserPrincipal;
import com.example.starter.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserPrincipal user) {
        return orderService.findMyOrders(user.getId());
    }
}
package com.example.starter.controller;

import com.example.starter.dto.OrderCreationResult;
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
import java.util.HashMap;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody OrderRequest request) {

        OrderCreationResult result = orderService.createOrder(user.getId(), request);

        if (result.isRequiresConfirmation()) {
            Map<String, Object> body = new HashMap<>();
            body.put("requiresConfirmation", true);
            body.put("message", "偵測到零件相容性問題,如仍要購買請將 confirmIncompatible 設為 true 後重新送出");
            body.put("warnings", result.getWarnings());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.getOrder());
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserPrincipal user) {
        return orderService.findMyOrders(user.getId());
    }

}
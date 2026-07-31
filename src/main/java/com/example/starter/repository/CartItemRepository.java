package com.example.starter.repository;

import com.example.starter.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndComponentId(Long userId, Long componentId);
    void deleteByUserIdAndComponentId(Long userId, Long componentId);
    void deleteByUserId(Long userId);
}
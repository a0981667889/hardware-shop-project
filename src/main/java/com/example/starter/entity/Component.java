package com.example.starter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "components")
@Getter @Setter
@NoArgsConstructor
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 一定用 IDENTITY，不要用 AUTO
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;   // CPU / MOTHERBOARD / GPU / RAM / PSU / CASE

    @Column(length = 100)
    private String brand;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 50)
    private String socket;     // CPU / 主機板才有值

    @Column(name = "power_watt")
    private Integer powerWatt; // GPU 建議瓦數 / PSU 供應瓦數

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
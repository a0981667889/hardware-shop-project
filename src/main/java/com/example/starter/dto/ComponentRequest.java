package com.example.starter.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ComponentRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    private String brand;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    private String socket;

    private Integer powerWatt;
}
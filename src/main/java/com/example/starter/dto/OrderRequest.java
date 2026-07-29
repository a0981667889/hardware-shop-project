package com.example.starter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderRequest {

    @NotEmpty(message = "訂單至少要有一項零件")
    @Valid
    private List<OrderItemRequest> items;

    @Getter @Setter
    public static class OrderItemRequest {
        @NotNull
        private Long componentId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
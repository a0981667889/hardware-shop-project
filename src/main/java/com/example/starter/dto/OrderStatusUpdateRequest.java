package com.example.starter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderStatusUpdateRequest {

    @NotBlank
    @Pattern(regexp = "PENDING|SHIPPED|COMPLETED", message = "status 必須是 PENDING / SHIPPED / COMPLETED 其中之一")
    private String status;
}
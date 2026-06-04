package com.deliveryflow.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        String description,
        @Positive BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency
) {
}

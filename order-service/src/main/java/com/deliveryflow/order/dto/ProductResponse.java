package com.deliveryflow.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String currency
) {
}

package com.deliveryflow.order.dto;

import com.deliveryflow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        List<OrderItemResponse> items,
        DeliveryAddressDto deliveryAddress,
        Instant createdAt,
        Instant updatedAt
) {
}

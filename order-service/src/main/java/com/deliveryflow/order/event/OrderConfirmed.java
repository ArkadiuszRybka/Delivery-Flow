package com.deliveryflow.order.event;

import com.deliveryflow.order.dto.DeliveryAddressDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderConfirmed(
        UUID eventId,
        UUID aggregateId,
        Instant timestamp,
        String eventType,
        int version,
        UUID customerId,
        BigDecimal totalAmount,
        String currency,
        DeliveryAddressDto deliveryAddress
) implements OrderEvent {

    public static OrderConfirmed of(UUID orderId, UUID customerId, BigDecimal totalAmount,
                                     String currency, DeliveryAddressDto deliveryAddress) {
        return new OrderConfirmed(UUID.randomUUID(), orderId, Instant.now(), "OrderConfirmed", 1,
                customerId, totalAmount, currency, deliveryAddress);
    }
}

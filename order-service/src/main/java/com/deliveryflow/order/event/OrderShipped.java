package com.deliveryflow.order.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OrderShipped(
        UUID eventId,
        UUID aggregateId,
        Instant timestamp,
        String eventType,
        int version,
        UUID customerId,
        LocalDate estimatedDelivery
) implements OrderEvent {

    public static OrderShipped of(UUID orderId, UUID customerId, LocalDate estimatedDelivery) {
        return new OrderShipped(UUID.randomUUID(), orderId, Instant.now(), "OrderShipped", 1,
                customerId, estimatedDelivery);
    }
}

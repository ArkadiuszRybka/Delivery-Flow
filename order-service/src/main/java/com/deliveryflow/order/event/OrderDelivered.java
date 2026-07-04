package com.deliveryflow.order.event;

import java.time.Instant;
import java.util.UUID;

public record OrderDelivered(
        UUID eventId,
        UUID aggregateId,
        Instant timestamp,
        String eventType,
        int version,
        UUID customerId
) implements OrderEvent {

    public static OrderDelivered of(UUID orderId, UUID customerId) {
        return new OrderDelivered(UUID.randomUUID(), orderId, Instant.now(), "OrderDelivered", 1, customerId);
    }
}

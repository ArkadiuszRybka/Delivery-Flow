package com.deliveryflow.order.event;

import com.deliveryflow.order.domain.CancellationReason;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelled(
        UUID eventId,
        UUID aggregateId,
        Instant timestamp,
        String eventType,
        int version,
        UUID customerId,
        String reason
) implements OrderEvent {

    public static OrderCancelled of(UUID orderId, UUID customerId, CancellationReason reason) {
        return new OrderCancelled(UUID.randomUUID(), orderId, Instant.now(), "OrderCancelled", 1,
                customerId, reason.name());
    }
}

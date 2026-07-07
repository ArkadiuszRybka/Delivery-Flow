package com.deliveryflow.notification.event;

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
}

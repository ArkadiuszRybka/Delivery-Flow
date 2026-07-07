package com.deliveryflow.notification.event;

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
}

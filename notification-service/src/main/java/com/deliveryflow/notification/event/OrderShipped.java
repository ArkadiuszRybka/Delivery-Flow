package com.deliveryflow.notification.event;

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
}

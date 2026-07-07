package com.deliveryflow.notification.event;

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
        DeliveryAddress deliveryAddress
) implements OrderEvent {
}

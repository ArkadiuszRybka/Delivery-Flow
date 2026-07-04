package com.deliveryflow.order.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface OrderEvent permits OrderConfirmed, OrderCancelled, OrderShipped, OrderDelivered {
    UUID eventId();
    UUID aggregateId();
    Instant timestamp();
    String eventType();
    int version();
}

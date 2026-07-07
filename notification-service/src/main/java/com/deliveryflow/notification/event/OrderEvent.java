package com.deliveryflow.notification.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderConfirmed.class, name = "OrderConfirmed"),
        @JsonSubTypes.Type(value = OrderCancelled.class, name = "OrderCancelled"),
        @JsonSubTypes.Type(value = OrderShipped.class, name = "OrderShipped"),
        @JsonSubTypes.Type(value = OrderDelivered.class, name = "OrderDelivered")
})
public sealed interface OrderEvent permits OrderConfirmed, OrderCancelled, OrderShipped, OrderDelivered {
    UUID eventId();
    UUID aggregateId();
    Instant timestamp();
    String eventType();
    int version();
}

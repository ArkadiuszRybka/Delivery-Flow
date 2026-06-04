package com.deliveryflow.order.dto;

import java.time.Instant;
import java.util.UUID;

public record TrackingInfo(
        UUID trackingId,
        UUID orderId,
        String status,
        String location,
        String note,
        Instant recordedAt
) {
}

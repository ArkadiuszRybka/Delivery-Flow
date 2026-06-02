package com.deliveryflow.tracking.dto;

import java.time.Instant;
import java.util.UUID;

public record TrackingEntryResponse(
        UUID trackingId,
        UUID orderId,
        String status,
        String location,
        String note,
        Instant recordedAt
) {
}

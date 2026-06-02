package com.deliveryflow.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTrackingEntryRequest(
        @NotNull
        UUID orderId,
        @NotBlank
        String status,
        String location,
        String note
) {
}

package com.deliveryflow.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import org.antlr.v4.runtime.misc.NotNull;

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

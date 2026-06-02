package com.deliveryflow.tracking.exception;

import java.util.UUID;

public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(UUID orderId) {
        super("No tracking entries found for orderId: " + orderId);
    }
}

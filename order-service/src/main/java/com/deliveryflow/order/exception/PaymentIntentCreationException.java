package com.deliveryflow.order.exception;

import java.util.UUID;

public class PaymentIntentCreationException extends RuntimeException {
    public PaymentIntentCreationException(UUID orderId, Throwable cause) {
        super("Failed to create payment intent for order: " + orderId, cause);
    }
}

package com.deliveryflow.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    public void sendOrderConfirmed(UUID orderId, UUID customerId) {
        log.info("[NOTIFICATION] Order {} CONFIRMED – email/SMS to customer {}", orderId, customerId);
    }

    public void sendOrderShipped(UUID orderId, UUID customerId) {
        log.info("[NOTIFICATION] Order {} SHIPPED – email/SMS to customer {}", orderId, customerId);
    }

    public void sendOrderDelivered(UUID orderId, UUID customerId) {
        log.info("[NOTIFICATION] Order {} DELIVERED – email/SMS to customer {}", orderId, customerId);
    }

    public void sendOrderCancelled(UUID orderId, UUID customerId, String reason) {
        log.info("[NOTIFICATION] Order {} CANCELLED (reason: {}) – email/SMS to customer {}", orderId, reason, customerId);
    }
}

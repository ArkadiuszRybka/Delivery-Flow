package com.deliveryflow.order.controller;

import com.deliveryflow.order.config.StripeProperties;
import com.deliveryflow.order.service.OrderService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@Slf4j
public class StripeWebhookController {

    private final OrderService orderService;
    private final StripeProperties stripeProperties;

    public StripeWebhookController(OrderService orderService, StripeProperties stripeProperties) {
        this.orderService = orderService;
        this.stripeProperties = stripeProperties;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Rejected Stripe webhook with invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            default -> log.info("Ignoring unhandled Stripe event type={}", event.getType());
        }

        return ResponseEntity.ok().build();
    }

    private void handlePaymentSucceeded(Event event) {
        Optional<UUID> orderId = extractOrderId(event);
        if (orderId.isEmpty()) {
            log.warn("Could not extract orderId from payment_intent.succeeded event eventId={}", event.getId());
            return;
        }
        orderService.confirmOrder(orderId.get());
    }

    private void handlePaymentFailed(Event event) {
        Optional<UUID> orderId = extractOrderId(event);
        if (orderId.isEmpty()) {
            log.warn("Could not extract orderId from payment_intent.payment_failed event eventId={}", event.getId());
            return;
        }
        orderService.cancelOrderForPaymentFailure(orderId.get());
    }

    private Optional<UUID> extractOrderId(Event event) {
        StripeObject stripeObject;
        Optional<StripeObject> deserialized = event.getDataObjectDeserializer().getObject();
        if (deserialized.isPresent()) {
            stripeObject = deserialized.get();
        } else {
            try {
                stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.warn("Failed to deserialize Stripe event data eventId={}: {}", event.getId(), e.getMessage());
                return Optional.empty();
            }
        }

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            return Optional.empty();
        }

        String orderId = paymentIntent.getMetadata().get("orderId");
        return Optional.ofNullable(orderId).map(UUID::fromString);
    }
}

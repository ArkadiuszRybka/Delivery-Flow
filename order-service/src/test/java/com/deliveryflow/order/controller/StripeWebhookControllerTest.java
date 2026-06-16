package com.deliveryflow.order.controller;

import com.deliveryflow.order.config.StripeProperties;
import com.deliveryflow.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class StripeWebhookControllerTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    private final OrderService orderService = mock(OrderService.class);
    private final StripeWebhookController controller =
            new StripeWebhookController(orderService, new StripeProperties("sk_test_dummy", WEBHOOK_SECRET));

    @Test
    void paymentSucceeded_confirmsOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = paymentIntentEventPayload("payment_intent.succeeded", orderId);

        ResponseEntity<Void> response = controller.handleWebhook(payload, sign(payload));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).confirmOrder(orderId);
    }

    @Test
    void paymentFailed_cancelsOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = paymentIntentEventPayload("payment_intent.payment_failed", orderId);

        controller.handleWebhook(payload, sign(payload));

        verify(orderService).cancelOrderForPaymentFailure(orderId);
    }

    @Test
    void invalidSignature_rejectedWithoutCallingOrderService() {
        String payload = paymentIntentEventPayload("payment_intent.succeeded", UUID.randomUUID());

        ResponseEntity<Void> response = controller.handleWebhook(payload, "t=1,v1=invalid");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(orderService);
    }

    @Test
    void unhandledEventType_ignoredWithoutCallingOrderService() throws Exception {
        String payload = """
                {
                  "id": "evt_test_unhandled",
                  "object": "event",
                  "type": "charge.refunded",
                  "data": { "object": { "id": "ch_test", "object": "charge" } }
                }
                """;

        ResponseEntity<Void> response = controller.handleWebhook(payload, sign(payload));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoInteractions(orderService);
    }

    private String paymentIntentEventPayload(String type, UUID orderId) {
        return """
                {
                  "id": "evt_test_1",
                  "object": "event",
                  "api_version": "2020-01-01.oldversion",
                  "type": "%s",
                  "data": {
                    "object": {
                      "id": "pi_test_1",
                      "object": "payment_intent",
                      "metadata": { "orderId": "%s" }
                    }
                  }
                }
                """.formatted(type, orderId);
    }

    private String sign(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
        return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(hash);
    }
}

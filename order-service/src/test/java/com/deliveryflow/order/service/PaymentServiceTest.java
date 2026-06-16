package com.deliveryflow.order.service;

import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void createPaymentIntent_convertsAmountAndSetsIdempotencyKeyAndMetadata() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentIntent fakeIntent = new PaymentIntent();
        fakeIntent.setId("pi_test_123");

        try (MockedStatic<PaymentIntent> mocked = mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.create(
                            argThat((PaymentIntentCreateParams params) ->
                                    params.getAmount().equals(5998L)
                                            && "pln".equals(params.getCurrency())
                                            && orderId.toString().equals(params.getMetadata().get("orderId"))),
                            argThat((RequestOptions options) -> orderId.toString().equals(options.getIdempotencyKey()))))
                    .thenReturn(fakeIntent);

            PaymentIntent result = paymentService.createPaymentIntent(orderId, new BigDecimal("59.98"), "PLN");

            assertThat(result.getId()).isEqualTo("pi_test_123");
        }
    }
}

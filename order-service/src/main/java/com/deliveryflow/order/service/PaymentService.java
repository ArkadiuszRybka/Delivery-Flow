package com.deliveryflow.order.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    public PaymentIntent createPaymentIntent(UUID orderId, BigDecimal amount, String currency) throws StripeException {
        long amountInMinorUnits = amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInMinorUnits)
                .setCurrency(currency.toLowerCase())
                .putMetadata("orderId", orderId.toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(orderId.toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params, options);
        log.info("Created payment intent paymentIntentId={} for orderId={}", paymentIntent.getId(), orderId);
        return paymentIntent;
    }
}

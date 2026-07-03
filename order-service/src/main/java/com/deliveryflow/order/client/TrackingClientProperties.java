package com.deliveryflow.order.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "clients.tracking-service")
public record TrackingClientProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}

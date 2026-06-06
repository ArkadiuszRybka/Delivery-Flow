package com.deliveryflow.order.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients.tracking-service")
public record TrackingClientProperties(String baseUrl) {
}

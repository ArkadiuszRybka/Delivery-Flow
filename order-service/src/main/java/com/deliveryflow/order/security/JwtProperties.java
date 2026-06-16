package com.deliveryflow.order.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Resource privateKey,
        Resource publicKey,
        long accessExpiration,
        long refreshExpiration
) {
}

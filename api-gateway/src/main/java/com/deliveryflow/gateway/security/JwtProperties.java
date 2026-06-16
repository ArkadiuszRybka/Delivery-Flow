package com.deliveryflow.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(Resource publicKey) {
}

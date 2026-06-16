package com.deliveryflow.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    private PublicKey publicKey;

    @PostConstruct
    void init() throws Exception {
        publicKey = readPublicKey(properties.publicKey().getContentAsString(StandardCharsets.UTF_8));
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey readPublicKey(String pem) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPemHeaders(pem));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private String stripPemHeaders(String pem) {
        return pem.replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
    }
}

package com.example.lostfound.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.charset.StandardCharsets;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, String issuer) {
    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must contain at least 32 UTF-8 bytes");
        }
        if (issuer == null || issuer.isBlank()) throw new IllegalArgumentException("JWT issuer is required");
    }
}

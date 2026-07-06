package com.calmlywriter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String cookieName,
        boolean cookieSecure
) {
    public JwtProperties {
        if (cookieName == null || cookieName.isBlank()) {
            cookieName = "auth_token";
        }
    }
}

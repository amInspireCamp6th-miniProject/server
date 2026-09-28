package com.example.server.global.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param secret               Base64로 인코딩한 HMAC 비밀키 (디코딩 후 32바이트 이상)
 * @param accessTokenValidity  Access Token 유효 기간 (예: 24h)
 * @param refreshTokenValidity Refresh Token 유효 기간 (예: 14d)
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull Duration accessTokenValidity,
        @NotNull Duration refreshTokenValidity
) {
}

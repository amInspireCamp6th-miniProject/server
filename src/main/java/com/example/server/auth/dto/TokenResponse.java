package com.example.server.auth.dto;

/**
 * 토큰 재발급 응답 본문. 새 Refresh Token은 본문이 아니라 쿠키로 나간다. (회전)
 */
public record TokenResponse(String accessToken) {
}

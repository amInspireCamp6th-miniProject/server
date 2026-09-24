package com.example.server.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

/**
 * Authorization: Bearer {token} 헤더에서 토큰을 꺼낸다. 인증 필터와 로그아웃이 같은 규칙을 쓴다.
 */
public final class BearerTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokenResolver() {
    }

    /**
     * @return 토큰. 헤더가 없거나 Bearer 형식이 아니면 null
     */
    public static String resolve(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length()).trim();
    }
}

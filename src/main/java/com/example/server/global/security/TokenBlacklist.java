package com.example.server.global.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 로그아웃한 Access Token 목록. 토큰의 원래 만료 시각까지만 보관한다.
 * 서버 메모리에 저장하므로 서버를 재시작하면 목록이 비고, 서버를 여러 대 띄우면 공유되지 않는다. (B-18)
 * 토큰 원문 대신 SHA-256 해시를 저장한다.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklist {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();
    private final Clock clock;

    public void add(String token, Instant expiresAt) {
        removeExpired();
        revokedTokens.put(hash(token), expiresAt);
    }

    public boolean contains(String token) {
        Instant expiresAt = revokedTokens.get(hash(token));
        return expiresAt != null && expiresAt.isAfter(clock.instant());
    }

    // 만료된 토큰은 서명 검증 단계에서 이미 거부되므로 목록에 남겨 둘 필요가 없다.
    private void removeExpired() {
        Instant now = clock.instant();
        revokedTokens.values().removeIf(expiresAt -> !expiresAt.isAfter(now));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}

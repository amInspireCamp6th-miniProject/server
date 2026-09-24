package com.example.server.global.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Refresh Token 발급·검증·폐기. 토큰 자체는 JWT(JwtProvider)이고, 발급한 토큰 목록을 함께 보관한다.
 * 서명만으로 검증하면 회전(한 번 쓰면 폐기)과 로그아웃 시 폐기를 할 수 없어서 목록이 필요하다.
 * 서버 메모리에 저장하므로 서버를 재시작하면 모두 무효가 되고, 서버를 여러 대 띄우면 공유되지 않는다. (B-18)
 * 토큰 원문 대신 SHA-256 해시를 저장한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final Map<String, StoredToken> issuedTokens = new ConcurrentHashMap<>();
    private final JwtProvider jwtProvider;
    private final Clock clock;

    public String issue(Long userId) {
        removeExpired();
        String token = jwtProvider.createRefreshToken(userId);
        issuedTokens.put(hash(token), new StoredToken(userId, jwtProvider.getExpiration(token)));
        return token;
    }

    /**
     * 토큰을 한 번만 쓰고 폐기한다. (회전) 재발급 때마다 새 토큰을 내주므로 같은 값은 두 번 통하지 않는다.
     *
     * @return 토큰의 주인 회원 ID. 서명·유효기간·종류가 맞지 않거나 이미 쓴 토큰이면 빈 값
     */
    public Optional<Long> consume(String token) {
        Optional<Long> userId = jwtProvider.findRefreshTokenUserId(token);
        if (userId.isEmpty()) {
            return Optional.empty();
        }
        // 발급 목록에 없으면 이미 썼거나(회전) 로그아웃으로 폐기된 토큰이다.
        return issuedTokens.remove(hash(token)) == null ? Optional.empty() : userId;
    }

    /**
     * 해당 회원의 Refresh Token을 모두 폐기한다. 로그아웃에서 사용한다.
     * 폐기하지 않으면 로그아웃한 뒤에도 재발급으로 새 Access Token을 받을 수 있다.
     */
    public void revokeAll(Long userId) {
        issuedTokens.values().removeIf(stored -> stored.userId().equals(userId));
    }

    private void removeExpired() {
        Instant now = clock.instant();
        issuedTokens.values().removeIf(stored -> !stored.expiresAt().isAfter(now));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }

    private record StoredToken(Long userId, Instant expiresAt) {
    }
}

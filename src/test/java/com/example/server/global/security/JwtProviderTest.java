package com.example.server.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.server.global.exception.BusinessException;
import com.example.server.global.exception.GlobalErrorCode;
import io.jsonwebtoken.security.WeakKeyException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = encode("test-secret-key-must-be-at-least-32-bytes!!");
    private static final Duration VALIDITY = Duration.ofHours(1);
    private static final Instant NOW = Instant.parse("2026-09-22T00:00:00Z");

    private final JwtProvider jwtProvider = providerAt(NOW, SECRET);

    @Test
    void 발급한_토큰에서_사용자_ID를_꺼낸다() {
        String token = jwtProvider.createAccessToken(42L);

        assertThat(jwtProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void 발급한_토큰의_만료_시각은_발급_시각에_유효기간을_더한_값() {
        String token = jwtProvider.createAccessToken(42L);

        assertThat(jwtProvider.getExpiration(token)).isEqualTo(NOW.plus(VALIDITY));
    }

    @Test
    void 유효기간이_지난_토큰은_TOKEN_EXPIRED() {
        String token = jwtProvider.createAccessToken(42L);
        JwtProvider later = providerAt(NOW.plus(VALIDITY).plusSeconds(1), SECRET);

        assertErrorCode(() -> later.getUserId(token), GlobalErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void 다른_키로_서명한_토큰은_INVALID_TOKEN() {
        String otherToken = providerAt(NOW, encode("another-secret-key-at-least-32-bytes-long!!"))
                .createAccessToken(42L);

        assertErrorCode(() -> jwtProvider.getUserId(otherToken), GlobalErrorCode.INVALID_TOKEN);
    }

    @Test
    void 변조된_토큰은_INVALID_TOKEN() {
        String token = jwtProvider.createAccessToken(42L);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertErrorCode(() -> jwtProvider.getUserId(tampered), GlobalErrorCode.INVALID_TOKEN);
    }

    @Test
    void 형식이_아닌_문자열은_INVALID_TOKEN() {
        assertErrorCode(() -> jwtProvider.getUserId("not-a-jwt"), GlobalErrorCode.INVALID_TOKEN);
        assertErrorCode(() -> jwtProvider.getUserId(""), GlobalErrorCode.INVALID_TOKEN);
    }

    @Test
    void Refresh_토큰을_Access_토큰_자리에_넣으면_INVALID_TOKEN() {
        String refreshToken = jwtProvider.createRefreshToken(42L);

        assertErrorCode(() -> jwtProvider.getUserId(refreshToken), GlobalErrorCode.INVALID_TOKEN);
    }

    @Test
    void Access_토큰은_Refresh_토큰으로_인정하지_않는다() {
        String accessToken = jwtProvider.createAccessToken(42L);

        assertThat(jwtProvider.findRefreshTokenUserId(accessToken)).isEmpty();
    }

    @Test
    void 발급한_Refresh_토큰에서_사용자_ID를_꺼낸다() {
        String refreshToken = jwtProvider.createRefreshToken(42L);

        assertThat(jwtProvider.findRefreshTokenUserId(refreshToken)).contains(42L);
    }

    @Test
    void 비밀키가_32바이트보다_짧으면_생성_시점에_실패한다() {
        assertThatThrownBy(() -> providerAt(NOW, encode("too-short-key")))
                .isInstanceOf(WeakKeyException.class);
    }

    private static JwtProvider providerAt(Instant instant, String secret) {
        return new JwtProvider(new JwtProperties(secret, VALIDITY, Duration.ofDays(14)), Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    private static void assertErrorCode(Runnable action, GlobalErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }
}

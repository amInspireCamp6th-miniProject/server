package com.example.server.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class RefreshTokenStoreTest {

    private static final Instant NOW = Instant.parse("2026-09-22T00:00:00Z");
    private static final Duration VALIDITY = Duration.ofDays(14);

    private static final String SECRET =
            Base64.getEncoder().encodeToString("test-secret-key-must-be-at-least-32-bytes!!".getBytes());

    private final MutableClock clock = new MutableClock(NOW);
    private final JwtProvider jwtProvider =
            new JwtProvider(new JwtProperties(SECRET, Duration.ofHours(1), VALIDITY), clock);
    private final RefreshTokenStore refreshTokenStore = new RefreshTokenStore(jwtProvider, clock);

    @Test
    void 발급한_토큰으로_회원_ID를_확인한다() {
        String token = refreshTokenStore.issue(1L);

        assertThat(refreshTokenStore.consume(token)).contains(1L);
    }

    @Test
    void 한_번_쓴_토큰은_다시_쓸_수_없다() {
        String token = refreshTokenStore.issue(1L);
        refreshTokenStore.consume(token);

        assertThat(refreshTokenStore.consume(token)).isEmpty();
    }

    @Test
    void 발급할_때마다_다른_값이_나온다() {
        assertThat(refreshTokenStore.issue(1L)).isNotEqualTo(refreshTokenStore.issue(1L));
    }

    @Test
    void 유효기간이_지난_토큰은_쓸_수_없다() {
        String token = refreshTokenStore.issue(1L);

        clock.moveTo(NOW.plus(VALIDITY).plusSeconds(1));

        assertThat(refreshTokenStore.consume(token)).isEmpty();
    }

    @Test
    void 폐기하면_해당_회원의_토큰만_쓸_수_없다() {
        String mine = refreshTokenStore.issue(1L);
        String others = refreshTokenStore.issue(2L);

        refreshTokenStore.revokeAll(1L);

        assertThat(refreshTokenStore.consume(mine)).isEmpty();
        assertThat(refreshTokenStore.consume(others)).contains(2L);
    }

    @Test
    void 없는_토큰이나_null은_빈_값() {
        assertThat(refreshTokenStore.consume("not-issued")).isEmpty();
        assertThat(refreshTokenStore.consume(null)).isEmpty();
    }

    @Test
    void Access_토큰을_Refresh_토큰_자리에_넣으면_빈_값() {
        assertThat(refreshTokenStore.consume(jwtProvider.createAccessToken(1L))).isEmpty();
    }
}

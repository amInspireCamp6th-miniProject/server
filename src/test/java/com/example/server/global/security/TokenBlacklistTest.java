package com.example.server.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TokenBlacklistTest {

    private static final Instant NOW = Instant.parse("2026-09-22T00:00:00Z");

    private final MutableClock clock = new MutableClock(NOW);
    private final TokenBlacklist tokenBlacklist = new TokenBlacklist(clock);

    @Test
    void 등록한_토큰만_포함된다() {
        tokenBlacklist.add("token-a", NOW.plusSeconds(60));

        assertThat(tokenBlacklist.contains("token-a")).isTrue();
        assertThat(tokenBlacklist.contains("token-b")).isFalse();
    }

    @Test
    void 만료_시각이_지나면_포함되지_않는다() {
        tokenBlacklist.add("token-a", NOW.plusSeconds(60));

        clock.moveTo(NOW.plusSeconds(60));

        assertThat(tokenBlacklist.contains("token-a")).isFalse();
    }
}

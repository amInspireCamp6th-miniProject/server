package com.example.server.ingredient.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ExpirationPolicyTest {

    private final ExpirationPolicy policy = new ExpirationPolicy(
            Clock.fixed(Instant.parse("2026-09-21T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void calculatesRemainingDaysUntilExpiration() {
        assertEquals(3, policy.calculateRemainingDays(LocalDate.of(2026, 9, 24)));
    }

    @Test
    void reportsPastDateAsExpired() {
        assertTrue(policy.isExpired(LocalDate.of(2026, 9, 20)));
    }

    @Test
    void reportsTodayAsNotExpired() {
        assertFalse(policy.isExpired(LocalDate.of(2026, 9, 21)));
    }

    @Test
    void reportsFutureDateAsNotExpired() {
        assertFalse(policy.isExpired(LocalDate.of(2026, 9, 22)));
    }

    @Test
    void reportsPastDateAsNotExpiring() {
        assertFalse(policy.isExpiring(LocalDate.of(2026, 9, 20)));
    }

    @Test
    void reportsTodayAsExpiring() {
        assertTrue(policy.isExpiring(LocalDate.of(2026, 9, 21)));
    }

    @Test
    void reportsTomorrowAsExpiring() {
        assertTrue(policy.isExpiring(LocalDate.of(2026, 9, 22)));
    }

    @Test
    void reportsFifthDayAsExpiring() {
        assertTrue(policy.isExpiring(LocalDate.of(2026, 9, 26)));
    }

    @Test
    void reportsSixthDayAsNotExpiring() {
        assertFalse(policy.isExpiring(LocalDate.of(2026, 9, 27)));
    }
}

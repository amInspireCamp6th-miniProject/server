package com.example.server.ingredient.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ExpirationPolicy {

    private static final long EXPIRING_THRESHOLD_DAYS = 5;

    private final Clock clock;

    public ExpirationPolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public long calculateRemainingDays(LocalDate expirationDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(clock), expirationDate);
    }

    public boolean isExpired(LocalDate expirationDate) {
        return expirationDate.isBefore(LocalDate.now(clock));
    }

    public boolean isExpiring(LocalDate expirationDate) {
        long remainingDays = calculateRemainingDays(expirationDate);
        return remainingDays >= 0 && remainingDays <= EXPIRING_THRESHOLD_DAYS;
    }
}

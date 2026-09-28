package com.example.server.ingredient.dto;

import java.util.List;

public record IngredientExpirationSummary(
        long totalCount,
        List<IngredientExpirationResponse> expiringIngredients,
        long expiredCount) {
}

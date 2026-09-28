package com.example.server.dashboard.dto;

import com.example.server.ingredient.dto.IngredientExpirationResponse;
import java.util.List;

public record DashboardResponse(
        long totalCount,
        long expiringCount,
        long expiredCount,
        List<IngredientExpirationResponse> priorityIngredients) {
}

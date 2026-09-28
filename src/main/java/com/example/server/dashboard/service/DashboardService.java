package com.example.server.dashboard.service;

import com.example.server.dashboard.dto.DashboardResponse;
import com.example.server.ingredient.dto.IngredientExpirationSummary;
import com.example.server.ingredient.service.IngredientExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final IngredientExpirationService ingredientExpirationService;

    public DashboardResponse getDashboard(Long userId) {
        IngredientExpirationSummary summary = ingredientExpirationService.summarize(userId);
        return new DashboardResponse(
                summary.totalCount(),
                summary.expiringIngredients().size(),
                summary.expiredCount(),
                summary.expiringIngredients());
    }
}

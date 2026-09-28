package com.example.server.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.server.dashboard.dto.DashboardResponse;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.dto.IngredientExpirationSummary;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.service.IngredientExpirationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IngredientExpirationService ingredientExpirationService;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(ingredientExpirationService);
    }

    @Test
    void buildsDashboardFromExpirationSummary() {
        List<IngredientExpirationResponse> expiringIngredients = List.of(
                response(1L, 0L),
                response(2L, 1L),
                response(3L, 2L),
                response(4L, 3L),
                response(5L, 5L));
        given(ingredientExpirationService.summarize(7L))
                .willReturn(new IngredientExpirationSummary(8L, expiringIngredients, 2L));

        DashboardResponse response = dashboardService.getDashboard(7L);

        assertThat(response.totalCount()).isEqualTo(8L);
        assertThat(response.expiringCount()).isEqualTo(5L);
        assertThat(response.expiredCount()).isEqualTo(2L);
        assertThat(response.priorityIngredients()).containsExactlyElementsOf(expiringIngredients);
        verify(ingredientExpirationService).summarize(7L);
        verifyNoMoreInteractions(ingredientExpirationService);
    }

    private IngredientExpirationResponse response(Long ingredientId, long daysLeft) {
        return new IngredientExpirationResponse(
                ingredientId,
                "테스트 상품 " + ingredientId,
                "테스트 식재료 " + ingredientId,
                "테스트 카테고리",
                BigDecimal.ONE,
                "개",
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 21).plusDays(daysLeft),
                StorageType.REFRIGERATED,
                null,
                daysLeft);
    }
}

package com.example.server.dashboard.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.dashboard.dto.DashboardResponse;
import com.example.server.dashboard.service.DashboardService;
import com.example.server.global.security.WithLoginUser;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.entity.StorageType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @WithLoginUser(userId = 7L)
    void returnsDashboardForLoggedInUser() throws Exception {
        given(dashboardService.getDashboard(7L))
                .willReturn(new DashboardResponse(
                        4L,
                        2L,
                        1L,
                        List.of(response(1L, 0L), response(2L, 3L))));

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(4L))
                .andExpect(jsonPath("$.expiringCount").value(2L))
                .andExpect(jsonPath("$.expiredCount").value(1L))
                .andExpect(jsonPath("$.priorityIngredients.length()").value(2))
                .andExpect(jsonPath("$.priorityIngredients[0].ingredientId").value(1L))
                .andExpect(jsonPath("$.priorityIngredients[0].daysLeft").value(0L));

        verify(dashboardService).getDashboard(7L);
    }

    @Test
    void rejectsRequestWithoutLoggedInUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(dashboardService);
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

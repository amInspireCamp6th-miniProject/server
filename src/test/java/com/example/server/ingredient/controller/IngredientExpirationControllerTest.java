package com.example.server.ingredient.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.security.WithLoginUser;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.service.IngredientExpirationService;
import com.example.server.ingredient.service.IngredientService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({IngredientExpirationController.class, IngredientController.class})
@AutoConfigureMockMvc(addFilters = false)
@WithLoginUser(userId = 7L)
class IngredientExpirationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientExpirationService ingredientExpirationService;

    @MockitoBean
    private IngredientService ingredientService;

    @Test
    void mapsExpiringPathToExpirationController() throws Exception {
        given(ingredientExpirationService.findExpiring(7L))
                .willReturn(List.of(response(15L, LocalDate.of(2026, 9, 21), 0L)));

        mockMvc.perform(get("/api/v1/ingredients/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientId").value(15L))
                .andExpect(jsonPath("$[0].daysLeft").value(0L))
                .andExpect(jsonPath("$[0].imageUrl")
                        .value("/api/v1/ingredients/15/image"));

        verify(ingredientExpirationService).findExpiring(7L);
        verifyNoInteractions(ingredientService);
    }

    @Test
    void mapsExpiredPathToExpirationController() throws Exception {
        given(ingredientExpirationService.findExpired(7L))
                .willReturn(List.of(response(16L, LocalDate.of(2026, 9, 20), -1L)));

        mockMvc.perform(get("/api/v1/ingredients/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientId").value(16L))
                .andExpect(jsonPath("$[0].daysLeft").value(-1L));

        verify(ingredientExpirationService).findExpired(7L);
        verifyNoInteractions(ingredientService);
    }

    private IngredientExpirationResponse response(
            Long ingredientId, LocalDate expirationDate, long daysLeft) {
        return new IngredientExpirationResponse(
                ingredientId,
                "테스트 상품",
                "테스트 식재료",
                "테스트 카테고리",
                BigDecimal.ONE,
                "개",
                LocalDate.of(2026, 9, 18),
                expirationDate,
                StorageType.REFRIGERATED,
                "/api/v1/ingredients/" + ingredientId + "/image",
                daysLeft);
    }
}

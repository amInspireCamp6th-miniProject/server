package com.example.server.recipe.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.exception.BusinessException;
import com.example.server.recipe.dto.RecipeDetailResponse;
import com.example.server.recipe.dto.RecipeIngredientResponse;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.exception.RecipeErrorCode;
import com.example.server.recipe.service.RecipeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void returnsRecipeDetailAsJson() throws Exception {
        RecipeDetailResponse response = new RecipeDetailResponse(
                1L,
                "김치볶음밥",
                "간단한 김치볶음밥",
                15,
                "https://example.com/kimchi.jpg",
                List.of(
                        new RecipeIngredientResponse("김치", new BigDecimal("200.00"), "g"),
                        new RecipeIngredientResponse("밥", new BigDecimal("1.00"), "공기")));
        when(recipeService.getRecipeDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.name").value("김치볶음밥"))
                .andExpect(jsonPath("$.description").value("간단한 김치볶음밥"))
                .andExpect(jsonPath("$.cookingTime").value(15))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/kimchi.jpg"))
                .andExpect(jsonPath("$.ingredients", hasSize(2)))
                .andExpect(jsonPath("$.ingredients[0].ingredientName").value("김치"))
                .andExpect(jsonPath("$.ingredients[0].amount").value(200.00))
                .andExpect(jsonPath("$.ingredients[0].unit").value("g"))
                .andExpect(jsonPath("$.ingredients[1].ingredientName").value("밥"));

        verify(recipeService).getRecipeDetail(1L);
    }

    @Test
    void returnsNotFoundWhenRecipeDetailDoesNotExist() throws Exception {
        when(recipeService.getRecipeDetail(999L))
                .thenThrow(new BusinessException(RecipeErrorCode.RECIPE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/recipes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("레시피를 찾을 수 없습니다."));

        verify(recipeService).getRecipeDetail(999L);
    }

    @Test
    void returnsInstructionsAsJson() throws Exception {
        RecipeInstructionsResponse response = new RecipeInstructionsResponse(1L, List.of(
                new RecipeInstructionStepResponse(1, "김치를 잘게 썬다"),
                new RecipeInstructionStepResponse(2, "팬에 김치와 밥을 볶는다"),
                new RecipeInstructionStepResponse(3, "완성된 볶음밥을 그릇에 담는다")));
        when(recipeService.getInstructions(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/recipes/1/instructions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.instructions", hasSize(3)))
                .andExpect(jsonPath("$.instructions[0].stepNo").value(1))
                .andExpect(jsonPath("$.instructions[0].description").value("김치를 잘게 썬다"))
                .andExpect(jsonPath("$.instructions[1].stepNo").value(2))
                .andExpect(jsonPath("$.instructions[2].stepNo").value(3));

        verify(recipeService).getInstructions(1L);
    }

    @Test
    void returnsNotFoundThroughGlobalExceptionHandler() throws Exception {
        when(recipeService.getInstructions(999L))
                .thenThrow(new BusinessException(RecipeErrorCode.RECIPE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/recipes/999/instructions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("레시피를 찾을 수 없습니다."));

        verify(recipeService).getInstructions(999L);
    }
}

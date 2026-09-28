package com.example.server.recipe.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.server.global.exception.BusinessException;
import com.example.server.global.security.WithLoginUser;
import com.example.server.recipe.dto.RecipeDetailResponse;
import com.example.server.recipe.dto.RecipeIngredientResponse;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.dto.RecipeRecommendationRequest;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import com.example.server.recipe.exception.RecipeErrorCode;
import com.example.server.recipe.service.RecipeRecommendationService;
import com.example.server.recipe.service.RecipeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipeController.class)
@WithLoginUser
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private RecipeRecommendationService recipeRecommendationService;

    @Test
    void returnsRecipeRecommendationsForLoggedInUser() throws Exception {
        when(recipeRecommendationService.recommend(
                        eq(1L), any(RecipeRecommendationRequest.class)))
                .thenReturn(List.of(new RecipeRecommendationResponse(
                        1L,
                        "두부 대파 볶음",
                        20,
                        List.of("두부", "대파"),
                        List.of("간장", "참기름"),
                        "1. 두부를 자릅니다.\n2. 대파를 볶습니다.")));

        mockMvc.perform(post("/api/v1/recipes/recommendations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientIds\":[11,25,31]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipeId").value(1L))
                .andExpect(jsonPath("$[0].name").value("두부 대파 볶음"))
                .andExpect(jsonPath("$[0].usedIngredients", hasSize(2)))
                .andExpect(jsonPath("$[0].additionalIngredients", hasSize(2)))
                .andExpect(jsonPath("$[0].instructions")
                        .value("1. 두부를 자릅니다.\n2. 대파를 볶습니다."));

        verify(recipeRecommendationService)
                .recommend(eq(1L), any(RecipeRecommendationRequest.class));
    }

    @Test
    void rejectsInvalidRecommendationRequest() throws Exception {
        mockMvc.perform(post("/api/v1/recipes/recommendations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientIds\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recipeRecommendationService);
    }

    @Test
    @WithAnonymousUser
    void rejectsRecommendationRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/recipes/recommendations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientIds\":[11]}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(recipeRecommendationService);
    }

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

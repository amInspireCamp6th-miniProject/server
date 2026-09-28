package com.example.server.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.recipe.client.OpenAiRecipeClient;
import com.example.server.recipe.client.dto.AiRecipeRecommendation;
import com.example.server.recipe.client.dto.AiRecipeRecommendationResult;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiRecipeRecommendationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);

    @Mock
    private OpenAiRecipeClient openAiRecipeClient;

    private AiRecipeRecommendationService aiRecommendationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-21T00:00:00Z"), ZoneOffset.UTC);
        aiRecommendationService = new AiRecipeRecommendationService(
                openAiRecipeClient, new ExpirationPolicy(clock));
    }

    @Test
    void mapsAiResultAndLimitsRecommendationsToThree() {
        Ingredient egg = ingredient("계란", 1);
        AiRecipeRecommendation first = recommendation("계란볶음", "계란");
        AiRecipeRecommendation second = recommendation("계란국", "계란");
        AiRecipeRecommendation third = recommendation("계란찜", "계란");
        AiRecipeRecommendation fourth = recommendation("계란말이", "계란");
        given(openAiRecipeClient.recommend(anyString())).willReturn(Optional.of(
                new AiRecipeRecommendationResult(List.of(first, second, third, fourth))));

        List<RecipeRecommendationResponse> responses =
                aiRecommendationService.recommend(List.of(egg)).orElseThrow();

        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).recipeId()).isNull();
        assertThat(responses.get(0).name()).isEqualTo("계란볶음");
        assertThat(responses.get(0).cookingTime()).isEqualTo(10);
        assertThat(responses.get(0).usedIngredients()).containsExactly("계란");
        assertThat(responses.get(0).additionalIngredients()).containsExactly("소금");
        assertThat(responses.get(0).instructions())
                .isEqualTo("1. 계란을 푼다.\n2. 팬에서 익힌다.");
    }

    @Test
    void returnsEmptyWhenAiIsUnavailableOrReturnsNoRecipes() {
        Ingredient egg = ingredient("계란", 1);
        given(openAiRecipeClient.recommend(anyString()))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(new AiRecipeRecommendationResult(List.of())));

        assertThat(aiRecommendationService.recommend(List.of(egg))).isEmpty();
        assertThat(aiRecommendationService.recommend(List.of(egg))).isEmpty();
    }

    @Test
    void returnsEmptyWhenAiClientThrowsException() {
        Ingredient egg = ingredient("계란", 1);
        given(openAiRecipeClient.recommend(anyString()))
                .willThrow(new IllegalStateException("external API failure"));

        assertThat(aiRecommendationService.recommend(List.of(egg))).isEmpty();
    }

    @Test
    void excludesExpiredIngredientFromAiPrompt() {
        Ingredient egg = ingredient("계란", 1);
        Ingredient expiredOnion = ingredient("양파", -1);
        given(openAiRecipeClient.recommend(anyString())).willReturn(Optional.of(
                new AiRecipeRecommendationResult(List.of(recommendation("계란볶음", "계란")))));

        aiRecommendationService.recommend(List.of(egg, expiredOnion));

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(openAiRecipeClient).recommend(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("계란 / D-1")
                .doesNotContain("양파");
    }

    @Test
    void rejectsAiResultUsingIngredientNotSelectedByUser() {
        Ingredient egg = ingredient("계란", 1);
        given(openAiRecipeClient.recommend(anyString())).willReturn(Optional.of(
                new AiRecipeRecommendationResult(List.of(recommendation("두부 요리", "두부")))));

        assertThat(aiRecommendationService.recommend(List.of(egg))).isEmpty();
    }

    @Test
    void doesNotCallAiWhenAllIngredientsAreExpired() {
        Ingredient expiredEgg = ingredient("계란", -1);

        assertThat(aiRecommendationService.recommend(List.of(expiredEgg))).isEmpty();
        verifyNoInteractions(openAiRecipeClient);
    }

    private Ingredient ingredient(String ingredientName, long daysLeft) {
        Ingredient ingredient = mock(Ingredient.class);
        lenient().when(ingredient.getIngredientName()).thenReturn(ingredientName);
        lenient().when(ingredient.getExpirationDate()).thenReturn(TODAY.plusDays(daysLeft));
        return ingredient;
    }

    private AiRecipeRecommendation recommendation(String name, String usedIngredient) {
        return new AiRecipeRecommendation(
                name,
                10,
                List.of(usedIngredient, usedIngredient),
                List.of("소금", "소금"),
                List.of("계란을 푼다.", "팬에서 익힌다."));
    }
}

package com.example.server.recipe.service;

import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.recipe.client.OpenAiRecipeClient;
import com.example.server.recipe.client.dto.AiRecipeRecommendation;
import com.example.server.recipe.client.dto.AiRecipeRecommendationResult;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecipeRecommendationService {

    private static final int MAX_RECOMMENDATIONS = 3;

    private final OpenAiRecipeClient openAiRecipeClient;
    private final ExpirationPolicy expirationPolicy;

    public Optional<List<RecipeRecommendationResponse>> recommend(
            List<Ingredient> selectedIngredients) {
        List<Ingredient> availableIngredients = selectedIngredients.stream()
                .filter(ingredient -> !expirationPolicy.isExpired(ingredient.getExpirationDate()))
                .toList();
        if (availableIngredients.isEmpty()) {
            return Optional.empty();
        }

        Set<String> availableIngredientNames = availableIngredients.stream()
                .map(Ingredient::getIngredientName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        try {
            return openAiRecipeClient
                    .recommend(createPrompt(availableIngredients))
                    .filter(result -> isValid(result, availableIngredientNames))
                    .map(this::toResponses)
                    .filter(responses -> !responses.isEmpty());
        } catch (RuntimeException exception) {
            log.warn(
                    "OpenAI 레시피 추천에 실패해 DB 추천으로 전환합니다. cause={}",
                    exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private String createPrompt(List<Ingredient> ingredients) {
        String ingredientLines = ingredients.stream()
                .map(ingredient -> "- " + ingredient.getIngredientName() + " / "
                        + formatDaysLeft(expirationPolicy.calculateRemainingDays(
                                ingredient.getExpirationDate())))
                .collect(Collectors.joining("\n"));

        return """
                사용 가능한 식재료:
                %s

                추천 조건:
                - 사용 가능한 식재료를 최대한 활용하세요.
                - 소비기한이 가까운 식재료를 우선 활용하세요.
                - 현실적으로 조리 가능한 음식만 추천하세요.
                - 최대 3개를 추천하세요.
                - 조리법은 간결하고 순서가 명확해야 합니다.
                - instructions에는 단계 설명만 넣으세요. 단계 번호는 넣지 마세요.
                """.formatted(ingredientLines);
    }

    private String formatDaysLeft(long daysLeft) {
        if (daysLeft == 0) {
            return "D-Day";
        }
        return "D-" + daysLeft;
    }

    private boolean isValid(
            AiRecipeRecommendationResult result, Set<String> availableIngredientNames) {
        if (result.recipes() == null || result.recipes().isEmpty()) {
            return false;
        }
        return result.recipes().stream()
                .allMatch(recipe -> isValid(recipe, availableIngredientNames));
    }

    private boolean isValid(
            AiRecipeRecommendation recipe, Set<String> availableIngredientNames) {
        return recipe != null
                && recipe.name() != null
                && !recipe.name().isBlank()
                && recipe.cookingTime() != null
                && recipe.cookingTime() > 0
                && recipe.usedIngredients() != null
                && !recipe.usedIngredients().isEmpty()
                && recipe.usedIngredients().stream().allMatch(availableIngredientNames::contains)
                && recipe.additionalIngredients() != null
                && recipe.additionalIngredients().stream()
                        .allMatch(ingredient -> ingredient != null
                                && !ingredient.isBlank()
                                && !availableIngredientNames.contains(ingredient))
                && recipe.instructions() != null
                && !recipe.instructions().isEmpty()
                && recipe.instructions().stream()
                        .allMatch(instruction -> instruction != null && !instruction.isBlank());
    }

    private List<RecipeRecommendationResponse> toResponses(
            AiRecipeRecommendationResult result) {
        return result.recipes().stream()
                .limit(MAX_RECOMMENDATIONS)
                .map(this::toResponse)
                .toList();
    }

    private RecipeRecommendationResponse toResponse(AiRecipeRecommendation recipe) {
        return new RecipeRecommendationResponse(
                null,
                recipe.name(),
                recipe.cookingTime(),
                distinct(recipe.usedIngredients()),
                distinct(recipe.additionalIngredients()),
                formatInstructions(recipe.instructions()));
    }

    private List<String> distinct(List<String> ingredients) {
        return List.copyOf(new LinkedHashSet<>(ingredients));
    }

    private String formatInstructions(List<String> instructions) {
        return IntStream.range(0, instructions.size())
                .mapToObj(index -> (index + 1) + ". " + instructions.get(index))
                .collect(Collectors.joining("\n"));
    }
}

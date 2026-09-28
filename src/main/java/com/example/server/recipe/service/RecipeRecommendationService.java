package com.example.server.recipe.service;

import com.example.server.global.exception.BusinessException;
import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.exception.IngredientErrorCode;
import com.example.server.ingredient.repository.IngredientRepository;
import com.example.server.recipe.dto.RecipeRecommendationRequest;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import com.example.server.recipe.entity.Recipe;
import com.example.server.recipe.entity.RecipeIngredient;
import com.example.server.recipe.repository.RecipeIngredientRepository;
import com.example.server.recipe.repository.RecipeStepRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeRecommendationService {

    private static final int MAX_RECOMMENDATIONS = 3;
    private static final long EXPIRING_WEIGHT_BASE = 6;
    private static final long MAX_WEIGHTED_DAYS = 5;

    private static final Comparator<RecommendationCandidate> RECOMMENDATION_ORDER =
            Comparator.comparingLong(RecommendationCandidate::score)
                    .reversed()
                    .thenComparing(Comparator.comparingInt(RecommendationCandidate::usedCount)
                            .reversed())
                    .thenComparingInt(RecommendationCandidate::additionalCount)
                    .thenComparing(RecommendationCandidate::recipeId);

    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final AiRecipeRecommendationService aiRecipeRecommendationService;
    private final ExpirationPolicy expirationPolicy;

    public List<RecipeRecommendationResponse> recommend(
            Long userId, RecipeRecommendationRequest request) {
        Set<Long> requestedIngredientIds = new LinkedHashSet<>(request.ingredientIds());
        List<Ingredient> selectedIngredients = ingredientRepository
                .findAllByUserIdAndIdIn(userId, requestedIngredientIds);
        validateOwnership(requestedIngredientIds, selectedIngredients);

        Optional<List<RecipeRecommendationResponse>> aiRecommendations =
                aiRecipeRecommendationService.recommend(selectedIngredients);
        if (aiRecommendations.isPresent()) {
            return aiRecommendations.get();
        }

        Map<String, Long> weightsByIngredientName = selectedIngredients.stream()
                .filter(ingredient -> !expirationPolicy.isExpired(ingredient.getExpirationDate()))
                .collect(Collectors.toMap(
                        Ingredient::getIngredientName,
                        ingredient -> calculateWeight(ingredient.getExpirationDate()),
                        Long::sum,
                        LinkedHashMap::new));
        if (weightsByIngredientName.isEmpty()) {
            return List.of();
        }

        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = recipeIngredientRepository
                .findAllWithRecipe()
                .stream()
                .collect(Collectors.groupingBy(
                        recipeIngredient -> recipeIngredient.getRecipe().getRecipeId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return ingredientsByRecipe.values().stream()
                .map(recipeIngredients -> createCandidate(
                        recipeIngredients, weightsByIngredientName))
                .filter(candidate -> !candidate.usedIngredients().isEmpty())
                .sorted(RECOMMENDATION_ORDER)
                .limit(MAX_RECOMMENDATIONS)
                .map(this::toResponse)
                .toList();
    }

    private void validateOwnership(
            Set<Long> requestedIngredientIds, List<Ingredient> selectedIngredients) {
        Set<Long> selectedIngredientIds = selectedIngredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());
        if (!selectedIngredientIds.equals(requestedIngredientIds)) {
            throw new BusinessException(IngredientErrorCode.INGREDIENT_NOT_FOUND);
        }
    }

    private long calculateWeight(LocalDate expirationDate) {
        long daysLeft = expirationPolicy.calculateRemainingDays(expirationDate);
        if (daysLeft < 0 || daysLeft > MAX_WEIGHTED_DAYS) {
            return 0;
        }
        return EXPIRING_WEIGHT_BASE - daysLeft;
    }

    private RecommendationCandidate createCandidate(
            List<RecipeIngredient> recipeIngredients,
            Map<String, Long> weightsByIngredientName) {
        Recipe recipe = recipeIngredients.get(0).getRecipe();
        Set<String> requiredIngredientNames = recipeIngredients.stream()
                .map(RecipeIngredient::getIngredientName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> usedIngredients = requiredIngredientNames.stream()
                .filter(weightsByIngredientName::containsKey)
                .toList();
        List<String> additionalIngredients = requiredIngredientNames.stream()
                .filter(ingredientName -> !weightsByIngredientName.containsKey(ingredientName))
                .toList();
        long score = usedIngredients.stream()
                .mapToLong(weightsByIngredientName::get)
                .sum();
        return new RecommendationCandidate(
                recipe, score, usedIngredients, additionalIngredients);
    }

    private RecipeRecommendationResponse toResponse(RecommendationCandidate candidate) {
        String instructions = recipeStepRepository
                .findAllByRecipe_RecipeIdOrderByStepNoAsc(candidate.recipeId())
                .stream()
                .map(step -> step.getStepNo() + ". " + step.getDescription())
                .collect(Collectors.joining("\n"));
        return new RecipeRecommendationResponse(
                candidate.recipeId(),
                candidate.recipe().getName(),
                candidate.recipe().getCookingTime(),
                candidate.usedIngredients(),
                candidate.additionalIngredients(),
                instructions);
    }

    private record RecommendationCandidate(
            Recipe recipe,
            long score,
            List<String> usedIngredients,
            List<String> additionalIngredients) {

        private Long recipeId() {
            return recipe.getRecipeId();
        }

        private int usedCount() {
            return usedIngredients.size();
        }

        private int additionalCount() {
            return additionalIngredients.size();
        }
    }
}

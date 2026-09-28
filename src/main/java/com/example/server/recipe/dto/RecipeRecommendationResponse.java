package com.example.server.recipe.dto;

import java.util.List;

public record RecipeRecommendationResponse(
        Long recipeId,
        String name,
        Integer cookingTime,
        List<String> usedIngredients,
        List<String> additionalIngredients,
        String instructions) {
}

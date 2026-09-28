package com.example.server.recipe.client.dto;

import java.util.List;

public record AiRecipeRecommendation(
        String name,
        Integer cookingTime,
        List<String> usedIngredients,
        List<String> additionalIngredients,
        List<String> instructions) {
}

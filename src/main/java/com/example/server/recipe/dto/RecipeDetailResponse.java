package com.example.server.recipe.dto;

import java.util.List;

public record RecipeDetailResponse(
        Long recipeId,
        String name,
        String description,
        Integer cookingTime,
        String imageUrl,
        List<RecipeIngredientResponse> ingredients) {
}

package com.example.server.recipe.client.dto;

import java.util.List;

public record AiRecipeRecommendationResult(
        List<AiRecipeRecommendation> recipes) {
}

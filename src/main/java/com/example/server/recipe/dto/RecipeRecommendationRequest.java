package com.example.server.recipe.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record RecipeRecommendationRequest(
        @NotEmpty List<@NotNull @Positive Long> ingredientIds) {
}

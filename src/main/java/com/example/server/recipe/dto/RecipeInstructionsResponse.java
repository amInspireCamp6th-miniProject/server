package com.example.server.recipe.dto;

import java.util.List;

public record RecipeInstructionsResponse(Long recipeId, List<RecipeInstructionStepResponse> instructions) {
}

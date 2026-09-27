package com.example.server.recipe.dto;

import java.math.BigDecimal;

public record RecipeIngredientResponse(String ingredientName, BigDecimal amount, String unit) {
}

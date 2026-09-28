package com.example.server.ingredient.dto;

import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IngredientExpirationResponse(
        Long ingredientId,
        String productName,
        String ingredientName,
        String category,
        BigDecimal quantity,
        String unit,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        StorageType storageType,
        String imageUrl,
        long daysLeft) {

    public static IngredientExpirationResponse from(Ingredient ingredient, long daysLeft) {
        IngredientResponse ingredientResponse = IngredientResponse.from(ingredient);
        return new IngredientExpirationResponse(
                ingredientResponse.ingredientId(),
                ingredientResponse.productName(),
                ingredientResponse.ingredientName(),
                ingredientResponse.category(),
                ingredientResponse.quantity(),
                ingredientResponse.unit(),
                ingredientResponse.purchaseDate(),
                ingredientResponse.expirationDate(),
                ingredientResponse.storageType(),
                ingredientResponse.imageUrl(),
                daysLeft);
    }
}

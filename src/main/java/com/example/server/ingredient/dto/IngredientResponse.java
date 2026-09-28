package com.example.server.ingredient.dto;

import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 프론트엔드에 반환할 식재료 정보다. DB 엔티티를 외부에 직접 노출하지 않는다. */
public record IngredientResponse(
        Long ingredientId,
        String productName,
        String ingredientName,
        String category,
        BigDecimal quantity,
        String unit,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        StorageType storageType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /** 엔티티를 API 응답 DTO로 변환한다. */
    public static IngredientResponse from(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getProductName(),
                ingredient.getIngredientName(),
                ingredient.getCategory(),
                ingredient.getQuantity(),
                ingredient.getUnit(),
                ingredient.getPurchaseDate(),
                ingredient.getExpirationDate(),
                ingredient.getStorageType(),
                ingredient.getCreatedAt(),
                ingredient.getUpdatedAt());
    }
}

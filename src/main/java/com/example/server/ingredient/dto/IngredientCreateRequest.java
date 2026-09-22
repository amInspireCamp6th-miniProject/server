package com.example.server.ingredient.dto;

import com.example.server.ingredient.entity.StorageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 식재료 등록 API가 받는 JSON의 모양과 입력 검증 규칙을 정의한다. */
public record IngredientCreateRequest(
        @NotBlank @Size(max = 150) String productName,
        @NotBlank @Size(max = 100) String ingredientName,
        @Size(max = 30) String category,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
        @NotBlank @Size(max = 20) String unit,
        @NotNull LocalDate purchaseDate,
        @NotNull LocalDate expirationDate,
        @NotNull StorageType storageType) {}

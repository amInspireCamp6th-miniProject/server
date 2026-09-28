package com.example.server.ingredient.dto;

import com.example.server.ingredient.entity.StorageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 식재료 부분 수정 API가 받는 JSON이다.
 *
 * <p>모든 값은 선택 사항이며, 전달된 값에 대해서만 형식과 길이를 검증한다.
 */
public record IngredientUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "제품명은 공백일 수 없습니다")
                @Size(max = 150)
                String productName,
        @Pattern(regexp = ".*\\S.*", message = "식재료명은 공백일 수 없습니다")
                @Size(max = 100)
                String ingredientName,
        @Size(max = 30) String category,
        @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
        @Pattern(regexp = ".*\\S.*", message = "수량 단위는 공백일 수 없습니다")
                @Size(max = 20)
                String unit,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        StorageType storageType) {}

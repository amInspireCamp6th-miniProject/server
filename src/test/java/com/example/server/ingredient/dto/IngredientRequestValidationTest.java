package com.example.server.ingredient.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.ingredient.entity.StorageType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 프론트에서 잘못된 값이 들어왔을 때 DB 저장 전에 거부되는지 확인한다. */
class IngredientRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("등록 수량은 0보다 커야 한다")
    void quantityMustBePositive() {
        IngredientCreateRequest request = new IngredientCreateRequest(
                "두부",
                "두부",
                "가공식품",
                BigDecimal.ZERO,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);

        Set<ConstraintViolation<IngredientCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("quantity");
    }

    @Test
    @DisplayName("부분 수정에서 공백 제품명은 허용하지 않는다")
    void updatedProductNameMustNotBeBlank() {
        IngredientUpdateRequest request =
                new IngredientUpdateRequest("   ", null, null, null, null, null, null, null);

        Set<ConstraintViolation<IngredientUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("productName");
    }
}

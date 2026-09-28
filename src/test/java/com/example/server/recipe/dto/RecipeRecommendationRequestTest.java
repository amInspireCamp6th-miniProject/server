package com.example.server.recipe.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeRecommendationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void ingredientIds가_null이면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(null);

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void ingredientIds가_비어_있으면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of());

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void ingredientIds가_유효하면_검증을_통과한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of(11L, 25L, 31L));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void ingredientIds에_null이_포함되면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(Arrays.asList(11L, null, 31L));

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void ingredientId가_0이면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of(0L));

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void ingredientId가_음수이면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of(-1L));

        assertThat(validator.validate(request)).hasSize(1);
    }
}

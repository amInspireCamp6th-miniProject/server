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
    void categoryIds가_null이면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(null);

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void categoryIds가_비어_있으면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of());

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void categoryIds가_유효하면_검증을_통과한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(List.of(1, 3, 6));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void categoryIds에_null이_포함되면_검증에_실패한다() {
        RecipeRecommendationRequest request = new RecipeRecommendationRequest(Arrays.asList(1, null, 6));

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void categoryId가_0이거나_음수이면_검증에_실패한다() {
        RecipeRecommendationRequest zeroRequest = new RecipeRecommendationRequest(List.of(0));
        RecipeRecommendationRequest negativeRequest = new RecipeRecommendationRequest(List.of(-1));

        assertThat(validator.validate(zeroRequest)).hasSize(1);
        assertThat(validator.validate(negativeRequest)).hasSize(1);
    }
}

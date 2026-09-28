package com.example.server.ingredient.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.ingredient.entity.Ingredient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IngredientResponseTest {

    @Test
    void createsImageUrlWhenIngredientHasImage() {
        Ingredient ingredient = Mockito.mock(Ingredient.class);
        Mockito.when(ingredient.getId()).thenReturn(15L);
        Mockito.when(ingredient.getImageData()).thenReturn(new byte[] {(byte) 0xFF, (byte) 0xD8});

        IngredientResponse response = IngredientResponse.from(ingredient);

        assertThat(response.imageUrl()).isEqualTo("/api/v1/ingredients/15/image");
    }

    @Test
    void returnsNullImageUrlWhenIngredientHasNoImage() {
        Ingredient ingredient = Mockito.mock(Ingredient.class);
        Mockito.when(ingredient.getId()).thenReturn(15L);

        IngredientResponse response = IngredientResponse.from(ingredient);

        assertThat(response.imageUrl()).isNull();
    }
}

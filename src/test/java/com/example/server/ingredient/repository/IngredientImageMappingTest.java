package com.example.server.ingredient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class IngredientImageMappingTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsIngredientImageData() {
        byte[] imageData = {(byte) 0x89, 0x50, 0x4E, 0x47};
        Ingredient ingredient = Ingredient.create(1L, createRequest());
        ingredient.updateImage(imageData, "image/png", "tofu.png");

        Long ingredientId = ingredientRepository.saveAndFlush(ingredient).getId();
        entityManager.clear();

        Ingredient saved = ingredientRepository.findById(ingredientId).orElseThrow();
        assertThat(saved.getImageData()).containsExactly(imageData);
        assertThat(saved.getImageContentType()).isEqualTo("image/png");
        assertThat(saved.getImageFileName()).isEqualTo("tofu.png");
    }

    private IngredientCreateRequest createRequest() {
        return new IngredientCreateRequest(
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);
    }
}

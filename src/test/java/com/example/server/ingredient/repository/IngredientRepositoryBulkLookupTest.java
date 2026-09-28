package com.example.server.ingredient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class IngredientRepositoryBulkLookupTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void findsOnlyRequestedIngredientsOwnedByUser() {
        Ingredient firstOwned = ingredientRepository.save(Ingredient.create(1L, request("두부")));
        Ingredient secondOwned = ingredientRepository.save(Ingredient.create(1L, request("대파")));
        Ingredient anotherUsers = ingredientRepository.save(Ingredient.create(2L, request("삼겹살")));
        ingredientRepository.flush();

        List<Ingredient> ingredients = ingredientRepository.findAllByUserIdAndIdIn(
                1L, List.of(firstOwned.getId(), secondOwned.getId(), anotherUsers.getId()));

        assertThat(ingredients)
                .extracting(Ingredient::getId)
                .containsExactlyInAnyOrder(firstOwned.getId(), secondOwned.getId());
        assertThat(ingredients)
                .extracting(Ingredient::getUserId)
                .containsOnly(1L);
    }

    private IngredientCreateRequest request(String ingredientName) {
        return new IngredientCreateRequest(
                ingredientName + " 상품",
                ingredientName,
                "테스트 카테고리",
                BigDecimal.ONE,
                "개",
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);
    }
}

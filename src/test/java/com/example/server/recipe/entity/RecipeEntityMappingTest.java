package com.example.server.recipe.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(statements = {
    "INSERT INTO RECIPES (name, description, cooking_time, image_url) "
            + "VALUES ('김치볶음밥', '간단한 김치볶음밥', 15, 'https://example.com/kimchi.jpg')",
    "INSERT INTO RECIPE_INGREDIENTS (recipe_id, ingredient_name, amount, unit) "
            + "SELECT recipe_id, '김치', 200.00, 'g' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_STEPS (recipe_id, step_no, description) "
            + "SELECT recipe_id, 1, '김치를 잘게 썬다' FROM RECIPES WHERE name = '김치볶음밥'"
})
class RecipeEntityMappingTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void mapsRecipeColumnsAndDatabaseGeneratedCreatedAt() {
        Recipe recipe = findRecipe();

        assertNotNull(recipe.getRecipeId());
        assertEquals("김치볶음밥", recipe.getName());
        assertEquals("간단한 김치볶음밥", recipe.getDescription());
        assertEquals(15, recipe.getCookingTime());
        assertEquals("https://example.com/kimchi.jpg", recipe.getImageUrl());
        assertNotNull(recipe.getCreatedAt());
    }

    @Test
    void mapsRecipeIngredientColumnsAndRecipeAssociation() {
        RecipeIngredient ingredient = entityManager.createQuery(
                        "select ingredient from RecipeIngredient ingredient", RecipeIngredient.class)
                .getSingleResult();

        assertNotNull(ingredient.getRecipeIngredientId());
        assertEquals(findRecipe().getRecipeId(), ingredient.getRecipe().getRecipeId());
        assertEquals("김치", ingredient.getIngredientName());
        assertEquals(new BigDecimal("200.00"), ingredient.getAmount());
        assertEquals("g", ingredient.getUnit());
    }

    @Test
    void mapsRecipeStepColumnsAndRecipeAssociation() {
        RecipeStep step = entityManager.createQuery("select step from RecipeStep step", RecipeStep.class)
                .getSingleResult();

        assertNotNull(step.getStepId());
        assertEquals(findRecipe().getRecipeId(), step.getRecipe().getRecipeId());
        assertEquals(1, step.getStepNo());
        assertEquals("김치를 잘게 썬다", step.getDescription());
    }

    private Recipe findRecipe() {
        return entityManager.createQuery("select recipe from Recipe recipe", Recipe.class).getSingleResult();
    }
}

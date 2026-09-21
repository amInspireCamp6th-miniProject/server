package com.example.server.recipe.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.server.recipe.entity.Recipe;
import com.example.server.recipe.entity.RecipeIngredient;
import com.example.server.recipe.entity.RecipeStep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(statements = {
    "INSERT INTO RECIPES (name) VALUES ('김치볶음밥')",
    "INSERT INTO RECIPES (name) VALUES ('계란볶음밥')",
    "INSERT INTO RECIPE_INGREDIENTS (recipe_id, ingredient_name, amount, unit) "
            + "SELECT recipe_id, '김치', 200.00, 'g' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_INGREDIENTS (recipe_id, ingredient_name, amount, unit) "
            + "SELECT recipe_id, '밥', 1.00, '공기' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_INGREDIENTS (recipe_id, ingredient_name, amount, unit) "
            + "SELECT recipe_id, '달걀', 1.00, '개' FROM RECIPES WHERE name = '계란볶음밥'",
    "INSERT INTO RECIPE_STEPS (recipe_id, step_no, description) "
            + "SELECT recipe_id, 3, '완성된 볶음밥을 그릇에 담는다' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_STEPS (recipe_id, step_no, description) "
            + "SELECT recipe_id, 1, '김치를 잘게 썬다' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_STEPS (recipe_id, step_no, description) "
            + "SELECT recipe_id, 2, '팬에 김치와 밥을 볶는다' FROM RECIPES WHERE name = '김치볶음밥'",
    "INSERT INTO RECIPE_STEPS (recipe_id, step_no, description) "
            + "SELECT recipe_id, 1, '달걀을 볶는다' FROM RECIPES WHERE name = '계란볶음밥'"
})
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeStepRepository recipeStepRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findsRecipeById() {
        Long recipeId = findRecipeId("김치볶음밥");

        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow();

        assertEquals(recipeId, recipe.getRecipeId());
        assertEquals("김치볶음밥", recipe.getName());
    }

    @Test
    void findsOnlyIngredientsForRequestedRecipe() {
        Long recipeId = findRecipeId("김치볶음밥");

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findAllByRecipe_RecipeId(recipeId);

        assertEquals(2, ingredients.size());
        assertEquals(Set.of("김치", "밥"), ingredients.stream()
                .map(RecipeIngredient::getIngredientName)
                .collect(Collectors.toSet()));
        assertTrue(ingredients.stream().allMatch(ingredient -> recipeId.equals(ingredient.getRecipe().getRecipeId())));
        assertEquals(1, recipeIngredientRepository.findAllByRecipe_RecipeId(findRecipeId("계란볶음밥")).size());
        assertTrue(recipeIngredientRepository.findAllByRecipe_RecipeId(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void findsOnlyStepsForRequestedRecipeInStepNumberOrder() {
        Long recipeId = findRecipeId("김치볶음밥");

        List<RecipeStep> steps = recipeStepRepository.findAllByRecipe_RecipeIdOrderByStepNoAsc(recipeId);

        assertEquals(3, steps.size());
        assertEquals(List.of(1, 2, 3), steps.stream().map(RecipeStep::getStepNo).toList());
        assertEquals(List.of("김치를 잘게 썬다", "팬에 김치와 밥을 볶는다", "완성된 볶음밥을 그릇에 담는다"),
                steps.stream().map(RecipeStep::getDescription).toList());
        assertTrue(steps.stream().allMatch(step -> recipeId.equals(step.getRecipe().getRecipeId())));
        assertEquals(1, recipeStepRepository.findAllByRecipe_RecipeIdOrderByStepNoAsc(findRecipeId("계란볶음밥")).size());
        assertTrue(recipeStepRepository.findAllByRecipe_RecipeIdOrderByStepNoAsc(Long.MAX_VALUE).isEmpty());
    }

    private Long findRecipeId(String name) {
        Number id = (Number) entityManager.createNativeQuery("SELECT recipe_id FROM RECIPES WHERE name = :name")
                .setParameter("name", name)
                .getSingleResult();
        return id.longValue();
    }
}

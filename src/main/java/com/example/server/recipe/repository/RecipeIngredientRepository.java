package com.example.server.recipe.repository;

import com.example.server.recipe.entity.RecipeIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    List<RecipeIngredient> findAllByRecipe_RecipeId(Long recipeId);

    @Query("""
            SELECT recipeIngredient
            FROM RecipeIngredient recipeIngredient
            JOIN FETCH recipeIngredient.recipe recipe
            ORDER BY recipe.recipeId ASC, recipeIngredient.recipeIngredientId ASC
            """)
    List<RecipeIngredient> findAllWithRecipe();
}

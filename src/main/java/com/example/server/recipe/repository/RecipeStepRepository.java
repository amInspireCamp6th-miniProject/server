package com.example.server.recipe.repository;

import com.example.server.recipe.entity.RecipeStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    List<RecipeStep> findAllByRecipe_RecipeIdOrderByStepNoAsc(Long recipeId);
}

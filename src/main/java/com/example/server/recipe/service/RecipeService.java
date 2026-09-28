package com.example.server.recipe.service;

import com.example.server.global.exception.BusinessException;
import com.example.server.recipe.dto.RecipeDetailResponse;
import com.example.server.recipe.dto.RecipeIngredientResponse;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.entity.Recipe;
import com.example.server.recipe.exception.RecipeErrorCode;
import com.example.server.recipe.repository.RecipeIngredientRepository;
import com.example.server.recipe.repository.RecipeRepository;
import com.example.server.recipe.repository.RecipeStepRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            RecipeStepRepository recipeStepRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeStepRepository = recipeStepRepository;
    }

    public RecipeDetailResponse getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOT_FOUND));

        List<RecipeIngredientResponse> ingredients = recipeIngredientRepository
                .findAllByRecipe_RecipeId(recipeId)
                .stream()
                .map(ingredient -> new RecipeIngredientResponse(
                        ingredient.getIngredientName(), ingredient.getAmount(), ingredient.getUnit()))
                .toList();

        return new RecipeDetailResponse(
                recipe.getRecipeId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getCookingTime(),
                recipe.getImageUrl(),
                ingredients);
    }

    public RecipeInstructionsResponse getInstructions(Long recipeId) {
        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOT_FOUND));

        List<RecipeInstructionStepResponse> instructions = recipeStepRepository
                .findAllByRecipe_RecipeIdOrderByStepNoAsc(recipeId)
                .stream()
                .map(step -> new RecipeInstructionStepResponse(step.getStepNo(), step.getDescription()))
                .toList();

        return new RecipeInstructionsResponse(recipeId, instructions);
    }
}

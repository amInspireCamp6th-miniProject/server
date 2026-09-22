package com.example.server.recipe.service;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.repository.RecipeRepository;
import com.example.server.recipe.repository.RecipeStepRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;

    public RecipeService(RecipeRepository recipeRepository, RecipeStepRepository recipeStepRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeStepRepository = recipeStepRepository;
    }

    public RecipeInstructionsResponse getInstructions(Long recipeId) {
        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        List<RecipeInstructionStepResponse> instructions = recipeStepRepository
                .findAllByRecipe_RecipeIdOrderByStepNoAsc(recipeId)
                .stream()
                .map(step -> new RecipeInstructionStepResponse(step.getStepNo(), step.getDescription()))
                .toList();

        return new RecipeInstructionsResponse(recipeId, instructions);
    }
}

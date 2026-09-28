package com.example.server.recipe.controller;

import com.example.server.global.security.LoginUser;
import com.example.server.recipe.dto.RecipeDetailResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.dto.RecipeRecommendationRequest;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import com.example.server.recipe.service.RecipeRecommendationService;
import com.example.server.recipe.service.RecipeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeRecommendationService recipeRecommendationService;

    public RecipeController(
            RecipeService recipeService,
            RecipeRecommendationService recipeRecommendationService) {
        this.recipeService = recipeService;
        this.recipeRecommendationService = recipeRecommendationService;
    }

    @PostMapping("/recommendations")
    public List<RecipeRecommendationResponse> recommend(
            @LoginUser Long userId,
            @Valid @RequestBody RecipeRecommendationRequest request) {
        return recipeRecommendationService.recommend(userId, request);
    }

    @GetMapping("/{recipeId}")
    public RecipeDetailResponse getRecipeDetail(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getRecipeDetail(recipeId);
    }

    @GetMapping("/{recipeId}/instructions")
    public RecipeInstructionsResponse getInstructions(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getInstructions(recipeId);
    }
}

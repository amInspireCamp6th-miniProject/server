package com.example.server.recipe.controller;

import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/{recipeId}/instructions")
    public RecipeInstructionsResponse getInstructions(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getInstructions(recipeId);
    }
}

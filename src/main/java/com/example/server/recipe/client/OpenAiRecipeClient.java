package com.example.server.recipe.client;

import com.example.server.recipe.client.dto.AiRecipeRecommendationResult;
import java.util.Optional;

public interface OpenAiRecipeClient {

    Optional<AiRecipeRecommendationResult> recommend(String prompt);
}

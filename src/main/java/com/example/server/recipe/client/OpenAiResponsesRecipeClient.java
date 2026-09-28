package com.example.server.recipe.client;

import com.example.server.recipe.client.dto.AiRecipeRecommendationResult;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiResponsesRecipeClient implements OpenAiRecipeClient {

    private static final long MAX_OUTPUT_TOKENS = 1_500;
    private static final String SYSTEM_INSTRUCTIONS = """
            당신은 냉장고 식재료를 활용해 현실적인 요리를 추천하는 조리 도우미입니다.
            응답 스키마를 정확히 지키고 한국 사용자가 이해하기 쉬운 한국어로 작성하세요.
            usedIngredients에는 사용 가능한 식재료 목록에 있는 이름만 정확히 사용하세요.
            그 밖에 필요한 재료는 모두 additionalIngredients에 넣으세요.
            name은 비워 두지 말고 cookingTime은 양수로 작성하세요.
            usedIngredients와 instructions에는 각각 항목을 하나 이상 넣으세요.
            추가 재료가 없으면 additionalIngredients는 빈 배열로 작성하세요.
            """;

    private final Optional<OpenAIClient> openAIClient;
    private final String model;

    public OpenAiResponsesRecipeClient(
            Optional<OpenAIClient> openAIClient,
            @Value("${openai.model:gpt-5.4-mini}") String model) {
        this.openAIClient = openAIClient;
        this.model = model;
    }

    @Override
    public Optional<AiRecipeRecommendationResult> recommend(String prompt) {
        if (openAIClient.isEmpty()) {
            return Optional.empty();
        }

        StructuredResponseCreateParams<AiRecipeRecommendationResult> params =
                StructuredResponseCreateParams.<AiRecipeRecommendationResult>builder()
                        .model(model)
                        .instructions(SYSTEM_INSTRUCTIONS)
                        .input(prompt)
                        .text(AiRecipeRecommendationResult.class)
                        .maxOutputTokens(MAX_OUTPUT_TOKENS)
                        .store(false)
                        .build();
        StructuredResponse<AiRecipeRecommendationResult> response =
                openAIClient.get().responses().create(params);

        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst();
    }
}

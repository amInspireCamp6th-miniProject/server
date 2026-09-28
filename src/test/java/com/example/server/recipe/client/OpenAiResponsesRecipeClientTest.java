package com.example.server.recipe.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.server.recipe.client.dto.AiRecipeRecommendationResult;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.openai.services.blocking.ResponseService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OpenAiResponsesRecipeClientTest {

    @Test
    void returnsEmptyWhenOpenAiClientBeanIsUnavailable() {
        OpenAiResponsesRecipeClient client =
                new OpenAiResponsesRecipeClient(Optional.empty(), "gpt-5.4-mini");

        assertThat(client.recommend("prompt")).isEmpty();
    }

    @Test
    void buildsStructuredResponsesRequestBeforeCallingClient() {
        OpenAIClient openAIClient = mock(OpenAIClient.class);
        ResponseService responseService = mock(ResponseService.class);
        given(openAIClient.responses()).willReturn(responseService);
        given(responseService.create(anyStructuredResponseParams()))
                .willThrow(new IllegalStateException("stop after request validation"));
        OpenAiResponsesRecipeClient client =
                new OpenAiResponsesRecipeClient(Optional.of(openAIClient), "gpt-5.4-mini");

        assertThatThrownBy(() -> client.recommend("prompt"))
                .isInstanceOf(IllegalStateException.class);
        verify(responseService).create(anyStructuredResponseParams());
    }

    private StructuredResponseCreateParams<AiRecipeRecommendationResult>
            anyStructuredResponseParams() {
        return any();
    }
}

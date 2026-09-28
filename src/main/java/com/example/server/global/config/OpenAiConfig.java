package com.example.server.global.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

@Configuration
public class OpenAiConfig {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);
    private static final int MAX_RETRIES = 1;

    @Bean(destroyMethod = "close")
    @Conditional(OpenAiApiKeyPresentCondition.class)
    public OpenAIClient openAIClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .timeout(REQUEST_TIMEOUT)
                .maxRetries(MAX_RETRIES)
                .build();
    }

    static class OpenAiApiKeyPresentCondition implements Condition {

        @Override
        public boolean matches(
                ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(System.getenv("OPENAI_API_KEY"));
        }
    }
}

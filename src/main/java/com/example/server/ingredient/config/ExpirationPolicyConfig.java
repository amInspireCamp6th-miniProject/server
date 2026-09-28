package com.example.server.ingredient.config;

import com.example.server.ingredient.domain.ExpirationPolicy;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpirationPolicyConfig {

    @Bean
    public ExpirationPolicy expirationPolicy(Clock clock) {
        return new ExpirationPolicy(clock);
    }
}

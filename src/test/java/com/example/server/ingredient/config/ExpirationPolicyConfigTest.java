package com.example.server.ingredient.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.server.global.config.ClockConfig;
import com.example.server.ingredient.domain.ExpirationPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class ExpirationPolicyConfigTest {

    @Test
    void registersExpirationPolicyBean() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                ClockConfig.class, ExpirationPolicyConfig.class)) {
            assertNotNull(context.getBean(ExpirationPolicy.class));
        }
    }
}

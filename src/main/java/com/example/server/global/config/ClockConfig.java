package com.example.server.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 현재 시각을 주입받아 쓰기 위한 Clock. 테스트에서 시각을 고정할 수 있다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

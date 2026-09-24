package com.example.server.global.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param allowedOrigins 브라우저 요청을 허용할 프론트엔드 주소. 포트가 다르면 다른 출처이므로 정확히 적는다.
 */
@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        @NotEmpty List<String> allowedOrigins
) {
}

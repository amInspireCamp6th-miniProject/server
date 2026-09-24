package com.example.server.global.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Refresh Token 쿠키 설정. 환경(로컬 / 배포)마다 달라지는 값만 모은다.
 *
 * @param path     쿠키를 보낼 경로. 재발급 API 경로와 같아야 한다. 다르면 브라우저가 쿠키를 보내지 않는다.
 * @param secure   HTTPS에서만 쿠키를 저장·전송할지. 로컬(http)은 false, 배포(https)는 true
 * @param sameSite 프론트와 API가 같은 사이트면 Lax. 다른 도메인이면 None(이때 secure는 반드시 true)
 */
@Validated
@ConfigurationProperties(prefix = "auth.refresh-cookie")
public record RefreshCookieProperties(
        @NotBlank String path,
        boolean secure,
        @NotBlank String sameSite
) {
}

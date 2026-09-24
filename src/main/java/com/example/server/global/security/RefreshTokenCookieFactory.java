package com.example.server.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh Token을 담는 HttpOnly 쿠키를 만든다. (C-07)
 * HttpOnly라 JavaScript가 읽거나 지울 수 없으므로, 발급과 삭제를 모두 서버가 한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {

    public static final String COOKIE_NAME = "refreshToken";

    private final RefreshCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    /**
     * 쿠키 유효 기간은 Refresh Token 유효 기간과 같게 둔다.
     */
    public ResponseCookie create(String refreshToken) {
        return builder(refreshToken)
                .maxAge(jwtProperties.refreshTokenValidity())
                .build();
    }

    /**
     * 로그아웃 시 브라우저의 쿠키를 지우기 위한 빈 쿠키. 이름·경로가 같아야 삭제된다.
     */
    public ResponseCookie expired() {
        return builder("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder builder(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path());
    }
}

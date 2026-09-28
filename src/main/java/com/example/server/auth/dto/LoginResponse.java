package com.example.server.auth.dto;

import com.example.server.user.dto.UserResponse;

/**
 * 로그인 응답 본문. API 명세: "Access Token, 회원 정보"
 * Refresh Token은 본문이 아니라 HttpOnly 쿠키로 나간다. (C-07)
 */
public record LoginResponse(String accessToken, UserResponse user) {
}

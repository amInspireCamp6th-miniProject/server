package com.example.server.auth.dto;

/**
 * 서비스가 컨트롤러에 돌려주는 재발급 결과. 컨트롤러가 본문과 쿠키로 나눈다.
 */
public record TokenResult(String accessToken, String refreshToken) {
}

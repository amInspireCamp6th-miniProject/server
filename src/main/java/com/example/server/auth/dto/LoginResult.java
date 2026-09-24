package com.example.server.auth.dto;

/**
 * 서비스가 컨트롤러에 돌려주는 로그인 결과. 컨트롤러가 본문(LoginResponse)과 쿠키(refreshToken)로 나눈다.
 * Refresh Token을 어디에 담을지는 전달 방식의 문제라 서비스가 아닌 컨트롤러에서 결정한다.
 */
public record LoginResult(LoginResponse response, String refreshToken) {
}

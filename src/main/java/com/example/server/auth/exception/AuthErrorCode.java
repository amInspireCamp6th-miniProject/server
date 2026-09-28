package com.example.server.auth.exception;

import com.example.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 이메일이 없는 경우와 비밀번호가 틀린 경우를 구분하지 않는다. (가입 여부 노출 방지)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 없는 토큰, 만료된 토큰, 이미 사용한 토큰(회전), 로그아웃으로 폐기된 토큰을 구분하지 않는다.
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}

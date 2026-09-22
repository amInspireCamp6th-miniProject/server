package com.example.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 오류 코드와 HTTP 상태, 사용자 메시지를 한곳에서 관리한다.
 *
 * <p>새 오류가 필요하면 각 Service에서 문자열을 직접 만들지 않고 이 enum에 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "식재료를 찾을 수 없습니다."),
    INVALID_OCR_IMAGE(HttpStatus.BAD_REQUEST, "JPG 또는 PNG 이미지를 5MB 이하로 업로드해 주세요."),
    OCR_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "OCR 서비스 설정이 필요합니다."),
    OCR_MONTHLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이번 달 이미지 인식 사용 한도를 초과했습니다."),
    OCR_FAILED(HttpStatus.BAD_GATEWAY, "이미지 인식에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}

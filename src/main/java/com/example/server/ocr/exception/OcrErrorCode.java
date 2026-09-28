package com.example.server.ocr.exception;

import com.example.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OcrErrorCode implements ErrorCode {

    INVALID_OCR_IMAGE(HttpStatus.BAD_REQUEST, "JPG 또는 PNG 이미지를 5MB 이하로 업로드해 주세요."),
    OCR_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "OCR 서비스 설정이 필요합니다."),
    OCR_MONTHLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이번 달 이미지 인식 사용 한도를 초과했습니다."),
    OCR_FAILED(HttpStatus.BAD_GATEWAY, "이미지 인식에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}

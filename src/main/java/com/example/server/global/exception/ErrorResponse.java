package com.example.server.global.exception;

/** 프론트엔드에 전달하는 공통 오류 응답 형식이다. */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}

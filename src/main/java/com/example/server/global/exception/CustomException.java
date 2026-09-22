package com.example.server.global.exception;

import lombok.Getter;

/**
 * Service가 업무 규칙 위반을 알릴 때 사용하는 공통 예외다.
 * Handler는 내부의 ErrorCode를 보고 HTTP 응답을 만든다.
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

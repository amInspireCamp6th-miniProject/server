package com.example.server.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 공통 규약. 도메인별로 enum을 만들어 이 인터페이스를 구현한다.
 * 한 enum에 모두 모으면 세 명이 같은 파일을 동시에 수정하게 되므로 도메인별로 분리한다.
 */
public interface ErrorCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();
}

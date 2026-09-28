package com.example.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 형식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    /**
     * 상태 코드에 해당하는 에러 코드를 찾는다. 같은 상태 코드가 여러 개면 먼저 선언한 값을 쓴다.
     * Spring이 상태 코드를 정해 던지는 예외(예: 415)를 우리 응답 형식으로 바꿀 때 사용한다.
     */
    public static Optional<GlobalErrorCode> findByStatus(HttpStatusCode status) {
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.status.equals(status))
                .findFirst();
    }

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}

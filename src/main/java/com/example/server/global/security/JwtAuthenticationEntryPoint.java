package com.example.server.global.security;

import com.example.server.global.exception.ErrorCode;
import com.example.server.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증이 필요한 요청에 인증 정보가 없을 때 401을 응답한다.
 * 토큰이 있었지만 잘못된 경우에는 필터가 남긴 사유(INVALID_TOKEN, TOKEN_EXPIRED)를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = request.getAttribute(JwtAuthenticationFilter.ERROR_CODE_ATTRIBUTE) instanceof ErrorCode code
                ? code
                : GlobalErrorCode.UNAUTHORIZED;
        errorResponseWriter.write(response, errorCode);
    }
}

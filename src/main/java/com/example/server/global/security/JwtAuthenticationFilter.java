package com.example.server.global.security;

import com.example.server.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.example.server.global.exception.GlobalErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization: Bearer {token} 헤더를 검증해 인증 정보(principal = 사용자 ID Long)를 설정한다.
 * 토큰이 잘못되면 인증 없이 다음 필터로 넘기고, 사유는 요청 속성에 남겨 EntryPoint가 응답에 사용한다.
 * 로그아웃한 토큰(TokenBlacklist)은 서명이 맞아도 INVALID_TOKEN으로 처리한다.
 * SecurityConfig에서 직접 생성한다. (@Component로 등록하면 서블릿 필터로 한 번 더 등록됨)
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ERROR_CODE_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".ERROR_CODE";
    private final JwtProvider jwtProvider;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = BearerTokenResolver.resolve(request);
        if (token != null) {
            try {
                Long userId = jwtProvider.getUserId(token);
                if (tokenBlacklist.contains(token)) {
                    throw new BusinessException(GlobalErrorCode.INVALID_TOKEN);
                }
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of()));
                SecurityContextHolder.setContext(context);
            } catch (BusinessException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_CODE_ATTRIBUTE, e.getErrorCode());
            }
        }
        filterChain.doFilter(request, response);
    }

}

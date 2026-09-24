package com.example.server.global.security;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * JwtAuthenticationFilter와 같은 형태(principal = 사용자 ID Long)의 인증 정보를 만든다.
 */
public class WithLoginUserSecurityContextFactory implements WithSecurityContextFactory<WithLoginUser> {

    @Override
    public SecurityContext createSecurityContext(WithLoginUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(annotation.userId(), null, List.of()));
        return context;
    }
}

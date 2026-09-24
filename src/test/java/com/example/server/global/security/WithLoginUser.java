package com.example.server.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * 테스트에서 로그인 상태를 만든다. 컨트롤러의 @LoginUser Long userId에 이 값이 주입된다.
 *
 * <pre>
 * &#64;Test
 * &#64;WithLoginUser(userId = 1L)
 * void 내_식재료_조회() { ... }
 * </pre>
 *
 * &#64;SpringBootTest + &#64;AutoConfigureMockMvc 테스트에서 사용한다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithLoginUserSecurityContextFactory.class)
public @interface WithLoginUser {

    long userId() default 1L;
}

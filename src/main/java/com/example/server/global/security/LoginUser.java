package com.example.server.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인한 사용자의 ID를 컨트롤러 파라미터로 주입한다.
 *
 * <pre>
 * public ResponseEntity&lt;...&gt; get(@LoginUser Long userId) { ... }
 * </pre>
 *
 * 파라미터 타입은 Long만 지원한다. 인증 정보가 없으면 401(UNAUTHORIZED)을 응답한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}

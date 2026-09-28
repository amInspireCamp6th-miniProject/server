package com.example.server.auth.controller;

import com.example.server.auth.dto.LoginRequest;
import com.example.server.auth.dto.LoginResponse;
import com.example.server.auth.dto.LoginResult;
import com.example.server.auth.dto.TokenResponse;
import com.example.server.auth.dto.TokenResult;
import com.example.server.auth.dto.SignupRequest;
import com.example.server.auth.service.AuthService;
import com.example.server.global.security.BearerTokenResolver;
import com.example.server.global.security.RefreshTokenCookieFactory;
import com.example.server.global.security.LoginUser;
import com.example.server.user.dto.UserResponse;
import com.example.server.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    // Refresh Token은 본문이 아니라 HttpOnly 쿠키로 내려준다. (C-07)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
                .body(result.response());
    }

    // Access Token이 만료된 상태에서 호출하므로 인증을 요구하지 않는다. (SecurityConfig 공개 경로)
    // Refresh Token은 브라우저가 쿠키로 자동 전송한다. 재발급할 때마다 새 쿠키로 교체한다. (회전)
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        TokenResult result = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
                .body(new TokenResponse(result.accessToken()));
    }

    // 요청에 쓴 토큰을 블랙리스트에 올리고 Refresh Token도 폐기한다. 클라이언트도 보관 중인 토큰을 삭제해야 한다. (D-19)
    // 돌려줄 데이터가 없으므로 삭제와 같이 204로 응답한다. (C-20)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, @LoginUser Long userId) {
        authService.logout(BearerTokenResolver.resolve(request), userId);
        // HttpOnly 쿠키는 JavaScript가 지울 수 없으므로 서버가 빈 쿠키로 덮어써 삭제한다.
        ResponseCookie expiredCookie = refreshTokenCookieFactory.expired();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}

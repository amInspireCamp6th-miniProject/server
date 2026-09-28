package com.example.server.auth.service;

import com.example.server.auth.dto.LoginRequest;
import com.example.server.auth.dto.LoginResult;
import com.example.server.auth.dto.TokenResult;
import com.example.server.auth.dto.LoginResponse;
import com.example.server.auth.dto.SignupRequest;
import com.example.server.auth.exception.AuthErrorCode;
import com.example.server.global.exception.BusinessException;
import com.example.server.global.security.JwtProvider;
import com.example.server.global.security.RefreshTokenStore;
import com.example.server.global.security.TokenBlacklist;
import com.example.server.user.dto.UserResponse;
import com.example.server.user.entity.User;
import com.example.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlacklist tokenBlacklist;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        User user = userService.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname());
        return UserResponse.from(user);
    }

    public LoginResult login(LoginRequest request) {
        User user = userService.findByEmail(request.email())
                .filter(found -> passwordMatches(request.password(), found.getPassword()))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));
        LoginResponse response = new LoginResponse(
                jwtProvider.createAccessToken(user.getId()),
                UserResponse.from(user));
        return new LoginResult(response, refreshTokenStore.issue(user.getId()));
    }

    /**
     * Refresh Token으로 Access Token을 재발급한다. 쓴 Refresh Token은 폐기하고 새로 발급한다. (회전)
     *
     * @param refreshToken 쿠키에서 꺼낸 Refresh Token. 쿠키가 없으면 null
     * @throws BusinessException 토큰이 없거나 만료됐거나 이미 쓴 토큰이면 INVALID_REFRESH_TOKEN
     */
    public TokenResult refresh(String refreshToken) {
        Long userId = refreshTokenStore.consume(refreshToken)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        return new TokenResult(jwtProvider.createAccessToken(userId), refreshTokenStore.issue(userId));
    }

    /**
     * Access Token을 만료 시각까지 사용할 수 없게 하고, 해당 회원의 Refresh Token도 모두 폐기한다. (D-19)
     * Refresh Token을 남겨 두면 로그아웃 뒤에도 재발급으로 새 Access Token을 받을 수 있다.
     * 인증 필터를 통과한 토큰만 들어오므로 여기서는 다시 검증하지 않는다.
     */
    public void logout(String accessToken, Long userId) {
        tokenBlacklist.add(accessToken, jwtProvider.getExpiration(accessToken));
        refreshTokenStore.revokeAll(userId);
    }

    // BCrypt는 72바이트를 넘는 입력에 예외를 던진다. 로그인에서는 단순 불일치로 처리한다.
    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

package com.example.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.server.auth.dto.LoginRequest;
import com.example.server.auth.dto.LoginResult;
import com.example.server.auth.dto.TokenResult;
import com.example.server.auth.dto.SignupRequest;
import com.example.server.auth.exception.AuthErrorCode;
import com.example.server.global.exception.BusinessException;
import com.example.server.global.security.JwtProvider;
import com.example.server.global.security.RefreshTokenStore;
import com.example.server.global.security.TokenBlacklist;
import java.time.Instant;
import com.example.server.user.entity.User;
import com.example.server.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입은_비밀번호를_암호화해서_넘긴다() {
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(userService.create("user@example.com", "encoded", "닉네임"))
                .willReturn(user(1L, "user@example.com", "encoded"));

        authService.signup(new SignupRequest("user@example.com", "password123", "닉네임"));

        verify(userService).create("user@example.com", "encoded", "닉네임");
    }

    @Test
    void 로그인에_성공하면_토큰과_회원_정보를_돌려준다() {
        given(userService.findByEmail("user@example.com"))
                .willReturn(Optional.of(user(1L, "user@example.com", "encoded")));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtProvider.createAccessToken(1L)).willReturn("token");
        given(refreshTokenStore.issue(1L)).willReturn("refresh-token");

        LoginResult result = authService.login(new LoginRequest("user@example.com", "password123"));

        assertThat(result.response().accessToken()).isEqualTo("token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.response().user().userId()).isEqualTo(1L);
    }

    @Test
    void 재발급하면_Access와_Refresh_토큰을_모두_새로_준다() {
        given(refreshTokenStore.consume("old-refresh")).willReturn(Optional.of(1L));
        given(jwtProvider.createAccessToken(1L)).willReturn("new-access");
        given(refreshTokenStore.issue(1L)).willReturn("new-refresh");

        TokenResult result = authService.refresh("old-refresh");

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void 쓸_수_없는_Refresh_토큰이면_INVALID_REFRESH_TOKEN() {
        given(refreshTokenStore.consume("used")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("used"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 없는_이메일이면_INVALID_CREDENTIALS() {
        given(userService.findByEmail("none@example.com")).willReturn(Optional.empty());

        assertInvalidCredentials(new LoginRequest("none@example.com", "password123"));
    }

    @Test
    void 비밀번호가_틀리면_INVALID_CREDENTIALS() {
        given(userService.findByEmail("user@example.com"))
                .willReturn(Optional.of(user(1L, "user@example.com", "encoded")));
        given(passwordEncoder.matches("wrong-password", "encoded")).willReturn(false);

        assertInvalidCredentials(new LoginRequest("user@example.com", "wrong-password"));
    }

    @Test
    void 비밀번호가_BCrypt_한도를_넘어도_500이_아니라_INVALID_CREDENTIALS() {
        given(userService.findByEmail("user@example.com"))
                .willReturn(Optional.of(user(1L, "user@example.com", "encoded")));
        given(passwordEncoder.matches("너무긴비밀번호", "encoded"))
                .willThrow(new IllegalArgumentException("password cannot be more than 72 bytes"));

        assertInvalidCredentials(new LoginRequest("user@example.com", "너무긴비밀번호"));
    }

    @Test
    void 로그아웃하면_Access_토큰을_블랙리스트에_올리고_Refresh_토큰을_폐기한다() {
        Instant expiresAt = Instant.parse("2026-09-23T00:00:00Z");
        given(jwtProvider.getExpiration("token")).willReturn(expiresAt);

        authService.logout("token", 1L);

        verify(tokenBlacklist).add("token", expiresAt);
        verify(refreshTokenStore).revokeAll(1L);
    }

    private void assertInvalidCredentials(LoginRequest request) {
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    private static User user(Long id, String email, String encodedPassword) {
        User user = User.create(email, encodedPassword, "닉네임");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}

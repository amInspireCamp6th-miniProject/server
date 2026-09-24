package com.example.server.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.security.RefreshTokenCookieFactory;
import com.example.server.global.security.WithLoginUser;
import com.example.server.user.entity.User;
import com.example.server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 회원가입_성공은_201과_회원_정보() throws Exception {
        signup("User@Example.com", "password123", "닉네임")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.nickname").value("닉네임"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 가입_직후_updated_at은_NULL이고_비밀번호는_암호화되어_있다() throws Exception {
        signup("user@example.com", "password123", "닉네임").andExpect(status().isCreated());

        User saved = userRepository.findByEmail("user@example.com").orElseThrow();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNull();
        assertThat(saved.getPassword()).isNotEqualTo("password123").startsWith("$2");
    }

    @Test
    void 같은_이메일로_가입하면_409_DUPLICATE_EMAIL() throws Exception {
        signup("user@example.com", "password123", "닉네임").andExpect(status().isCreated());

        signup("USER@example.com", "password456", "다른닉네임")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    void 입력값이_잘못되면_400과_필드별_사유() throws Exception {
        signup("not-an-email", "short", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void 로그인_성공은_200과_토큰_회원_정보() throws Exception {
        signup("user@example.com", "password123", "닉네임");

        login("User@example.com", "password123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                // Refresh Token은 본문이 아니라 HttpOnly 쿠키로 나간다.
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists(RefreshTokenCookieFactory.COOKIE_NAME))
                .andExpect(cookie().httpOnly(RefreshTokenCookieFactory.COOKIE_NAME, true))
                .andExpect(cookie().path(RefreshTokenCookieFactory.COOKIE_NAME, "/api/v1/auth/refresh"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.nickname").value("닉네임"));
    }

    @Test
    void 비밀번호가_틀리면_401_INVALID_CREDENTIALS() throws Exception {
        signup("user@example.com", "password123", "닉네임");

        login("user@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void 로그인_토큰으로_내_정보를_조회한다() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        String token = accessToken("user@example.com", "password123");

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.nickname").value("닉네임"));
    }

    @Test
    void 토큰_없이_내_정보를_조회하면_401_UNAUTHORIZED() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void 잘못된_토큰으로_조회하면_401_INVALID_TOKEN() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void 로그아웃은_인증이_필요하다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_상태면_로그아웃은_204_본문_없음() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        String token = accessToken("user@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void 로그아웃한_토큰은_401_INVALID_TOKEN() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        String token = accessToken("user@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    @WithLoginUser(userId = 999L)
    void 토큰의_사용자가_DB에_없으면_404_USER_NOT_FOUND() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @WithLoginUser
    void 없는_경로는_404_RESOURCE_NOT_FOUND() throws Exception {
        mockMvc.perform(get("/api/v1/no-such-path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 재발급은_인증_없이_쿠키로_호출하고_새_토큰을_받는다() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        Cookie refreshCookie = loginCookie("user@example.com", "password123");

        MockHttpServletResponse response = refresh(refreshCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                // 회전: 재발급할 때마다 새 쿠키로 교체된다.
                .andExpect(cookie().exists(RefreshTokenCookieFactory.COOKIE_NAME))
                .andReturn().getResponse();

        assertThat(response.getCookie(RefreshTokenCookieFactory.COOKIE_NAME).getValue())
                .isNotEqualTo(refreshCookie.getValue());

        // 재발급받은 Access Token으로 바로 조회할 수 있다.
        String accessToken = objectMapper.readTree(response.getContentAsString()).get("accessToken").asText();
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void 같은_Refresh_토큰을_두_번_쓰면_401() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        Cookie refreshCookie = loginCookie("user@example.com", "password123");
        refresh(refreshCookie).andExpect(status().isOk());

        refresh(refreshCookie)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void 쿠키_없이_재발급하면_401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void 로그아웃하면_Refresh_토큰도_쓸_수_없고_쿠키가_삭제된다() throws Exception {
        signup("user@example.com", "password123", "닉네임");
        MockHttpServletResponse loginResponse = login("user@example.com", "password123").andReturn().getResponse();
        Cookie refreshCookie = loginResponse.getCookie(RefreshTokenCookieFactory.COOKIE_NAME);
        String accessToken = objectMapper.readTree(loginResponse.getContentAsString()).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                // 브라우저의 쿠키를 지우는 빈 쿠키
                .andExpect(cookie().maxAge(RefreshTokenCookieFactory.COOKIE_NAME, 0));

        refresh(refreshCookie)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    private ResultActions refresh(Cookie refreshCookie) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie));
    }

    private Cookie loginCookie(String email, String password) throws Exception {
        return login(email, password).andReturn().getResponse().getCookie(RefreshTokenCookieFactory.COOKIE_NAME);
    }

    private ResultActions signup(String email, String password, String nickname) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", email, "password", password, "nickname", nickname))));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))));
    }

    private String accessToken(String email, String password) throws Exception {
        String body = login(email, password).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}

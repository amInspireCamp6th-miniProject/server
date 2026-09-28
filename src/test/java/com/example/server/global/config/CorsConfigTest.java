package com.example.server.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.security.WithLoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 프론트엔드(다른 포트)에서 오는 요청이 브라우저 CORS 규칙을 통과하는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 허용된_출처의_사전_요청은_인증_없이_통과한다() throws Exception {
        mockMvc.perform(options("/api/v1/auth/me")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void 허용되지_않은_출처의_사전_요청은_거부된다() throws Exception {
        mockMvc.perform(options("/api/v1/auth/me")
                        .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @WithLoginUser(userId = 999L)
    void 실제_요청_응답에도_허용_출처_헤더가_붙는다() throws Exception {
        // 회원이 없어 404지만, CORS 헤더는 응답 결과와 상관없이 붙어야 한다.
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void 인증_실패_응답에도_허용_출처_헤더가_붙는다() throws Exception {
        // 헤더가 없으면 브라우저가 401 응답 본문을 읽지 못해 에러 코드로 분기할 수 없다.
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }
}

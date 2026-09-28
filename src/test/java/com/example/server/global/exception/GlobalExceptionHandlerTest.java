package com.example.server.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.response.ErrorResponse;
import com.example.server.global.security.WithLoginUser;
import com.example.server.recipe.exception.RecipeErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 잘못된 요청이 500이 아니라 알맞은 4xx로 나가는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void JSON이_아닌_형식으로_요청하면_415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=user@example.com"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void 본문이_JSON_형식이_아니면_400_INVALID_INPUT() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @WithLoginUser
    void 지원하지_않는_Method로_요청하면_405_METHOD_NOT_ALLOWED() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void BusinessException의_레시피_미존재를_404로_변환한다() {
        ErrorCode errorCode = RecipeErrorCode.RECIPE_NOT_FOUND;
        BusinessException exception = new BusinessException(errorCode);

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler().handleBusinessException(exception);

        assertSame(errorCode, exception.getErrorCode());
        assertEquals("레시피를 찾을 수 없습니다.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, errorCode.getStatus());
        assertEquals("RECIPE_NOT_FOUND", errorCode.getCode());
        assertEquals("레시피를 찾을 수 없습니다.", errorCode.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RECIPE_NOT_FOUND", response.getBody().code());
        assertEquals("레시피를 찾을 수 없습니다.", response.getBody().message());
    }
}

package com.example.server.ingredient.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.service.IngredientService;
import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 실제 서버를 띄우지 않고 식재료 API 주소, JSON 변환, 검증, HTTP 상태 코드를 확인한다.
 */
@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngredientService ingredientService;

    @Test
    @DisplayName("식재료 등록 성공 시 201을 반환한다")
    void createIngredient() throws Exception {
        IngredientCreateRequest request = createRequest();
        given(ingredientService.create(eq(1L), any(IngredientCreateRequest.class)))
                .willReturn(response());

        mockMvc.perform(post("/api/v1/ingredients")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredientId").value(10L))
                .andExpect(jsonPath("$.ingredientName").value("두부"));
    }

    @Test
    @DisplayName("잘못된 등록 요청은 400을 반환한다")
    void rejectInvalidCreateRequest() throws Exception {
        IngredientCreateRequest request = new IngredientCreateRequest(
                "",
                "두부",
                "가공식품",
                BigDecimal.ZERO,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);

        mockMvc.perform(post("/api/v1/ingredients")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("보관 상태별 식재료 목록을 조회한다")
    void findAllIngredientsByStorageType() throws Exception {
        given(ingredientService.findAll(1L, StorageType.REFRIGERATED))
                .willReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/ingredients")
                        .requestAttr("userId", 1L)
                        .param("storageType", "REFRIGERATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientName").value("두부"));
    }

    @Test
    @DisplayName("식재료 상세 정보를 조회한다")
    void findIngredientById() throws Exception {
        given(ingredientService.findById(1L, 10L)).willReturn(response());

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", 10L)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientId").value(10L));
    }

    @Test
    @DisplayName("없는 식재료를 조회하면 공통 형식으로 404를 반환한다")
    void returnNotFoundErrorResponse() throws Exception {
        given(ingredientService.findById(1L, 999L))
                .willThrow(new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", 999L)
                        .requestAttr("userId", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INGREDIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("식재료를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("식재료 일부 정보를 수정한다")
    void updateIngredient() throws Exception {
        IngredientUpdateRequest request = new IngredientUpdateRequest(
                null,
                null,
                null,
                new BigDecimal("2.00"),
                null,
                null,
                null,
                null);
        given(ingredientService.update(eq(1L), eq(10L), any(IngredientUpdateRequest.class)))
                .willReturn(response());

        mockMvc.perform(patch("/api/v1/ingredients/{ingredientId}", 10L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientId").value(10L));
    }

    @Test
    @DisplayName("식재료 삭제 성공 시 204를 반환한다")
    void deleteIngredient() throws Exception {
        mockMvc.perform(delete("/api/v1/ingredients/{ingredientId}", 10L)
                        .requestAttr("userId", 1L))
                .andExpect(status().isNoContent());

        verify(ingredientService).delete(1L, 10L);
    }

    private IngredientCreateRequest createRequest() {
        return new IngredientCreateRequest(
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);
    }

    private IngredientResponse response() {
        return new IngredientResponse(
                10L,
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED,
                LocalDateTime.of(2026, 9, 22, 9, 0),
                null);
    }
}

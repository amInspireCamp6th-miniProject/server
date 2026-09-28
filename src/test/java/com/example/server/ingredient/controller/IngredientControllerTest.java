package com.example.server.ingredient.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientImageData;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.service.IngredientService;
import com.example.server.global.exception.BusinessException;
import com.example.server.ingredient.exception.IngredientErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.server.global.security.WithLoginUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 실제 서버를 띄우지 않고 식재료 API 주소, JSON 변환, 검증, HTTP 상태 코드를 확인한다.
 * addFilters = false: Security 필터를 태우지 않는다. 인증 자체는 인증 담당 테스트에서 검증한다.
 * @WithLoginUser: @LoginUser Long userId에 주입될 로그인 정보를 만든다. (userId = 1)
 */
@WebMvcTest(IngredientController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithLoginUser
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
                        .param("storageType", "REFRIGERATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientName").value("두부"));
    }

    @Test
    @DisplayName("식재료 상세 정보를 조회한다")
    void findIngredientById() throws Exception {
        given(ingredientService.findById(1L, 10L)).willReturn(response());

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientId").value(10L));
    }

    @Test
    @DisplayName("본인 식재료 이미지를 바이너리로 조회한다")
    void findIngredientImage() throws Exception {
        byte[] imageData = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        given(ingredientService.findImage(1L, 10L))
                .willReturn(new IngredientImageData(imageData, "image/jpeg", "tofu.jpg"));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}/image", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, imageData.length))
                .andExpect(content().bytes(imageData));

        verify(ingredientService).findImage(1L, 10L);
    }

    @Test
    @DisplayName("다른 사용자의 식재료 이미지는 404를 반환한다")
    void cannotFindAnotherUsersIngredientImage() throws Exception {
        given(ingredientService.findImage(1L, 999L))
                .willThrow(new BusinessException(IngredientErrorCode.INGREDIENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}/image", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INGREDIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("식재료에 이미지가 없으면 404를 반환한다")
    void returnNotFoundWhenIngredientHasNoImage() throws Exception {
        given(ingredientService.findImage(1L, 10L))
                .willThrow(new BusinessException(IngredientErrorCode.INGREDIENT_IMAGE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}/image", 10L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INGREDIENT_IMAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("식재료 이미지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("식재료 이미지를 교체하면 200과 imageUrl을 반환한다")
    void updateIngredientImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
        given(ingredientService.updateImage(eq(1L), eq(10L), any(MultipartFile.class)))
                .willReturn(new IngredientResponse(
                        10L, "풀무원 국산콩 두부 300g", "두부", "가공식품", BigDecimal.ONE, "모",
                        LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 25),
                        StorageType.REFRIGERATED, LocalDateTime.of(2026, 9, 22, 9, 0),
                        LocalDateTime.of(2026, 9, 28, 9, 0), "/api/v1/ingredients/10/image"));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/ingredients/{ingredientId}/image", 10L)
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientId").value(10L))
                .andExpect(jsonPath("$.imageUrl").value("/api/v1/ingredients/10/image"));

        verify(ingredientService).updateImage(eq(1L), eq(10L), any(MultipartFile.class));
    }

    @Test
    @DisplayName("교체할 이미지 파트가 없으면 400을 반환한다")
    void rejectMissingImagePart() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/ingredients/{ingredientId}/image", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(ingredientService);
    }

    @Test
    @DisplayName("없는 식재료를 조회하면 공통 형식으로 404를 반환한다")
    void returnNotFoundErrorResponse() throws Exception {
        given(ingredientService.findById(1L, 999L))
                .willThrow(new BusinessException(IngredientErrorCode.INGREDIENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/ingredients/{ingredientId}", 999L))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientId").value(10L));
    }

    @Test
    @DisplayName("식재료 삭제 성공 시 204를 반환한다")
    void deleteIngredient() throws Exception {
        mockMvc.perform(delete("/api/v1/ingredients/{ingredientId}", 10L))
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
                null,
                null);
    }
}

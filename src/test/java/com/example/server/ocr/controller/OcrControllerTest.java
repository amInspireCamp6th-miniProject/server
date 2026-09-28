package com.example.server.ocr.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.exception.BusinessException;
import com.example.server.global.security.WithLoginUser;
import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ocr.dto.OcrIngredientResponse;
import com.example.server.ocr.exception.OcrErrorCode;
import com.example.server.ocr.service.OcrIngredientRegistrationService;
import com.example.server.ocr.service.OcrService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 업로드 API 주소, multipart 필드명, JSON과 오류 응답을 확인한다.
 * addFilters = false: Security 필터를 태우지 않는다. 인증 자체는 인증 담당 테스트에서 검증한다.
 */
@WebMvcTest(OcrController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithLoginUser
class OcrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OcrService ocrService;

    @MockitoBean
    private OcrIngredientRegistrationService registrationService;

    @Test
    @DisplayName("image 필드로 제품 사진을 업로드한다")
    void recognizeIngredientImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "tofu.png", "image/png", new byte[] {(byte) 0x89, 0x50});
        given(ocrService.recognize(any(MultipartFile.class)))
                .willReturn(new OcrIngredientResponse("국산콩 두부", "두부", "가공식품"));

        mockMvc.perform(multipart("/api/v1/ocr/ingredients").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("국산콩 두부"))
                .andExpect(jsonPath("$.ingredientName").value("두부"))
                .andExpect(jsonPath("$.category").value("가공식품"));
    }

    @Test
    @DisplayName("잘못된 이미지이면 공통 오류 형식으로 400을 반환한다")
    void rejectInvalidImage() throws Exception {
        MockMultipartFile image =
                new MockMultipartFile("image", "memo.txt", "text/plain", "hello".getBytes());
        given(ocrService.recognize(any(MultipartFile.class)))
                .willThrow(new BusinessException(OcrErrorCode.INVALID_OCR_IMAGE));

        mockMvc.perform(multipart("/api/v1/ocr/ingredients").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_OCR_IMAGE"));
    }

    @Test
    @DisplayName("식재료 정보와 원본 이미지를 등록한다")
    void registerIngredient() throws Exception {
        IngredientCreateRequest request = createRequest();
        MockMultipartFile requestPart = requestPart(request);
        MockMultipartFile image = validJpegImage();
        IngredientResponse response = new IngredientResponse(
                15L,
                request.productName(),
                request.ingredientName(),
                request.category(),
                request.quantity(),
                request.unit(),
                request.purchaseDate(),
                request.expirationDate(),
                request.storageType(),
                null,
                null,
                "/api/v1/ingredients/15/image");
        given(registrationService.register(
                        eq(1L),
                        any(IngredientCreateRequest.class),
                        any(MultipartFile.class)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/v1/ocr/ingredients/register")
                        .file(requestPart)
                        .file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredientId").value(15L))
                .andExpect(jsonPath("$.imageUrl")
                        .value("/api/v1/ingredients/15/image"));

        verify(registrationService).register(
                eq(1L),
                any(IngredientCreateRequest.class),
                any(MultipartFile.class));
    }

    @Test
    @DisplayName("등록 이미지가 없으면 400을 반환한다")
    void rejectMissingRegistrationImage() throws Exception {
        mockMvc.perform(multipart("/api/v1/ocr/ingredients/register")
                        .file(requestPart(createRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(registrationService);
    }

    @Test
    @DisplayName("식재료 등록 요청 검증을 적용한다")
    void validateIngredientRequest() throws Exception {
        IngredientCreateRequest invalidRequest = new IngredientCreateRequest(
                "",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 10, 3),
                StorageType.REFRIGERATED);

        mockMvc.perform(multipart("/api/v1/ocr/ingredients/register")
                        .file(requestPart(invalidRequest))
                        .file(validJpegImage()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrationService);
    }

    private MockMultipartFile requestPart(IngredientCreateRequest request) throws Exception {
        return new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));
    }

    private MockMultipartFile validJpegImage() {
        byte[] jpegBytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        return new MockMultipartFile("image", "tofu.jpg", "image/jpeg", jpegBytes);
    }

    private IngredientCreateRequest createRequest() {
        return new IngredientCreateRequest(
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 10, 3),
                StorageType.REFRIGERATED);
    }
}

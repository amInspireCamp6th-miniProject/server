package com.example.server.ocr.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.dto.OcrIngredientResponse;
import com.example.server.ocr.service.OcrService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

/** 이미지 업로드 API 주소, multipart 필드명, JSON과 오류 응답을 확인한다. */
@WebMvcTest(OcrController.class)
class OcrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OcrService ocrService;

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
                .willThrow(new CustomException(ErrorCode.INVALID_OCR_IMAGE));

        mockMvc.perform(multipart("/api/v1/ocr/ingredients").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_OCR_IMAGE"));
    }
}

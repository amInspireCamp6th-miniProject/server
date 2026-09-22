package com.example.server.ocr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.client.OcrClient;
import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

/** 외부 API 호출 없이 이미지 검증과 OCR 처리 순서를 확인한다. */
@ExtendWith(MockitoExtension.class)
class OcrServiceTest {

    @Mock
    private OcrClient ocrClient;

    @Mock
    private IngredientTextAnalyzer ingredientTextAnalyzer;

    private OcrService ocrService;

    @BeforeEach
    void setUp() {
        ocrService = new OcrService(ocrClient, ingredientTextAnalyzer);
    }

    @Test
    @DisplayName("정상 PNG 이미지를 인식한다")
    void recognizePngImage() {
        MockMultipartFile image = validPngImage();
        List<OcrTextBlock> blocks = List.of(new OcrTextBlock("두부", 0.99, true));
        OcrIngredientResponse expected =
                new OcrIngredientResponse("국산콩 두부", "두부", "가공식품");
        given(ocrClient.extractText(image)).willReturn(blocks);
        given(ingredientTextAnalyzer.analyze(blocks)).willReturn(expected);

        OcrIngredientResponse response = ocrService.recognize(image);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미지가 아닌 파일은 거부한다")
    void rejectNonImageFile() {
        MockMultipartFile textFile =
                new MockMultipartFile("image", "memo.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> ocrService.recognize(textFile))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_OCR_IMAGE);
    }

    private MockMultipartFile validPngImage() {
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
        return new MockMultipartFile("image", "tofu.png", "image/png", pngBytes);
    }
}

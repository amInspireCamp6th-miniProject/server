package com.example.server.ocr.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.config.ClovaOcrProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** 실제 CLOVA 과금 없이 요청 헤더와 응답 JSON 변환을 검증한다. */
class ClovaOcrClientTest {

    @Test
    @DisplayName("CLOVA OCR 응답의 fields를 글자 목록으로 변환한다")
    void extractRecognizedText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ClovaOcrProperties properties = properties("https://example.com/general", "secret");
        ClovaOcrClient client = new ClovaOcrClient(
                builder, properties, new ObjectMapper(), new OcrMonthlyUsageLimiter(properties));
        String responseBody = """
                {
                  "images": [{
                    "inferResult": "SUCCESS",
                    "fields": [{
                      "inferText": "두부",
                      "inferConfidence": 0.99,
                      "lineBreak": true
                    }]
                  }]
                }
                """;
        server.expect(requestTo("https://example.com/general"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-OCR-SECRET", "secret"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<OcrTextBlock> blocks = client.extractText(validPngImage());

        assertThat(blocks).containsExactly(new OcrTextBlock("두부", 0.99, true));
        server.verify();
    }

    @Test
    @DisplayName("CLOVA 접속 정보가 없으면 외부 호출 전에 오류를 반환한다")
    void rejectMissingConfiguration() {
        ClovaOcrProperties properties = properties("", "");
        ClovaOcrClient client = new ClovaOcrClient(
                RestClient.builder(),
                properties,
                new ObjectMapper(),
                new OcrMonthlyUsageLimiter(properties));

        assertThatThrownBy(() -> client.extractText(validPngImage()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OCR_NOT_CONFIGURED);
    }

    private ClovaOcrProperties properties(String invokeUrl, String secretKey) {
        ClovaOcrProperties properties = new ClovaOcrProperties();
        properties.setInvokeUrl(invokeUrl);
        properties.setSecretKey(secretKey);
        return properties;
    }

    private MockMultipartFile validPngImage() {
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
        return new MockMultipartFile("image", "tofu.png", "image/png", pngBytes);
    }
}

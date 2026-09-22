package com.example.server.ocr.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.config.ClovaOcrProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OcrMonthlyUsageLimiterTest {

    @Test
    @DisplayName("설정된 월 요청 수에 도달하면 CLOVA 호출 전에 차단한다")
    void rejectRequestOverMonthlyLimit() {
        ClovaOcrProperties properties = new ClovaOcrProperties();
        properties.setMonthlyLimit(2);
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-22T06:00:00Z"), ZoneId.of("Asia/Seoul"));
        OcrMonthlyUsageLimiter limiter = new OcrMonthlyUsageLimiter(properties, clock);

        limiter.acquire();
        limiter.acquire();

        assertThatThrownBy(limiter::acquire)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OCR_MONTHLY_LIMIT_EXCEEDED);
    }
}

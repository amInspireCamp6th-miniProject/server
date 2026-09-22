package com.example.server.ocr.client;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.config.ClovaOcrProperties;
import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 한 달 동안 CLOVA OCR에 보낼 수 있는 요청 수를 애플리케이션 내부에서 제한한다.
 *
 * <p>이 카운터는 서버 프로세스가 다시 시작되면 초기화된다. 따라서 영구적인 최종 차단은
 * API Gateway의 월 90회 Usage Plan이 담당하고, 이 클래스는 그보다 먼저 차단하는 보조 안전장치다.
 */
@Component
public class OcrMonthlyUsageLimiter {

    private static final ZoneId BILLING_ZONE = ZoneId.of("Asia/Seoul");

    private final ClovaOcrProperties properties;
    private final Clock clock;
    private YearMonth countedMonth;
    private int requestCount;

    @Autowired
    public OcrMonthlyUsageLimiter(ClovaOcrProperties properties) {
        this(properties, Clock.system(BILLING_ZONE));
    }

    OcrMonthlyUsageLimiter(ClovaOcrProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.countedMonth = YearMonth.now(clock);
    }

    /** 동시 요청에서도 설정된 월 한도를 넘지 않도록 확인과 증가를 한 번에 처리한다. */
    public synchronized void acquire() {
        YearMonth currentMonth = YearMonth.now(clock);
        if (!currentMonth.equals(countedMonth)) {
            countedMonth = currentMonth;
            requestCount = 0;
        }

        if (requestCount >= properties.getMonthlyLimit()) {
            throw new CustomException(ErrorCode.OCR_MONTHLY_LIMIT_EXCEEDED);
        }

        requestCount++;
    }
}

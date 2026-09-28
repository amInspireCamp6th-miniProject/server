package com.example.server.ocr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CLOVA OCR 접속 정보를 환경변수에서 받아 보관한다.
 * 실제 URL과 Secret은 소스 코드나 Git에 저장하지 않는다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "clova.ocr")
public class ClovaOcrProperties {

    private String invokeUrl = "";
    private String secretKey = "";
    /** API Gateway의 월 90회 제한보다 먼저 차단하기 위한 애플리케이션 내부 한도다. */
    private int monthlyLimit = 80;
}

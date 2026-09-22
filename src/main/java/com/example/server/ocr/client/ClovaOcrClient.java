package com.example.server.ocr.client;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.config.ClovaOcrProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

/** 네이버 CLOVA General OCR V2 API를 multipart/form-data 방식으로 호출한다. */
@Component
public class ClovaOcrClient implements OcrClient {

    private static final String SECRET_HEADER = "X-OCR-SECRET";

    private final RestClient restClient;
    private final ClovaOcrProperties properties;
    private final ObjectMapper objectMapper;
    private final OcrMonthlyUsageLimiter usageLimiter;

    public ClovaOcrClient(
            RestClient.Builder restClientBuilder,
            ClovaOcrProperties properties,
            ObjectMapper objectMapper,
            OcrMonthlyUsageLimiter usageLimiter) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.usageLimiter = usageLimiter;
    }

    @Override
    public List<OcrTextBlock> extractText(MultipartFile image) {
        validateConfiguration();
        // 외부 API를 호출하기 직전에 횟수를 예약하여 동시 요청도 한도를 넘지 않게 한다.
        usageLimiter.acquire();

        try {
            String format = resolveFormat(image.getContentType());
            String imageName = resolveImageName(image.getOriginalFilename());
            ClovaRequest request = new ClovaRequest(
                    "V2",
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    "ko",
                    List.of(new ClovaRequestImage(format, imageName)),
                    false);

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", new NamedByteArrayResource(image.getBytes(), image.getOriginalFilename()));
            parts.add("message", objectMapper.writeValueAsString(request));

            ClovaResponse response = restClient
                    .post()
                    .uri(properties.getInvokeUrl())
                    .header(SECRET_HEADER, properties.getSecretKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(parts)
                    .retrieve()
                    .body(ClovaResponse.class);

            return extractSuccessfulFields(response);
        } catch (IOException | RestClientException exception) {
            // 외부 응답이나 Secret을 로그에 남기지 않고 공통 오류로 변환한다.
            throw new CustomException(ErrorCode.OCR_FAILED);
        }
    }

    private void validateConfiguration() {
        if (properties.getInvokeUrl().isBlank() || properties.getSecretKey().isBlank()) {
            throw new CustomException(ErrorCode.OCR_NOT_CONFIGURED);
        }
    }

    private List<OcrTextBlock> extractSuccessfulFields(ClovaResponse response) {
        if (response == null || response.images() == null || response.images().isEmpty()) {
            throw new CustomException(ErrorCode.OCR_FAILED);
        }

        ClovaImageResult imageResult = response.images().get(0);
        if (!"SUCCESS".equals(imageResult.inferResult()) || imageResult.fields() == null) {
            throw new CustomException(ErrorCode.OCR_FAILED);
        }

        return imageResult.fields().stream()
                .map(field -> new OcrTextBlock(
                        field.inferText(),
                        field.inferConfidence() == null ? 0.0 : field.inferConfidence(),
                        Boolean.TRUE.equals(field.lineBreak())))
                .toList();
    }

    private String resolveFormat(String contentType) {
        return "image/png".equals(contentType) ? "png" : "jpg";
    }

    private String resolveImageName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "ingredient-image";
        }
        int extensionIndex = originalFilename.lastIndexOf('.');
        return extensionIndex > 0 ? originalFilename.substring(0, extensionIndex) : originalFilename;
    }

    private record ClovaRequest(
            String version,
            String requestId,
            long timestamp,
            String lang,
            List<ClovaRequestImage> images,
            boolean enableTableDetection) {}

    private record ClovaRequestImage(String format, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ClovaResponse(List<ClovaImageResult> images) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ClovaImageResult(String inferResult, List<ClovaField> fields) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ClovaField(String inferText, Double inferConfidence, Boolean lineBreak) {}

    /** 메모리의 이미지 파일도 multipart 파일 이름을 갖도록 보완한다. */
    private static class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename == null || filename.isBlank() ? "ingredient-image" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}

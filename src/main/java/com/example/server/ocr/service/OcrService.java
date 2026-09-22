package com.example.server.ocr.service;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.client.OcrClient;
import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 이미지 검증, OCR 호출, 식재료 정보 분석 순서를 담당한다. */
@Service
@RequiredArgsConstructor
public class OcrService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png");

    private final OcrClient ocrClient;
    private final IngredientTextAnalyzer ingredientTextAnalyzer;

    public OcrIngredientResponse recognize(MultipartFile image) {
        validateImage(image);
        List<OcrTextBlock> textBlocks = ocrClient.extractText(image);
        return ingredientTextAnalyzer.analyze(textBlocks);
    }

    /** 파일 이름만 믿지 않고 MIME 타입과 실제 파일 시작 바이트를 함께 확인한다. */
    private void validateImage(MultipartFile image) {
        if (image == null
                || image.isEmpty()
                || image.getSize() > MAX_IMAGE_SIZE
                || !ALLOWED_CONTENT_TYPES.contains(image.getContentType())
                || !hasValidImageSignature(image)) {
            throw new CustomException(ErrorCode.INVALID_OCR_IMAGE);
        }
    }

    private boolean hasValidImageSignature(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            boolean isJpeg = bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF;
            boolean isPng = bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;
            return isJpeg || isPng;
        } catch (IOException exception) {
            return false;
        }
    }
}

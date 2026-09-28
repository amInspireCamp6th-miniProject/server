package com.example.server.ocr.service;

import com.example.server.ocr.client.OcrClient;
import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 이미지 검증, OCR 호출, 식재료 정보 분석 순서를 담당한다. */
@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrClient ocrClient;
    private final IngredientTextAnalyzer ingredientTextAnalyzer;
    private final OcrImageValidator imageValidator;

    public OcrIngredientResponse recognize(MultipartFile image) {
        imageValidator.validateAndRead(image);
        List<OcrTextBlock> textBlocks = ocrClient.extractText(image);
        return ingredientTextAnalyzer.analyze(textBlocks);
    }
}

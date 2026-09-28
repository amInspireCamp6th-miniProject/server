package com.example.server.ocr.service;

import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.util.List;

/** OCR 글자를 제품명, 일반 식재료명, 카테고리로 정리하는 분석 단계다. */
public interface IngredientTextAnalyzer {

    OcrIngredientResponse analyze(List<OcrTextBlock> textBlocks);
}

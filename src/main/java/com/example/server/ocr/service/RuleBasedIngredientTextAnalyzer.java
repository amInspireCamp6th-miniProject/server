package com.example.server.ocr.service;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * OCR 글자에서 자주 쓰는 식재료 키워드를 찾아 기본 등록값을 만든다.
 *
 * <p>초기 버전은 규칙 기반이며, 이후 LLM 분석기를 구현해 이 클래스를 교체할 수 있다.
 * 사용자가 등록 화면에서 결과를 최종 확인하고 수정하는 것을 전제로 한다.
 */
@Component
public class RuleBasedIngredientTextAnalyzer implements IngredientTextAnalyzer {

    private static final int MIN_CONFIDENCE_PERCENT = 50;

    private static final List<IngredientRule> RULES = List.of(
            new IngredientRule("닭가슴살", "닭가슴살", "육류"),
            new IngredientRule("돼지고기", "돼지고기", "육류"),
            new IngredientRule("소고기", "소고기", "육류"),
            new IngredientRule("요거트", "요거트", "유제품"),
            new IngredientRule("달걀", "계란", "난류"),
            new IngredientRule("계란", "계란", "난류"),
            new IngredientRule("두부", "두부", "가공식품"),
            new IngredientRule("우유", "우유", "유제품"),
            new IngredientRule("치즈", "치즈", "유제품"),
            new IngredientRule("김치", "김치", "가공식품"),
            new IngredientRule("양파", "양파", "채소"),
            new IngredientRule("감자", "감자", "채소"),
            new IngredientRule("당근", "당근", "채소"),
            new IngredientRule("대파", "대파", "채소"),
            new IngredientRule("버섯", "버섯", "채소"),
            new IngredientRule("사과", "사과", "과일"),
            new IngredientRule("바나나", "바나나", "과일"));

    @Override
    public OcrIngredientResponse analyze(List<OcrTextBlock> textBlocks) {
        List<String> recognizedTexts = textBlocks.stream()
                .filter(block -> block.text() != null && !block.text().isBlank())
                .filter(block -> block.confidence() * 100 >= MIN_CONFIDENCE_PERCENT)
                .map(block -> block.text().trim())
                .toList();

        if (recognizedTexts.isEmpty()) {
            throw new CustomException(ErrorCode.OCR_FAILED);
        }

        String recognizedText = String.join(" ", recognizedTexts);
        String normalizedText = recognizedText.replaceAll("\\s+", "").toLowerCase();

        IngredientRule matchedRule = RULES.stream()
                .filter(rule -> normalizedText.contains(rule.keyword()))
                .findFirst()
                .orElse(null);

        String productName = limitLength(recognizedText, 150);
        String ingredientName = matchedRule == null
                ? limitLength(findFallbackIngredientName(recognizedTexts), 100)
                : matchedRule.ingredientName();
        String category = matchedRule == null ? null : matchedRule.category();

        return new OcrIngredientResponse(productName, ingredientName, category);
    }

    /** 매칭되는 키워드가 없으면 한글이 포함된 첫 문구를 수정 가능한 기본값으로 사용한다. */
    private String findFallbackIngredientName(List<String> recognizedTexts) {
        return recognizedTexts.stream()
                .filter(text -> text.matches(".*[가-힣].*"))
                .findFirst()
                .orElse(recognizedTexts.get(0));
    }

    private String limitLength(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record IngredientRule(String keyword, String ingredientName, String category) {}
}

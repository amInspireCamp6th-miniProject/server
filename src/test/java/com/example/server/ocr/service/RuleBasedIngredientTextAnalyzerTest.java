package com.example.server.ocr.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.ocr.client.OcrTextBlock;
import com.example.server.ocr.dto.OcrIngredientResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** OCR 글자가 식재료 등록 기본값으로 정리되는지 확인한다. */
class RuleBasedIngredientTextAnalyzerTest {

    private final RuleBasedIngredientTextAnalyzer analyzer =
            new RuleBasedIngredientTextAnalyzer();

    @Test
    @DisplayName("두부가 포함된 OCR 결과를 가공식품으로 분류한다")
    void analyzeTofuText() {
        List<OcrTextBlock> blocks = List.of(
                new OcrTextBlock("풀무원 국산콩", 0.99, false),
                new OcrTextBlock("두부 300g", 0.98, true));

        OcrIngredientResponse response = analyzer.analyze(blocks);

        assertThat(response.productName()).isEqualTo("풀무원 국산콩 두부 300g");
        assertThat(response.ingredientName()).isEqualTo("두부");
        assertThat(response.category()).isEqualTo("가공식품");
    }

    @Test
    @DisplayName("등록되지 않은 식재료는 첫 한글 문구를 수정 가능한 기본값으로 사용한다")
    void useFallbackForUnknownText() {
        List<OcrTextBlock> blocks = List.of(
                new OcrTextBlock("새로운 식품", 0.95, true),
                new OcrTextBlock("500g", 0.99, true));

        OcrIngredientResponse response = analyzer.analyze(blocks);

        assertThat(response.ingredientName()).isEqualTo("새로운 식품");
        assertThat(response.category()).isNull();
    }
}

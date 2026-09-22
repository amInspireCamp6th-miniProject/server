package com.example.server.ocr.controller;

import com.example.server.ocr.dto.OcrIngredientResponse;
import com.example.server.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 프론트엔드가 업로드한 제품 사진을 식재료 기본 정보로 변환한다. */
@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(value = "/ingredients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrIngredientResponse> recognizeIngredient(
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ocrService.recognize(image));
    }
}

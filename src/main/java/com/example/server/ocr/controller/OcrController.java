package com.example.server.ocr.controller;

import com.example.server.global.security.LoginUser;
import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ocr.dto.OcrIngredientResponse;
import com.example.server.ocr.service.OcrIngredientRegistrationService;
import com.example.server.ocr.service.OcrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final OcrIngredientRegistrationService registrationService;

    @PostMapping(value = "/ingredients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrIngredientResponse> recognizeIngredient(
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ocrService.recognize(image));
    }

    @PostMapping(value = "/ingredients/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngredientResponse> registerIngredient(
            @LoginUser Long userId,
            @Valid @RequestPart("request") IngredientCreateRequest request,
            @RequestPart("image") MultipartFile image) {
        IngredientResponse response = registrationService.register(userId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

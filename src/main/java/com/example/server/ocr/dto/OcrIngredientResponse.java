package com.example.server.ocr.dto;

/** 식재료 이미지 인식 결과를 프론트엔드에 전달한다. */
public record OcrIngredientResponse(
        String productName, String ingredientName, String category) {}

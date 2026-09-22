package com.example.server.ocr.client;

/** CLOVA OCR이 이미지에서 인식한 글자 한 덩어리와 신뢰도다. */
public record OcrTextBlock(String text, double confidence, boolean lineBreak) {}

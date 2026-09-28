package com.example.server.ingredient.dto;

public record IngredientImageData(byte[] data, String contentType, String fileName) {
}

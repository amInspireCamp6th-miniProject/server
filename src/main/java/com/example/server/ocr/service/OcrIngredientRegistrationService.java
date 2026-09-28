package com.example.server.ocr.service;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrIngredientRegistrationService {

    private final IngredientRepository ingredientRepository;
    private final OcrImageValidator imageValidator;

    @Transactional
    public IngredientResponse register(
            Long userId,
            IngredientCreateRequest request,
            MultipartFile image) {
        byte[] imageData = imageValidator.validateAndRead(image);
        Ingredient ingredient = Ingredient.create(userId, request);
        ingredient.updateImage(imageData, image.getContentType(), image.getOriginalFilename());
        return IngredientResponse.from(ingredientRepository.save(ingredient));
    }
}

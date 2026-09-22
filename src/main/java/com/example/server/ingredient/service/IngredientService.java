package com.example.server.ingredient.service;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.repository.IngredientRepository;
import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 식재료 CRUD의 처리 순서와 사용자 소유권 검사를 담당한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional
    public IngredientResponse create(Long userId, IngredientCreateRequest request) {
        Ingredient ingredient = Ingredient.create(userId, request);
        return IngredientResponse.from(ingredientRepository.save(ingredient));
    }

    public List<IngredientResponse> findAll(Long userId, StorageType storageType) {
        List<Ingredient> ingredients = storageType == null
                ? ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                : ingredientRepository.findAllByUserIdAndStorageTypeOrderByCreatedAtDesc(
                        userId, storageType);

        return ingredients.stream().map(IngredientResponse::from).toList();
    }

    public IngredientResponse findById(Long userId, Long ingredientId) {
        return IngredientResponse.from(findOwnedIngredient(userId, ingredientId));
    }

    @Transactional
    public IngredientResponse update(
            Long userId, Long ingredientId, IngredientUpdateRequest request) {
        Ingredient ingredient = findOwnedIngredient(userId, ingredientId);
        ingredient.update(request);

        // 영속 상태 엔티티는 트랜잭션 종료 시 변경 감지되어 UPDATE SQL이 실행된다.
        return IngredientResponse.from(ingredient);
    }

    @Transactional
    public void delete(Long userId, Long ingredientId) {
        Ingredient ingredient = findOwnedIngredient(userId, ingredientId);
        ingredientRepository.delete(ingredient);
    }

    /** 조회 조건에 userId를 포함해 모든 상세·수정·삭제 경로에서 소유권을 검사한다. */
    private Ingredient findOwnedIngredient(Long userId, Long ingredientId) {
        return ingredientRepository
                .findByIdAndUserId(ingredientId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));
    }
}

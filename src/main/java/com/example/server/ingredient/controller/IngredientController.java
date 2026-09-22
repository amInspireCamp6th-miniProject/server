package com.example.server.ingredient.controller;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 프론트엔드의 식재료 HTTP 요청을 받아 Service에 전달한다. */
@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * userId 요청 속성은 추후 인증 담당자의 JWT 필터가 설정한다.
     * 클라이언트가 보낸 userId를 그대로 믿지 않아야 타인의 식재료 접근을 막을 수 있다.
     */
    @PostMapping
    public ResponseEntity<IngredientResponse> create(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody IngredientCreateRequest request) {
        IngredientResponse response = ingredientService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> findAll(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) StorageType storageType) {
        return ResponseEntity.ok(ingredientService.findAll(userId, storageType));
    }

    @GetMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponse> findById(
            @RequestAttribute("userId") Long userId, @PathVariable Long ingredientId) {
        return ResponseEntity.ok(ingredientService.findById(userId, ingredientId));
    }

    @PatchMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponse> update(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long ingredientId,
            @Valid @RequestBody IngredientUpdateRequest request) {
        return ResponseEntity.ok(ingredientService.update(userId, ingredientId, request));
    }

    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<Void> delete(
            @RequestAttribute("userId") Long userId, @PathVariable Long ingredientId) {
        ingredientService.delete(userId, ingredientId);
        return ResponseEntity.noContent().build();
    }
}

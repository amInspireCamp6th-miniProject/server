package com.example.server.ingredient.controller;

import com.example.server.global.security.LoginUser;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.service.IngredientExpirationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientExpirationController {

    private final IngredientExpirationService ingredientExpirationService;

    @GetMapping("/expiring")
    public ResponseEntity<List<IngredientExpirationResponse>> findExpiring(
            @LoginUser Long userId) {
        return ResponseEntity.ok(ingredientExpirationService.findExpiring(userId));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<IngredientExpirationResponse>> findExpired(
            @LoginUser Long userId) {
        return ResponseEntity.ok(ingredientExpirationService.findExpired(userId));
    }
}

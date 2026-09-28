package com.example.server.ingredient.service;

import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.repository.IngredientRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientExpirationService {

    private static final Comparator<IngredientExpirationResponse> EXPIRING_ORDER =
            Comparator.comparingLong(IngredientExpirationResponse::daysLeft)
                    .thenComparing(IngredientExpirationResponse::ingredientId);

    private static final Comparator<IngredientExpirationResponse> EXPIRED_ORDER =
            Comparator.comparingLong(IngredientExpirationResponse::daysLeft)
                    .reversed()
                    .thenComparing(IngredientExpirationResponse::ingredientId);

    private final IngredientRepository ingredientRepository;
    private final ExpirationPolicy expirationPolicy;

    public List<IngredientExpirationResponse> findExpiring(Long userId) {
        return findAllByUserId(userId).stream()
                .filter(ingredient -> expirationPolicy.isExpiring(ingredient.getExpirationDate()))
                .map(this::toResponse)
                .sorted(EXPIRING_ORDER)
                .toList();
    }

    public List<IngredientExpirationResponse> findExpired(Long userId) {
        return findAllByUserId(userId).stream()
                .filter(ingredient -> expirationPolicy.isExpired(ingredient.getExpirationDate()))
                .map(this::toResponse)
                .sorted(EXPIRED_ORDER)
                .toList();
    }

    private List<Ingredient> findAllByUserId(Long userId) {
        return ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    private IngredientExpirationResponse toResponse(Ingredient ingredient) {
        long daysLeft = expirationPolicy.calculateRemainingDays(ingredient.getExpirationDate());
        return IngredientExpirationResponse.from(ingredient, daysLeft);
    }
}

package com.example.server.ingredient.service;

import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.dto.IngredientExpirationSummary;
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
        return findExpiring(findAllByUserId(userId));
    }

    public List<IngredientExpirationResponse> findExpired(Long userId) {
        return findExpired(findAllByUserId(userId));
    }

    public IngredientExpirationSummary summarize(Long userId) {
        List<Ingredient> ingredients = findAllByUserId(userId);
        List<IngredientExpirationResponse> expiringIngredients = findExpiring(ingredients);
        return new IngredientExpirationSummary(
                ingredients.size(),
                expiringIngredients,
                countExpired(ingredients));
    }

    private List<IngredientExpirationResponse> findExpiring(List<Ingredient> ingredients) {
        return ingredients.stream()
                .filter(this::isExpiring)
                .map(this::toResponse)
                .sorted(EXPIRING_ORDER)
                .toList();
    }

    private List<IngredientExpirationResponse> findExpired(List<Ingredient> ingredients) {
        return ingredients.stream()
                .filter(this::isExpired)
                .map(this::toResponse)
                .sorted(EXPIRED_ORDER)
                .toList();
    }

    private long countExpired(List<Ingredient> ingredients) {
        return ingredients.stream().filter(this::isExpired).count();
    }

    private boolean isExpiring(Ingredient ingredient) {
        return expirationPolicy.isExpiring(ingredient.getExpirationDate());
    }

    private boolean isExpired(Ingredient ingredient) {
        return expirationPolicy.isExpired(ingredient.getExpirationDate());
    }

    private List<Ingredient> findAllByUserId(Long userId) {
        return ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    private IngredientExpirationResponse toResponse(Ingredient ingredient) {
        long daysLeft = expirationPolicy.calculateRemainingDays(ingredient.getExpirationDate());
        return IngredientExpirationResponse.from(ingredient, daysLeft);
    }
}

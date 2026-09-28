package com.example.server.ingredient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.dto.IngredientExpirationResponse;
import com.example.server.ingredient.dto.IngredientExpirationSummary;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.repository.IngredientRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngredientExpirationServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);

    @Mock
    private IngredientRepository ingredientRepository;

    private IngredientExpirationService ingredientExpirationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-21T00:00:00Z"), ZoneOffset.UTC);
        ingredientExpirationService = new IngredientExpirationService(
                ingredientRepository, new ExpirationPolicy(clock));
    }

    @Test
    void findsExpiringIngredientsInNearestExpirationOrder() {
        List<Ingredient> ingredients = List.of(
                ingredient(20L, TODAY.minusDays(1), false),
                ingredient(6L, TODAY.plusDays(6), false),
                ingredient(5L, TODAY.plusDays(5), false),
                ingredient(3L, TODAY.plusDays(1), false),
                ingredient(2L, TODAY, true),
                ingredient(1L, TODAY, false));
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID))
                .willReturn(ingredients);

        List<IngredientExpirationResponse> responses =
                ingredientExpirationService.findExpiring(USER_ID);

        assertThat(responses)
                .extracting(IngredientExpirationResponse::ingredientId)
                .containsExactly(1L, 2L, 3L, 5L);
        assertThat(responses)
                .extracting(IngredientExpirationResponse::daysLeft)
                .containsExactly(0L, 0L, 1L, 5L);
        assertThat(responses.get(0).imageUrl()).isNull();
        assertThat(responses.get(1).imageUrl())
                .isEqualTo("/api/v1/ingredients/2/image");
        verify(ingredientRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        verifyNoMoreInteractions(ingredientRepository);
    }

    @Test
    void findsExpiredIngredientsInMostRecentlyExpiredOrder() {
        List<Ingredient> ingredients = List.of(
                ingredient(5L, TODAY.minusDays(5), false),
                ingredient(3L, TODAY.minusDays(1), false),
                ingredient(2L, TODAY.minusDays(1), false),
                ingredient(10L, TODAY, false),
                ingredient(11L, TODAY.plusDays(1), false));
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID))
                .willReturn(ingredients);

        List<IngredientExpirationResponse> responses =
                ingredientExpirationService.findExpired(USER_ID);

        assertThat(responses)
                .extracting(IngredientExpirationResponse::ingredientId)
                .containsExactly(2L, 3L, 5L);
        assertThat(responses)
                .extracting(IngredientExpirationResponse::daysLeft)
                .containsExactly(-1L, -1L, -5L);
        verify(ingredientRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        verifyNoMoreInteractions(ingredientRepository);
    }

    @Test
    void queriesOnlyTheLoggedInUsersIngredients() {
        Ingredient ownedIngredient = ingredient(1L, TODAY, false);
        Ingredient anotherUsersIngredient = ingredient(99L, TODAY, false);
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID))
                .willReturn(List.of(ownedIngredient));
        lenient().when(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(anotherUsersIngredient));

        List<IngredientExpirationResponse> responses =
                ingredientExpirationService.findExpiring(USER_ID);

        assertThat(responses)
                .extracting(IngredientExpirationResponse::ingredientId)
                .containsExactly(1L);
        verify(ingredientRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        verify(ingredientRepository, never()).findAllByUserIdOrderByCreatedAtDesc(2L);
    }

    @Test
    void summarizesAllIngredientsWithoutLimitingExpiringIngredients() {
        List<Ingredient> ingredients = List.of(
                ingredient(5L, TODAY.plusDays(5), false),
                ingredient(4L, TODAY.plusDays(3), false),
                ingredient(3L, TODAY.plusDays(2), false),
                ingredient(2L, TODAY, true),
                ingredient(1L, TODAY, false),
                ingredient(6L, TODAY.minusDays(1), false),
                ingredient(7L, TODAY.plusDays(6), false));
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID))
                .willReturn(ingredients);

        IngredientExpirationSummary summary = ingredientExpirationService.summarize(USER_ID);

        assertThat(summary.totalCount()).isEqualTo(7L);
        assertThat(summary.expiringIngredients()).hasSize(5);
        assertThat(summary.expiringIngredients())
                .extracting(IngredientExpirationResponse::ingredientId)
                .containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(summary.expiringIngredients())
                .extracting(IngredientExpirationResponse::daysLeft)
                .containsExactly(0L, 0L, 2L, 3L, 5L);
        assertThat(summary.expiringIngredients().get(0).imageUrl()).isNull();
        assertThat(summary.expiringIngredients().get(1).imageUrl())
                .isEqualTo("/api/v1/ingredients/2/image");
        assertThat(summary.expiredCount()).isEqualTo(1L);
        verify(ingredientRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        verifyNoMoreInteractions(ingredientRepository);
    }

    @Test
    void returnsEmptyExpiringIngredientsWhenThereAreNone() {
        List<Ingredient> ingredients = List.of(
                ingredient(1L, TODAY.minusDays(1), false),
                ingredient(2L, TODAY.plusDays(6), false));
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID))
                .willReturn(ingredients);

        IngredientExpirationSummary summary = ingredientExpirationService.summarize(USER_ID);

        assertThat(summary.totalCount()).isEqualTo(2L);
        assertThat(summary.expiringIngredients()).isEmpty();
        assertThat(summary.expiredCount()).isEqualTo(1L);
    }

    private Ingredient ingredient(Long id, LocalDate expirationDate, boolean hasImage) {
        Ingredient ingredient = mock(Ingredient.class);
        lenient().when(ingredient.getId()).thenReturn(id);
        lenient().when(ingredient.getProductName()).thenReturn("테스트 상품 " + id);
        lenient().when(ingredient.getIngredientName()).thenReturn("테스트 식재료 " + id);
        lenient().when(ingredient.getCategory()).thenReturn("테스트 카테고리");
        lenient().when(ingredient.getQuantity()).thenReturn(BigDecimal.ONE);
        lenient().when(ingredient.getUnit()).thenReturn("개");
        lenient().when(ingredient.getPurchaseDate()).thenReturn(TODAY.minusDays(1));
        lenient().when(ingredient.getExpirationDate()).thenReturn(expirationDate);
        lenient().when(ingredient.getStorageType()).thenReturn(StorageType.REFRIGERATED);
        if (hasImage) {
            lenient().when(ingredient.getImageData()).thenReturn(new byte[] {0x01});
        }
        return ingredient;
    }
}

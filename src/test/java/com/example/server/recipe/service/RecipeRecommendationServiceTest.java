package com.example.server.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.server.global.exception.BusinessException;
import com.example.server.ingredient.domain.ExpirationPolicy;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.exception.IngredientErrorCode;
import com.example.server.ingredient.repository.IngredientRepository;
import com.example.server.recipe.dto.RecipeRecommendationRequest;
import com.example.server.recipe.dto.RecipeRecommendationResponse;
import com.example.server.recipe.entity.Recipe;
import com.example.server.recipe.entity.RecipeIngredient;
import com.example.server.recipe.entity.RecipeStep;
import com.example.server.recipe.repository.RecipeIngredientRepository;
import com.example.server.recipe.repository.RecipeStepRepository;
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
class RecipeRecommendationServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private RecipeStepRepository recipeStepRepository;

    private RecipeRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-21T00:00:00Z"), ZoneOffset.UTC);
        recommendationService = new RecipeRecommendationService(
                ingredientRepository,
                recipeIngredientRepository,
                recipeStepRepository,
                new ExpirationPolicy(clock));
        lenient().when(recipeStepRepository
                        .findAllByRecipe_RecipeIdOrderByStepNoAsc(anyLong()))
                .thenReturn(List.of());
    }

    @Test
    void rejectsRequestContainingMissingIngredient() {
        Ingredient tofu = ingredient(1L, "두부", 0);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.containsAll(List.of(1L, 999L)))))
                .willReturn(List.of(tofu));

        assertThatThrownBy(() -> recommend(List.of(1L, 999L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(IngredientErrorCode.INGREDIENT_NOT_FOUND);
        verifyNoInteractions(recipeIngredientRepository, recipeStepRepository);
    }

    @Test
    void rejectsRequestContainingAnotherUsersIngredient() {
        Ingredient ownedIngredient = ingredient(1L, "두부", 0);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.containsAll(List.of(1L, 2L)))))
                .willReturn(List.of(ownedIngredient));

        assertThatThrownBy(() -> recommend(List.of(1L, 2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(IngredientErrorCode.INGREDIENT_NOT_FOUND);
        verifyNoInteractions(recipeIngredientRepository, recipeStepRepository);
    }

    @Test
    void handlesDuplicateIngredientIdsOnlyOnce() {
        Ingredient tofu = ingredient(1L, "두부", 0);
        Ingredient greenOnion = ingredient(2L, "대파", 1);
        Recipe tofuAndOnion = recipe(1L, "두부 대파 볶음", 20);
        Recipe tofuOnly = recipe(2L, "두부 구이", 10);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.size() == 2 && ids.containsAll(List.of(1L, 2L)))))
                .willReturn(List.of(tofu, greenOnion));
        givenRecipeIngredients(
                recipeIngredient(tofuAndOnion, "두부"),
                recipeIngredient(tofuAndOnion, "대파"),
                recipeIngredient(tofuOnly, "두부"));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L, 1L, 2L));

        assertThat(responses)
                .extracting(RecipeRecommendationResponse::recipeId)
                .containsExactly(1L, 2L);
        verify(ingredientRepository).findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                ids.size() == 2 && ids.containsAll(List.of(1L, 2L))));
    }

    @Test
    void appliesExpirationWeightsAndReturnsOnlyTopThreeOfFiveCandidates() {
        List<Ingredient> ingredients = List.of(
                ingredient(1L, "D-Day 재료", 0),
                ingredient(2L, "D-1 재료", 1),
                ingredient(3L, "D-5 재료", 5),
                ingredient(4L, "D-6 재료 A", 6),
                ingredient(5L, "D-6 재료 B", 6));
        List<Recipe> recipes = List.of(
                recipe(10L, "D-Day 레시피", 10),
                recipe(20L, "D-1 레시피", 10),
                recipe(30L, "D-5 레시피", 10),
                recipe(40L, "D-6 레시피 A", 10),
                recipe(50L, "D-6 레시피 B", 10));
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.size() == 5)))
                .willReturn(ingredients);
        givenRecipeIngredients(
                recipeIngredient(recipes.get(0), "D-Day 재료"),
                recipeIngredient(recipes.get(1), "D-1 재료"),
                recipeIngredient(recipes.get(2), "D-5 재료"),
                recipeIngredient(recipes.get(3), "D-6 재료 A"),
                recipeIngredient(recipes.get(4), "D-6 재료 B"));

        List<RecipeRecommendationResponse> responses =
                recommend(List.of(1L, 2L, 3L, 4L, 5L));

        assertThat(responses)
                .extracting(RecipeRecommendationResponse::recipeId)
                .containsExactly(10L, 20L, 30L);
    }

    @Test
    void appliesAllRecommendationTieBreakers() {
        List<Ingredient> ingredients = List.of(
                ingredient(1L, "두부", 0),
                ingredient(2L, "대파", 1),
                ingredient(3L, "소금", 5));
        Recipe moreUsed = recipe(20L, "대파 소금 볶음", 10);
        Recipe noAdditional = recipe(30L, "두부 구이", 10);
        Recipe lowerId = recipe(10L, "두부 소스 A", 10);
        Recipe higherId = recipe(40L, "두부 소스 B", 10);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.size() == 3)))
                .willReturn(ingredients);
        givenRecipeIngredients(
                recipeIngredient(lowerId, "두부"),
                recipeIngredient(lowerId, "간장"),
                recipeIngredient(moreUsed, "대파"),
                recipeIngredient(moreUsed, "소금"),
                recipeIngredient(moreUsed, "간장"),
                recipeIngredient(moreUsed, "참기름"),
                recipeIngredient(noAdditional, "두부"),
                recipeIngredient(higherId, "두부"),
                recipeIngredient(higherId, "고추장"));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L, 2L, 3L));

        assertThat(responses)
                .extracting(RecipeRecommendationResponse::recipeId)
                .containsExactly(20L, 30L, 10L);
    }

    @Test
    void excludesExpiredIngredientsAndReturnsEmptyWhenAllAreExpired() {
        Ingredient expiredTofu = ingredient(1L, "두부", -1);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.contains(1L))))
                .willReturn(List.of(expiredTofu));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L));

        assertThat(responses).isEmpty();
        verifyNoInteractions(recipeIngredientRepository, recipeStepRepository);
    }

    @Test
    void excludesExpiredIngredientFromUsedIngredientsWhenValidIngredientMatches() {
        Ingredient expiredTofu = ingredient(1L, "두부", -1);
        Ingredient greenOnion = ingredient(2L, "대파", 1);
        Recipe recipe = recipe(1L, "두부 대파 볶음", 20);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.size() == 2)))
                .willReturn(List.of(expiredTofu, greenOnion));
        givenRecipeIngredients(
                recipeIngredient(recipe, "두부"),
                recipeIngredient(recipe, "대파"));

        RecipeRecommendationResponse response = recommend(List.of(1L, 2L)).get(0);

        assertThat(response.usedIngredients()).containsExactly("대파");
        assertThat(response.additionalIngredients()).containsExactly("두부");
    }

    @Test
    void keepsNonExpiredIngredientBeyondWeightWindowAsZeroScoreCandidate() {
        Ingredient rice = ingredient(1L, "밥", 6);
        Recipe riceRecipe = recipe(1L, "밥 요리", 10);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.contains(1L))))
                .willReturn(List.of(rice));
        givenRecipeIngredients(recipeIngredient(riceRecipe, "밥"));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L));

        assertThat(responses)
                .extracting(RecipeRecommendationResponse::recipeId)
                .containsExactly(1L);
    }

    @Test
    void excludesRecipesWithoutMatchingSelectedIngredients() {
        Ingredient tofu = ingredient(1L, "두부", 1);
        Recipe unrelatedRecipe = recipe(1L, "김치찌개", 30);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.contains(1L))))
                .willReturn(List.of(tofu));
        givenRecipeIngredients(recipeIngredient(unrelatedRecipe, "김치"));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L));

        assertThat(responses).isEmpty();
        verifyNoInteractions(recipeStepRepository);
    }

    @Test
    void returnsAllCandidatesWhenFewerThanRecommendationLimit() {
        Ingredient tofu = ingredient(1L, "두부", 0);
        Ingredient greenOnion = ingredient(2L, "대파", 1);
        Recipe tofuRecipe = recipe(1L, "두부 구이", 10);
        Recipe onionRecipe = recipe(2L, "대파 볶음", 10);
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.size() == 2)))
                .willReturn(List.of(tofu, greenOnion));
        givenRecipeIngredients(
                recipeIngredient(tofuRecipe, "두부"),
                recipeIngredient(onionRecipe, "대파"));

        List<RecipeRecommendationResponse> responses = recommend(List.of(1L, 2L));

        assertThat(responses).hasSize(2);
    }

    @Test
    void mapsIngredientsRecipeFieldsAndOrderedInstructions() {
        Ingredient tofu = ingredient(1L, "두부", 1);
        Recipe recipe = recipe(7L, "두부 조림", 25);
        RecipeStep first = step(1, "두부를 자릅니다.");
        RecipeStep second = step(2, "양념과 함께 조립니다.");
        given(ingredientRepository.findAllByUserIdAndIdIn(eq(USER_ID), argThat(ids ->
                        ids.contains(1L))))
                .willReturn(List.of(tofu));
        givenRecipeIngredients(
                recipeIngredient(recipe, "두부"),
                recipeIngredient(recipe, "간장"),
                recipeIngredient(recipe, "간장"),
                recipeIngredient(recipe, "참기름"));
        given(recipeStepRepository.findAllByRecipe_RecipeIdOrderByStepNoAsc(7L))
                .willReturn(List.of(first, second));

        RecipeRecommendationResponse response = recommend(List.of(1L)).get(0);

        assertThat(response.recipeId()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("두부 조림");
        assertThat(response.cookingTime()).isEqualTo(25);
        assertThat(response.usedIngredients()).containsExactly("두부");
        assertThat(response.additionalIngredients()).containsExactly("간장", "참기름");
        assertThat(response.instructions())
                .isEqualTo("1. 두부를 자릅니다.\n2. 양념과 함께 조립니다.");
    }

    private List<RecipeRecommendationResponse> recommend(List<Long> ingredientIds) {
        return recommendationService.recommend(
                USER_ID, new RecipeRecommendationRequest(ingredientIds));
    }

    private Ingredient ingredient(Long id, String ingredientName, long daysLeft) {
        Ingredient ingredient = mock(Ingredient.class);
        lenient().when(ingredient.getId()).thenReturn(id);
        lenient().when(ingredient.getIngredientName()).thenReturn(ingredientName);
        lenient().when(ingredient.getExpirationDate()).thenReturn(TODAY.plusDays(daysLeft));
        return ingredient;
    }

    private Recipe recipe(Long recipeId, String name, Integer cookingTime) {
        Recipe recipe = mock(Recipe.class);
        lenient().when(recipe.getRecipeId()).thenReturn(recipeId);
        lenient().when(recipe.getName()).thenReturn(name);
        lenient().when(recipe.getCookingTime()).thenReturn(cookingTime);
        return recipe;
    }

    private RecipeIngredient recipeIngredient(Recipe recipe, String ingredientName) {
        RecipeIngredient recipeIngredient = mock(RecipeIngredient.class);
        lenient().when(recipeIngredient.getRecipe()).thenReturn(recipe);
        lenient().when(recipeIngredient.getIngredientName()).thenReturn(ingredientName);
        return recipeIngredient;
    }

    private void givenRecipeIngredients(RecipeIngredient... recipeIngredients) {
        given(recipeIngredientRepository.findAllWithRecipe())
                .willReturn(List.of(recipeIngredients));
    }

    private RecipeStep step(Integer stepNo, String description) {
        RecipeStep step = mock(RecipeStep.class);
        lenient().when(step.getStepNo()).thenReturn(stepNo);
        lenient().when(step.getDescription()).thenReturn(description);
        return step;
    }
}

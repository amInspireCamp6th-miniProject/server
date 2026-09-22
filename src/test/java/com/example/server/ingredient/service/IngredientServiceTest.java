package com.example.server.ingredient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.repository.IngredientRepository;
import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** DB 없이 Service의 분기와 소유권 검사만 빠르게 검증하는 단위 테스트다. */
@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(ingredientRepository);
    }

    @Test
    @DisplayName("식재료를 등록한다")
    void createIngredient() {
        given(ingredientRepository.save(any(Ingredient.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        IngredientResponse response = ingredientService.create(1L, createRequest());

        assertThat(response.productName()).isEqualTo("풀무원 국산콩 두부 300g");
        assertThat(response.storageType()).isEqualTo(StorageType.REFRIGERATED);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    @DisplayName("보관 상태가 없으면 사용자의 전체 식재료를 조회한다")
    void findAllIngredients() {
        Ingredient ingredient = Ingredient.create(1L, createRequest());
        given(ingredientRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(ingredient));

        List<IngredientResponse> responses = ingredientService.findAll(1L, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).ingredientName()).isEqualTo("두부");
    }

    @Test
    @DisplayName("보관 상태가 있으면 해당 상태의 식재료만 조회한다")
    void findIngredientsByStorageType() {
        given(ingredientRepository.findAllByUserIdAndStorageTypeOrderByCreatedAtDesc(
                        1L, StorageType.FROZEN))
                .willReturn(List.of());

        List<IngredientResponse> responses =
                ingredientService.findAll(1L, StorageType.FROZEN);

        assertThat(responses).isEmpty();
        verify(ingredientRepository)
                .findAllByUserIdAndStorageTypeOrderByCreatedAtDesc(1L, StorageType.FROZEN);
    }

    @Test
    @DisplayName("요청에 포함된 식재료 정보만 수정한다")
    void updateIngredientPartially() {
        Ingredient ingredient = Ingredient.create(1L, createRequest());
        IngredientUpdateRequest request = new IngredientUpdateRequest(
                null,
                null,
                null,
                new BigDecimal("2.00"),
                null,
                null,
                null,
                null);
        given(ingredientRepository.findByIdAndUserId(10L, 1L))
                .willReturn(Optional.of(ingredient));

        IngredientResponse response = ingredientService.update(1L, 10L, request);

        assertThat(response.quantity()).isEqualByComparingTo("2.00");
        assertThat(response.productName()).isEqualTo("풀무원 국산콩 두부 300g");
    }

    @Test
    @DisplayName("다른 사용자의 식재료는 상세 조회할 수 없다")
    void cannotFindAnotherUsersIngredient() {
        given(ingredientRepository.findByIdAndUserId(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.findById(2L, 10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INGREDIENT_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 식재료를 삭제한다")
    void deleteIngredient() {
        Ingredient ingredient = Ingredient.create(1L, createRequest());
        given(ingredientRepository.findByIdAndUserId(10L, 1L))
                .willReturn(Optional.of(ingredient));

        ingredientService.delete(1L, 10L);

        verify(ingredientRepository).delete(ingredient);
    }

    private IngredientCreateRequest createRequest() {
        return new IngredientCreateRequest(
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 25),
                StorageType.REFRIGERATED);
    }
}

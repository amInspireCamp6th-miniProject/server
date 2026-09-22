package com.example.server.recipe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.entity.Recipe;
import com.example.server.recipe.entity.RecipeStep;
import com.example.server.recipe.repository.RecipeRepository;
import com.example.server.recipe.repository.RecipeStepRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeStepRepository recipeStepRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void returnsInstructionsInRepositoryOrderWithMappedFields() {
        Long recipeId = 1L;
        RecipeStep first = step(1, "김치를 잘게 썬다");
        RecipeStep second = step(2, "팬에 김치와 밥을 볶는다");
        RecipeStep third = step(3, "완성된 볶음밥을 그릇에 담는다");
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(mock(Recipe.class)));
        when(recipeStepRepository.findAllByRecipe_RecipeIdOrderByStepNoAsc(recipeId))
                .thenReturn(List.of(first, second, third));

        RecipeInstructionsResponse response = recipeService.getInstructions(recipeId);

        assertEquals(recipeId, response.recipeId());
        assertEquals(3, response.instructions().size());
        assertEquals(List.of(1, 2, 3), response.instructions().stream()
                .map(RecipeInstructionStepResponse::stepNo)
                .toList());
        assertEquals(List.of("김치를 잘게 썬다", "팬에 김치와 밥을 볶는다", "완성된 볶음밥을 그릇에 담는다"),
                response.instructions().stream()
                        .map(RecipeInstructionStepResponse::description)
                        .toList());

        InOrder order = inOrder(recipeRepository, recipeStepRepository);
        order.verify(recipeRepository).findById(recipeId);
        order.verify(recipeStepRepository).findAllByRecipe_RecipeIdOrderByStepNoAsc(recipeId);
        order.verifyNoMoreInteractions();
    }

    @Test
    void throwsRecipeNotFoundWithoutLoadingSteps() {
        Long recipeId = 999L;
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> recipeService.getInstructions(recipeId));

        assertSame(ErrorCode.RECIPE_NOT_FOUND, exception.getErrorCode());
        verify(recipeRepository).findById(recipeId);
        verifyNoInteractions(recipeStepRepository);
    }

    private RecipeStep step(Integer stepNo, String description) {
        RecipeStep step = mock(RecipeStep.class);
        when(step.getStepNo()).thenReturn(stepNo);
        when(step.getDescription()).thenReturn(description);
        return step;
    }
}

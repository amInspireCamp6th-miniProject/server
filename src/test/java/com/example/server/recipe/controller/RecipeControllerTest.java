package com.example.server.recipe.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.server.global.exception.CustomException;
import com.example.server.global.exception.ErrorCode;
import com.example.server.recipe.dto.RecipeInstructionStepResponse;
import com.example.server.recipe.dto.RecipeInstructionsResponse;
import com.example.server.recipe.service.RecipeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void returnsInstructionsAsJson() throws Exception {
        RecipeInstructionsResponse response = new RecipeInstructionsResponse(1L, List.of(
                new RecipeInstructionStepResponse(1, "김치를 잘게 썬다"),
                new RecipeInstructionStepResponse(2, "팬에 김치와 밥을 볶는다"),
                new RecipeInstructionStepResponse(3, "완성된 볶음밥을 그릇에 담는다")));
        when(recipeService.getInstructions(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/recipes/1/instructions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.instructions", hasSize(3)))
                .andExpect(jsonPath("$.instructions[0].stepNo").value(1))
                .andExpect(jsonPath("$.instructions[0].description").value("김치를 잘게 썬다"))
                .andExpect(jsonPath("$.instructions[1].stepNo").value(2))
                .andExpect(jsonPath("$.instructions[2].stepNo").value(3));

        verify(recipeService).getInstructions(1L);
    }

    @Test
    void returnsNotFoundThroughGlobalExceptionHandler() throws Exception {
        when(recipeService.getInstructions(999L))
                .thenThrow(new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/recipes/999/instructions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("레시피를 찾을 수 없습니다."));

        verify(recipeService).getInstructions(999L);
    }
}

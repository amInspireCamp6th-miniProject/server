package com.example.server.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.example.server.global.response.ErrorResponse;
import com.example.server.recipe.exception.RecipeErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void mapsRecipeNotFoundToNotFoundResponse() {
        ErrorCode errorCode = RecipeErrorCode.RECIPE_NOT_FOUND;
        BusinessException exception = new BusinessException(errorCode);

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler().handleBusinessException(exception);

        assertSame(errorCode, exception.getErrorCode());
        assertEquals("레시피를 찾을 수 없습니다.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, errorCode.getStatus());
        assertEquals("RECIPE_NOT_FOUND", errorCode.getCode());
        assertEquals("레시피를 찾을 수 없습니다.", errorCode.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RECIPE_NOT_FOUND", response.getBody().code());
        assertEquals("레시피를 찾을 수 없습니다.", response.getBody().message());
    }
}

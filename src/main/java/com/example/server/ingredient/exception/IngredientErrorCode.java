package com.example.server.ingredient.exception;

import com.example.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IngredientErrorCode implements ErrorCode {

    // 다른 사용자의 식재료에 접근한 경우도 같은 코드로 응답한다. (존재 여부 노출 방지, B-11)
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "식재료를 찾을 수 없습니다."),
    INGREDIENT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "식재료 이미지를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}

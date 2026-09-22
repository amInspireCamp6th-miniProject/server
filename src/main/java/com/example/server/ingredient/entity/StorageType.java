package com.example.server.ingredient.entity;

/**
 * 식재료를 보관하는 장소를 나타낸다.
 *
 * <p>DB와 API에는 enum 이름 자체가 문자열로 저장되므로 프론트엔드도 같은 값을 사용해야 한다.
 */
public enum StorageType {
    REFRIGERATED,
    FROZEN,
    ROOM_TEMP
}

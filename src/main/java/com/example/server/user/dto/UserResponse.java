package com.example.server.user.dto;

import com.example.server.user.entity.User;

/**
 * 회원 정보 응답. 회원가입, 로그인, 로그인 사용자 조회에서 함께 쓴다.
 */
public record UserResponse(Long userId, String email, String nickname) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}

package com.example.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 길이 제한은 USERS 테이블 컬럼 길이(email 100, nickname 50)와 같다.
 * 비밀번호는 BCrypt 입력 한도(72바이트)를 넘지 않도록 영문·숫자·특수문자(ASCII)만 허용한다.
 */
public record SignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하로 입력해 주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해 주세요.")
        @Pattern(regexp = "^[!-~]*$", message = "비밀번호는 공백 없이 영문, 숫자, 특수문자만 사용할 수 있습니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하로 입력해 주세요.")
        String nickname
) {
}

package com.example.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.server.global.exception.BusinessException;
import com.example.server.user.entity.User;
import com.example.server.user.exception.UserErrorCode;
import com.example.server.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 이메일은_소문자와_앞뒤_공백_제거로_정규화해_저장한다() {
        given(userRepository.existsByEmail("user@example.com")).willReturn(false);
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User user = userService.create("  User@Example.COM ", "encoded", " 닉네임 ");

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getNickname()).isEqualTo("닉네임");
    }

    @Test
    void 이미_있는_이메일이면_DUPLICATE_EMAIL() {
        given(userRepository.existsByEmail("user@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.create("user@example.com", "encoded", "닉네임"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 동시_가입으로_UNIQUE_제약에_걸려도_DUPLICATE_EMAIL() {
        given(userRepository.existsByEmail("user@example.com")).willReturn(false);
        given(userRepository.saveAndFlush(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.create("user@example.com", "encoded", "닉네임"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void 조회할_때도_같은_규칙으로_정규화한다() {
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.empty());

        userService.findByEmail(" USER@example.com");

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void 없는_사용자를_조회하면_USER_NOT_FOUND() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}

package com.example.server.user.service;

import com.example.server.global.exception.BusinessException;
import com.example.server.user.dto.UserResponse;
import com.example.server.user.entity.User;
import com.example.server.user.exception.UserErrorCode;
import com.example.server.user.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * @param encodedPassword 암호화가 끝난 비밀번호
     * @throws BusinessException 이메일이 이미 있으면 DUPLICATE_EMAIL
     */
    @Transactional
    public User create(String email, String encodedPassword, String nickname) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
        try {
            return userRepository.saveAndFlush(User.create(normalizedEmail, encodedPassword, nickname.trim()));
        } catch (DataIntegrityViolationException e) {
            // 중복 확인과 저장 사이에 같은 이메일로 동시에 가입한 경우 (email UNIQUE 제약)
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    // 대소문자·공백이 다른 같은 이메일로 계정이 두 개 생기지 않도록 저장과 조회에 같은 규칙을 쓴다.
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

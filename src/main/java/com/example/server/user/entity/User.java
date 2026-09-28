package com.example.server.user.entity;

import com.example.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * USERS 테이블. 이름은 팀 공유 DDL 스크립트와 같은 대문자로 쓴다.
 * (Windows·macOS MariaDB는 대소문자를 구분하지 않지만, Linux는 구분하므로 스크립트와 정확히 맞춘다)
 * Spring Security의 User 클래스와 이름이 같으므로 한 파일에서 둘 다 쓸 때는 패키지명으로 구분한다.
 */
@Getter
@Entity
@Table(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt로 암호화된 값만 저장한다.
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    private User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname);
    }
}

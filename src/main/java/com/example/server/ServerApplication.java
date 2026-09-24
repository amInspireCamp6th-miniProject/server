package com.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration 제외: JWT 인증만 쓰므로 기본 계정이 필요 없고,
// 제외하지 않으면 자동 생성된 비밀번호가 시작 로그에 출력된다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}

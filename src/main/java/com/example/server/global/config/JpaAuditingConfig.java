package com.example.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * modifyOnCreate = false: 등록 시 updated_at을 채우지 않는다. (테이블 명세의 기본값 NULL과 일치)
 */
@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
public class JpaAuditingConfig {
}

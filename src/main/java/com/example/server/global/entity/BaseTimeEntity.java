package com.example.server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * created_at, updated_at이 있는 테이블용 (예: USERS, INGREDIENTS).
 * 테이블 명세상 updated_at은 등록 시 NULL이고, 수정될 때만 채워진다. (JpaAuditingConfig 참고)
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedTimeEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

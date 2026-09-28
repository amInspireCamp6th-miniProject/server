package com.example.server.ingredient.entity;

import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * INGREDIENTS 테이블과 연결되는 JPA 엔티티다.
 *
 * <p>엔티티는 DB 저장용 객체이므로 컨트롤러에서 직접 반환하지 않고 IngredientResponse로 변환한다.
 */
@Getter
@Entity
@Table(name = "ingredients")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long id;

    /**
     * 인증 담당자의 User 엔티티가 아직 없어서 FK 값만 보관한다.
     * User 엔티티가 합쳐지면 팀 합의에 따라 연관관계로 변경할 수 있다.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Ingredient(Long userId, IngredientCreateRequest request) {
        this.userId = userId;
        this.productName = request.productName();
        this.ingredientName = request.ingredientName();
        this.category = request.category();
        this.quantity = request.quantity();
        this.unit = request.unit();
        this.purchaseDate = request.purchaseDate();
        this.expirationDate = request.expirationDate();
        this.storageType = request.storageType();
    }

    /** 새 식재료를 만들 때 필수 값이 한 번에 들어오도록 생성 과정을 한곳에 모은다. */
    public static Ingredient create(Long userId, IngredientCreateRequest request) {
        return new Ingredient(userId, request);
    }

    /**
     * PATCH 요청에 포함된 값만 변경한다.
     * null은 "수정하지 않음"을 뜻하므로 기존 값을 유지한다.
     */
    public void update(IngredientUpdateRequest request) {
        if (request.productName() != null) {
            productName = request.productName();
        }
        if (request.ingredientName() != null) {
            ingredientName = request.ingredientName();
        }
        if (request.category() != null) {
            category = request.category();
        }
        if (request.quantity() != null) {
            quantity = request.quantity();
        }
        if (request.unit() != null) {
            unit = request.unit();
        }
        if (request.purchaseDate() != null) {
            purchaseDate = request.purchaseDate();
        }
        if (request.expirationDate() != null) {
            expirationDate = request.expirationDate();
        }
        if (request.storageType() != null) {
            storageType = request.storageType();
        }
    }
}

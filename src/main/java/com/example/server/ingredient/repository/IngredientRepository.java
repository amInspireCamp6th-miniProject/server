package com.example.server.ingredient.repository;

import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 식재료 DB 작업을 담당한다.
 *
 * <p>메서드 이름을 해석해 Spring Data JPA가 필요한 SQL을 자동으로 만든다.
 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Ingredient> findAllByUserIdAndStorageTypeOrderByCreatedAtDesc(
            Long userId, StorageType storageType);

    /** id와 userId를 함께 조건으로 사용해 다른 사용자의 식재료가 조회되지 않게 한다. */
    Optional<Ingredient> findByIdAndUserId(Long id, Long userId);
}

package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    Page<ProductEntity> findAll(Pageable pageable);

    List<ProductEntity> findByShopId(String shopId);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(p.category) LIKE LOWER(concat('%', :keyword, '%')) ORDER BY p.name ASC")
    List<ProductEntity> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.category) LIKE LOWER(concat('%', :category, '%')) ORDER BY p.name ASC")
    List<ProductEntity> findSimilarProductsByCategory(@Param("category") String category);

    @Query("SELECT p, " +
            "CASE " +
            "    WHEN :name IS NOT NULL AND LOWER(p.name) = LOWER(:name) THEN 3 " +
            "    WHEN :name IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT(:name, '%')) THEN 2 " +
            "    WHEN :name IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) THEN 1 " +
            "    ELSE 0 " +
            "END AS relevance_score " +
            "FROM ProductEntity p " +
            "JOIN p.shop s " +
            "LEFT JOIN p.reviews r " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:name IS NULL OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "     (:category IS NOT NULL AND p.category = :category)) " +
            "AND (:rentDuration IS NULL OR " +
            "     (:rentDuration = 1 AND p.dailyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 2 AND p.weeklyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 3 AND p.monthlyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 4 AND p.dailyPrice IS NOT NULL AND p.weeklyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 5 AND p.weeklyPrice IS NOT NULL AND p.monthlyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 6 AND p.dailyPrice IS NOT NULL AND p.monthlyPrice IS NOT NULL) OR " +
            "     (:rentDuration = 7 AND p.dailyPrice IS NOT NULL AND p.weeklyPrice IS NOT NULL AND p.monthlyPrice IS NOT NULL)) " +
            "AND (:minPrice IS NULL OR " +
            "    (p.dailyPrice >= :minPrice OR p.weeklyPrice >= :minPrice OR p.monthlyPrice >= :minPrice)) " +
            "AND (:maxPrice IS NULL OR " +
            "    (p.dailyPrice <= :maxPrice OR p.weeklyPrice <= :maxPrice OR p.monthlyPrice <= :maxPrice)) " +
            "AND (:isRnb IS NULL OR p.isRnb = :isRnb) " +
            "GROUP BY p.id, p.name, p.rentCategory, p.isRnb, p.dailyPrice, p.weeklyPrice, p.monthlyPrice, p.image, s.regency " +
            "HAVING (:minRating IS NULL OR COALESCE(AVG(r.rating), 0) >= :minRating)")
    Page<Object[]> findFilteredProductsWithRelevance(
            @Param("category") String category,
            @Param("name") String name,
            @Param("rentDuration") Integer rentDuration,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isRnb") Boolean isRnb,
            @Param("minRating") Integer minRating,
            Pageable pageable);

    @Query("SELECT p FROM ProductEntity p " +
            "JOIN p.shop s " +
            "WHERE LOWER(s.regency) = LOWER(:regency)")
    List<ProductEntity> findByShopRegency(@Param("regency") String regency);
}


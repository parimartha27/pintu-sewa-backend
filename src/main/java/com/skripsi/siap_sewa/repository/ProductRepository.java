package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    Page<ProductEntity> findAll(Pageable pageable);

    Page<ProductEntity> findByCategory(String category, Pageable pageable);
    
    List<ProductEntity> findByShopId(String shopId);

    @Query("SELECT p FROM ProductEntity p WHERE p.shop.id = :shopId")
    Page<ProductEntity> findByShopId(@Param("shopId") String shopId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(p.category) LIKE LOWER(concat('%', :keyword, '%')) ORDER BY p.name ASC")
    List<ProductEntity> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.category) LIKE LOWER(concat('%', :category, '%')) ORDER BY p.name ASC")
    List<ProductEntity> findSimilarProductsByCategory(@Param("category") String category);
}

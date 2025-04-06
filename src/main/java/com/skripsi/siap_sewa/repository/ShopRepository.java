package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopRepository  extends JpaRepository<ShopEntity, String> {

    boolean existsByName(String name);

    List<ShopEntity> findByRegency(String regency);

    @Query("SELECT s FROM ShopEntity s " +
            "LEFT JOIN FETCH s.products p " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH s.customer " +
            "WHERE s.id = :shopId")
    Optional<ShopEntity> findByIdWithProductsAndReviews(@Param("shopId") String shopId);
}

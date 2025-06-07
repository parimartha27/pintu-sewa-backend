package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopRepository  extends JpaRepository<ShopEntity, String> {

    boolean existsByName(String name);

    List<ShopEntity> findByRegency(String regency);

    @Query("SELECT s FROM ShopEntity s WHERE LOWER(s.name) LIKE LOWER(concat('%', :keyword, '%')) ORDER BY s.name ASC")
    List<ShopEntity> searchShops(@Param("keyword") String keyword);

    @Query("SELECT s FROM ShopEntity s WHERE LOWER(s.name) LIKE LOWER(concat('%', :shopName, '%')) ORDER BY s.name ASC")
    List<ShopEntity> findSimilarShopsByName(@Param("shopName") String shopName);

    Page<ShopEntity> findAll(Pageable pageable);

    @Query("SELECT s FROM ShopEntity s WHERE s.customer.id = :customerId")
        Optional<ShopEntity> findByCustomerId(@Param("customerId") String customerId);
}

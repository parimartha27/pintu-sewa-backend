package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    Page<ProductEntity> findAll(Pageable pageable);

    List<ProductEntity> findByShopRegencyAndIdNot(String regency, String productId);
}

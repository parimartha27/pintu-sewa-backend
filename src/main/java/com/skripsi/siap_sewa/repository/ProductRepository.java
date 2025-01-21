package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {


    Optional<ProductEntity> findBySlug(String slug);
}

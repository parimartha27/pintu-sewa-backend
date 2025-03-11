package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<CartEntity, String> {

    List<CartEntity> findByCustomerId(String customerId);
}


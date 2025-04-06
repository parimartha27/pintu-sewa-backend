package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, String> {

    List<CartEntity> findByCustomerId(String customerId);

    Optional<CartEntity> findByCustomerIdAndProductId(@NotBlank(message = "Customer ID tidak boleh kosong") String customerId, @NotBlank(message = "Product ID tidak boleh kosong") String productId);

    boolean existsByCustomerIdAndProductId(@NotBlank(message = "Customer ID tidak boleh kosong") String customerId, @NotBlank(message = "Product ID tidak boleh kosong") String productId);
}


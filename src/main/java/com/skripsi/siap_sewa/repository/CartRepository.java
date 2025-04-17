package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, String> {

    List<CartEntity> findByCustomerId(String customerId);

    Optional<CartEntity> findByCustomerIdAndProductIdAndStartRentDateAndEndRentDate(
            String customerId,
            String productId,
            LocalDate startRentDate,
            LocalDate endRentDate);
}


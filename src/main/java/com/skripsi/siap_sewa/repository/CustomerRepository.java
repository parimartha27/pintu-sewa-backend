package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    List<CustomerEntity> findByUsername(String username);

    List<CustomerEntity> findByEmailOrPhoneNumber(String email, String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

   Optional<CustomerEntity> findByPhoneNumberOrEmail(String phoneNumber, String email);

}

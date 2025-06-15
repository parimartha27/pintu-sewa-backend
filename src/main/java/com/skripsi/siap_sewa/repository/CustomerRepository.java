package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    List<CustomerEntity> findByUsername(String username);

    Optional<CustomerEntity> findByPhoneNumberOrEmail(String phoneNumber, String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumberOrEmail(String phoneNumber, String email);

    boolean existsByEmailAndIdNot(@NotBlank(message = "Email tidak boleh kosong") @Email(message = "Email harus valid") String email, @NotBlank(message = "ID tidak boleh kosong") String id);

    boolean existsByUsernameAndIdNot(@NotBlank(message = "Username tidak boleh kosong") @Size(min = 3, max = 50, message = "Username harus 3-50 karakter") String username, @NotBlank(message = "ID tidak boleh kosong") String id);

    Page<CustomerEntity> findAll(Pageable pageable);

    Optional<CustomerEntity> findByEmail(String email);

    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

    Optional<CustomerEntity> findByEmailOrPhoneNumberAndStatus(String email, String phoneNumber, String status);
}

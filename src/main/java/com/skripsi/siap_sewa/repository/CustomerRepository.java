package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {
}

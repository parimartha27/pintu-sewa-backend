package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.WalletReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletReportRepository extends JpaRepository<WalletReportEntity, String> {
    Page<WalletReportEntity> findByCustomerId(String customerId, Pageable pageable);
}


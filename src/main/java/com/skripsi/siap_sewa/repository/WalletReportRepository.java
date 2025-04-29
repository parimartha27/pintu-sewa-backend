package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.WalletReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalletReportRepository extends JpaRepository<WalletReportEntity, String> {
    Page<WalletReportEntity> findByCustomerId(String customerId, Pageable pageable);
    Page<WalletReportEntity> findByShopId(String shopId, Pageable pageable);

    @Query("SELECT s FROM WalletReportEntity s WHERE s.shopId = :shopId ORDER BY s.createAt DESC")
    List<WalletReportEntity> findShopByIdOrderByCreatedAtDesc(@Param("shopId") String shopId);
}


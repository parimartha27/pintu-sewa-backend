package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletReportRepository extends JpaRepository<CartEntity, String> {
}


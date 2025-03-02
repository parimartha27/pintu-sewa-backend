package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.OtpHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OtpHistoryRepository extends JpaRepository<String, OtpHistoryEntity> {

    void deleteByCreatedAtBefore(LocalDateTime time);
}

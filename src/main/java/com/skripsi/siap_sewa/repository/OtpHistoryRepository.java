package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.OtpHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpHistoryRepository extends JpaRepository<OtpHistoryEntity, String> {

    void deleteByCreatedAtAfter(LocalDateTime time);

    Optional<OtpHistoryEntity> findByOtpAndUsernameAndCreatedAtAfter(String otp, String username, LocalDateTime createdAt);

}

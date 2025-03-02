package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.repository.OtpHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpHistoryService {

    private final OtpHistoryRepository otpHistoryRepository;

    @Autowired
    public OtpHistoryService(OtpHistoryRepository otpHistoryRepository) {
        this.otpHistoryRepository = otpHistoryRepository;
    }

    @Scheduled(fixedRate = 1800000)
    public void deleteExpiredOtpHistory() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        otpHistoryRepository.deleteByCreatedAtBefore(thirtyMinutesAgo);
    }
}


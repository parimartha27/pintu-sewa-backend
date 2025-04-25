package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/amount")
    public ResponseEntity<ApiResponse> getWalletBalance(
            @RequestParam String customerId) {
        log.info("Get wallet balance for customer: {}", customerId);
        return walletService.getWalletBalance(customerId);
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getWalletHistory(
            @RequestParam String id,
            @RequestParam String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("Get wallet history for customer: {}", id);
        return walletService.getWalletHistory(id,role,
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10));
    }

    @PatchMapping("/topup")
    public ResponseEntity<ApiResponse> topUpWallet(@RequestParam String customerId, BigDecimal amount) {
        log.info("Top Up Wallet balance for customer: {}", customerId);
        return walletService.topUpWallet(customerId,amount);
    }
}
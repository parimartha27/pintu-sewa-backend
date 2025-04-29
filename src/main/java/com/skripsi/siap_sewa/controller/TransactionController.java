package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;

import com.skripsi.siap_sewa.dto.transaction.ShopTransactionFilterRequest;
import com.skripsi.siap_sewa.dto.transaction.TransactionFilterRequest;
import com.skripsi.siap_sewa.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerTransactions(
            @PathVariable String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        TransactionFilterRequest filterRequest = TransactionFilterRequest.builder()
                .customerId(customerId)
                .status(status)
                .searchQuery(search)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return transactionService.getCustomerTransactions(filterRequest);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse> getShopTransactions(
            @PathVariable String shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ShopTransactionFilterRequest filterRequest = ShopTransactionFilterRequest.builder()
                .shopId(shopId)
                .status(status)
                .searchQuery(search)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return transactionService.getShopTransactions(filterRequest);
    }

    @GetMapping("transaction-detail/{transactionId}")
    public ResponseEntity<ApiResponse> getTransactionDetails(@PathVariable String transactionId){
        return transactionService.getTransactionDetails(transactionId);
    }

    @PatchMapping("transaction-detail/set-status")
    public ResponseEntity<ApiResponse> setStatus(@RequestParam String transactionId,@RequestParam String status){
        return transactionService.setStatus(transactionId,status);
    }

    @PatchMapping("transaction-detail/set-shipping")
    public ResponseEntity<ApiResponse> setShippingCode(@RequestParam String transactionId,@RequestParam String shippingCode,@RequestParam String type){
        return transactionService.setShippingCode(transactionId,shippingCode,type);
    }
}
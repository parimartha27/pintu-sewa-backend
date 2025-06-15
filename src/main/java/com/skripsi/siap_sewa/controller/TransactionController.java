package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;

import com.skripsi.siap_sewa.dto.transaction.*;
import com.skripsi.siap_sewa.dto.transaction.UpdateStatusTransactionRequest;
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

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getTransactionDetail(
            @RequestBody TransactionDetailRequest request) {
        return transactionService.getTransactionDetail(request);
    }

    @PatchMapping("/transaction-detail/process")
    public ResponseEntity<ApiResponse> processTransaction(@RequestBody ProcessStatusTransactionRequest request){
        return transactionService.processTransaction(request);
    }

    @PatchMapping("/transaction-detail/payment")
    public ResponseEntity<ApiResponse> paymentTransaction(@RequestBody PaymentStatusTransactionRequest request){
        return transactionService.paymentTransaction(request);
    }

    @PatchMapping("/transaction-detail/set-shipping")
    public ResponseEntity<ApiResponse> shippingTransaction(@RequestBody ShippingStatusTransactionRequest request){
        return transactionService.shippingTransaction(request);
    }

    @PatchMapping("/transaction-detail/receive-item")
    public ResponseEntity<ApiResponse> receiveTransaction(@RequestBody ReceiveStatusTransactionRequest request){
        return transactionService.receiveTransaction(request);
    }

    @PatchMapping("/transaction-detail/return-item")
    public ResponseEntity<ApiResponse> returnTransaction(@RequestBody ReturnStatusTransactionRequest request){
        return transactionService.returnTransaction(request);
    }

    @PatchMapping("/transaction-detail/done")
    public ResponseEntity<ApiResponse> doneTransaction(@RequestBody DoneStatusTransactionRequest request){
        return transactionService.doneTransaction(request);
    }

        @GetMapping("/transaction-detail/shipping/{referenceNumber}")
    public ResponseEntity<ApiResponse> getProcessShippingDetail(@PathVariable String referenceNumber) {
        return transactionService.getProcessShippingDetail(referenceNumber);
    }
    @PatchMapping("/transaction-detail/cancelled")
    public ResponseEntity<ApiResponse> cancelTransaction(@RequestBody CancelStatusTransactionRequest request){
        return transactionService.cancelTransaction(request);
    }

}
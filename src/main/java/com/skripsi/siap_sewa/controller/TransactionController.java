package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.CheckoutRequest;

import com.skripsi.siap_sewa.dto.transaction.TransactionFilterRequest;
import com.skripsi.siap_sewa.service.CheckoutService;
import com.skripsi.siap_sewa.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
    private final CheckoutService checkoutService;

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

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse> checkout(@Valid @RequestBody CheckoutRequest request) throws BadRequestException {
        log.info("Checkout type: {}", request.isCartCheckout() ? "CART" : "PRODUCT_DETAIL");
        return checkoutService.checkout(request);
    }


}
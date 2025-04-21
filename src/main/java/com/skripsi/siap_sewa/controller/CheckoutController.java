package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.CartCheckoutRequest;
import com.skripsi.siap_sewa.dto.checkout.CheckoutDetailsRequest;
import com.skripsi.siap_sewa.dto.checkout.ProductCheckoutRequest;
import com.skripsi.siap_sewa.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/product")
    public ResponseEntity<ApiResponse> checkoutProduct(@RequestBody ProductCheckoutRequest request) throws BadRequestException {
        log.info("Received product checkout request for customer: {}", request.getCustomerId());
        return checkoutService.processProductCheckout(request);
    }

    @PostMapping("/cart")
    public ResponseEntity<ApiResponse> checkoutCart(@RequestBody CartCheckoutRequest request) throws BadRequestException {
        log.info("Received cart checkout request for customer: {} with {} cart items",
                request.getCustomerId(), request.getCartIds().size());
        return checkoutService.processCartCheckout(request);
    }

    @PostMapping("/details")
    public ResponseEntity<ApiResponse> getCheckoutDetails(@RequestBody CheckoutDetailsRequest request) {
        log.info("Fetching checkout details for {} transaction IDs", request.getTransactionIds().size());
        return checkoutService.getCheckoutDetails(request);
    }
}
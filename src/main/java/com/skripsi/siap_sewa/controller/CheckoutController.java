package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.CheckoutService;
import com.skripsi.siap_sewa.utils.CommonUtils;
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
    private final CommonUtils commonUtils;

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

    @PatchMapping("/shipping")
    public ResponseEntity<ApiResponse> updateShippingMethod(
            @RequestBody UpdateShippingRequest request) throws BadRequestException {

        log.info("Updating shipping method for transactions: {}", request.getTransactionIds());

        CheckoutResponse updatedCheckout = checkoutService.updateShippingMethod(
                request.getTransactionIds(),
                request.getShippingPartnerId()
        );

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, updatedCheckout);
    }
}
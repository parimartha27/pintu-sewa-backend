package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.cart.EditCartRequest;
import com.skripsi.siap_sewa.dto.cart.AddCartRequest;
import com.skripsi.siap_sewa.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse> getAllCartProductByCustomerId(@PathVariable String customerId) {
        log.info("Get all cart products for customer: {}", customerId);
        return cartService.getAllCartProductByCustomerId(customerId);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProductToCart(@RequestBody @Valid AddCartRequest request) {
        log.info("Add product to cart request: {}", request);
        return cartService.addProductToCart(request);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editProductInCart(@RequestBody @Valid EditCartRequest request) {
        log.info("Edit product in cart request: {}", request);
        return cartService.editProductInCart(request);
    }
}
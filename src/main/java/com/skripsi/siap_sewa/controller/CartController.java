package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCartItemByCustomerId(@RequestParam String customerId){
        return cartService.getAllCartItemByCustomerId(customerId);
    }
}

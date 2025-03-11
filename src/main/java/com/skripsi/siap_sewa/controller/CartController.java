package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.cart.EditCartRequest;
import com.skripsi.siap_sewa.dto.cart.AddCartRequest;
import com.skripsi.siap_sewa.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCartProductByCustomerId(@RequestParam String customerId){
        return cartService.getAllCartProductByCustomerId(customerId);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProductToCart(@RequestBody @Valid AddCartRequest request){
        return cartService.addProductToCart(request);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editProductInCart(@RequestBody @Valid EditCartRequest request){
        return cartService.editProductInCart(request);
    }
}

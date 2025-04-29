package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.CreateShopRequest;
import com.skripsi.siap_sewa.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getShopDetail(@PathVariable String id) {
        return shopService.shopDetail(id);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getShopId(@PathVariable String id) {
        return shopService.getShopId(id);
    }

    //    for shop in page detail
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getShopByProductId(@PathVariable String productId) {
        return shopService.getShopDataByProductId(productId);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createShop (@RequestBody @Valid CreateShopRequest request){
        return shopService.createShop(request);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editShop(@RequestBody @Valid EditShopRequest request){
        return shopService.editShop(request);
    }

    @GetMapping("/dashboard/{id}")
    public ResponseEntity<ApiResponse> getShopDashboardDetail(@PathVariable String id) {
        return shopService.getShopDashboardDetail(id);
    }
}

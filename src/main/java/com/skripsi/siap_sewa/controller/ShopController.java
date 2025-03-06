package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.CreateShopRequest;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ShopService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createShop (@RequestBody @Valid CreateShopRequest request){
        return shopService.createShop(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getShopDetail (@PathVariable String shopId){
        return shopService.shopDetail(shopId);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editShop(@RequestBody @Valid EditShopRequest request){
        return shopService.editShop(request);
    }
}

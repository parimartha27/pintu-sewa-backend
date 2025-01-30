package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.AddProductRequest;
import com.skripsi.siap_sewa.dto.AddProductResponse;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.CreateShopRequest;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ShopService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final CommonUtils utils;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createStore(@RequestBody @Valid CreateShopRequest request){
        return utils.setResponse(ErrorMessageEnum.SUCCESS, shopService.createShop(request));
    }
}

package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.CreateShopRequest;
import com.skripsi.siap_sewa.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse> getShopDetail(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort.Direction direction = sort.length > 1 ?
                Sort.Direction.fromString(sort[1]) : Sort.Direction.ASC;
        String sortField = sort.length > 0 ? sort[0] : "name";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

        return shopService.shopDetail(id, pageable);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editShop(@RequestBody @Valid EditShopRequest request){
        return shopService.editShop(request);
    }
}

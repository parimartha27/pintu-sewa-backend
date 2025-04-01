package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.*;
import com.skripsi.siap_sewa.dto.product.AddProductRequest;
import com.skripsi.siap_sewa.dto.product.AddProductResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ProductService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Sort sorting = Sort.by(direction, sort[0]);

        Pageable pageable = PageRequest.of(page, size, sorting);
        return productService.getProducts(pageable);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductDetail(@PathVariable String productId){
        return productService.getProductDetail(productId);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody @Valid AddProductRequest request){
        return productService.addProduct(request);
    }


}

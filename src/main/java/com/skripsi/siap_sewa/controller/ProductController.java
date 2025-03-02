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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CommonUtils utils;

    @GetMapping()
    public ResponseEntity<ApiResponse> getProduct(){
        List<ProductResponse> responses = productService.getProducts();

        return utils.setResponse(ErrorMessageEnum.SUCCESS, responses);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductDetail(@PathVariable String productId){
        return productService.getProductDetail(productId);
    }

    @PostMapping("/insert")
    public ResponseEntity<ApiResponse> insertProduct(@RequestBody @Valid AddProductRequest request){
        AddProductResponse response = productService.insertProduct(request);

        return utils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }
}

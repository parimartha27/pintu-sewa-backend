package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.ProductResponse;
import com.skripsi.siap_sewa.dto.UserDetail;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ProductService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse> getProductDetail(String productId){
        ProductResponse response = productService.getProductDetail(productId);

        return utils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> insertProduct(ProductRequest request){

    }
}

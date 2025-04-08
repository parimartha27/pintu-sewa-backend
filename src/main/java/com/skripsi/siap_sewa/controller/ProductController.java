package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.*;
import com.skripsi.siap_sewa.dto.product.AddProductRequest;
import com.skripsi.siap_sewa.dto.product.AddProductResponse;
import com.skripsi.siap_sewa.dto.product.EditProductRequest;
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

//    =============== for product card in dashboard ==================
    @GetMapping("/most-rented")
    public ResponseEntity<ApiResponse> getProductByMostRented(){
        return productService.getProductByMostRented();
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse> getProductRecommendedForGuest(){
        return productService.getProductRecommendedForGuest();
    }

    @GetMapping("/near-customer")
    public ResponseEntity<ApiResponse> getProductNearCustomer(@RequestParam String customerId){
        return productService.getProductNearCustomer(customerId);
    }

//    =============== for search product by category ==================

    @GetMapping
    public ResponseEntity<ApiResponse> getProductsByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Sort sorting = Sort.by(direction, sort[0]);

        Pageable pageable = PageRequest.of(page - 1, size, sorting);
        return productService.getProductsByCategory(category,pageable);
    }

//    for prodcut detail page

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductDetail(@PathVariable String productId){
        return productService.getProductDetail(productId);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody @Valid AddProductRequest request){
        return productService.addProduct(request);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse> editProduct(
            @PathVariable String id,
            @RequestBody @Valid EditProductRequest request){
        return productService.editProduct(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String id){
        return productService.deleteProduct(id);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse> getProductByShopId(@PathVariable String shopId){
        return productService.getProductByShopId(shopId);
    }
}

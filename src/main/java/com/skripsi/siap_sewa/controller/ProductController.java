package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.*;
import com.skripsi.siap_sewa.dto.product.*;
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

import java.math.BigDecimal;
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

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> getFilteredProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer rentDuration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isRnb,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {

        if (!sortBy.equals("name") &&
                !sortBy.equals("dailyPrice") &&
                !sortBy.equals("weeklyPrice") &&
                !sortBy.equals("monthlyPrice")) {
            sortBy = "name";
        }

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .category(category)
                .rentDuration(rentDuration)
                .location(location)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isRnb(isRnb)
                .minRating(minRating)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        return productService.getFilteredProducts(filterRequest);
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

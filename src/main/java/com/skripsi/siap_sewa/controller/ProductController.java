package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.*;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.service.ProductFilterService;
import com.skripsi.siap_sewa.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final ProductFilterService productFilterService;

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

    @GetMapping("/top")
    public ResponseEntity<ApiResponse> getTopProductByShopId(@RequestParam String shopId){
        return productService.getTopProductByShopId(shopId);
    }

//    =============== for search product by category ==================

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> getFilteredProducts(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) List<Integer> rentDurations,
            @RequestParam(required = false) List<String> locations,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Boolean> isRnbOptions,
            @RequestParam(required = false) List<Double> minRatings,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size) {

        if (!sortBy.equals("name") &&
                !sortBy.equals("dailyPrice") &&
                !sortBy.equals("weeklyPrice") &&
                !sortBy.equals("monthlyPrice")) {
            sortBy = "name";
        }

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .categories(categories)
                .name(name)
                .rentDurations(rentDurations)
                .locations(locations)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isRnbOptions(isRnbOptions)
                .minRatings(minRatings)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page-1)
                .size(size)
                .build();

        return productFilterService.getFilteredProducts(filterRequest);
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

    //    find product in shop page
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse> getProductsByShopId(
            @PathVariable String shopId,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) List<Integer> rentDurations,
            @RequestParam(required = false) List<String> locations,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Boolean> isRnbOptions,
            @RequestParam(required = false) List<Double> minRatings,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size) {

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .shopId(shopId) // Tambahan properti ini di DTO
                .categories(categories)
                .name(name)
                .rentDurations(rentDurations)
                .locations(locations)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isRnbOptions(isRnbOptions)
                .minRatings(minRatings)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page - 1)
                .size(size)
                .build();

        return productFilterService.getFilteredProducts(filterRequest);
    }

}

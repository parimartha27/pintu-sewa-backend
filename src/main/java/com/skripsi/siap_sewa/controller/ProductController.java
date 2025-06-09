package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.*;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ProductFilterService;
import com.skripsi.siap_sewa.service.ProductService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductFilterService productFilterService;
    private final CommonUtils commonUtils;

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
            @RequestParam(required = false) String name,
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

        try {
            // Validate sort field
            List<String> validSortFields = List.of("name", "dailyPrice", "weeklyPrice",
                    "monthlyPrice", "rating", "rentedTimes");

            if (!validSortFields.contains(sortBy)) {
                log.warn("Invalid sort field: {}. Defaulting to 'name'", sortBy);
                sortBy = "name";
            }

            ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                    .categories(categories)
                    .name(name)
                    .rentDurations(rentDurations)
                    .provinces(locations)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .isRnbOptions(isRnbOptions)
                    .minRatings(minRatings)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .page(page)
                    .size(size)
                    .build();

            filterRequest.validate();
            return productFilterService.getFilteredProducts(filterRequest);
        } catch (IllegalArgumentException e) {
            log.error("Validation error in filter request: {}", e.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing filter request", e);
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, null);
        }
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

    @PutMapping("/edit/{productId}")
    public ResponseEntity<ApiResponse> editProduct(
            @PathVariable String productId,
            @RequestBody @Valid EditProductRequest request){
        return productService.editProduct(productId, request);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String productId){
        return productService.deleteProduct(productId);
    }

    //    find product in shop page
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse> getProductsByShopId(
            @PathVariable String shopId,
            @RequestParam(required = false) List<Integer> rentDurations,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Boolean> isRnbOptions,
            @RequestParam(required = false) List<Double> minRatings,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size) {

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .shopId(shopId)
                .rentDurations(rentDurations)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isRnbOptions(isRnbOptions)
                .minRatings(minRatings)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        return productFilterService.getFilteredProducts(filterRequest);
    }

    @PutMapping("/edit-stock/{id}")
    public ResponseEntity<ApiResponse> editStockProduct(
            @PathVariable String id,
            @RequestParam Integer newStock){
        return productService.editStockProduct(id, newStock);
    }

    @GetMapping("/shop/penyedia-jasa/{shopId}")
    public ResponseEntity<ApiResponse> getProductsByShop(
            @PathVariable String shopId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isRnb,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") int page) {

        ProductFilterDto request = ProductFilterDto.builder()
                .category(category)
                .isRnb(isRnb)
                .search(search)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .build();

        return productService.getProductsByShop(shopId, request);
    }
}

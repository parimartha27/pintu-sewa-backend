package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ProductDetailResponse;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;

    public ResponseEntity<ApiResponse> getProductsByCategory(String category, Pageable pageable) {
        Page<ProductEntity> productPage = productRepository.findByCategory(category, pageable);

        List<ProductResponse> responseList = productPage.getContent().stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());

        PaginationResponse<ProductResponse> paginationResponse = new PaginationResponse<>(
                responseList,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);
    }

    public ResponseEntity<ApiResponse> getProductDetail(String id) {
        Optional<ProductEntity> productEntity = productRepository.findById(id);

        if (productEntity.isPresent()) {
            ProductEntity product = productEntity.get();

            ProductDetailResponse response = modelMapper.map(product, ProductDetailResponse.class);

            Double averageRating = calculateAverageRating(product.getReviews());
            response.setRating(averageRating);

            // Calculate rentedTimes (isSelled = false) and buyTimes (isSelled = true)
            int[] transactionCounts = countProductTransactions(product.getId());
            response.setRentedTimes(transactionCounts[0]);
            response.setBuyTimes(transactionCounts[1]);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }
        log.info("Data not found for customer with ID: {}", id);
        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    public ResponseEntity<ApiResponse> getProductNearCustomer(String customerId) {
        Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            log.info("Data not found for customer with ID: {}", customerId);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        String customerRegency = customerOpt.get().getRegency();
        List<ShopEntity> shopsInSameRegency = shopRepository.findByRegency(customerRegency);

        if (shopsInSameRegency.isEmpty()) {
            log.info("Data not found for shop in {} regency", customerRegency);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        List<ProductEntity> products = shopsInSameRegency.stream()
                .flatMap(shop -> shop.getProducts().stream())
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            log.info("Data not found for products in shop {}", shopsInSameRegency);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        Collections.shuffle(products);

        List<ProductResponse> responseList = products.stream()
                .map(this::buildProductResponse)
                .limit(10)
                .collect(Collectors.toList());

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);
    }

    public ResponseEntity<ApiResponse> getProductByMostRented() {
        List<ProductEntity> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        List<ProductResponse> sortedProducts = allProducts.stream()
                .map(this::buildProductResponse)
                .sorted(Comparator.comparingInt(ProductResponse::getRentedTimes).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, sortedProducts);
    }

    public ResponseEntity<ApiResponse> getProductRecommendedForGuest() {
        List<ProductEntity> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            log.info("Data not found for guest");
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }
        
        Collections.shuffle(allProducts);

        List<ProductResponse> responseList = allProducts.stream()
                .map(this::buildProductResponse)
                .limit(10)
                .collect(Collectors.toList());

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);
    }

    public ResponseEntity<ApiResponse> addProduct(@Valid AddProductRequest request) {

        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getShopId());

        if(shopEntity.isPresent()){
            ShopEntity shopDetail = shopEntity.get();
            ProductEntity newProduct = ProductEntity.builder()
                    .name(request.getName())
                    .category(request.getCategory())
                    .rentCategory(request.getRentCategory())
                    .isRnb(request.isRnb())
                    .weight(request.getWeight())
                    .height(request.getHeight())
                    .width(request.getWidth())
                    .length(request.getLength())
                    .dailyPrice(request.getDailyPrice())
                    .weeklyPrice(request.getWeeklyPrice())
                    .monthlyPrice(request.getMonthlyPrice())
                    .description(request.getDescription())
                    .conditionDescription(request.getConditionDescription())
                    .stock(request.getStock())
                    .status(request.getStatus())
                    .image(request.getImage())
                    .createdAt(LocalDateTime.now())
                    .lastUpdateAt(LocalDateTime.now())
                    .shop(shopDetail)
                    .build();

            productRepository.save(newProduct);

            AddProductResponse response = objectMapper.convertValue(newProduct, AddProductResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }
        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Shop ID not exist");
    }

    private Double calculateAverageRating(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(ReviewEntity::getRating)
                .average()
                .orElse(0.0);
    }

    private int countRentedTimes(Set<TransactionEntity> transactions) {
        if (transactions == null) {
            return 0;
        }
        return (int) transactions.stream()
                .filter(t -> !t.isSelled())
                .count();
    }

    private ProductResponse buildProductResponse(ProductEntity product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);

        response.setAddress(product.getShop() != null ? product.getShop().getRegency() : "Unknown");

        Double averageRating = calculateAverageRating(product.getReviews());
        response.setRating(averageRating);

        int rentedTimes = countRentedTimes(product.getTransactions());
        response.setRentedTimes(rentedTimes);

        return response;
    }

    private int[] countProductTransactions(String productId) {
        List<TransactionEntity> transactions = transactionRepository.findByProductId(productId);

        int rentedTimes = 0;
        int buyTimes = 0;

        for (TransactionEntity transaction : transactions) {
            if (transaction.isSelled()) {
                buyTimes++;
            } else {
                rentedTimes++;
            }
        }

        return new int[]{rentedTimes, buyTimes};
    }


    public ResponseEntity<ApiResponse> editProduct(String id, @Valid EditProductRequest request) {
        Optional<ProductEntity> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            log.info("Product not found with ID: {}", id);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        Optional<ShopEntity> shopOpt = shopRepository.findById(request.getShopId());
        if (shopOpt.isEmpty()) {
            log.info("Shop not found with ID: {}", request.getShopId());
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Shop ID not exist");
        }

        ProductEntity product = productOpt.get();
        ShopEntity shop = shopOpt.get();

        // Update product data
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setRentCategory(request.getRentCategory());
        product.setRnb(request.isRnb());
        product.setWeight(request.getWeight());
        product.setHeight(request.getHeight());
        product.setWidth(request.getWidth());
        product.setLength(request.getLength());
        product.setDailyPrice(request.getDailyPrice());
        product.setWeeklyPrice(request.getWeeklyPrice());
        product.setMonthlyPrice(request.getMonthlyPrice());
        product.setDescription(request.getDescription());
        product.setConditionDescription(request.getConditionDescription());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setImage(request.getImage());
        product.setShop(shop);
        product.setLastUpdateAt(LocalDateTime.now());

        productRepository.save(product);

        EditProductResponse response = EditProductResponse.builder()
                .name(product.getName())
                .category(product.getCategory())
                .rentCategory(product.getRentCategory())
                .isRnb(product.isRnb())
                .weight(product.getWeight())
                .height(product.getHeight())
                .width(product.getWidth())
                .length(product.getLength())
                .dailyPrice(product.getDailyPrice())
                .weeklyPrice(product.getWeeklyPrice())
                .monthlyPrice(product.getMonthlyPrice())
                .description(product.getDescription())
                .conditionDescription(product.getConditionDescription())
                .stock(product.getStock())
                .status(product.getStatus())
                .image(product.getImage())
                .shop(product.getShop())
                .build();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> deleteProduct(String id) {
        Optional<ProductEntity> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            log.info("Product not found with ID: {}", id);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        try {
            ProductEntity product = productOpt.get();

            // Check if product has associated transactions
            boolean hasTransactions = !transactionRepository.findByProductId(id).isEmpty();

            if (hasTransactions) {
                log.info("Cannot delete product with ID: {} because it has associated transactions", id);
                return commonUtils.setResponse(
                        ErrorMessageEnum.FAILED,
                        "Product cannot be deleted because it has associated transactions"
                );
            }

            productRepository.delete(product);
            log.info("Product deleted successfully with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Product deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, null);
        }
    }
}

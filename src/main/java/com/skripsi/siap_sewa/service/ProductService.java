package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ProductDetailResponse;
import com.skripsi.siap_sewa.dto.product.AddProductRequest;
import com.skripsi.siap_sewa.dto.product.AddProductResponse;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ReviewEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.ReviewRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository  reviewRepository;
    private final TransactionRepository transactionRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;

    public ResponseEntity<ApiResponse> getProducts(Pageable pageable) {
        Page<ProductEntity> productPage = productRepository.findAll(pageable);

        List<ProductResponse> responseList = productPage.getContent().stream().map(product -> {
            Double averageRating = reviewRepository.findByProductId(product.getId())
                    .stream()
                    .mapToDouble(ReviewEntity::getRating)
                    .average()
                    .orElse(0.0);

            int rentedTimes = transactionRepository.countByProductsId(product.getId());

            String address = product.getShop() != null ?
                    product.getShop().getRegency() :
                    "Unknown";

            return ProductResponse.builder()
                    .id(product.getId())
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
                    .address(address)
                    .rating(averageRating)
                    .rentedTimes(rentedTimes)
                    .build();
        }).collect(Collectors.toList());

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
        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    private Double calculateAverageRating(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
                .mapToDouble(ReviewEntity::getRating)
                .sum();

        return sum / reviews.size();
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

}

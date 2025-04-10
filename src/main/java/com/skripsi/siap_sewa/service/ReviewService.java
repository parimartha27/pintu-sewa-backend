package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.review.ReviewRequest;
import com.skripsi.siap_sewa.dto.review.ProductReviewResponse;
import com.skripsi.siap_sewa.dto.review.ShopReviewResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ReviewEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.ReviewRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CommonUtils commonUtils;

    public ResponseEntity<ApiResponse> getReviewsByProductId(String productId, ReviewRequest request) {
        try {
            log.info("Fetching reviews for product with ID: {}", productId);

            if (!productRepository.existsById(productId)) {
                log.error("Product with ID: {} not found", productId);
                return commonUtils.setResponse(ErrorMessageEnum.PRODUCT_NOT_FOUND, null);
            }

            Sort sort = Sort.by(request.getSortDirection(), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Page<ReviewEntity> reviewsPage = reviewRepository.findByProductIdWithFilters(
                    productId,
                    request.getHasMedia(),
                    request.getRating(),
                    request.getReviewTopics(),
                    pageable);

            if (reviewsPage.isEmpty()) {
                log.info("No reviews found for product with ID: {}", productId);
                return commonUtils.setResponse(ErrorMessageEnum.NO_REVIEWS_FOUND, null);
            }

            List<ProductReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                    .map(this::mapToReviewResponse)
                    .toList();

            PaginationResponse<ProductReviewResponse> paginationResponse = new PaginationResponse<>(
                    reviewResponses,
                    reviewsPage.getNumber(),
                    reviewsPage.getSize(),
                    reviewsPage.getTotalElements(),
                    reviewsPage.getTotalPages()
            );

            log.info("Successfully fetched {} reviews for product with ID: {}", reviewResponses.size(), productId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception e) {
            log.error("Error fetching reviews for product with ID: {}: {}", productId, e.getMessage(), e);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ProductReviewResponse mapToReviewResponse(ReviewEntity review) {
        CustomerEntity customer = review.getCustomer();
        String username = customer != null ? customer.getUsername() : "Unknown User";

        return ProductReviewResponse.builder()
                .username(username)
                .comment(review.getComment())
                .image(review.getImage())
                .rating(review.getRating())
                .createdAt(CommonUtils.getRelativeTimeFromNow(review.getCreatedAt()))
                .build();
    }

    public ResponseEntity<ApiResponse> getReviewsByShopId(String shopId, ReviewRequest request) {
        try {
            log.info("Fetching reviews for shop ID: {}", shopId);

             if (!shopRepository.existsById(shopId)) {
                 return commonUtils.setResponse(ErrorMessageEnum.SHOP_NOT_FOUND, null);
             }

            // Create pageable
            Sort sort = Sort.by(request.getSortDirection(), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            // Get reviews with filters
            Page<ReviewEntity> reviewsPage = reviewRepository.findByProduct_Shop_IdWithFilters(
                    shopId,
                    request.getHasMedia(),
                    request.getRating(),
                    request.getReviewTopics(),
                    pageable);

            if (reviewsPage.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.NO_REVIEWS_FOUND, null);
            }

            List<ShopReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                    .map(this::mapToShopReviewResponse)
                    .toList();

            PaginationResponse<ShopReviewResponse> paginationResponse = new PaginationResponse<>(
                    reviewResponses,
                    reviewsPage.getNumber(),
                    reviewsPage.getSize(),
                    reviewsPage.getTotalElements(),
                    reviewsPage.getTotalPages()
            );

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception e) {
            log.error("Error fetching reviews for shop ID: {}", shopId, e);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ShopReviewResponse mapToShopReviewResponse(ReviewEntity review) {
        CustomerEntity customer = review.getCustomer();
        ProductEntity product = review.getProduct();

        return ShopReviewResponse.builder()
                .username(customer != null ? customer.getUsername() : "Unknown User")
                .comment(review.getComment())
                .image(review.getImage())
                .rating(review.getRating())
                .createdAt(CommonUtils.getRelativeTimeFromNow(review.getCreatedAt()))
                .productImage(product.getImage())
                .productName(product.getName())
                .build();
    }
}

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
import org.springframework.util.StringUtils;

import java.util.Arrays;
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
            log.debug("Validating product existence for ID: {}", productId);
            if (!productRepository.existsById(productId)) {
                log.warn("Product not found with ID: {}", productId);
                return commonUtils.setResponse(ErrorMessageEnum.PRODUCT_NOT_FOUND, null);
            }

            Sort sort = Sort.by(request.getSortDirection(), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            log.debug("Fetching reviews from repository with filters");
            Page<ReviewEntity> reviewsPage = reviewRepository.findByProductIdWithFilters(
                    productId,
                    request.getHasMedia(),
                    request.getRating(),
                    request.getReviewTopics(),
                    pageable);

            if (reviewsPage.isEmpty()) {
                log.info("No reviews found for product ID: {}", productId);
                return commonUtils.setResponse(ErrorMessageEnum.NO_REVIEWS_FOUND, null);
            }

            log.debug("Mapping {} reviews to response", reviewsPage.getNumberOfElements());
            List<ProductReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                    .map(this::mapToProductReviewResponse)
                    .toList();

            var paginationResponse = new PaginationResponse<>(
                    reviewResponses,
                    reviewsPage.getNumber(),
                    reviewsPage.getSize(),
                    reviewsPage.getTotalElements(),
                    reviewsPage.getTotalPages()
            );

            log.info("Successfully retrieved {} reviews for product ID: {}", reviewResponses.size(), productId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception e) {
            log.error("Error processing reviews for product ID: {} - {}", productId, e.getMessage(), e);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getReviewsByShopId(String shopId, ReviewRequest request) {
        try {
            log.debug("Validating shop existence for ID: {}", shopId);
            if (!shopRepository.existsById(shopId)) {
                log.warn("Shop not found with ID: {}", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.SHOP_NOT_FOUND, null);
            }

            Sort sort = Sort.by(request.getSortDirection(), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            log.debug("Fetching shop reviews from repository with filters");
            Page<ReviewEntity> reviewsPage = reviewRepository.findByProduct_Shop_IdWithFilters(
                    shopId,
                    request.getHasMedia(),
                    request.getRating(),
                    request.getReviewTopics(),
                    pageable);

            if (reviewsPage.isEmpty()) {
                log.info("No reviews found for shop ID: {}", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.NO_REVIEWS_FOUND, null);
            }

            log.debug("Mapping {} shop reviews to response", reviewsPage.getNumberOfElements());
            List<ShopReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                    .map(this::mapToShopReviewResponse)
                    .toList();

            var paginationResponse = new PaginationResponse<>(
                    reviewResponses,
                    reviewsPage.getNumber(),
                    reviewsPage.getSize(),
                    reviewsPage.getTotalElements(),
                    reviewsPage.getTotalPages()
            );

            log.info("Successfully retrieved {} reviews for shop ID: {}", reviewResponses.size(), shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception e) {
            log.error("Error processing reviews for shop ID: {} - {}", shopId, e.getMessage(), e);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ProductReviewResponse mapToProductReviewResponse(ReviewEntity review) {
        log.trace("Mapping review ID {} to product review response", review.getId());

        return ProductReviewResponse.builder()
                .username(getCustomerUsername(review.getCustomer()))
                .comment(review.getComment())
                .images(processImageString(review.getImage()))
                .rating(review.getRating())
                .createdAt(CommonUtils.getRelativeTimeFromNow(review.getCreatedAt()))
                .build();
    }

    private ShopReviewResponse mapToShopReviewResponse(ReviewEntity review) {
        log.trace("Mapping review ID {} to shop review response", review.getId());

        ProductEntity product = review.getProduct();
        return ShopReviewResponse.builder()
                .username(getCustomerUsername(review.getCustomer()))
                .comment(review.getComment())
                .images(processImageString(review.getImage()))
                .rating(review.getRating())
                .createdAt(CommonUtils.getRelativeTimeFromNow(review.getCreatedAt()))
                .productImage(product != null ? processImageString(product.getImage()).get(0) : null)
                .productName(product != null ? product.getName() : "Unknown Product")
                .build();
    }

    private String getCustomerUsername(CustomerEntity customer) {
        return customer != null ? customer.getUsername() : "Unknown User";
    }

    private List<String> processImageString(String imageString) {
        if (!StringUtils.hasText(imageString)) {
            return List.of();
        }
        return Arrays.stream(imageString.split(";"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}

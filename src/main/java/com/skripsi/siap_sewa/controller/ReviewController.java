package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.review.AddReviewRequest;
import com.skripsi.siap_sewa.dto.review.ReviewRequest;
import com.skripsi.siap_sewa.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getReviewsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false) Boolean hasMedia,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) List<String> reviewTopics) {

        log.info("Fetching reviews for product ID: {} with params - page: {}, size: {}, sort: {} {}, hasMedia: {}, rating: {}, topics: {}",
                productId, page, size, sortBy, sortDirection, hasMedia, rating, reviewTopics);

        ReviewRequest request = ReviewRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .hasMedia(hasMedia)
                .rating(rating)
                .reviewTopics(reviewTopics)
                .build();

        return reviewService.getReviewsByProductId(productId, request);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse> getReviewsByShopId(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false) Boolean hasMedia,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) List<String> reviewTopics) {

        log.info("Fetching reviews for shop ID: {} with params - page: {}, size: {}, sort: {} {}, hasMedia: {}, rating: {}, topics: {}",
                shopId, page, size, sortBy, sortDirection, hasMedia, rating, reviewTopics);

        ReviewRequest request = ReviewRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .hasMedia(hasMedia)
                .rating(rating)
                .reviewTopics(reviewTopics)
                .build();

        return reviewService.getReviewsByShopId(shopId, request);
    }

    @PostMapping(value ="/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addReview(AddReviewRequest request) throws IOException {
        return reviewService.addReview(request);
    }
}

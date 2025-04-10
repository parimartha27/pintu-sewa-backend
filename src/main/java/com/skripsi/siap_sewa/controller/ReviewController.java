package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.review.ReviewProductRequest;
import com.skripsi.siap_sewa.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        log.info("Received request to get reviews for product ID: {} with page: {}, size: {}, filters - hasMedia: {}, rating: {}, topics: {}",
                productId, page, size, hasMedia, rating, reviewTopics);

        ReviewProductRequest request = ReviewProductRequest.builder()
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


}

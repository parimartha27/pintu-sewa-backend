package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.product.ProductFilterRequest;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.spesification.ProductSpecification;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.helper.ProductHelper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.skripsi.siap_sewa.service.ProductService.getProductResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFilterService {
    private final ProductRepository productRepository;
    private final CommonUtils commonUtils;

    public ResponseEntity<ApiResponse> getFilteredProducts(ProductFilterRequest filterRequest) {
        try {
            log.info("Fetching products with filters: {}", filterRequest);

            // Build specification from filters
            Specification<ProductEntity> spec = ProductSpecification.withFilters(filterRequest);

            // Create pageable with sorting
            Sort sort = Sort.by(filterRequest.getSortDirection(), filterRequest.getSortBy());
            Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

            // Execute query
            Page<ProductEntity> resultPage = productRepository.findAll(spec, pageable);

            // Apply rating filter if needed (post-query as it's calculated)
            List<ProductEntity> filteredContent = applyRatingFilter(resultPage.getContent(), filterRequest);

            log.info("resultPage.getSize {}", resultPage.getSize());
            log.info("resultPage.getTotalElements {}", resultPage.getTotalElements());
            log.info("resultPage.getTotalPages {}", resultPage.getTotalPages());
            log.info("filteredContent {}", filteredContent);
            log.info("resultPage.getNumber :{}", resultPage.getNumber());

            // Paginate the filtered results
            PaginationResponse<ProductResponse> paginationResponse = createPaginationResponse(
                    filteredContent,
                    resultPage.getPageable().getPageNumber(),
                    resultPage.getSize(),
                    (int) resultPage.getTotalElements()
            );

            if (paginationResponse.getContent().isEmpty()) {
                log.info("No products found with given filters");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            log.info("Successfully fetched {} products with filters", paginationResponse.getContent().size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.error("Error fetching filtered products: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductsByShopId(String shopId, ProductFilterRequest filterRequest, Pageable pageable) {
        try {
            log.info("Fetching products for shopId={}, rentDuration={}, minPrice={}, maxPrice={}, minRating={}",
                    shopId, filterRequest.getRentDuration(), filterRequest.getMinPrice(),
                    filterRequest.getMaxPrice(), filterRequest.getMinRating());

            // First apply all filters except rating
            Specification<ProductEntity> spec = withShopAndFilters(
                    shopId, filterRequest.getRentDuration(), filterRequest.getMinPrice(), filterRequest.getMaxPrice());

            // Get the full filtered list (without rating filter)
            Page<ProductEntity> allFilteredProducts = productRepository.findAll(spec, pageable);

            // Then apply rating filter
            List<ProductEntity> filteredContent = applyRatingFilter(allFilteredProducts.getContent(), filterRequest);

            // Create pagination response from the fully filtered list
            PaginationResponse<ProductResponse> paginationResponse = createPaginationResponse(
                    filteredContent,
                    allFilteredProducts.getPageable().getPageNumber(),
                    allFilteredProducts.getSize(),
                    (int) allFilteredProducts.getTotalElements()
            );

            if (paginationResponse.getContent().isEmpty()) {
                log.warn("No products found for shopId={} with given filters", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            log.info("Successfully fetched {} products for shopId={}",
                    paginationResponse.getContent().size(), shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.error("Error fetching products by shopId {}: {}", shopId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private Specification<ProductEntity> withShopAndFilters(String shopId, Integer rentDuration,
                                                            BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by shopId
            if (StringUtils.hasText(shopId)) {
                Join<Object, Object> shopJoin = root.join("shop");
                predicates.add(criteriaBuilder.equal(shopJoin.get("id"), shopId));
            }

            // Rent Duration
            if (rentDuration != null) {
                predicates.add(criteriaBuilder.equal(root.get("rentCategory"), rentDuration));
            }

            // Price range
            if (minPrice != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dailyPrice"), minPrice),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("weeklyPrice"), minPrice),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("monthlyPrice"), minPrice)
                ));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.lessThanOrEqualTo(root.get("dailyPrice"), maxPrice),
                        criteriaBuilder.lessThanOrEqualTo(root.get("weeklyPrice"), maxPrice),
                        criteriaBuilder.lessThanOrEqualTo(root.get("monthlyPrice"), maxPrice)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PaginationResponse<ProductResponse> createPaginationResponse(
            List<ProductEntity> products, int page, int size, int totalElement) {

        int totalElements = totalElement;
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = 0;
        int toIndex = products.size();

        List<ProductEntity> pagedProducts = (fromIndex < totalElements)
                ? products.subList(fromIndex, toIndex)
                : Collections.emptyList();

        List<ProductResponse> responseList = pagedProducts.stream()
                .map(this::buildProductResponse)
                .toList();

        return new PaginationResponse<>(
                responseList,
                page + 1, // FE expects 1-based index
                size,
                totalElements,
                totalPages
        );
    }


    private List<ProductEntity> applyRatingFilter(List<ProductEntity> products, ProductFilterRequest filterRequest) {
        if (filterRequest.getMinRating() == null) {
            return products;
        }

        return products.stream()
                .filter(product -> {
                    Double rating = ProductHelper.calculateWeightedRating(product.getReviews());
                    return rating != null && rating >= filterRequest.getMinRating();
                })
                .toList();
    }

    private ProductResponse buildProductResponse(ProductEntity product) {
        return getProductResponse(product);
    }
}
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
import com.skripsi.siap_sewa.utils.ProductUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

            // Paginate the filtered results
            PaginationResponse<ProductResponse> paginationResponse = createPaginationResponse(
                    filteredContent,
                    filterRequest.getPage(),
                    filterRequest.getSize()
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

    private PaginationResponse<ProductResponse> createPaginationResponse(
            List<ProductEntity> products, int page, int size) {

        int totalElements = products.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ProductEntity> pagedProducts = (fromIndex < totalElements)
                ? products.subList(fromIndex, toIndex)
                : Collections.emptyList();

        List<ProductResponse> responseList = pagedProducts.stream()
                .map(this::buildProductResponse)
                .toList();

        return new PaginationResponse<>(
                responseList,
                page,
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
                    Double rating = ProductUtils.calculateWeightedRating(product.getReviews());
                    return rating != null && rating >= filterRequest.getMinRating();
                })
                .toList();
    }

    private ProductResponse buildProductResponse(ProductEntity product) {
        return getProductResponse(product);
    }
}
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFilterService {
    private final ProductRepository productRepository;
    private final CommonUtils commonUtils;
    
    public ResponseEntity<ApiResponse> getFilteredProducts(ProductFilterRequest filterRequest) {
        try {

            Specification<ProductEntity> spec = ProductSpecification.withFilters(filterRequest);
            Pageable pageable = PageRequest.of(
                    filterRequest.getPage() - 1,
                    filterRequest.getSize(),
                    Sort.by(filterRequest.getSortDirection(), filterRequest.getSortBy())
            );

            Page<ProductEntity> resultPage = productRepository.findAll(spec, pageable);

            List<ProductEntity> filteredContent = applyPostFilters(resultPage.getContent(), filterRequest);

            PaginationResponse<ProductResponse> response = createPaginationResponse(
                    filteredContent,
                    resultPage.getNumber() + 1,
                    resultPage.getSize(),
                    (int) resultPage.getTotalElements(),
                    resultPage.getTotalPages()
            );

            if (filteredContent.isEmpty() && resultPage.getTotalElements() > 0) {
                log.info("Post-filtering removed all results. Original count: {}", resultPage.getTotalElements());
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Error filtering products: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, null);
        }
    }

    private List<ProductEntity> applyPostFilters(List<ProductEntity> products, ProductFilterRequest filterRequest) {
        if (filterRequest.getMinRatings() == null || filterRequest.getMinRatings().isEmpty()) {
            return products;
        }

        return products.stream()
                .filter(product -> {
                    Double rating = ProductHelper.calculateWeightedRating(product.getReviews());
                    return filterRequest.getMinRatings().stream()
                            .anyMatch(min -> rating >= min && rating < min + 1);
                })
                .toList();
    }

    private PaginationResponse<ProductResponse> createPaginationResponse(
            List<ProductEntity> products, int page, int size, int totalElements, int totalPages) {

        List<ProductResponse> content = products.stream()
                .map(ProductHelper::convertToResponse)
                .toList();

        return PaginationResponse.<ProductResponse>builder()
                .content(content)
                .currentPage(page)
                .pageSize(size)
                .totalItems(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
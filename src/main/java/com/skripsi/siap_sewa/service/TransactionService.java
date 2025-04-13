// TransactionService.java
package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.transaction.TransactionFilterRequest;
import com.skripsi.siap_sewa.dto.transaction.TransactionResponse;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.helper.ProductHelper;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.spesification.TransactionSpecification;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CommonUtils commonUtils;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Transactional
    public ResponseEntity<ApiResponse> getCustomerTransactions(TransactionFilterRequest filterRequest) {
        try {
            log.info("Fetching transactions for customer {} with filters: {}",
                    filterRequest.getCustomerId(), filterRequest);

            // Build specification from filters
            Specification<TransactionEntity> spec = TransactionSpecification.withFilters(filterRequest);

            // Get pageable from request
            Pageable pageable = filterRequest.getPageable();

            // Execute query
            Page<TransactionEntity> resultPage = transactionRepository.findAll(spec, pageable);

            // Convert to response DTO
            List<TransactionResponse> responseList = resultPage.getContent().stream()
                    .map(this::buildTransactionResponse)
                    .collect(Collectors.toList());

            // Create pagination response
            PaginationResponse<TransactionResponse> paginationResponse = new PaginationResponse<>(
                    responseList,
                    resultPage.getNumber(),
                    resultPage.getSize(),
                    resultPage.getTotalElements(),
                    resultPage.getTotalPages()
            );

            if (responseList.isEmpty()) {
                log.info("No transactions found for customer {} with given filters",
                        filterRequest.getCustomerId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            log.info("Successfully fetched {} transactions for customer {}",
                    responseList.size(), filterRequest.getCustomerId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.error("Error fetching transactions: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private TransactionResponse buildTransactionResponse(TransactionEntity transaction) {
        // Get first product (assuming one product per transaction for simplicity)
        // Adjust if you need to handle multiple products
        var product = transaction.getProducts().stream().findFirst().orElse(null);

        return TransactionResponse.builder()
                .orderId(transaction.getId())
                .status(transaction.getStatus())
                .transactionDate(transaction.getCreatedAt().format(DATE_FORMATTER))
                .referenceNumber(transaction.getTransactionNumber())
                .shop(product != null ? TransactionResponse.ShopInfo.builder()
                        .id(product.getShop().getId())
                        .name(product.getShop().getName())
                        .build() : null)
                .products(transaction.getProducts().stream()
                        .map(p -> TransactionResponse.ProductInfo.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .quantity(transaction.getQuantity())
                                .price(ProductHelper.getLowestPrice(p))
                                .build())
                        .collect(Collectors.toList()))
                .totalPrice(transaction.getAmount() != null ?
                        transaction.getAmount() : BigDecimal.ZERO)
                .build();
    }
}
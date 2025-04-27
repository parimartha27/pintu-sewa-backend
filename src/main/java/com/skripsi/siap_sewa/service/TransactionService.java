package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.transaction.ShopTransactionFilterRequest;
import com.skripsi.siap_sewa.dto.transaction.TransactionFilterRequest;
import com.skripsi.siap_sewa.dto.transaction.TransactionResponse;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.spesification.TransactionSpecification;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CommonUtils commonUtils;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private final ProductRepository productRepository;

    @Transactional
    public ResponseEntity<ApiResponse> getCustomerTransactions(TransactionFilterRequest filterRequest) {
        try {
            log.info("Fetching transactions for customer {} with filters: {}",
                    filterRequest.getCustomerId(), filterRequest);

            // 1. Get all transactions matching filters
            Specification<TransactionEntity> spec = TransactionSpecification.withFilters(filterRequest);
            List<TransactionEntity> transactions = transactionRepository.findAll(spec);

            if (transactions.isEmpty()) {
                log.info("No transactions found for customer {}", filterRequest.getCustomerId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            // 2. Group transactions by referenceNumber
            Map<String, List<TransactionEntity>> groupedTransactions = transactions.stream()
                    .collect(Collectors.groupingBy(TransactionEntity::getTransactionNumber));

            // 3. Build response
            List<TransactionResponse> responseList = groupedTransactions.entrySet().stream()
                    .map(entry -> buildGroupedTransactionResponse(entry.getKey(), entry.getValue()))
                    .toList();

            log.info("Found {} transaction groups for customer {}", responseList.size(), filterRequest.getCustomerId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (Exception ex) {
            log.error("Error fetching transactions: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private TransactionResponse buildGroupedTransactionResponse(
            String referenceNumber,
            List<TransactionEntity> transactions
    ) {
        // All transactions in this group share the same referenceNumber
        TransactionEntity firstTransaction = transactions.get(0);

        return TransactionResponse.builder()
                .referenceNumber(referenceNumber)
                .status(firstTransaction.getStatus())
                .transactionDate(firstTransaction.getCreatedAt().format(DATE_FORMATTER))
                .shop(buildShopInfo(firstTransaction))
                .products(buildProductInfoList(transactions))
                .totalPrice(calculateTotalPrice(transactions))
                .totalDeposit(calculateTotalDeposit(transactions))
                .shippingPartner(firstTransaction.getShippingPartner())
                .shippingPrice(firstTransaction.getShippingPrice())
                .build();
    }

    private TransactionResponse.ShopInfo buildShopInfo(TransactionEntity transaction) {
        return transaction.getProducts().stream()
                .findFirst()
                .map(product -> TransactionResponse.ShopInfo.builder()
                        .id(product.getShop().getId())
                        .name(product.getShop().getName())
                        .build())
                .orElse(null);
    }

    private List<TransactionResponse.ProductInfo> buildProductInfoList(List<TransactionEntity> transactions) {
        return transactions.stream()
                .flatMap(transaction -> transaction.getProducts().stream()
                        .map(product -> TransactionResponse.ProductInfo.builder()
                                .orderId(transaction.getId())  // Unique transaction ID
                                .productId(product.getId())
                                .productName(product.getName())
                                .image(product.getImage())
                                .quantity(transaction.getQuantity())
                                .price(transaction.getAmount().divide(
                                        BigDecimal.valueOf(transaction.getQuantity()),
                                        RoundingMode.HALF_UP))
                                .subTotal(transaction.getAmount())
                                .startDate(transaction.getStartDate().format(DATE_FORMATTER))
                                .endDate(transaction.getEndDate().format(DATE_FORMATTER))
                                .build()))
                .toList();
    }

    private BigDecimal calculateTotalPrice(List<TransactionEntity> transactions) {
        return transactions.stream()
                .map(TransactionEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalDeposit(List<TransactionEntity> transactions) {
        return transactions.stream()
                .map(TransactionEntity::getTotalDeposit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ResponseEntity<ApiResponse> getShopTransactions(ShopTransactionFilterRequest filterRequest) {
        try {
            log.info("Fetching transactions for shop {} with filters: {}",
                    filterRequest.getShopId(), filterRequest);

            // 1. Get all transactions matching filters
            Specification<TransactionEntity> spec = TransactionSpecification.withFilters(filterRequest);
            List<TransactionEntity> transactions = transactionRepository.findAll(spec);

            if (transactions.isEmpty()) {
                log.info("No transactions found for Shop  {}", filterRequest.getShopId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            // 2. Group transactions by referenceNumber
            Map<String, List<TransactionEntity>> groupedTransactions = transactions.stream()
                    .collect(Collectors.groupingBy(TransactionEntity::getTransactionNumber));

            // 3. Build response
            List<TransactionResponse> responseList = groupedTransactions.entrySet().stream()
                    .map(entry -> buildGroupedTransactionResponse(entry.getKey(), entry.getValue()))
                    .toList();

            log.info("Found {} transaction groups for Shop {}", responseList.size(), filterRequest.getShopId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (Exception ex) {
            log.error("Error fetching transactions: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getTransactionDetails(String transactionId) {
        try {
            log.info("Fetching transactions Details with ID {}", transactionId);
            Optional<TransactionEntity> transaction = transactionRepository.findById(transactionId);

            if(transaction.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, transaction);
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", transactionId,ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}
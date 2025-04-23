package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.payment.PaymentRequest;
import com.skripsi.siap_sewa.dto.payment.PaymentResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.exception.InsufficientBalanceException;
import com.skripsi.siap_sewa.exception.TransactionProcessingException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final WalletReportRepository walletReportRepository;
    private final ProductRepository productRepository;
    private final CommonUtils commonUtils;

    @Transactional
    public ResponseEntity<ApiResponse> processPayment(PaymentRequest request) {
        try {
            // 1. Validate customer exists
            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            // 2. Get all transactions
            List<TransactionEntity> transactions = transactionRepository.findAllById(request.getTransactionIds());

            // 3. Group by transaction number
            Map<String, List<TransactionEntity>> transactionsByNumber = transactions.stream()
                    .collect(Collectors.groupingBy(TransactionEntity::getTransactionNumber));

            List<String> succeeded = new ArrayList<>();
            List<String> failed = new ArrayList<>();

            // 4. Process each group
            for (Map.Entry<String, List<TransactionEntity>> entry : transactionsByNumber.entrySet()) {
                try {
                    processTransactionGroup(customer, entry.getValue());
                    succeeded.addAll(entry.getValue().stream()
                            .map(TransactionEntity::getId)
                            .toList());
                } catch (Exception e) {
                    // Mark all in group as failed
                    failTransactionGroup(entry.getValue(), e.getMessage());
                    failed.addAll(entry.getValue().stream()
                            .map(TransactionEntity::getId)
                            .toList());

                    log.error("Failed to process transaction group {}: {}", entry.getKey(), e.getMessage());
                }
            }

            // 5. Prepare response
            PaymentResponse response = PaymentResponse.builder()
                    .paymentStatus(failed.isEmpty() ? "COMPLETED" : "PARTIAL")
                    .succeededTransactions(succeeded)
                    .failedTransactions(failed)
                    .message(failed.isEmpty() ? "All payments processed successfully"
                            : failed.size() == transactions.size() ? "All payments failed"
                            : "Some payments failed")
                    .totalPaid(calculateTotalSuccessfulPayment(succeeded, transactions))
                    .paymentTime(LocalDateTime.now())
                    .build();

            return buildResponse(succeeded, failed, response);
        } catch (Exception ex) {
            log.error("Payment processing failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private ResponseEntity<ApiResponse> buildResponse(List<String> succeeded, List<String> failed,
                                                      PaymentResponse response) {
        if (failed.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } else if (succeeded.isEmpty()) {
            return commonUtils.setResponse(Constant.TRANSACTION_GROUP_FAILED_CODE,
                    "All transactions failed to process",
                    HttpStatus.BAD_REQUEST,
                    response);
        } else {
            return commonUtils.setResponse(
                    Constant.PAYMENT_PARTIAL_SUCCESS_CODE,
                    "Some transactions failed to process",
                    HttpStatus.MULTI_STATUS,
                    response);
        }
    }

    private BigDecimal calculateTotalSuccessfulPayment(List<String> succeeded,
                                                       List<TransactionEntity> allTransactions) {
        return allTransactions.stream()
                .filter(t -> succeeded.contains(t.getId()))
                .map(TransactionEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void processTransactionGroup(CustomerEntity customer, List<TransactionEntity> transactions) {
        // 1. Validate all transactions belong to customer and are unpaid
        validateTransactions(transactions, customer.getId());

        // 2. Calculate total amount for this group
        BigDecimal totalAmount = calculateTotalPayment(transactions);

        // 3. Validate customer balance
        if (customer.getWalletAmount().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(customer.getWalletAmount(), totalAmount);
        }

        // 4. Deduct from customer wallet
        customer.setWalletAmount(customer.getWalletAmount().subtract(totalAmount));

        // 5. Process each transaction in group
        for (TransactionEntity transaction : transactions) {
            processSingleTransaction(transaction, customer);
        }

        // 6. Save customer with updated balance
        customerRepository.save(customer);
    }

    private void validateTransactions(List<TransactionEntity> transactions, String customerId) {
        if (transactions.isEmpty()) {
            throw new DataNotFoundException("No transactions found");
        }

        // Check all transactions belong to customer
        boolean allBelongToCustomer = transactions.stream()
                .allMatch(t -> t.getCustomer().getId().equals(customerId));
        if (!allBelongToCustomer) {
            throw new TransactionProcessingException(
                    "Some transactions don't belong to this customer",
                    Constant.TRANSACTION_NOT_BELONG_TO_CUSTOMER_CODE,
                    transactions.stream().map(TransactionEntity::getId).toList());
        }

        // Check all transactions are unpaid
        boolean allUnpaid = transactions.stream()
                .allMatch(t -> Constant.TRANSACTION_STATUS_PENDING.equals(t.getStatus()));
        if (!allUnpaid) {
            throw new TransactionProcessingException(
                    "Some transactions are already paid",
                    Constant.TRANSACTION_ALREADY_PAID_CODE,
                    transactions.stream()
                            .filter(t -> !Constant.TRANSACTION_STATUS_PENDING.equals(t.getStatus()))
                            .map(TransactionEntity::getId)
                            .toList());
        }
    }

    private BigDecimal calculateTotalPayment(List<TransactionEntity> transactions) {
        return transactions.stream()
                .map(TransactionEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void failTransactionGroup(List<TransactionEntity> transactions, String reason) {
        transactions.forEach(t -> {
            t.setStatus(Constant.TRANSACTION_STATUS_CANCELLED);
            t.setLastUpdateAt(LocalDateTime.now());
            t.setPaymentMethod("FAILED");
        });
        transactionRepository.saveAll(transactions);
        returnProductStock(transactions);
    }

    private void returnProductStock(List<TransactionEntity> transactions) {
        transactions.forEach(t -> {
            t.getProducts().forEach(p -> {
                p.setStock(p.getStock() + t.getQuantity());
            });
        });
        // Save all updated products
        Set<ProductEntity> productsToUpdate = transactions.stream()
                .flatMap(t -> t.getProducts().stream())
                .collect(Collectors.toSet());
        productRepository.saveAll(productsToUpdate);
    }

    private void processSingleTransaction(TransactionEntity transaction, CustomerEntity customer) {
        // 1. Update transaction status
        transaction.setStatus(Constant.TRANSACTION_STATUS_PROCESSED);
        transaction.setPaymentMethod("WALLET");
        transaction.setLastUpdateAt(LocalDateTime.now());

        // 2. Get shop
        ProductEntity product = transaction.getProducts().iterator().next();
        ShopEntity shop = product.getShop();

        // 3. Calculate seller amount (minus service fee)
        BigDecimal sellerAmount = transaction.getAmount()
                .add(transaction.getShippingPrice())
                .subtract(transaction.getServiceFee());

        // 4. Add to shop balance
        shop.setBalance(shop.getBalance().add(sellerAmount));

        // 5. Create wallet reports
        createWalletReport(
                customer,
                null, // shop is null for customer
                transaction.getTotalAmount(),
                WalletReportEntity.WalletType.DEBIT,
                "Payment for transaction " + transaction.getTransactionNumber()
        );

        createWalletReport(
                null, // customer is null for shop
                shop,
                sellerAmount,
                WalletReportEntity.WalletType.CREDIT,
                "Income from transaction " + transaction.getTransactionNumber()
        );

        // 6. Save all changes
        transactionRepository.save(transaction);
        shopRepository.save(shop);
    }

    private void createWalletReport(CustomerEntity customer, ShopEntity shop,
                                    BigDecimal amount, WalletReportEntity.WalletType type,
                                    String description) {
        WalletReportEntity report = WalletReportEntity.builder()
                .customerId(customer != null ? customer.getId() : null)
                .shopId(shop != null ? shop.getId() : null)
                .amount(amount)
                .type(type)
                .description(description)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        walletReportRepository.save(report);
    }
}
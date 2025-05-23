package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.transaction.*;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.repository.WalletReportRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
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
import java.time.LocalDateTime;
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
    private final CustomerRepository customerRepository;
    private final WalletReportRepository walletReportRepository;
    private final ShopRepository shopRepository;
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
            Specification<TransactionEntity> spec = TransactionSpecification.withFiltersShop(filterRequest);
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

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, transaction);
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", transactionId,ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> setStatus(UpdateStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into {}", request.getReferenceNumbers(), request.getNextStatus());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumberIn(request.getReferenceNumbers());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus(request.getNextStatus());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumbers(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> setShippingCode(String transactionId,String shippingCode,String type) {
        try {
            log.info("Update Transaction Id {} {} Code Into {}",transactionId,shippingCode,type);
            Optional<TransactionEntity> transaction = transactionRepository.findById(transactionId);
            if(transaction.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            if(type == "return"){
                transaction.get().setReturnCode(shippingCode);
            }else if(type == "shipping"){
                transaction.get().setShippingCode(shippingCode);
            }else{
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Type not exist");
            }

            transaction.get().setLastUpdateAt(LocalDateTime.now());
            transactionRepository.save(transaction.get());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", transactionId,ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getTransactionDetail(TransactionDetailRequest request) {
        try {
            log.info("Fetching transaction detail for reference: {}", request.getReferenceNumber());

            // 1. Find all transactions with the same reference number
            List<TransactionEntity> transactions = transactionRepository
                    .findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
                log.info("No transactions found with reference: {}", request.getReferenceNumber());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            // 2. Validate ownership (either customer or shop)
            boolean isValid = validateOwnership(transactions, request);
            if (!isValid) {
                log.warn("Unauthorized access to transaction reference: {}", request.getReferenceNumber());
                return commonUtils.setResponse(ErrorMessageEnum.CUSTOMER_NOT_FOUND, null);
            }

            // 3. Build response using existing helper methods
            TransactionDetailResponse response = buildTransactionDetailResponse(transactions);

            log.info("Successfully fetched transaction detail for reference: {}", request.getReferenceNumber());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Error fetching transaction detail: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private boolean validateOwnership(List<TransactionEntity> transactions, TransactionDetailRequest request) {
        // If no ownership validation required
        if (request.getCustomerId() == null && request.getShopId() == null) {
            return true;
        }

        // Validate customer ownership
        if (request.getCustomerId() != null) {
            return transactions.get(0).getCustomer().getId().equals(request.getCustomerId());
        }

        // Validate shop ownership
        if (request.getShopId() != null) {
            return transactions.stream()
                    .allMatch(t -> t.getShopId().equals(request.getShopId()));
        }

        return true;
    }

    private TransactionDetailResponse buildTransactionDetailResponse(List<TransactionEntity> transactions) {
        TransactionEntity firstTransaction = transactions.get(0);

        return TransactionDetailResponse.builder()
                .transactionDetail(buildTransactionDetail(firstTransaction))
                .productDetails(buildProductDetails(transactions))
                .paymentDetail(buildPaymentDetail(transactions))
                .shopDetail(buildShopInfo(transactions.get(0)))
                .build();
    }

    private TransactionDetailResponse.TransactionDetail buildTransactionDetail(TransactionEntity transaction) {
        return TransactionDetailResponse.TransactionDetail.builder()
                .referenceNumber(transaction.getTransactionNumber())
                .status(transaction.getStatus())
                .transactionDate(transaction.getCreatedAt().format(DATE_FORMATTER))
                .shippingAddress(transaction.getShippingAddress())
                .shippingPartner(transaction.getShippingPartner())
                .shippingCode(transaction.getShippingCode())
                .build();
    }

    private List<TransactionDetailResponse.ProductDetail> buildProductDetails(List<TransactionEntity> transactions) {
        return transactions.stream()
                .map(transaction -> {
                    ProductEntity product = transaction.getProducts().iterator().next(); // Get first product
                    return TransactionDetailResponse.ProductDetail.builder()
                            .orderId(transaction.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .image(product.getImage())
                            .startRentDate(transaction.getStartDate().format(DATE_FORMATTER))
                            .endRentDate(transaction.getEndDate().format(DATE_FORMATTER))
                            .quantity(transaction.getQuantity())
                            .price(transaction.getAmount().divide(
                                    BigDecimal.valueOf(transaction.getQuantity()),
                                    RoundingMode.HALF_UP))
                            .subTotal(transaction.getAmount())
                            .deposit(product.getDeposit().multiply(BigDecimal.valueOf(transaction.getQuantity())))
                            .build();
                })
                .toList();
    }

    private TransactionDetailResponse.ProductDetail.ShopInfo buildShopInfo(ShopEntity shop) {
        return TransactionDetailResponse.ProductDetail.ShopInfo.builder()
                .id(shop.getId())
                .name(shop.getName())
                .build();
    }

    private TransactionDetailResponse.PaymentDetail buildPaymentDetail(List<TransactionEntity> transactions) {
        BigDecimal subTotal = calculateTotalPrice(transactions);
        BigDecimal totalDeposit = calculateTotalDeposit(transactions);

        return TransactionDetailResponse.PaymentDetail.builder()
                .paymentMethod(transactions.get(0).getPaymentMethod())
                .subTotal(subTotal)
                .shippingPrice(transactions.get(0).getShippingPrice())
                .serviceFee(transactions.get(0).getServiceFee())
                .totalDeposit(totalDeposit)
                .grandTotal(subTotal
                        .add(transactions.get(0).getShippingPrice())
                        .add(transactions.get(0).getServiceFee())
                        .add(totalDeposit))
                .build();
    }

    public ResponseEntity<ApiResponse> processTransaction(ProcessStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Belum Dibayar", request.getReferenceNumbers());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumberIn(request.getReferenceNumbers());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus("Belum Dibayar");
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumbers(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> paymentTransaction(PaymentStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Belum Dibayar", request.getReferenceNumbers());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumberIn(request.getReferenceNumbers());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            if (!customerRepository.existsById(request.getCustomerId())) {
                throw new DataNotFoundException("Customer not found");
            }
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Amount Must be greater than zero");
            }

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", request.getCustomerId());
                        return new DataNotFoundException("Customer not found");
                    });

            if (customer.getWalletAmount().compareTo(request.getAmount()) < 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Insufficient balance");
            }

            customer.setWalletAmount(customer.getWalletAmount().subtract(request.getAmount()));
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);


            WalletReportEntity wallet = new WalletReportEntity();
            wallet.setDescription("Payment Transaction : " + request.getAmount());
            wallet.setAmount(request.getAmount());
            wallet.setType(WalletReportEntity.WalletType.CREDIT);
            wallet.setCustomerId(customer.getId());
            wallet.setCreateAt(LocalDateTime.now());
            wallet.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(wallet);

            log.info("Successfully Payment Customer ID: {}",request.getCustomerId());

            transactions.forEach(transaction -> {
                transaction.setStatus("Diproses");
                transaction.setPaymentMethod(request.getPaymentMethod());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumbers(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> shippingTransaction(ShippingStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Dikirim", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());
            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus("Dikirim");
                transaction.setShippingCode(request.getShippingCode());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> receiveTransaction(ReceiveStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Sedang Disewa", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus("Sedang Disewa");
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> returnTransaction(ReturnStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Dikembalikan", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus("Dikembalikan");
                transaction.setReturnCode(request.getReturnCode());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> doneTransaction(DoneStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Selesai", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if(transactions.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            BigDecimal deposit = transactions.getFirst().getTotalDeposit();
            transactions.forEach(transaction -> {
                transaction.setStatus("Selesai");
                transaction.setIsReturn("RETURNED");
                transaction.setDepositReturned(true);
                transaction.setDepositReturnedAt(LocalDateTime.now());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", request.getCustomerId());
                        return new DataNotFoundException("Customer not found");
                    });

            ShopEntity shop = shopRepository.findById(transactions.getFirst().getShopId())
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", transactions.getFirst().getShopId());
                        return new DataNotFoundException("Shop not found");
                    });

            if (shop.getBalance().compareTo(deposit) < 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Insufficient balance");
            }

            customer.setWalletAmount(customer.getWalletAmount().add(deposit));
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);


            shop.setBalance(shop.getBalance().subtract(deposit));
            shop.setLastUpdateAt(LocalDateTime.now());
            shopRepository.save(shop);

            WalletReportEntity walletCustomer = new WalletReportEntity();
            walletCustomer.setDescription("Deposit Return From Transaction Amount : "+ deposit);
            walletCustomer.setAmount(deposit);
            walletCustomer.setType(WalletReportEntity.WalletType.DEBIT);
            walletCustomer.setCustomerId(customer.getId());
            walletCustomer.setCreateAt(LocalDateTime.now());
            walletCustomer.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletCustomer);

            WalletReportEntity walletShop = new WalletReportEntity();
            walletShop.setDescription("Deposit Return From Transaction ID Amount : "+ deposit);
            walletShop.setAmount(deposit);
            walletShop.setType(WalletReportEntity.WalletType.CREDIT);
            walletShop.setCustomerId(shop.getId());
            walletShop.setCreateAt(LocalDateTime.now());
            walletShop.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletShop);

            log.info("Successfully Return Deposit of Transaction ID {} ",transactions.getFirst().getTransactionNumber());

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(),ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}
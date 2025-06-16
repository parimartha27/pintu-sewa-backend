package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.shop.dashboard.TransactionResponseShopDashboard;
import com.skripsi.siap_sewa.dto.transaction.*;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.spesification.TransactionSpecification;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final WalletReportRepository walletReportRepository;
    private final ShopRepository shopRepository;
    private final CommonUtils commonUtils;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Transactional
    public ResponseEntity<ApiResponse> getCustomerTransactions(TransactionFilterRequest filterRequest) {
        try {
            Specification<TransactionEntity> spec = TransactionSpecification.withFilters(filterRequest);
            List<TransactionEntity> transactions = transactionRepository.findAll(spec);

            if (transactions.isEmpty()) {
                log.info("No transactions found for customer {}", filterRequest.getCustomerId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            Map<String, List<TransactionEntity>> groupedTransactions =
                    transactions.stream().collect(Collectors.groupingBy(TransactionEntity::getTransactionNumber));

            List<TransactionResponse> responseList = groupedTransactions.entrySet().stream().map(
                    entry -> buildGroupedTransactionResponse(entry.getKey(), entry.getValue())
            ).toList();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (Exception ex) {
            log.error("Error fetching transactions: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private TransactionResponse buildGroupedTransactionResponse(String referenceNumber, List<TransactionEntity> transactions) {

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
                .shippingPrice(calculateTotalShippingPrice(transactions))
                .build();
    }

    private TransactionResponse.ShopInfo buildShopInfo(TransactionEntity transaction) {
        return transaction.getProducts().stream().findFirst().map(
                product -> TransactionResponse.ShopInfo.builder()
                        .id(product.getShop().getId())
                        .name(product.getShop().getName())
                        .build()).orElse(null);
    }

    private List<TransactionResponse.ProductInfo> buildProductInfoList(List<TransactionEntity> transactions) {
        return transactions.stream().flatMap(
                transaction -> transaction.getProducts().stream().map(
                        product -> TransactionResponse.ProductInfo.builder()
                                .orderId(transaction.getId())
                                .productId(product.getId())
                                .productName(product.getName())
                                .image(product.getImage())
                                .quantity(transaction.getQuantity())
                                .price(transaction.getAmount())
                                .subTotal(transaction.getAmount().multiply(new BigDecimal(transaction.getQuantity())))
                                .startDate(transaction.getStartDate().format(DATE_FORMATTER))
                                .endDate(transaction.getEndDate().format(DATE_FORMATTER))
                                .build())).toList();
    }

    private BigDecimal calculateTotalPrice(List<TransactionEntity> transactions) {
        return transactions.stream().map(TransactionEntity::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalDeposit(List<TransactionEntity> transactions) {
        return transactions.stream().map(TransactionEntity::getTotalDeposit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalShippingPrice(List<TransactionEntity> transactions) {
        return transactions.stream().map(TransactionEntity::getShippingPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ResponseEntity<ApiResponse> getShopTransactions(ShopTransactionFilterRequest filterRequest) {
        try {
            log.info("Fetching transactions for shop {} with filters: {}", filterRequest.getShopId(), filterRequest);


            Specification<TransactionEntity> spec = TransactionSpecification.withFiltersShop(filterRequest);
            List<TransactionEntity> transactions = transactionRepository.findAll(spec);

            if (transactions.isEmpty()) {
                log.info("No transactions found for Shop  {}", filterRequest.getShopId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<TransactionResponseShopDashboard> responseList = transactions.stream().map(transaction -> TransactionResponseShopDashboard.builder().referenceNumber(transaction.getTransactionNumber()).createAt(transaction.getCreatedAt().toString()).customerName(transaction.getCustomer().getName()).startDate(transaction.getStartDate().toString()).endDate(transaction.getEndDate().toString()).duration(BigDecimal.valueOf(ChronoUnit.DAYS.between(transaction.getStartDate(), transaction.getEndDate()))).status(transaction.getStatus()).depositStatus(transaction.isDepositReturned()).build()).collect(Collectors.toList());

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

            if (transaction.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, transaction);
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", transactionId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }


    public ResponseEntity<ApiResponse> getTransactionDetail(TransactionDetailRequest request) {
        try {
            log.info("Fetching transaction detail for reference: {}", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
                log.info("No transactions found with reference: {}", request.getReferenceNumber());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            boolean isValid = validateOwnership(transactions, request);
            if (!isValid) {
                log.warn("Unauthorized access to transaction reference: {}", request.getReferenceNumber());
                return commonUtils.setResponse(ErrorMessageEnum.CUSTOMER_NOT_FOUND, null);
            }

            TransactionDetailResponse response = buildTransactionDetailResponse(transactions);

            log.info("Successfully fetched transaction detail for reference: {}", request.getReferenceNumber());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Error fetching transaction detail: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private boolean validateOwnership(List<TransactionEntity> transactions, TransactionDetailRequest request) {

        if (request.getCustomerId() == null && request.getShopId() == null) {
            return true;
        }

        if (request.getCustomerId() != null) {
            return transactions.get(0).getCustomer().getId().equals(request.getCustomerId());
        }

        if (request.getShopId() != null) {
            return transactions.stream().allMatch(t -> t.getShopId().equals(request.getShopId()));
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
        return TransactionDetailResponse.TransactionDetail.builder().
                referenceNumber(transaction.getTransactionNumber())
                .status(transaction.getStatus())
                .transactionDate(transaction.getCreatedAt().format(DATE_FORMATTER))
                .shippingAddress(transaction.getShippingAddress())
                .shippingPartner(transaction.getShippingPartner())
                .shippingCode(transaction.getShippingCode())
                .build();
    }

    private List<TransactionDetailResponse.ProductDetail> buildProductDetails(List<TransactionEntity> transactions) {
        return transactions.stream()
                .flatMap(transaction -> transaction.getProducts().stream().map(product ->
                        TransactionDetailResponse.ProductDetail.builder()
                                .orderId(transaction.getId())
                                .productId(product.getId())
                                .productName(product.getName())
                                .image(product.getImage())
                                .startRentDate(transaction.getStartDate().format(DATE_FORMATTER))
                                .endRentDate(transaction.getEndDate().format(DATE_FORMATTER))
                                .quantity(transaction.getQuantity())
                                .price(transaction.getAmount())
                                .subTotal(transaction.getAmount().multiply(BigDecimal.valueOf(transaction.getQuantity())))
                                .deposit(product.getDeposit().multiply(BigDecimal.valueOf(transaction.getQuantity())))
                                .build()
                )).toList();
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
                .grandTotal(subTotal.add(transactions.get(0).getShippingPrice()).add(transactions.get(0).getServiceFee()).add(totalDeposit))
                .build();
    }

//    Ini endpoint waktu customer checkout barang dan status
    public ResponseEntity<ApiResponse> processTransaction(ProcessStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Belum Dibayar", request.getReferenceNumbers());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumberIn(request.getReferenceNumbers());

            if (transactions.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            CustomerEntity customer = transactions.getFirst().getCustomer();
            transactions.forEach(transaction -> {
                transaction.setStatus("Belum Dibayar");
                transaction.setLastUpdateAt(LocalDateTime.now());
                log.info("Sekarang lagi ID yang ini {} startnya ini {} dan end nya ini {}",transaction.getId(),transaction.getStartDate(),transaction.getEndDate());
                for (ProductEntity product : transaction.getProducts()) {

                    Optional<CartEntity> cart = cartRepository.findByCustomerIdAndProduct_IdAndStartRentDateAndEndRentDate(customer.getId(), product.getId(),transaction.getStartDate(),transaction.getEndDate());
                    if (cart.isPresent()) {
                        cartRepository.delete(cart.get());
                    }
                }
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumbers(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//    endpoint untuk user bayar. Status "Belum Dibayar" berubah menjadi "Diproses"
    public ResponseEntity<ApiResponse> paymentTransaction(PaymentStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Diproses", request.getReferenceNumbers());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumberIn(request.getReferenceNumbers());
            if (transactions.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            if (!customerRepository.existsById(request.getCustomerId())) {
                throw new DataNotFoundException("Customer not found");
            }
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Amount Must be greater than zero");
            }

            CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> {
                log.info("Customer not found with ID: {}", request.getCustomerId());
                return new DataNotFoundException("Customer not found");
            });

            if("Pintu_Sewa_Wallet".equals(request.getPaymentMethod())) {
                if (customer.getWalletAmount().compareTo(request.getAmount()) < 0) {
                    return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Insufficient balance");
                }
                customer.setWalletAmount(customer.getWalletAmount().subtract(request.getAmount()));
                customer.setLastUpdateAt(LocalDateTime.now());
                customerRepository.save(customer);
            }

            ShopEntity shop = shopRepository.findById(transactions.getFirst().getShopId()).orElseThrow(() -> {
                log.info("Shop not found with ID: {}", transactions.getFirst().getShopId());
                return new DataNotFoundException("Shop not found");
            });
            
            if("Pintu_Sewa_Wallet".equals(request.getPaymentMethod())) {
                WalletReportEntity wallet = new WalletReportEntity();
                wallet.setDescription("Pembayaran penyewaan barang - " + request.getReferenceNumbers());
                wallet.setAmount(request.getAmount());
                wallet.setType(WalletReportEntity.WalletType.CREDIT);
                wallet.setCustomerId(customer.getId());
                wallet.setCreateAt(LocalDateTime.now());
                wallet.setUpdateAt(LocalDateTime.now());
                walletReportRepository.save(wallet);
            }

            log.info("Successfully Payment Customer ID: {}", request.getCustomerId());

            for (TransactionEntity transaction : transactions) {
                transaction.setStatus("Diproses");
                transaction.setPaymentMethod(request.getPaymentMethod());
                transaction.setLastUpdateAt(LocalDateTime.now());

                for (ProductEntity product : transaction.getProducts()) {
                    if (product.getStock() < transaction.getQuantity()) {
                        return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Stock Tidak Cukup");
                    }

                    product.setStock(product.getStock() - transaction.getQuantity());
                }
            }

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumbers(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//    endpoint untuk vendor ubah status dari "Diproses" menjadi "Dikirim"
    public ResponseEntity<ApiResponse> shippingTransaction(ShippingStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Dikirim", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());
            if (transactions.isEmpty()) {
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
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

// endpoint saat user menerima barang. Status berubah menjadi "Dikirim" menjadi "Sedang Disewa"
    public ResponseEntity<ApiResponse> receiveTransaction(ReceiveStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Sedang Disewa", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }

            transactions.forEach(transaction -> {
                transaction.setStatus("Sedang Disewa");
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//endpoint saat customer mengembalikan barang. Status berubah dari "Sedang Disewa" menjadi "Dikembalikan"
    public ResponseEntity<ApiResponse> returnTransaction(ReturnStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Dikembalikan", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
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
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//endpoint saat vendor saat menerima barang yang telah dikembalikan. Status berubah menjadi "Dikembalikan" menjadi "Selesai"
    public ResponseEntity<ApiResponse> doneTransaction(DoneStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} Into Selesai", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
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

            CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> {
                log.info("Customer not found with ID: {}", request.getCustomerId());
                return new DataNotFoundException("Customer not found");
            });

            ShopEntity shop = shopRepository.findById(transactions.getFirst().getShopId()).orElseThrow(() -> {
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
            walletCustomer.setDescription("Pengembalian dana deposit dari penyedia jasa sewa - " + transactions.getFirst().getTransactionNumber());
            walletCustomer.setAmount(deposit);
            walletCustomer.setType(WalletReportEntity.WalletType.DEBIT);
            walletCustomer.setCustomerId(customer.getId());
            walletCustomer.setCreateAt(LocalDateTime.now());
            walletCustomer.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletCustomer);

            WalletReportEntity walletShop = new WalletReportEntity();
            walletShop.setDescription("Pengembalian dana deposit penyewa" + transactions.getFirst().getTransactionNumber());
            walletShop.setAmount(deposit);
            walletShop.setType(WalletReportEntity.WalletType.CREDIT);
            walletShop.setShopId(shop.getId());
            walletShop.setCreateAt(LocalDateTime.now());
            walletShop.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletShop);

            log.info("Successfully Return Deposit of Transaction ID {} ", transactions.getFirst().getTransactionNumber());

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProcessShippingDetail(String referenceNumber,String Role) {
        try {
            log.info("Get Shipping Detail From Refference Number : {} ", referenceNumber);

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(referenceNumber);

            List<FlowShippingResponse> shippingFlows = new ArrayList<>();

            FlowShippingResponse flow1 = new FlowShippingResponse();
            flow1.setProcessDate(generateRandomDateTime(transactions.getFirst().getStartDate(), 3,9,12));
            flow1.setShippingMan("Kurir " + transactions.getFirst().getShippingPartner());
            flow1.setDetail("Barang Diambil Dari Toko");
            flow1.setStatus("DIKIRIM");
            shippingFlows.add(flow1);

            FlowShippingResponse flow2 = new FlowShippingResponse();
            flow2.setProcessDate(generateRandomDateTime(transactions.getFirst().getStartDate(), 2,9,12));
            flow2.setShippingMan("Kurir " + transactions.getFirst().getShippingPartner());
            flow2.setDetail("Barang Sedang Dalam Pengiriman Ke DC "+ transactions.getFirst().getCustomer().getDistrict());
            flow2.setStatus("DIKIRIM");
            shippingFlows.add(flow2);

            FlowShippingResponse flow3 = new FlowShippingResponse();
            flow3.setProcessDate(generateRandomDateTime(transactions.getFirst().getStartDate(), 1,9,12));
            flow3.setShippingMan("Kurir " + transactions.getFirst().getShippingPartner());
            flow3.setDetail("Barang Diantarkan Ke alamat Penerima");
            flow3.setStatus("DIKIRIM");
            shippingFlows.add(flow3);

            FlowShippingResponse flow4 = new FlowShippingResponse();
            flow4.setProcessDate(generateRandomDateTime(transactions.getFirst().getStartDate(), 1,9,12));
            flow4.setShippingMan("Kurir " + transactions.getFirst().getShippingPartner());
            flow4.setDetail("Barang Telah sampai pada alamat Penerima");
            flow4.setStatus("DIKIRIM");
            shippingFlows.add(flow4);


            ProcessShippingDetailResponse response = new ProcessShippingDetailResponse();
            response.setShippingPartner(transactions.getFirst().getShippingPartner());
            response.setEstimatedTime(transactions.getFirst().getStartDate().toString());
            log.info("ini ROLE {}" , Role);
            if("Seller".equals(Role)){
                response.setShippingCode(transactions.getFirst().getReturnCode());
            }else{
                response.setShippingCode(transactions.getFirst().getShippingCode());
            }
            response.setCustomerName(transactions.getFirst().getCustomer().getName());
            response.setShippingAddress(transactions.getFirst().getShippingAddress());
            response.setShippingFlow(shippingFlows);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//endpoint saat vendor ingin membatalkan transaksi, bisa disaat status masih "Diproses"
    public ResponseEntity<ApiResponse> cancelTransaction(CancelStatusTransactionRequest request) {
        try {
            log.info("Update Reference Number status {} into Dibatalkan", request.getReferenceNumber());

            List<TransactionEntity> transactions = transactionRepository.findByTransactionNumber(request.getReferenceNumber());

            if (transactions.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Transaction not exist");
            }
            BigDecimal amount = transactions.getFirst().getTotalAmount();

            transactions.forEach(transaction -> {
                transaction.setStatus("Dibatalkan");
                transaction.setDepositReturned(true);
                transaction.setDepositReturnedAt(LocalDateTime.now());
                transaction.setLastUpdateAt(LocalDateTime.now());
            });

            CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> {
                log.info("Customer not found with ID: {}", request.getCustomerId());
                return new DataNotFoundException("Customer not found");
            });

            ShopEntity shop = shopRepository.findById(transactions.getFirst().getShopId()).orElseThrow(() -> {
                log.info("Shop not found with ID: {}", transactions.getFirst().getShopId());
                return new DataNotFoundException("Shop not found");
            });

            if (shop.getBalance().compareTo(amount) < 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Insufficient balance amount");
            }

            customer.setWalletAmount(customer.getWalletAmount().add(amount));
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);

            shop.setBalance(shop.getBalance().subtract(amount));
            shop.setLastUpdateAt(LocalDateTime.now());
            shopRepository.save(shop);

            WalletReportEntity walletCustomer = new WalletReportEntity();
            walletCustomer.setDescription("Pengembalian Dana Transaksi Dibatalkan dari Penyedia Jasa Sewa - " + transactions.getFirst().getTransactionNumber());
            walletCustomer.setAmount(amount);
            walletCustomer.setType(WalletReportEntity.WalletType.DEBIT);
            walletCustomer.setCustomerId(customer.getId());
            walletCustomer.setCreateAt(LocalDateTime.now());
            walletCustomer.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletCustomer);

            WalletReportEntity walletShop = new WalletReportEntity();
            walletShop.setDescription("Pengembalian Dana Transaksi Dibatalkan ke Penyewa" + transactions.getFirst().getTransactionNumber());
            walletShop.setAmount(amount);
            walletShop.setType(WalletReportEntity.WalletType.CREDIT);
            walletShop.setShopId(shop.getId());
            walletShop.setCreateAt(LocalDateTime.now());
            walletShop.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(walletShop);

            transactionRepository.saveAll(transactions);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Success");
        } catch (Exception ex) {
            log.error("Error fetching transaction ID {} : {}", request.getReferenceNumber(), ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private String generateRandomDateTime(LocalDate date, int minusDays,int start, int end) {
        LocalDate targetDate = date.minusDays(minusDays);
        LocalDateTime dateTime = targetDate.atTime(
                ThreadLocalRandom.current().nextInt(start, end),
                ThreadLocalRandom.current().nextInt(60),
                ThreadLocalRandom.current().nextInt(60)
        );
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
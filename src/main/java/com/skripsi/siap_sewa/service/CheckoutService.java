package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.CheckoutRequest;
import com.skripsi.siap_sewa.dto.checkout.CheckoutResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.*;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.helper.PriceCalculator;
import com.skripsi.siap_sewa.helper.ShippingCalculator;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final TransactionRepository transactionRepository;
    private final CommonUtils commonUtils;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);

    @Transactional
    public ResponseEntity<ApiResponse> checkout(CheckoutRequest request) {
        try {
            log.info("Memulai proses checkout untuk customer: {}", request.getCustomerId());
            validateCheckoutRequest(request);

            CustomerEntity customer = getCustomer(request.getCustomerId());
            List<ProductEntity> productsToCheckout = getProductsToCheckout(request);

            if (productsToCheckout.isEmpty()) {
                throw new DataNotFoundException("Tidak ada produk yang akan di-checkout");
            }

            CheckoutResponse response = processCheckout(request, customer, productsToCheckout);
            log.info("Checkout berhasil untuk customer: {} dengan {} produk",
                    request.getCustomerId(), productsToCheckout.size());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            log.warn("Data tidak ditemukan: {}", ex.getMessage());
            throw ex; // Will be handled by GlobalExceptionHandler
        } catch (CheckoutValidationException ex) {
            log.warn("Validasi checkout gagal: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal melakukan checkout: {}", ex.getMessage(), ex);
            throw new CheckoutProcessingException(
                    ErrorMessageEnum.INTERNAL_SERVER_ERROR,
                    request.getProductId(),
                    ex.getMessage());
        }
    }

    private CustomerEntity getCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new DataNotFoundException("Customer tidak ditemukan"));
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.getCartId() == null && request.getProductId() == null) {
            throw new CheckoutValidationException(
                    ErrorMessageEnum.BAD_REQUEST,
                    "Product ID atau Cart ID harus diisi");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new CheckoutValidationException(
                    ErrorMessageEnum.BAD_REQUEST,
                    "Tanggal sewa harus diisi");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new CheckoutValidationException(
                    ErrorMessageEnum.BAD_REQUEST,
                    "Tanggal mulai sewa tidak boleh sebelum hari ini");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new CheckoutValidationException(
                    ErrorMessageEnum.BAD_REQUEST,
                    "Tanggal selesai sewa tidak boleh sebelum tanggal mulai");
        }
        if (request.getQuantity() <= 0) {
            throw new CheckoutValidationException(
                    ErrorMessageEnum.BAD_REQUEST,
                    "Jumlah sewa harus lebih dari 0");
        }
    }

    private List<ProductEntity> getProductsToCheckout(CheckoutRequest request) {
        List<ProductEntity> products = new ArrayList<>();

        try {
            if (request.getCartId() != null) {
                log.debug("Processing checkout from cart: {}", request.getCartId());
                CartEntity cart = cartRepository.findById(request.getCartId())
                        .orElseThrow(() -> new DataNotFoundException("Keranjang tidak ditemukan"));
                products.add(cart.getProduct());
            } else {
                log.debug("Processing direct checkout for product: {}", request.getProductId());
                ProductEntity product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new DataNotFoundException("Produk tidak ditemukan"));
                products.add(product);
            }
        } catch (DataNotFoundException ex) {
            log.warn("Data tidak ditemukan selama checkout: {}", ex.getMessage());
            throw ex;
        }

        return products;
    }

    private CheckoutResponse processCheckout(CheckoutRequest request, CustomerEntity customer,
                                             List<ProductEntity> productsToCheckout) {


        // Group products by shop
        Map<ShopEntity, List<ProductEntity>> productsByShop = productsToCheckout.stream()
                .collect(Collectors.groupingBy(ProductEntity::getShop));

        CheckoutResponse response = CheckoutResponse.builder()
                .transactions(new ArrayList<>())
                .subTotalProductPrice(BigDecimal.ZERO)
                .subTotalShippingCost(BigDecimal.ZERO)
                .subTotalDeposit(BigDecimal.ZERO)
                .serviceFee(BigDecimal.ZERO)
                .grandTotalPayment(BigDecimal.ZERO)
                .build();

        for (Map.Entry<ShopEntity, List<ProductEntity>> entry : productsByShop.entrySet()) {
            ShopEntity shop = entry.getKey();
            List<ProductEntity> shopProducts = entry.getValue();

            log.debug("Processing checkout for shop: {} with {} products",
                    shop.getName(), shopProducts.size());

            CheckoutResponse.TransactionGroup transactionGroup = processShopProducts(
                    shop, shopProducts, customer, request);

            response.getTransactions().add(transactionGroup);

            // Accumulate totals
            response.setSubTotalProductPrice(response.getSubTotalProductPrice().add(transactionGroup.getTotalPrice()));
            response.setSubTotalShippingCost(response.getSubTotalShippingCost().add(transactionGroup.getShippingPrice()));
            response.setSubTotalDeposit(response.getSubTotalDeposit().add(transactionGroup.getDeposit()));
        }

        // Calculate service fee (5% of product subtotal)
        BigDecimal serviceFee = response.getSubTotalProductPrice()
                .multiply(BigDecimal.valueOf(0.05))
                .setScale(0, RoundingMode.HALF_UP);
        response.setServiceFee(serviceFee);
        log.debug("Service fee calculated: {}", serviceFee);

        // Calculate grand total
        BigDecimal grandTotal = response.getSubTotalProductPrice()
                .add(response.getSubTotalShippingCost())
                .add(response.getSubTotalDeposit())
                .add(response.getServiceFee());
        response.setGrandTotalPayment(grandTotal);
        log.debug("Grand total calculated: {}", grandTotal);

        return response;
    }

    private CheckoutResponse.TransactionGroup processShopProducts(
            ShopEntity shop,
            List<ProductEntity> products,
            CustomerEntity customer,
            CheckoutRequest request) {

        log.info("Processing checkout for shop: {} with {} products", shop.getName(), products.size());

        CheckoutResponse.TransactionGroup transactionGroup = CheckoutResponse.TransactionGroup.builder()
                .shopId(shop.getId())
                .shopName(shop.getName())
                .rentedItems(new ArrayList<>())
                .deposit(BigDecimal.ZERO)
                .shippingPartner("")
                .shippingPrice(BigDecimal.ZERO)
                .totalRentedProduct(0)
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal totalWeight = BigDecimal.ZERO;
        List<ProductEntity> successfulProducts = new ArrayList<>();
        List<ProductEntity> failedProducts = new ArrayList<>();

        // First pass: validate all products
        for (ProductEntity product : products) {
            try {
                validateProductForCheckout(product, request.getQuantity());
                BigDecimal productWeight = product.getWeight().multiply(BigDecimal.valueOf(request.getQuantity()));
                totalWeight = totalWeight.add(productWeight);
                successfulProducts.add(product);

                log.debug("Product validated successfully: {} (Weight: {}kg)",
                        product.getName(), productWeight);
            } catch (InsufficientStockException ex) {
                log.warn("Insufficient stock for product {}: Available {} < Requested {}",
                        product.getName(), ex.getAvailableStock(), ex.getRequestedQuantity());
                failedProducts.add(product);
                transactionGroup.getRentedItems().add(createFailedRentedItem(
                        product, request, "Stok tidak mencukupi"));
            } catch (MinimumRentNotMetException ex) {
                log.warn("Minimum rent not met for product {}: Required {} > Requested {}",
                        product.getName(), ex.getMinRent(), ex.getRequestedQuantity());
                failedProducts.add(product);
                transactionGroup.getRentedItems().add(createFailedRentedItem(
                        product, request, "Minimal sewa tidak terpenuhi"));
            } catch (Exception ex) {
                log.error("Unexpected error validating product {}: {}",
                        product.getName(), ex.getMessage(), ex);
                failedProducts.add(product);
                transactionGroup.getRentedItems().add(createFailedRentedItem(
                        product, request, "Validasi produk gagal"));
            }
        }

        if (successfulProducts.isEmpty()) {
            log.warn("Checkout failed for shop {}: No valid products found", shop.getName());
            return transactionGroup;
        }

        // Calculate shipping once per shop
        try {
            ShippingCalculator.ShippingInfo shippingInfo = ShippingCalculator.calculateShipping(
                    totalWeight, shop, customer);

            log.info("Shipping calculated for shop {}: {} (Rp{})",
                    shop.getName(), shippingInfo.partnerName(), shippingInfo.shippingPrice());

            transactionGroup.setShippingPartner(shippingInfo.partnerName());
            transactionGroup.setShippingPrice(shippingInfo.shippingPrice());

            // Second pass: create transactions for successful products
            for (ProductEntity product : successfulProducts) {
                try {
                    TransactionEntity transaction = createTransaction(
                            product, customer, shop, request, shippingInfo);

                    transactionGroup.getRentedItems().add(createSuccessRentedItem(transaction, product));
                    transactionGroup.setDeposit(transactionGroup.getDeposit()
                            .add(transaction.getTotalDeposit()));
                    transactionGroup.setTotalRentedProduct(
                            transactionGroup.getTotalRentedProduct() + transaction.getQuantity());
                    transactionGroup.setTotalPrice(transactionGroup.getTotalPrice()
                            .add(transaction.getAmount()));

                    log.info("Transaction created for product {}: reference number{}",
                            product.getName(), transaction.getTransactionNumber());

                } catch (Exception ex) {
                    log.error("Failed to create transaction for product {}: {}",
                            product.getName(), ex.getMessage(), ex);
                    transactionGroup.getRentedItems().add(createFailedRentedItem(
                            product, request, "Gagal membuat transaksi"));
                    failedProducts.add(product);
                    successfulProducts.remove(product);
                }
            }

        } catch (Exception ex) {
            log.error("Failed to calculate shipping for shop {}: {}",
                    shop.getName(), ex.getMessage(), ex);
            // Mark all successful products as failed
            for (ProductEntity product : successfulProducts) {
                transactionGroup.getRentedItems().add(createFailedRentedItem(
                        product, request, "Gagal menghitung pengiriman"));
            }
        }

        log.info("Checkout processed for shop {}: {} success, {} failed",
                shop.getName(), successfulProducts.size(), failedProducts.size());

        return transactionGroup;
    }

    private CheckoutResponse.RentedItem createFailedRentedItem(
            ProductEntity product, CheckoutRequest request, String errorMessage) {
        return CheckoutResponse.RentedItem.builder()
                .transactionId("")
                .productId(product.getId())
                .productName(product.getName())
                .price(BigDecimal.ZERO)
                .startRentDate(request.getStartDate().format(DATE_FORMATTER))
                .endRentDate(request.getEndDate().format(DATE_FORMATTER))
                .rentDuration(getDurationText(
                        request.getStartDate(),
                        request.getEndDate()))
                .quantity(request.getQuantity())
                .availableToRent(false)
                .build();
    }

    private void validateProductForCheckout(ProductEntity product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(product.getStock(), requestedQuantity);
        }
        if (requestedQuantity < product.getMinRented()) {
            throw new MinimumRentNotMetException(product.getMinRented(), requestedQuantity);
        }
    }

    private TransactionEntity createTransaction(
            ProductEntity product,
            CustomerEntity customer,
            ShopEntity shop,
            CheckoutRequest request,
            ShippingCalculator.ShippingInfo shippingInfo) {

        // Calculate rental price
        PriceCalculator.RentalPrice rentalPrice = PriceCalculator.calculateRentalPrice(
                product, request.getStartDate(), request.getEndDate());

        // Calculate deposit
        BigDecimal deposit = product.getDeposit().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Create transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .customer(customer)
                .products(Set.of(product))
                .transactionNumber(generateTransactionNumber())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .shippingAddress(customer.getStreet() + ", " + customer.getRegency() + ", " + customer.getProvince())
                .quantity(request.getQuantity())
                .amount(rentalPrice.totalPrice())
                .totalAmount(rentalPrice.totalPrice().add(deposit))
                .paymentMethod("UNPAID")
                .status("PENDING")
                .isReturn("NOT_RETURNED")
                .createdAt(LocalDate.now().atStartOfDay())
                .lastUpdateAt(LocalDate.now().atStartOfDay())
                .isSelled(false)
                .shopId(shop.getId())
                .shopName(shop.getName())
                .totalDeposit(deposit)
                .isDepositReturned(false)
                .serviceFee(rentalPrice.totalPrice().multiply(BigDecimal.valueOf(0.05)))
                .shippingPartner(shippingInfo.partnerName())
                .shippingPrice(shippingInfo.shippingPrice())
                .build();

        // Update product stock
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);
        log.debug("Product stock updated: {} (new stock: {})",
                product.getName(), product.getStock());

        return transactionRepository.save(transaction);
    }

    private CheckoutResponse.RentedItem createSuccessRentedItem(
            TransactionEntity transaction, ProductEntity product) {
        return CheckoutResponse.RentedItem.builder()
                .transactionId(transaction.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(transaction.getAmount())
                .startRentDate(transaction.getStartDate().format(DATE_FORMATTER))
                .endRentDate(transaction.getEndDate().format(DATE_FORMATTER))
                .rentDuration(getDurationText(transaction.getStartDate(), transaction.getEndDate()))
                .quantity(transaction.getQuantity())
                .availableToRent(true)
                .build();
    }

    private CheckoutResponse.RentedItem createFailedRentedItem(
            ProductEntity product, CheckoutRequest request) {
        return CheckoutResponse.RentedItem.builder()
                .transactionId("")
                .productId(product.getId())
                .productName(product.getName())
                .price(BigDecimal.ZERO)
                .startRentDate(request.getStartDate().format(DATE_FORMATTER))
                .endRentDate(request.getEndDate().format(DATE_FORMATTER))
                .rentDuration(getDurationText(
                        request.getStartDate(),
                        request.getEndDate()))
                .quantity(request.getQuantity())
                .availableToRent(false)
                .build();
    }

    private String getDurationText(LocalDate startDate, LocalDate endDate) {
        try {
            long days = ChronoUnit.DAYS.between(
                    LocalDate.parse(startDate.format(DATE_FORMATTER), DATE_FORMATTER),
                    LocalDate.parse(endDate.format(DATE_FORMATTER), DATE_FORMATTER));
            return days + (days == 1 ? " Day" : " Days");
        } catch (Exception ex) {
            log.warn("Failed to parse dates for duration: {} - {}", startDate, endDate);
            return "N/A";
        }
    }

    private String generateTransactionNumber() {
        LocalDateTime now = LocalDateTime.now();
        String timestampPart = "PS" + now.format(DateTimeFormatter.ofPattern(Constant.TRANSACTION_NUMBER_FORMAT));
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100;
        return timestampPart + randomNum;
    }
}
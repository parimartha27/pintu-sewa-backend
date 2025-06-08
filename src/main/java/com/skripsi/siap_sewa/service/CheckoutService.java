package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.CartCheckoutRequest;
import com.skripsi.siap_sewa.dto.checkout.CheckoutDetailsRequest;
import com.skripsi.siap_sewa.dto.checkout.CheckoutResponse;
import com.skripsi.siap_sewa.dto.checkout.CheckoutResultResponse;
import com.skripsi.siap_sewa.dto.checkout.ProductCheckoutRequest;
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
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ShopRepository shopRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    @Transactional
    public ResponseEntity<ApiResponse> processProductCheckout(ProductCheckoutRequest request) throws BadRequestException {
        try {
            log.info("Processing product checkout for customer: {}", request.getCustomerId());

            // Validate request
            validateCheckoutDates(request.getStartDate(), request.getEndDate());

            // Process checkout
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            // Validate stock
            if (product.getStock() < request.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            String shippingPartnerId = request.getShippingPartnerId() != null ?
                    request.getShippingPartnerId() : Constant.DEFAULT_EKSPEDISI;

            List<String> transactionIds = processCheckoutAndGetTransactionIds(
                    customer,
                    Collections.singletonList(new CartCheckoutItem(
                            product,
                            request.getQuantity(),
                            request.getStartDate(),
                            request.getEndDate()
                    )),
                    shippingPartnerId
            );

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,
                    CheckoutResultResponse.builder().transactionIds(transactionIds).build());

        } catch (Exception ex) {
            log.error("Product checkout failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public ResponseEntity<ApiResponse> processCartCheckout(CartCheckoutRequest request) throws BadRequestException {
        try {
            log.info("Processing cart checkout for {} carts, customer: {}",
                    request.getCartIds().size(), request.getCustomerId());

            // Fetch and validate carts
            List<CartEntity> carts = cartRepository.findAllById(request.getCartIds());
            validateCarts(carts, request.getCustomerId());

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            // Convert carts to checkout items with their individual properties
            List<CartCheckoutItem> checkoutItems = carts.stream()
                    .map(cart -> {
                        // Validate stock for each cart item
                        if (cart.getProduct().getStock() < cart.getQuantity()) {
                            throw new InsufficientStockException(cart.getProduct().getStock(), cart.getQuantity());
                        }

                        // Create checkout item
                        return new CartCheckoutItem(
                                cart.getProduct(),
                                cart.getQuantity(),
                                cart.getStartRentDate(),
                                cart.getEndRentDate()
                        );
                    })
                    .toList();

            String shippingPartnerId = request.getShippingPartnerId() != null ?
                    request.getShippingPartnerId() : Constant.DEFAULT_EKSPEDISI;

            // Process checkout using cart-specific data
            List<String> transactionIds = processCheckoutAndGetTransactionIds(
                    customer,
                    checkoutItems,
                    shippingPartnerId
            );

            // Hapus cart secara permanen (hard delete)
            cartRepository.deleteAll(carts);
            log.info("Deleted {} carts permanently after checkout", carts.size());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,
                    CheckoutResultResponse.builder().transactionIds(transactionIds).build());

        } catch (Exception ex) {
            log.error("Cart checkout failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<ApiResponse> getCheckoutDetails(CheckoutDetailsRequest request) {
        try {
            log.info("Fetching checkout details for {} transactions", request.getTransactionIds().size());

            List<TransactionEntity> transactions = transactionRepository.findAllById(request.getTransactionIds());

            if (transactions.isEmpty()) {
                log.info("No transactions found for the provided IDs");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            // If customerId is provided, validate that all transactions belong to this customer
            if (request.getCustomerId() != null && !request.getCustomerId().isEmpty()) {
                boolean allTransactionsBelongToCustomer = transactions.stream()
                        .allMatch(t -> t.getCustomer().getId().equals(request.getCustomerId()));

                if (!allTransactionsBelongToCustomer) {
                    log.warn("Some transactions don't belong to customer: {}", request.getCustomerId());
                    return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
                }
            }

            CheckoutResponse response = buildCheckoutResponseFromTransactions(transactions);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Error fetching checkout details: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private CheckoutResponse buildCheckoutResponseFromTransactions(List<TransactionEntity> transactions) {
        // Group transactions by shop and transaction number
        Map<String, List<TransactionEntity>> transactionsByShop = transactions.stream()
                .collect(Collectors.groupingBy(TransactionEntity::getShopId));

        List<CheckoutResponse.TransactionGroup> transactionGroups = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalShipping = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;

        for (Map.Entry<String, List<TransactionEntity>> entry : transactionsByShop.entrySet()) {
            String shopId = entry.getKey();
            List<TransactionEntity> shopTransactions = entry.getValue();
            TransactionEntity firstTransaction = shopTransactions.get(0);

            // Process each transaction in the shop
            List<CheckoutResponse.RentedItem> rentedItems = new ArrayList<>();
            BigDecimal shopTotal = BigDecimal.ZERO;
            BigDecimal shopDeposit = BigDecimal.ZERO;
            BigDecimal shopShipping = firstTransaction.getShippingPrice(); // Assuming same shipping for shop

            for (TransactionEntity transaction : shopTransactions) {
                ProductEntity product = transaction.getProducts().iterator().next();

                rentedItems.add(createRentedItem(transaction, product));
                shopTotal = shopTotal.add(transaction.getAmount());
                shopDeposit = shopDeposit.add(transaction.getTotalDeposit());

                totalServiceFee = totalServiceFee.add(transaction.getServiceFee());
            }

            transactionGroups.add(CheckoutResponse.TransactionGroup.builder()
                    .shopId(shopId)
                    .shopName(firstTransaction.getShopName())
                    .referenceNumber(firstTransaction.getTransactionNumber())
                    .rentedItems(rentedItems)
                    .deposit(shopDeposit)
                    .shippingPartner(firstTransaction.getShippingPartner())
                    .shippingPrice(shopShipping)
                    .totalRentedProduct(shopTransactions.size())
                    .totalPrice(shopTotal)
                    .build());

            grandTotal = grandTotal.add(shopTotal);
            totalDeposit = totalDeposit.add(shopDeposit);
            totalShipping = totalShipping.add(shopShipping);
        }

        return CheckoutResponse.builder()
                .transactions(transactionGroups)
                .subTotalProductPrice(grandTotal)
                .subTotalShippingCost(totalShipping)
                .subTotalDeposit(totalDeposit)
                .serviceFee(totalServiceFee)
                .grandTotalPayment(grandTotal.add(totalShipping).add(totalDeposit).add(totalServiceFee))
                .build();
    }

    private CheckoutResponse.RentedItem createRentedItem(TransactionEntity transaction, ProductEntity product) {
        return CheckoutResponse.RentedItem.builder()
                .transactionId(transaction.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(transaction.getAmount())
                .startRentDate(transaction.getStartDate().format(DATE_FORMATTER))
                .endRentDate(transaction.getEndDate().format(DATE_FORMATTER))
                .rentDuration(CommonUtils.calculateRentDuration(
                        transaction.getStartDate(),
                        transaction.getEndDate()))
                .quantity(transaction.getQuantity())
                .availableToRent(true)
                .image(product.getImage())
                .build();
    }

    private BigDecimal calculateServiceFee(BigDecimal subtotal) {
        return subtotal.multiply(BigDecimal.valueOf(0.05))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private void validateCheckoutDates(LocalDate startDate, LocalDate endDate) throws BadRequestException {
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private void validateCarts(List<CartEntity> carts, String customerId) throws BadRequestException {
        if (carts.isEmpty()) {
            throw new DataNotFoundException("No carts found");
        }
        if (carts.stream().anyMatch(c -> !c.getCustomerId().equals(customerId))) {
            throw new BadRequestException("Some carts don't belong to this customer");
        }

        // Validate all cart dates
        for (CartEntity cart : carts) {
            validateCheckoutDates(cart.getStartRentDate(), cart.getEndRentDate());
        }
    }

    private TransactionEntity createTransaction(
            ProductEntity product,
            CustomerEntity customer,
            ShopEntity shop,
            int quantity,
            LocalDate startDate,
            LocalDate endDate,
            String shippingPartner,
            String transactionNumber
    ) {
        // 1. Calculate rental price
        PriceCalculator.RentalPrice rentalPrice = PriceCalculator.calculateRentalPrice(
                product, startDate, endDate);

        // 2. Calculate deposit
        BigDecimal deposit = product.getDeposit().multiply(BigDecimal.valueOf(quantity));

        // 3. Calculate service fee
        BigDecimal serviceFee = rentalPrice.totalPrice().multiply(BigDecimal.valueOf(0.05))
                .setScale(0, RoundingMode.HALF_UP);

        // 4. Calculate shipping price
        BigDecimal shippingPrice = ShippingCalculator.calculateShipping(
                product.getWeight().multiply(BigDecimal.valueOf(quantity)),
                shop,
                customer,
                shippingPartner
        ).shippingPrice();

        // 5. Create transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .customer(customer)
                .products(Set.of(product))
                .transactionNumber(transactionNumber)
                .startDate(startDate)
                .endDate(endDate)
                .shippingAddress(customer.getStreet() + ", " + customer.getRegency() + ", " + customer.getProvince())
                .quantity(quantity)
                .amount(rentalPrice.totalPrice())
                .totalAmount(rentalPrice.totalPrice().add(deposit).add(serviceFee).add(shippingPrice))
                .paymentMethod("-")
                .isReturn("N")
                .createdAt(LocalDateTime.now())
                .lastUpdateAt(LocalDateTime.now())
                .isSelled(false)
                .shopId(shop.getId())
                .shopName(shop.getName())
                .totalDeposit(deposit)
                .isDepositReturned(false)
                .serviceFee(serviceFee)
                .shippingPartner(shippingPartner)
                .shippingPrice(shippingPrice)
                .build();

        return transactionRepository.save(transaction);
    }

    private String generateTransactionNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100; // 100-999
        return "PS" + timestamp + random;
    }

    @Transactional
    public List<String> processCheckoutAndGetTransactionIds(
            CustomerEntity customer,
            List<CartCheckoutItem> checkoutItems,
            String shippingPartnerId) {

        Map<ShopEntity, List<CartCheckoutItem>> itemsByShop = checkoutItems.stream()
                .collect(Collectors.groupingBy(item -> item.product.getShop()));

        List<String> transactionIds = new ArrayList<>();
        List<String> processedShopIds = new ArrayList<>();

        for (Map.Entry<ShopEntity, List<CartCheckoutItem>> entry : itemsByShop.entrySet()) {
            ShopEntity shop = entry.getKey();
            List<CartCheckoutItem> shopItems = entry.getValue();

            try {
                // Generate a single transaction number for all items from this shop
                String transactionNumber = generateTransactionNumber();

                for (CartCheckoutItem item : shopItems) {
                    // Validate dates for each item
                    validateCheckoutDates(item.startDate, item.endDate);

                    // Calculate shipping info for each item
                    ShippingCalculator.ShippingInfo shippingInfo = ShippingCalculator.calculateShipping(
                            item.product.getWeight().multiply(BigDecimal.valueOf(item.quantity)),
                            shop,
                            customer,
                            shippingPartnerId);

                    // Create transaction with cart-specific data
                    TransactionEntity transaction = createTransaction(
                            item.product,
                            customer,
                            shop,
                            item.quantity,
                            item.startDate,
                            item.endDate,
                            shippingInfo.partnerName(),
                            transactionNumber
                    );

                    transactionIds.add(transaction.getId());

                    // Update product stock
//                    item.product.setStock(item.product.getStock() - item.quantity);
                    productRepository.save(item.product);
                }

                // Mark shop as successfully processed
                processedShopIds.add(shop.getId());

            } catch (Exception e) {
                log.error("Failed to process items from shop {}: {}", shop.getName(), e.getMessage());

                // Rollback stock updates for this shop's items that might have been processed
                for (CartCheckoutItem item : shopItems) {
                    try {
                        ProductEntity product = productRepository.findById(item.product.getId()).orElse(null);
                        if (product != null) {
                            // Add back quantity for any products that might have been decremented
                            product.setStock(product.getStock() + item.quantity);
                            productRepository.save(product);
                        }
                    } catch (Exception ex) {
                        log.error("Error reverting stock for product {}: {}", item.product.getId(), ex.getMessage());
                    }
                }

                // Rollback transactions for this shop
                if (!processedShopIds.contains(shop.getId())) {
                    List<TransactionEntity> shopTransactions = transactionRepository.findByShopId(shop.getId());
                    for (TransactionEntity transaction : shopTransactions) {
                        if (transaction.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
                            transactionRepository.delete(transaction);
                        }
                    }
                }

                // Re-throw to fail the entire transaction from this shop
                throw new CheckoutProcessingException(ErrorMessageEnum.FAILED,"", "");
            }
        }

        return transactionIds;
    }

    // Helper class to hold checkout item details
    private static class CartCheckoutItem {
        private final ProductEntity product;
        private final int quantity;
        private final LocalDate startDate;
        private final LocalDate endDate;

        public CartCheckoutItem(ProductEntity product, int quantity, LocalDate startDate, LocalDate endDate) {
            this.product = product;
            this.quantity = quantity;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    @Transactional
    public CheckoutResponse updateShippingMethod(
            List<String> transactionIds,
            String newShippingPartner
    ) {
        // 1. Ambil transaksi yang akan diupdate
        List<TransactionEntity> transactions = transactionRepository.findAllById(transactionIds);

        // 2. Group by shop (karena kurir dipilih per toko)
        Map<String, List<TransactionEntity>> transactionsByShop = transactions.stream()
                .collect(Collectors.groupingBy(TransactionEntity::getShopId));

        // 3. Update masing-masing toko
        for (Map.Entry<String, List<TransactionEntity>> entry : transactionsByShop.entrySet()) {
            ShopEntity shop = shopRepository.findById(entry.getKey()).orElseThrow();
            List<TransactionEntity> shopTransactions = entry.getValue();

            // 4. Hitung ulang ongkir untuk semua produk di toko ini
            BigDecimal totalWeight = shopTransactions.stream()
                    .map(t -> t.getProducts().iterator().next().getWeight()
                            .multiply(BigDecimal.valueOf(t.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CustomerEntity customer = shopTransactions.get(0).getCustomer();

            ShippingCalculator.ShippingInfo newShipping = ShippingCalculator.calculateShipping(
                    totalWeight,
                    shop,
                    customer,
                    newShippingPartner
            );

            // 5. Update semua transaksi di toko ini
            for (TransactionEntity transaction : shopTransactions) {
                BigDecimal newServiceFee = transaction.getAmount().multiply(BigDecimal.valueOf(0.1));

                transaction.setShippingPartner(newShipping.partnerName());
                transaction.setShippingPrice(newShipping.shippingPrice());
                transaction.setServiceFee(newServiceFee);
                transaction.setTotalAmount(
                        transaction.getAmount()
                                .add(transaction.getTotalDeposit())
                                .add(newServiceFee)
                                .add(newShipping.shippingPrice())
                );

                transactionRepository.save(transaction);
            }
        }

        // 6. Kembalikan struktur response sama seperti GET /details
        return buildCheckoutResponseFromTransactions(transactions);
    }
}
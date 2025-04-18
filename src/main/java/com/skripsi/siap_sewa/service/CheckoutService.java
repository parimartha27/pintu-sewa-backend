package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.checkout.CartCheckoutRequest;
import com.skripsi.siap_sewa.dto.checkout.CheckoutResponse;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);

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

            CheckoutResponse response = processCheckout(
                    customer,
                    List.of(product),
                    request.getQuantity(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
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

            // Extract products from carts
            List<ProductEntity> products = carts.stream()
                    .map(CartEntity::getProduct)
                    .toList();

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            // Process checkout (using first cart's dates and quantities)
            CartEntity firstCart = carts.get(0);
            CheckoutResponse response = processCheckout(
                    customer,
                    products,
                    firstCart.getQuantity(),
                    firstCart.getStartRentDate(),
                    firstCart.getEndRentDate()
            );

            // Mark carts as deleted
            carts.forEach(cart -> {
                cart.setDeleted(true);
                cartRepository.save(cart);
            });

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Cart checkout failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private CheckoutResponse processCheckout(
            CustomerEntity customer,
            List<ProductEntity> products,
            int quantity,
            LocalDate startDate,
            LocalDate endDate) {

        // Group products by shop
        Map<ShopEntity, List<ProductEntity>> productsByShop = products.stream()
                .collect(Collectors.groupingBy(ProductEntity::getShop));

        List<CheckoutResponse.TransactionGroup> transactionGroups = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalShipping = BigDecimal.ZERO;

        for (Map.Entry<ShopEntity, List<ProductEntity>> entry : productsByShop.entrySet()) {
            ShopEntity shop = entry.getKey();
            List<ProductEntity> shopProducts = entry.getValue();

            // Calculate total weight for shipping
            BigDecimal totalWeight = shopProducts.stream()
                    .map(p -> p.getWeight().multiply(BigDecimal.valueOf(quantity)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get shipping info
            ShippingCalculator.ShippingInfo shippingInfo = ShippingCalculator.calculateShipping(
                    totalWeight, shop, customer);

            // Process each product in the shop
            List<CheckoutResponse.RentedItem> rentedItems = new ArrayList<>();
            BigDecimal shopTotal = BigDecimal.ZERO;
            BigDecimal shopDeposit = BigDecimal.ZERO;

            for (ProductEntity product : shopProducts) {
                TransactionEntity transaction = createTransaction(
                        product,
                        customer,
                        shop,
                        quantity,
                        startDate,
                        endDate,
                        shippingInfo.partnerName()
                );

                rentedItems.add(createRentedItem(transaction, product));
                shopTotal = shopTotal.add(transaction.getAmount());
                shopDeposit = shopDeposit.add(transaction.getTotalDeposit());

                // Update product stock
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
            }

            transactionGroups.add(CheckoutResponse.TransactionGroup.builder()
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .rentedItems(rentedItems)
                    .deposit(shopDeposit)
                    .shippingPartner(shippingInfo.partnerName())
                    .shippingPrice(shippingInfo.shippingPrice())
                    .totalRentedProduct(shopProducts.size())
                    .totalPrice(shopTotal)
                    .build());

            grandTotal = grandTotal.add(shopTotal);
            totalDeposit = totalDeposit.add(shopDeposit);
            totalShipping = totalShipping.add(shippingInfo.shippingPrice());
        }

        return CheckoutResponse.builder()
                .transactions(transactionGroups)
                .subTotalProductPrice(grandTotal)
                .subTotalShippingCost(totalShipping)
                .subTotalDeposit(totalDeposit)
                .serviceFee(calculateServiceFee(grandTotal))
                .grandTotalPayment(grandTotal.add(totalShipping).add(totalDeposit)
                        .add(calculateServiceFee(grandTotal)))
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
        if (carts.stream().anyMatch(CartEntity::isDeleted)) {
            throw new BadRequestException("Cannot checkout deleted carts");
        }
    }

    private TransactionEntity createTransaction(
            ProductEntity product,
            CustomerEntity customer,
            ShopEntity shop,
            int quantity,
            LocalDate startDate,
            LocalDate endDate,
            String shippingPartner
    ) {
        // 1. Calculate rental price
        PriceCalculator.RentalPrice rentalPrice = PriceCalculator.calculateRentalPrice(
                product, startDate, endDate);

        // 2. Calculate deposit
        BigDecimal deposit = product.getDeposit().multiply(BigDecimal.valueOf(quantity));

        // 3. Create transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .customer(customer)
                .products(Set.of(product))
                .transactionNumber(generateTransactionNumber())
                .startDate(startDate)
                .endDate(endDate)
                .shippingAddress(customer.getStreet() + ", " + customer.getRegency() + ", " + customer.getProvince())
                .quantity(quantity)
                .amount(rentalPrice.totalPrice())
                .totalAmount(rentalPrice.totalPrice().add(deposit).add(rentalPrice.totalPrice().multiply(BigDecimal.valueOf(0.05))))
                .paymentMethod("UNPAID")
                .status("Belum Dibayar")
                .isReturn("NOT_RETURNED")
                .createdAt(LocalDateTime.now())
                .lastUpdateAt(LocalDateTime.now())
                .isSelled(false)
                .shopId(shop.getId())
                .shopName(shop.getName())
                .totalDeposit(deposit)
                .isDepositReturned(false)
                .serviceFee(rentalPrice.totalPrice().multiply(BigDecimal.valueOf(0.05)))
                .shippingPartner(shippingPartner)
                .shippingPrice(ShippingCalculator.calculateShipping(
                        product.getWeight().multiply(BigDecimal.valueOf(quantity)),
                        shop,
                        customer
                ).shippingPrice())
                .build();

        return transactionRepository.save(transaction);
    }

    private String generateTransactionNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100; // 100-999
        return "PS" + timestamp + random;
    }
}
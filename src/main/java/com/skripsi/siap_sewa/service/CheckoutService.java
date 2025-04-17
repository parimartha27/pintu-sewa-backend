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
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    public ResponseEntity<ApiResponse> checkout(CheckoutRequest request) throws BadRequestException {
        try {
            log.info("Processing {} checkout for customer: {}",
                    request.isCartCheckout() ? "CART" : "PRODUCT_DETAIL",
                    request.getCustomerId());

            if (request.isCartCheckout()) {
                return processCartCheckout(request);
            } else if (request.isProductDetailCheckout()) {
                return processProductDetailCheckout(request);
            } else {
                throw new BadRequestException("Invalid request: Provide either cartId or product details");
            }

        } catch (Exception ex) {
            log.error("Checkout failed for customer: {} | Type: {} | Error: {}",
                    request.getCustomerId(),
                    request.isCartCheckout() ? "CART" : "PRODUCT_DETAIL",
                    ex.getMessage());
            throw ex;
        }
    }

    private ResponseEntity<ApiResponse> processCartCheckout(CheckoutRequest request) throws BadRequestException {
        // 1. Validate cart exists
        CartEntity cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new DataNotFoundException("Cart not found"));

        // 2. Validate dates (if not set in cart)
        if (cart.getStartRentDate() == null || cart.getEndRentDate() == null) {
            throw new BadRequestException("Rental dates must be set in cart");
        }

        // 3. Process checkout (single cart item)
        ProductEntity product = cart.getProduct();
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new DataNotFoundException("Customer not found"));

        // 4. Calculate shipping (if not already set in cart)
        if (cart.getShippingPartner() == null) {
            ShippingCalculator.ShippingInfo shippingInfo = ShippingCalculator.calculateShipping(
                    product.getWeight().multiply(BigDecimal.valueOf(cart.getQuantity())),
                    product.getShop(),
                    customer
            );
            cart.setShippingPartner(shippingInfo.partnerName());
            cartRepository.save(cart);
        }

        // 5. Create transaction
        TransactionEntity transaction = createTransaction(
                product,
                customer,
                product.getShop(),
                cart.getQuantity(),
                cart.getStartRentDate(),
                cart.getEndRentDate(),
                cart.getShippingPartner()
        );

        // 6. Soft delete cart
        cart.setDeleted(true);
        cartRepository.save(cart);

        // 7. Build response
        CheckoutResponse response = CheckoutResponse.builder()
                .transactions(List.of(
                        CheckoutResponse.TransactionGroup.builder()
                                .shopId(product.getShop().getId())
                                .shopName(product.getShop().getName())
                                .rentedItems(List.of(createSuccessRentedItem(transaction, product)))
                                .deposit(transaction.getTotalDeposit())
                                .shippingPartner(transaction.getShippingPartner())
                                .shippingPrice(transaction.getShippingPrice())
                                .totalRentedProduct(transaction.getQuantity())
                                .totalPrice(transaction.getAmount())
                                .build()
                ))
                .subTotalProductPrice(transaction.getAmount())
                .subTotalShippingCost(transaction.getShippingPrice())
                .subTotalDeposit(transaction.getTotalDeposit())
                .serviceFee(transaction.getServiceFee())
                .grandTotalPayment(transaction.getTotalAmount())
                .build();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    private ResponseEntity<ApiResponse> processProductDetailCheckout(CheckoutRequest request) {
        // 1. Validate product exists
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // 2. Validate customer exists
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new DataNotFoundException("Customer not found"));

        // 3. Calculate shipping
        ShippingCalculator.ShippingInfo shippingInfo = ShippingCalculator.calculateShipping(
                product.getWeight().multiply(BigDecimal.valueOf(request.getQuantity())),
                product.getShop(),
                customer
        );

        // 4. Create transaction
        TransactionEntity transaction = createTransaction(
                product,
                customer,
                product.getShop(),
                request.getQuantity(),
                request.getStartDate(),
                request.getEndDate(),
                shippingInfo.partnerName()
        );

        // 5. Build response
        CheckoutResponse response = CheckoutResponse.builder()
                .transactions(List.of(
                        CheckoutResponse.TransactionGroup.builder()
                                .shopId(product.getShop().getId())
                                .shopName(product.getShop().getName())
                                .rentedItems(List.of(createSuccessRentedItem(transaction, product)))
                                .deposit(transaction.getTotalDeposit())
                                .shippingPartner(transaction.getShippingPartner())
                                .shippingPrice(transaction.getShippingPrice())
                                .totalRentedProduct(transaction.getQuantity())
                                .totalPrice(transaction.getAmount())
                                .build()
                ))
                .subTotalProductPrice(transaction.getAmount())
                .subTotalShippingCost(transaction.getShippingPrice())
                .subTotalDeposit(transaction.getTotalDeposit())
                .serviceFee(transaction.getServiceFee())
                .grandTotalPayment(transaction.getTotalAmount())
                .build();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
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
                .status("PENDING")
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

        // 4. Update product stock
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        return transactionRepository.save(transaction);
    }

    private String generateTransactionNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100; // 100-999
        return "PS" + timestamp + random;
    }

    private CheckoutResponse.RentedItem createSuccessRentedItem(TransactionEntity transaction, ProductEntity product) {
        return CheckoutResponse.RentedItem.builder()
                .transactionId(transaction.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(transaction.getAmount())
                .startRentDate(transaction.getStartDate().format(DATE_FORMATTER))
                .endRentDate(transaction.getEndDate().format(DATE_FORMATTER))
                .rentDuration(CommonUtils.calculateRentDuration(transaction.getStartDate(), transaction.getEndDate()))
                .quantity(transaction.getQuantity())
                .availableToRent(true)
                .build();
    }
}
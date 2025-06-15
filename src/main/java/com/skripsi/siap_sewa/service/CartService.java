package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.cart.AddCartRequest;
import com.skripsi.siap_sewa.dto.cart.CartResponse;
import com.skripsi.siap_sewa.dto.cart.DeleteCartRequest;
import com.skripsi.siap_sewa.dto.cart.EditCartRequest;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CartRepository;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.helper.ProductHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CommonUtils commonUtils;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;


    public ResponseEntity<ApiResponse> getAllCartProductByCustomerId(String customerId) {
        try {
            log.info("Fetching active cart products for customer: {}", customerId);

            List<CartEntity> activeCarts = cartRepository.findByCustomerId(customerId);

            if (activeCarts.isEmpty()) {
                log.info("No active carts found for customer: {}", customerId);
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,
                        CartResponse.builder()
                                .totalProductCart(0)
                                .shops(Collections.emptyList())
                                .build()
                );
            }

            // Group by shop
            Map<ShopEntity, List<CartEntity>> cartsByShop = activeCarts.stream()
                    .collect(Collectors.groupingBy(cart -> cart.getProduct().getShop()));

            // Build shop responses and calculate total count
            List<CartResponse.ShopInfo> shopResponses = new ArrayList<>();
            int totalCartItems = 0;

            for (Map.Entry<ShopEntity, List<CartEntity>> entry : cartsByShop.entrySet()) {
                ShopEntity shop = entry.getKey();
                List<CartEntity> shopCarts = entry.getValue();

                List<CartResponse.CartInfo> cartInfos = shopCarts.stream()
                        .map(this::buildCartInfo)
                        .toList();

                shopResponses.add(CartResponse.ShopInfo.builder()
                        .shopId(shop.getId())
                        .shopName(shop.getName())
                        .carts(cartInfos)
                        .build());

                totalCartItems += shopCarts.size();
            }

            // Build final response
            CartResponse response = CartResponse.builder()
                    .totalProductCart(totalCartItems)
                    .shops(shopResponses)
                    .build();

            log.info("Successfully fetched {} shops with {} active cart items",
                    shopResponses.size(), totalCartItems);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Failed to fetch cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private CartResponse.CartInfo buildCartInfo(CartEntity cart) {
        ProductEntity product = cart.getProduct();
        boolean isAvailable = product.getStock() >= cart.getQuantity();
        LocalDate today = LocalDate.now();
        boolean dateError;
        if(ChronoUnit.DAYS.between(today, cart.getStartRentDate()) >= 5){
            dateError = false;
        }else{
            dateError = true;
        }

        return CartResponse.CartInfo.builder()
                .cartId(cart.getId())
                .productId(product.getId())
                .productName(product.getName())
                .price(ProductHelper.getLowestPrice(product))
                .startRentDate(CommonUtils.formatDate(cart.getStartRentDate()))
                .endRentDate(CommonUtils.formatDate(cart.getEndRentDate()))
                .rentDuration(CommonUtils.calculateRentDuration(
                        cart.getStartRentDate(),
                        cart.getEndRentDate()))
                .quantity(cart.getQuantity())
                .isAvailableToRent(isAvailable)
                .image(product.getImage())
                .stock(product.getStock())
                .deposit(product.getDeposit())
                .minRented(product.getMinRented())
                .rentCategory(CommonUtils.getRentDurationName(product.getRentCategory()))
                .dailyPrice(product.getDailyPrice())
                .weeklyPrice(product.getWeeklyPrice())
                .monthlyPrice(product.getMonthlyPrice())
                .dateError(dateError)
                .build();
    }

    public ResponseEntity<ApiResponse> addProductToCart(AddCartRequest request) {
        try {
            log.info("Menambahkan produk ke cart: {}", request);

            // 1. Validasi customer exists
            if (!customerRepository.existsById(request.getCustomerId())) {
                return commonUtils.setResponse(ErrorMessageEnum.CUSTOMER_NOT_FOUND, null);
            }

            // 2. Validasi produk exists
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product tidak ditemukan dengan ID: " + request.getProductId()));

            // 3. Validasi status produk
            if (!"AVAILABLE".equalsIgnoreCase(product.getStatus())) {
                return commonUtils.setResponse(ErrorMessageEnum.PRODUCT_NOT_FOUND, null);
            }

            // 4. Validasi stok tersedia
            if (product.getStock() <= 0) {
                return commonUtils.setResponse(ErrorMessageEnum.INSUFFICIENT_STOCK, null);
            }

            // 5. Cari cart item yang sudah ada dengan tanggal yang sama
            Optional<CartEntity> existingCartItem = cartRepository.findByCustomerIdAndProductIdAndStartRentDateAndEndRentDate(
                    request.getCustomerId(),
                    request.getProductId(),
                    request.getStartRentDate(),
                    request.getEndRentDate());

            if (existingCartItem.isPresent()) {
                // Jika sudah ada dengan tanggal yang sama, update quantity
                CartEntity cartItem = existingCartItem.get();
                int newQuantity = cartItem.getQuantity() + request.getQuantity();

                // Validasi quantity baru
                int maxAllowed = product.getStock();
                if (newQuantity > maxAllowed) {
                    return commonUtils.setResponse(
                            ErrorMessageEnum.MAX_QUANTITY_EXCEEDED,
                            Map.of("max_allowed", maxAllowed)
                    );
                }

                if (newQuantity < product.getMinRented()) {
                    return commonUtils.setResponse(
                            ErrorMessageEnum.MIN_RENT_NOT_MET,
                            Map.of("min_rent", product.getMinRented())
                    );
                }

                // Update quantity dan total amount
                cartItem.setQuantity(newQuantity);
                cartItem.setTotalAmount(calculateRentalPrice(
                        product,
                        request.getStartRentDate(),
                        request.getEndRentDate(),
                        newQuantity
                ));

                cartItem.setLastUpdateAt(LocalDateTime.now());

                cartRepository.save(cartItem);
            } else {
                // Jika tidak ada dengan tanggal yang sama, buat baru
                // Validasi quantity
                if (request.getQuantity() > product.getStock()) {
                    return commonUtils.setResponse(
                            ErrorMessageEnum.MAX_QUANTITY_EXCEEDED,null
                    );
                }

                if (request.getQuantity() < product.getMinRented()) {
                    return commonUtils.setResponse(
                            ErrorMessageEnum.MIN_RENT_NOT_MET,
                            Map.of("min_rent", product.getMinRented())
                    );
                }

                // Hitung total amount
                BigDecimal totalAmount = calculateRentalPrice(
                        product,
                        request.getStartRentDate(),
                        request.getEndRentDate(),
                        request.getQuantity()
                );

                CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new DataNotFoundException("Customer tidak ditemukan"));

                String customerAddress = customer.getStreet() + "," +
                        customer.getDistrict() + "," +
                        customer.getRegency() + "," +
                        customer.getProvince() + "," +
                        customer.getPostCode();

                // Buat cart item baru
                CartEntity newCartItem = CartEntity.builder()
                        .customerId(request.getCustomerId())
                        .product(product)
                        .quantity(request.getQuantity())
                        .totalAmount(totalAmount)
                        .startRentDate(request.getStartRentDate())
                        .endRentDate(request.getEndRentDate())
                        .shippingAddress(customerAddress)
                        .createdAt(LocalDateTime.now())
                        .lastUpdateAt(LocalDateTime.now())
                        .build();

                cartRepository.save(newCartItem);
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, null);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal menambahkan ke cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private BigDecimal calculateRentalPrice(ProductEntity product, LocalDate startDate,
                                            LocalDate endDate, int quantity) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal totalPrice = BigDecimal.ZERO;
        long remainingDays = totalDays;

        // Hitung bulanan
        if (remainingDays >= 30) {
            long months = remainingDays / 30;
            totalPrice = totalPrice.add(product.getMonthlyPrice().multiply(BigDecimal.valueOf(months)));
            remainingDays %= 30;
        }

        // Hitung mingguan
        if (remainingDays >= 7) {
            long weeks = remainingDays / 7;
            totalPrice = totalPrice.add(product.getWeeklyPrice().multiply(BigDecimal.valueOf(weeks)));
            remainingDays %= 7;
        }

        // Hitung harian
        if (remainingDays > 0) {
            totalPrice = totalPrice.add(product.getDailyPrice().multiply(BigDecimal.valueOf(remainingDays)));
        }

        return totalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public ResponseEntity<ApiResponse> editProductInCart(EditCartRequest request) {
        try {
            log.info("Mengedit produk di cart: {}", request);

            // 1. Cari cart item
            CartEntity cartItem = cartRepository.findById(request.getCartId())
                    .orElseThrow(() -> new DataNotFoundException("Item cart tidak ditemukan"));

            ProductEntity product = cartItem.getProduct();

            // 2. Validasi stok produk
            if (product.getStock() <= 0) {
                return commonUtils.setResponse(ErrorMessageEnum.INSUFFICIENT_STOCK, null);
            }

            // 3. Validasi quantity vs stok
            if (request.getQuantity() > product.getStock()) {
                return commonUtils.setResponse(
                        ErrorMessageEnum.MAX_QUANTITY_EXCEEDED,
                       null
                );
            }

            // 4. Validasi minRented
            if (request.getQuantity() < product.getMinRented()) {
                return commonUtils.setResponse(
                        ErrorMessageEnum.MIN_RENT_NOT_MET,
                        Map.of("min_rent", product.getMinRented())
                );
            }

            // 5. Hitung ulang total amount
            BigDecimal totalAmount = calculateRentalPrice(
                    product,
                    request.getStartRentDate(),
                    request.getEndRentDate(),
                    request.getQuantity()
            );

            // 6. Update cart item
            cartItem.setQuantity(request.getQuantity());
            cartItem.setStartRentDate(request.getStartRentDate());
            cartItem.setEndRentDate(request.getEndRentDate());
            cartItem.setTotalAmount(totalAmount);
            cartItem.setLastUpdateAt(LocalDateTime.now());

            cartRepository.save(cartItem);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,null);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal mengedit cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> deleteCartItem(DeleteCartRequest request) {
        try {
            // 1. Cari cart item
            CartEntity cartItem = cartRepository.findById(request.getCartId())
                    .orElseThrow(() -> new DataNotFoundException("Cart item dengan ID "  + request.getCartId() + "tidak ditemukan"));

            // 2. Validasi kepemilikan
            if (!cartItem.getCustomerId().equals(request.getCustomerId())) {
                return commonUtils.setResponse(
                        ErrorMessageEnum.UNAUTHORIZED_CART_ACCESS,
                        null
                );
            }

            // 3. Hapus dari database (hard delete)
            cartRepository.delete(cartItem);
            log.info("Cart {} berhasil dihapus secara permanen", request.getCartId());

            return commonUtils.setResponse(
                    ErrorMessageEnum.SUCCESS,
                    null
            );

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal menghapus cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(
                    ErrorMessageEnum.INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }
}

package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.cart.AddCartRequest;
import com.skripsi.siap_sewa.dto.cart.CartResponse;
import com.skripsi.siap_sewa.dto.cart.EditCartRequest;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CartRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ObjectMapper objectMapper;
    private final CommonUtils commonUtils;
    private final ProductRepository productRepository;

    public ResponseEntity<ApiResponse> getAllCartProductByCustomerId(String customerId) {
        try {
            log.info("Mengambil semua produk cart untuk customer: {}", customerId);
            List<CartEntity> listCart = cartRepository.findByCustomerId(customerId);

            List<CartResponse> cartResponses = listCart.stream().map(cart -> {
                ProductEntity product = cart.getProduct();

                ProductResponse productResponse = ProductResponse.builder()
                        .name(product.getName())
                        .category(product.getCategory())
                        .rentCategory(product.getRentCategory())
                        .isRnb(product.isRnb())
                        .weight(product.getWeight())
                        .height(product.getHeight())
                        .width(product.getWidth())
                        .length(product.getLength())
                        .dailyPrice(product.getDailyPrice())
                        .weeklyPrice(product.getWeeklyPrice())
                        .monthlyPrice(product.getMonthlyPrice())
                        .description(product.getDescription())
                        .conditionDescription(product.getConditionDescription())
                        .stock(product.getStock())
                        .status(product.getStatus())
                        .image(product.getImage())
                        .build();

                return CartResponse.builder()
                        .product(productResponse)
                        .quantity(cart.getQuantity())
                        .totalAmount(cart.getTotalAmount())
                        .startRentDate(cart.getStartRentDate())
                        .endRentDate(cart.getEndRentDate())
                        .build();
            }).toList();

            log.info("Berhasil mengambil {} item cart", cartResponses.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, cartResponses);

        } catch (Exception ex) {
            log.error("Gagal mengambil cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> addProductToCart(@Valid AddCartRequest request) {
        try {
            log.info("Menambahkan produk ke cart: {}", request);

            Optional<ProductEntity> productToCart = productRepository.findById(request.getProductId());

            if(productToCart.isEmpty()){
                log.warn("Produk tidak ditemukan: {}", request.getProductId());
                throw new DataNotFoundException("Product dengan ID: " + request.getProductId() + " tidak ada");
            }

            // Cek apakah produk sudah ada di cart
            boolean existsInCart = cartRepository.existsByCustomerIdAndProductId(
                    request.getCustomerId(),
                    request.getProductId()
            );

            if (existsInCart) {
                log.warn("Produk sudah ada di cart");
                return commonUtils.setResponse(
                        ErrorMessageEnum.CART_ITEM_EXISTS,
                        null
                );
            }

            CartEntity newCartItem = new CartEntity();
            newCartItem.setCustomerId(request.getCustomerId());
            newCartItem.setProduct(productToCart.get());
            newCartItem.setQuantity(request.getQuantity());
            newCartItem.setTotalAmount(request.getTotalAmount());
            newCartItem.setStartRentDate(request.getStartRentDate());
            newCartItem.setEndRentDate(request.getEndRentDate());
            newCartItem.setShippingAddress(request.getShippingAddress());

            cartRepository.save(newCartItem);
            log.info("Berhasil menambahkan produk ke cart");

            CartResponse response = objectMapper.convertValue(newCartItem, CartResponse.class);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal menambahkan ke cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editProductInCart(@Valid EditCartRequest request) {
        try {
            log.info("Mengedit produk di cart: {}", request);

            Optional<ProductEntity> productToCart = productRepository.findById(request.getProductId());

            if(productToCart.isEmpty()){
                log.warn("Produk tidak ditemukan: {}", request.getProductId());
                throw new DataNotFoundException("Product dengan ID: " + request.getProductId() + " tidak ada");
            }

            ProductEntity productToEdit = productToCart.get();

            if(request.getQuantity() > productToEdit.getStock()){
                log.warn("Stok tidak cukup");
                return commonUtils.setResponse(
                        ErrorMessageEnum.INSUFFICIENT_STOCK,
                        null
                );
            }

            Optional<CartEntity> existingCartItem = cartRepository.findByCustomerIdAndProductId(
                    request.getCustomerId(),
                    request.getProductId()
            );

            if (existingCartItem.isEmpty()) {
                log.warn("Item cart tidak ditemukan");
                throw new DataNotFoundException("Item tidak ditemukan di keranjang");
            }

            CartEntity cartItem = existingCartItem.get();
            cartItem.setQuantity(request.getQuantity());
            cartItem.setStartRentDate(request.getStartRentDate());
            cartItem.setEndRentDate(request.getEndRentDate());
            cartItem.setShippingAddress(request.getShippingAddress());

            cartRepository.save(cartItem);
            log.info("Berhasil mengupdate cart");

            CartResponse response = objectMapper.convertValue(cartItem, CartResponse.class);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal mengedit cart: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}

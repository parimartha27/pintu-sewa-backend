package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.cart.AddCartRequest;
import com.skripsi.siap_sewa.dto.cart.CartResponse;
import com.skripsi.siap_sewa.dto.cart.EditCartRequest;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CartRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.ProductUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

            if (listCart.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, Collections.emptyList());
            }

            // Group by shop
            Map<ShopEntity, List<CartEntity>> cartsByShop = listCart.stream()
                    .collect(Collectors.groupingBy(cart -> cart.getProduct().getShop()));

            List<CartResponse> cartResponses = cartsByShop.entrySet().stream()
                    .map(entry -> {
                        ShopEntity shop = entry.getKey();
                        List<CartEntity> shopCarts = entry.getValue();

                        List<CartResponse.CartInfo> cartInfos = shopCarts.stream()
                                .map(cart -> {
                                    ProductEntity product = cart.getProduct();

                                    return CartResponse.CartInfo.builder()
                                            .productId(product.getId())
                                            .productName(product.getName())
                                            .price(ProductUtils.getLowestPrice(product))
                                            .startRentDate(CommonUtils.formatDate(cart.getStartRentDate()))
                                            .endRentDate(CommonUtils.formatDate(cart.getEndRentDate()))
                                            .rentDuration(CommonUtils.calculateRentDuration(
                                                    cart.getStartRentDate(),
                                                    cart.getEndRentDate()))
                                            .quantity(cart.getQuantity())
                                            .build();
                                })
                                .toList();

                        return CartResponse.builder()
                                .shopId(shop.getId())
                                .shopName(shop.getName())
                                .carts(cartInfos)
                                .build();
                    })
                    .toList();

            log.info("Berhasil mengambil {} toko dengan total {} item cart",
                    cartResponses.size(), listCart.size());
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

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ObjectMapper objectMapper;
    private final CommonUtils commonUtils;
    private final ProductRepository productRepository;

    public ResponseEntity<ApiResponse> getAllCartProductByCustomerId(String customerId) {
        List<CartEntity> listCart = cartRepository.findByCustomerId(customerId);

        List<CartResponse> cartResponses = listCart.stream().map(cart -> {
            ProductEntity product = cart.getProduct();

//            mapping product
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

//            mapping cart
            return CartResponse.builder()
                    .product(productResponse)
                    .quantity(cart.getQuantity())
                    .totalAmount(cart.getTotalAmount())
                    .startRentDate(cart.getStartRentDate())
                    .endRentDate(cart.getEndRentDate())
                    .build();
        }).toList();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, cartResponses);
    }


    public ResponseEntity<ApiResponse> addProductToCart(@Valid AddCartRequest request) {
        
        Optional<ProductEntity> productToCart = productRepository.findById(request.getProductId());

        if(productToCart.isEmpty()){
            throw new DataNotFoundException("Product dengan ID: " + request.getProductId() + " tidak ada");
        }

        CartEntity newCartItem = new CartEntity();
        newCartItem.setCustomerId(request.getCustomerId());
        newCartItem.setProduct(productToCart.get());
        newCartItem.setQuantity(request.getQuantity());
        newCartItem.setTotalAmount(request.getTotalAmount());
        newCartItem.setStartRentDate(request.getStartRentDate());
        newCartItem.setEndRentDate(request.getEndRentDate());
        newCartItem.setShippingAddress(request.getShippingAddress());

        CartResponse response = objectMapper.convertValue(newCartItem, CartResponse.class);
        cartRepository.save(newCartItem);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> editProductInCart(@Valid EditCartRequest request) {
        Optional<ProductEntity> productToCart = productRepository.findById(request.getProductId());

        if(productToCart.isEmpty()){
            throw new DataNotFoundException("Product dengan ID: " + request.getProductId() + " tidak ada");
        }

        ProductEntity productToEdit = productToCart.get();

        if(request.getQuantity() > productToEdit.getStock()){
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Stock tidak cukup");
        }

        CartEntity newCartItem = new CartEntity();
        newCartItem.setCustomerId(request.getCustomerId());
        newCartItem.setProduct(productToEdit);
        newCartItem.setQuantity(request.getQuantity());
        newCartItem.setStartRentDate(request.getStartRentDate());
        newCartItem.setEndRentDate(request.getEndRentDate());
        newCartItem.setShippingAddress(request.getShippingAddress());

        CartResponse response = objectMapper.convertValue(newCartItem, CartResponse.class);
        cartRepository.save(newCartItem);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }
}

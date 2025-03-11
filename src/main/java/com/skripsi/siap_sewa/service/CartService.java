package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.repository.CartRepository;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public ResponseEntity<ApiResponse> getAllCartItemByCustomerId(String customerId) {
        List<CartEntity> cartRepository.findByCustomerId(customerId);
    }
}

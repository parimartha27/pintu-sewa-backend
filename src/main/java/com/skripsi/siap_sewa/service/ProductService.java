package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductEntity> getProducts() {
        return productRepository.findAll();
    }
}

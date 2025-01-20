package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper mapper;

    public List<ProductResponse> getProducts() {
        List<ProductEntity> listProduct = productRepository.findAll();

        List<ProductResponse> responses = mapper.convertValue(listProduct, new TypeReference<List<ProductResponse>>() {});

        return responses;
    }
}

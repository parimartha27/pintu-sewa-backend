package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.product.AddProductRequest;
import com.skripsi.siap_sewa.dto.product.AddProductResponse;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper mapper;
    private final CommonUtils commonUtils;

    public List<ProductResponse> getProducts() {
        List<ProductEntity> listProduct = productRepository.findAll();

        List<ProductResponse> responses = mapper.convertValue(listProduct, new TypeReference<List<ProductResponse>>() {});

        return responses;
    }

    public ResponseEntity<ApiResponse> getProductDetail(String id) {
       Optional<ProductEntity> entity = productRepository.findById(id);

       if(entity.isEmpty()){
           return commonUtils.setResponse(ErrorMessageEnum.FAILED, null);
       }

       ProductResponse response = mapper.convertValue(entity, ProductResponse.class);

       return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public AddProductResponse insertProduct (AddProductRequest request){

        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getShopId());

        ProductEntity entity = ProductEntity.builder()
                .name(request.getName())
                .category(request.getCategory())
                .rentCategory(request.getRentCategory())
                .isCorenting(request.isCorenting())
                .isRnb(request.isRnb())
                .weight(request.getWeight())
                .height(request.getHeight())
                .width(request.getWidth())
                .length(request.getLength())
                .dailyPrice(request.getDailyPrice())
                .weeklyPrice(request.getWeeklyPrice())
                .monthlyPrice(request.getMonthlyPrice())
                .description(request.getDescription())
                .conditionDescription(request.getConditionDescription())
                .stock(request.getStock())
                .status(request.getStatus())
                .image(request.getImage())
                .createdAt(LocalDateTime.now())
                .lastUpdateAt(LocalDateTime.now())
                .shop(shopEntity.orElse(null))
                .build();

        productRepository.save(entity);

        return new AddProductResponse();
    }
}

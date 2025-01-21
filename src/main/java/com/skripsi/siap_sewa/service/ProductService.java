package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.AddProductRequest;
import com.skripsi.siap_sewa.dto.AddProductResponse;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Locale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper mapper;
    private final CommonUtils commonUtils;

    public List<ProductResponse> getProducts() {
        List<ProductEntity> listProduct = productRepository.findAll();

        List<ProductResponse> responses = mapper.convertValue(listProduct, new TypeReference<List<ProductResponse>>() {});

        return responses;
    }

    public ResponseEntity<ApiResponse> getProductDetail(String slug) {
       Optional<ProductEntity> entity = productRepository.findBySlug(slug);

       if(entity.isEmpty()){
           return commonUtils.setResponse(ErrorMessageEnum.FAILED, null);
       }

       ProductResponse response = mapper.convertValue(entity, ProductResponse.class);

       return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public AddProductResponse addProduct(AddProductRequest request){

        String slug = generateSlug(request.getName());

        ProductEntity entity = ProductEntity.builder()
                .name(request.getName())
                .priceInDay(request.getPriceInDay())
                .priceInWeek(request.getPriceInWeek())
                .priceInMonth(request.getPriceInMonth())
                .stock(request.getStock())
                .isRentToBuy(request.getIsRentToBuy())
                .minimumRentDay(request.getMinimumRentDay())
                .minimumRentQuantity(request.getMinimumRentQuantity())
                .maxQuantityToRent(request.getMaxQuantityToRent())
                .image(request.getImage())
                .description(request.getDescription())
                .slug(slug)
                .insertDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        productRepository.save(entity);

        return new AddProductResponse(slug);
    }

    private String generateSlug(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return null;
        }

        String normalizedString = Normalizer.normalize(productName, Normalizer.Form.NFD);
        String sanitizedString = normalizedString.replaceAll("[^\\p{ASCII}]", "");

        String slug = sanitizedString
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "-")
                .replaceAll("[^a-z0-9-]", "");

        return slug + "-" + generateHash(productName);
    }

    private  String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder hashString = new StringBuilder();
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }
            return hashString.toString().substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}

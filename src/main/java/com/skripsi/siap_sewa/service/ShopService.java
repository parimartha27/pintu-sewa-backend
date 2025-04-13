package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.dto.shop.*;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import com.skripsi.siap_sewa.utils.ProductUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    
    private final CommonUtils utils;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;

    public ResponseEntity<ApiResponse> createShop(CreateShopRequest request) {
        
        Optional<CustomerEntity> customer = customerRepository.findById(request.getCustomerId());
        
        if(customer.isEmpty()){
            return utils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer not exist");
        }

        boolean isShopNameExist = shopRepository.existsByName(request.getName());

        if(isShopNameExist){
            return utils.setResponse(ErrorMessageEnum.FAILED, "Shop name already exist");
        }
        
        CustomerEntity shopOwner = customer.get();
        
        ShopEntity newShop = new ShopEntity();
        
        if(request.isSameAddress()){
            newShop.setStreet(shopOwner.getStreet());
            newShop.setDistrict(shopOwner.getDistrict());
            newShop.setRegency(shopOwner.getRegency());
            newShop.setProvince(shopOwner.getProvince());
            newShop.setPostCode(shopOwner.getPostCode());
        }
        else{
            newShop.setStreet(request.getStreet());
            newShop.setDistrict(request.getDistrict());
            newShop.setRegency(request.getRegency());
            newShop.setProvince(request.getProvince());
            newShop.setPostCode(request.getPostCode());
        }
        
        newShop.setName(request.getName());
        newShop.setEmail(request.getEmail());
        newShop.setShopStatus("ACTIVE");
        newShop.setCustomer(shopOwner);
        newShop.setCreatedAt(LocalDateTime.now());
        newShop.setLastUpdateAt(LocalDateTime.now());
        
        shopRepository.save(newShop);

        CreateShopResponse response = objectMapper.convertValue(newShop, CreateShopResponse.class);

        emailService.sendEmail(response.getEmail(), Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateEmailShop(response.getName()));
        
        return utils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> shopDetail(String shopId) {
        log.info("Start find shop with ID: {}", shopId);
        try {
            ShopEntity shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new DataNotFoundException("Toko tidak ditemukan"));

            ShopDetailResponse response = modelMapper.map(shop, ShopDetailResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop details: {}", ex.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editShop(@Valid EditShopRequest request) {
        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getId());

        if(shopEntity.isPresent()) {
            ShopEntity updatedShop = getShopEntity(request, shopEntity);

            shopRepository.save(updatedShop);

            EditShopResponse response = objectMapper.convertValue(updatedShop, EditShopResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    private ShopEntity getShopEntity(EditShopRequest request, Optional<ShopEntity> shopEntity) {
        ShopEntity updatedShop = shopEntity.get();

        updatedShop.setName(request.getName());
        updatedShop.setDescription(request.getDescription());
        updatedShop.setImage(request.getImage());
        updatedShop.setStreet(request.getStreet());
        updatedShop.setDistrict(request.getDistrict());
        updatedShop.setRegency(request.getRegency());
        updatedShop.setProvince(request.getProvince());
        updatedShop.setPostCode(request.getPostCode());
        return updatedShop;
    }

    public ResponseEntity<ApiResponse> getShopDataByProductId(String productId) {
        try {
            log.info("Fetching shop data for product ID: {}", productId);

            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        log.info("Product not found with ID: {}", productId);
                        return new DataNotFoundException("Product not found");
                    });

            ShopEntity shop = product.getShop();
            if (shop == null) {
                log.info("No shop found for product ID: {}", productId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ReviewEntity> allShopReviews = shop.getProducts().stream()
                    .flatMap(p -> p.getReviews().stream())
                    .toList();

            Double shopRating = ProductUtils.calculateWeightedRating(allShopReviews);
            int totalReview = allShopReviews.size();

            ShopByProductResponse response = ShopByProductResponse.builder()
                    .id(shop.getId())
                    .name(shop.getName())
                    .image(shop.getImage())
                    .rating(shopRating)
                    .totalReview(totalReview)
                    .regency(shop.getRegency())
                    .build();

            log.info("Successfully fetched shop data for product ID: {}", productId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop data for product ID {}: {}", productId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}

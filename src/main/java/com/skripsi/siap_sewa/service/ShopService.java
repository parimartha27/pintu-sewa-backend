package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.shop.*;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import com.skripsi.siap_sewa.utils.ProductUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final TransactionRepository transactionRepository;
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
        try {
            log.info("Fetching shop details for ID: {}", shopId);

            ShopEntity shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> {
                        log.warn("Shop not found with ID: {}", shopId);
                        return new DataNotFoundException("Shop not found");
                    });

            ShopDetailResponse response = mapShopToDetailResponse(shop);

            log.debug("Successfully fetched shop details for ID: {}", shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop details for ID {}: {}", shopId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ShopDetailResponse mapShopToDetailResponse(ShopEntity shop) {
        ShopDetailResponse response = modelMapper.map(shop, ShopDetailResponse.class);

        // Set customer ID
        response.setCustomerId(shop.getCustomer().getId());

        // Get all reviews from all products
        List<ReviewEntity> allReviews = shop.getProducts().stream()
                .flatMap(p -> p.getReviews().stream())
                .collect(Collectors.toList());

        // Calculate shop rating (median of all product ratings)
        response.setRating(ProductUtils.calculateMedianRating(allReviews));

        // Count total unique reviewers
        response.setTotalReviewedTimes((int) ProductUtils.countUniqueReviewers(shop.getProducts()));

        // Map products
        response.setProducts(mapProductsToProductInfo(shop.getProducts()));

        return response;
    }

    private List<ShopDetailResponse.ProductInfo> mapProductsToProductInfo(List<ProductEntity> products) {
        return products.stream().map(product -> {
            ShopDetailResponse.ProductInfo productInfo = modelMapper.map(product, ShopDetailResponse.ProductInfo.class);

            // Set product rating (median)
            productInfo.setRating(ProductUtils.calculateMedianRating(product.getReviews()));

            // Set transaction counts
            List<TransactionEntity> transactions = transactionRepository.findByProductId(product.getId());
            int[] transactionCounts = ProductUtils.countProductTransactions(transactions);
            productInfo.setRentedTimes(transactionCounts[0]);
            productInfo.setBuyTimes(transactionCounts[1]);

            return productInfo;
        }).collect(Collectors.toList());
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
}

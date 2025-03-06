package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.CreateShopRequest;
import com.skripsi.siap_sewa.dto.shop.CreateShopResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopResponse;
import com.skripsi.siap_sewa.dto.shop.ShopResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopService {
    
    private final CommonUtils utils;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final CommonUtils commonUtils;

    public ResponseEntity<ApiResponse> createShop(CreateShopRequest request) {
        
        Optional<CustomerEntity> customer = customerRepository.findById(request.getCustomerId());
        
        if(customer.isEmpty()){
            return utils.setResponse(ErrorMessageEnum.FAILED, "Customer is Invalid");
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
        newShop.setShopStatus(1);
        newShop.setCustomer(shopOwner);
        newShop.setCreatedAt(LocalDateTime.now());
        newShop.setLastUpdateAt(LocalDateTime.now());
        
        shopRepository.save(newShop);

        CreateShopResponse response = objectMapper.convertValue(newShop, CreateShopResponse.class);
        
        return utils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> shopDetail(String shopId) {

        Optional<ShopEntity> optionalShop = shopRepository.findById(shopId);

        if(optionalShop.isPresent()) {
            ShopEntity shop = optionalShop.get();
            ShopResponse response = objectMapper.convertValue(shop, ShopResponse.class);

            return utils.setResponse(ErrorMessageEnum.SUCCESS,response);
        }

        return utils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND,null);
    }

    public ResponseEntity<ApiResponse> editShop(@Valid EditShopRequest request) {
        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getId());

        if(shopEntity.isPresent()) {
            ShopEntity updatedShop = shopEntity.get();

            updatedShop.setName(request.getName());
            updatedShop.setDescription(request.getDescription());
            updatedShop.setInstagram(request.getInstagram());
            updatedShop.setFacebook(request.getFacebook());
            updatedShop.setImage(request.getImage());
            updatedShop.setStreet(request.getStreet());
            updatedShop.setDistrict(request.getDistrict());
            updatedShop.setRegency(request.getRegency());
            updatedShop.setProvince(request.getProvince());
            updatedShop.setPostCode(request.getPostCode());

            shopRepository.save(updatedShop);

            EditShopResponse response = objectMapper.convertValue(updatedShop, EditShopResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }
}

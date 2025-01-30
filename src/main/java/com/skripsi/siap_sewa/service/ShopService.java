package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.CreateShopRequest;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;

    public String createShop(CreateShopRequest request) {

        Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getCustomerId());

        ShopEntity shopEntity = ShopEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .instagram(request.getInstagram())
                .facebook(request.getFacebook())
                .shopStatus(request.getShopStatus())
                .shopLocation(request.getShopLocation())
                .province(request.getProvince())
                .city(request.getCity())
                .postCode(request.getPostCode())
                .subDistrict(request.getSubDistrict())
                .createdAt(LocalDateTime.now())
                .lastUpdateAt(LocalDateTime.now())
                .customer(customerEntity.orElse(null))
                .build();

        shopRepository.save(shopEntity);

        return "berhasil";
    }
}

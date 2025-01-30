package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.AddCustomerRequest;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public String addCustomer(AddCustomerRequest request) {
        CustomerEntity customerEntity = CustomerEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .address(request.getAddress())
                .city(request.getCity())
                .province(request.getProvince())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .postCode(request.getPostCode())
                .createdAt(LocalDateTime.now())
                .lastUpdateAt(LocalDateTime.now())
                .build();

        customerRepository.save(customerEntity);

        return "berhasil";
    }
}

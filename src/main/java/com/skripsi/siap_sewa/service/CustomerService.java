package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.AddCustomerRequest;
import com.skripsi.siap_sewa.dto.EditCustomerRequest;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public String editCustomerData(EditCustomerRequest request) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());

        if (!customerEntity.isPresent()) {
            return "Data tidak ada";
        }

        CustomerEntity editedCustomerData = customerEntity.get();

        editedCustomerData.setName(request.getName());
        editedCustomerData.setEmail(request.getEmail());
        editedCustomerData.setPassword(request.getPassword());
        editedCustomerData.setAddress(request.getAddress());
        editedCustomerData.setCity(request.getCity());
        editedCustomerData.setProvince(request.getProvince());
        editedCustomerData.setGender(request.getGender());
        editedCustomerData.setBirthDate(request.getBirthDate());
        editedCustomerData.setPostCode(request.getPostCode());
        editedCustomerData.setLastUpdateAt(LocalDateTime.now());

        customerRepository.save(editedCustomerData);
        return "Berhasil edit data";
    }
    
}

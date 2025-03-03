package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;

    public ResponseEntity<ApiResponse> getCustomerDetails(String id) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(id);
        if(customerEntity.isEmpty()){
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }else{
            CustomerEntity data = customerEntity.get();
            EditCustomerResponse response = objectMapper.convertValue(data, EditCustomerResponse.class);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }
    }

    public ResponseEntity<ApiResponse> editCustomerData(EditCustomerRequest request) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());

        if (customerEntity.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }
        else {
            CustomerEntity editedCustomerData = customerEntity.get();

            boolean isValidUsername = validateSameUsername(request.getUsername());

            if(!isValidUsername){
                return commonUtils.setResponse("SIAP-SEWA-02-001", "Username Already Exists", HttpStatus.OK, null);
            }else{
//                personal information
                editedCustomerData.setUsername(request.getUsername());
                editedCustomerData.setName(request.getName());
                if(request.getEmail() != null){
                    editedCustomerData.setEmail(request.getEmail());
                }
                if(request.getPhoneNumber() != null){
                    editedCustomerData.setPhoneNumber(request.getPhoneNumber());
                }
                editedCustomerData.setGender(request.getGender());
                editedCustomerData.setBirthDate(request.getBirthDate());
//                Address
                editedCustomerData.setStreet(request.getStreet());
                editedCustomerData.setDistrict(request.getDistrict());
                editedCustomerData.setRegency(request.getRegency());
                editedCustomerData.setProvince(request.getProvince());
                editedCustomerData.setPostCode(request.getPostCode());
                editedCustomerData.setLastUpdateAt(LocalDateTime.now());

                customerRepository.save(editedCustomerData);

                EditCustomerResponse response = objectMapper.convertValue(editedCustomerData, EditCustomerResponse.class);
                response.setEmail(editedCustomerData.getEmail());
                response.setPhoneNumber(editedCustomerData.getPhoneNumber());

                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
            }
        }
    }

    private boolean validateSameUsername (String username){
        List<CustomerEntity> customerWithSameUsername = customerRepository.findByUsername(username);

       return customerWithSameUsername.size() == 1 ? true : false;
    }



}

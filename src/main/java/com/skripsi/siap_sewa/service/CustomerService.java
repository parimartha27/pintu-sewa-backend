package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.customer.*;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final JWTService jwtService;

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

    public ResponseEntity<ApiResponse> inputCustomerData (CreateNewCustomerRequest request) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());

        if (customerEntity.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }
        else {
            CustomerEntity inputCustomerData = customerEntity.get();

            boolean isValidUsername = customerRepository.existsByUsername(request.getUsername());

            if(isValidUsername){
                return commonUtils.setResponse("SIAP-SEWA-02-001", "Please use other username", HttpStatus.OK, null);
            }else{
//                personal information
                inputCustomerData.setUsername(request.getUsername());
                inputCustomerData.setName(request.getName());
                if(request.getEmail() != null){
                    inputCustomerData.setEmail(request.getEmail());
                }
                if(request.getPhoneNumber() != null){
                    inputCustomerData.setPhoneNumber(request.getPhoneNumber());
                }
                inputCustomerData.setGender(request.getGender());
                inputCustomerData.setBirthDate(request.getBirthDate());
                inputCustomerData.setPassword(encoder.encode(request.getPassword()));
                inputCustomerData.setStatus("ACTIVE");

//                OTP
                inputCustomerData.setOtp(null);
                inputCustomerData.setVerifyCount(0);
                inputCustomerData.setResendOtpCount(0);

//                Address
                inputCustomerData.setStreet(request.getStreet());
                inputCustomerData.setDistrict(request.getDistrict());
                inputCustomerData.setRegency(request.getRegency());
                inputCustomerData.setProvince(request.getProvince());
                inputCustomerData.setPostCode(request.getPostCode());
                inputCustomerData.setLastUpdateAt(LocalDateTime.now());

                customerRepository.save(inputCustomerData);

                EditCustomerResponse response = EditCustomerResponse.builder()
                        .customerId(inputCustomerData.getId())
                        .username(inputCustomerData.getUsername())
                        .phoneNumber(inputCustomerData.getPhoneNumber())
                        .email(inputCustomerData.getEmail())
                        .status(inputCustomerData.getStatus())
                        .token(jwtService.generateToken(new CustomerPrincipal(inputCustomerData)))
                        .duration(1800)
                        .build();
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
            }
        }
    }

    public ResponseEntity<ApiResponse> editCustomerData(EditCustomerRequest request) {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());

        if (customerEntity.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }
        else {
            CustomerEntity editedCustomerData = customerEntity.get();
//          Personal Information
            editedCustomerData.setGender(request.getGender());
            editedCustomerData.setBirthDate(request.getBirthDate());
            editedCustomerData.setPassword(encoder.encode(request.getPassword()));

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

    public ResponseEntity<ApiResponse> validateCredential(@Valid ValidateCredentialRequest request) {
        boolean isCustomerValid = customerRepository.existsByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail());

        if(isCustomerValid){
            Optional<CustomerEntity> validCustomer = customerRepository.findByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail());
            ValidateCredentialResponse response = ValidateCredentialResponse.builder()
                    .customerId(validCustomer.get().getId())
                    .build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "User not exist");
    }

    public ResponseEntity<ApiResponse> forgetPassword(@Valid ForgetPasswordRequest request) {
        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(request.getCustomerId());

        if(optionalCustomer.isEmpty()){
            throw new DataNotFoundException("Customer with: " + request + " is not available");
        }

        CustomerEntity updatedPassword = optionalCustomer.get();
        updatedPassword.setPassword(encoder.encode(request.getPassword()));

        customerRepository.save(updatedPassword);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Change password success");
    }
}

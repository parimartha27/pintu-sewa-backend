package com.skripsi.siap_sewa.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.LoginRequest;
import com.skripsi.siap_sewa.dto.authentication.LoginResponse;
import com.skripsi.siap_sewa.dto.authentication.RegisterRequest;
import com.skripsi.siap_sewa.dto.authentication.RegisterResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final CustomerRepository customerRepository;
    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<ApiResponse> register(RegisterRequest request){
       List<CustomerEntity> listCustomerWithSameEmailAndPhoneNumber = customerRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

       if(listCustomerWithSameEmailAndPhoneNumber.size() > 1) {
           return commonUtils.setResponse("SIAP-SEWA-01-001", "You have been registered. Please user other email", HttpStatus.OK, null);
       }
       else{
           CustomerEntity entity = new CustomerEntity();
           entity.setEmail(request.getEmail());
           entity.setPhoneNumber(request.getPhoneNumber());
           entity.setPassword(encoder.encode(request.getPassword()));
           entity.setUsername(generateTemporaryUserName(request));

           customerRepository.save(entity);

           RegisterResponse response = objectMapper.convertValue(entity, RegisterResponse.class);

           return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
       }
    }

    public ResponseEntity<ApiResponse> login(@Valid LoginRequest request) {

        List<CustomerEntity> customers = customerRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

        if(customers.size() > 1){
            return commonUtils.setResponse("SIAP-SEWA-01-002", "User Invalid", HttpStatus.OK, null);
        }
        else{
            CustomerEntity customer = customers.get(0);
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customer.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                LoginResponse response = LoginResponse.builder()
                        .username(customer.getUsername())
                        .email(customer.getEmail())
                        .phoneNumber(customer.getPhoneNumber())
                        .token(jwtService.generateToken(customer.getUsername()))
                        .duration(1800)
                        .build();

                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response) ;
            }
            else {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Failed to login");
            }
        }
    }

    private String generateTemporaryUserName(RegisterRequest request) {
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            return request.getEmail().split("@")[0];
        }
        else {
            return request.getPhoneNumber();
        }
    }


}

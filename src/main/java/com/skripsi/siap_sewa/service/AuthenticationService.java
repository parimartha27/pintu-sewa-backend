package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.*;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.OtpHistoryEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.OtpHistoryRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final JWTService jwtService;
    private final EmailService emailService;
    private final OtpHistoryRepository otpHistoryRepository;
    private final CustomerRepository customerRepository;

    public ResponseEntity<ApiResponse> register(RegisterRequest request){

        if(customerRepository.existsByEmail(request.getEmail())){
            return commonUtils.setResponse(
                    "SIAP-SEWA-01-003",
                    "Email has been registered. Please use other email",
                    HttpStatus.CONFLICT,
                    null);
        }
        else if(customerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            return commonUtils.setResponse(
                    "SIAP-SEWA-01-004",
                    "Phone number has been registered. Please use other phone number",
                    HttpStatus.CONFLICT,
                    null);
        }
        else{
            CustomerEntity entity = new CustomerEntity();
            entity.setEmail(request.getEmail());
            entity.setPhoneNumber(request.getPhoneNumber());
            entity.setPassword(encoder.encode(request.getPassword()));
            entity.setCreatedAt(LocalDateTime.now());
            entity.setLastUpdateAt(LocalDateTime.now());

            if(!request.getPhoneNumber().isEmpty() && !request.getPhoneNumber().isBlank()){
                entity.setUsername(request.getPhoneNumber());
            }else{
                entity.setUsername(request.getEmail());
            }

            customerRepository.save(entity);

            RegisterResponse response = objectMapper.convertValue(entity, RegisterResponse.class);

            String otp = commonUtils.generateOtp();

            OtpHistoryEntity otpHistory = OtpHistoryEntity.builder()
                    .otp(otp)
                    .email(response.getEmail())
                    .phoneNumber(response.getPhoneNumber())
                    .createdAt(LocalDateTime.now())
                    .build();
            otpHistoryRepository.save(otpHistory);

            response.setOtpId(otpHistory.getId());

            emailService.sendEmail(response.getEmail(),Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateOtpMessage(otpHistory.getOtp()));

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }
    }

    public ResponseEntity<ApiResponse> login(@Valid LoginRequest request) {

        List<CustomerEntity> customers = customerRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

        if(customers.size() > 1){
            return commonUtils.setResponse(
                    "SIAP-SEWA-01-001",
                    "Multiple accounts found with the same email or phone number. Please contact support for assistance.",
                    HttpStatus.CONFLICT,
                    null
            );
        }
        else{
            CustomerEntity customer = customers.get(0);
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customer.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                LoginResponse response = LoginResponse.builder()
                        .userId(customer.getId())
                        .username(customer.getUsername())
                        .email(customer.getEmail())
                        .phoneNumber(customer.getPhoneNumber())
                        .token(jwtService.generateToken(new CustomerPrincipal(customer)))
                        .duration(1800)
                        .build();

                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
            }
            else {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Failed to login");
            }
        }
    }


}

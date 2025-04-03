package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.*;
import com.skripsi.siap_sewa.dto.authentication.login.LoginRequest;
import com.skripsi.siap_sewa.dto.authentication.login.LoginResponse;
import com.skripsi.siap_sewa.dto.authentication.register.RegisterOauthRequest;
import com.skripsi.siap_sewa.dto.authentication.register.RegisterRequest;
import com.skripsi.siap_sewa.dto.authentication.register.RegisterResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.exception.EmailExistException;
import com.skripsi.siap_sewa.exception.PhoneNumberExistException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<ApiResponse> register(RegisterRequest request){

        if(commonUtils.isNull(request.getEmail())
                && commonUtils.isNull(request.getPhoneNumber())){
            return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
        }

        if(customerRepository.existsByEmail(request.getEmail())){
            return commonUtils.setResponse(Constant.FAILED_CODE,
                    "Please use other email to register",
                    HttpStatus.OK,
                    "Email:" +  request.getEmail() + "already exists");
        }
        else if(customerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            return commonUtils.setResponse(Constant.FAILED_CODE,
                    "Please use other email to register",
                    HttpStatus.OK,
                    "PhoneNumber:" +  request.getPhoneNumber() + " already exists");
        }

        CustomerEntity newCustomer = new CustomerEntity();

        String otp = commonUtils.generateOtp();

        if(request.getEmail().isEmpty()){
            newCustomer.setPhoneNumber(request.getPhoneNumber());
        }else{
            newCustomer.setEmail(request.getEmail());
        }

        newCustomer.setOtp(otp);
        newCustomer.setVerifyCount(0);
        newCustomer.setResendOtpCount(0);
        newCustomer.setStatus("VERIFY_OTP");
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setLastUpdateAt(LocalDateTime.now());

        RegisterResponse response = objectMapper.convertValue(newCustomer, RegisterResponse.class);

        customerRepository.save(newCustomer);
        response.setCustomerId(newCustomer.getId());

        emailService.sendEmail(response.getEmail(),Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateOtpMessage(otp));

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> registerOauth(RegisterOauthRequest request){

        if(commonUtils.isNull(request.getEmail())){
            return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
        }

        if(customerRepository.existsByEmail(request.getEmail())){
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Email:" +  request.getEmail() + "already exists");
        }

        CustomerEntity newCustomer = new CustomerEntity();

        newCustomer.setEmail(request.getEmail());
        newCustomer.setImage(request.getImage());
        newCustomer.setStatus("REGISTERED");
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setLastUpdateAt(LocalDateTime.now());

        RegisterResponse response = objectMapper.convertValue(newCustomer, RegisterResponse.class);

        customerRepository.save(newCustomer);
        response.setCustomerId(newCustomer.getId());

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
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
                        .customerId(customer.getId())
                        .username(customer.getUsername())
                        .email(customer.getEmail())
                        .phoneNumber(customer.getPhoneNumber())
                        .status(customer.getStatus())
                        .image(customer.getImage())
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

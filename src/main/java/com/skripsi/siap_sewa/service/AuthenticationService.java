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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    public ResponseEntity<ApiResponse> register(RegisterRequest request) {
        try {
            log.info("Validating register request");
            if(commonUtils.isNull(request.getEmail()) && commonUtils.isNull(request.getPhoneNumber())) {
                log.warn("Bad request - both email and phone number are null");
                return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
            }

            log.info("Checking existing customer");
            if(customerRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                throw new EmailExistException();
            } else if(customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                log.warn("Phone number already exists: {}", request.getPhoneNumber());
                throw new PhoneNumberExistException();
            }

            CustomerEntity newCustomer = new CustomerEntity();
            String otp = commonUtils.generateOtp();
            log.info("Generated OTP for customer registration");

            if(request.getEmail().isEmpty()) {
                newCustomer.setPhoneNumber(request.getPhoneNumber());
            } else {
                newCustomer.setEmail(request.getEmail());
            }

            newCustomer.setOtp(otp);
            newCustomer.setVerifyCount(0);
            newCustomer.setResendOtpCount(0);
            newCustomer.setStatus("VERIFY_OTP");
            newCustomer.setCreatedAt(LocalDateTime.now());
            newCustomer.setLastUpdateAt(LocalDateTime.now());

            customerRepository.save(newCustomer);
            log.info("Customer registered successfully with ID: {}", newCustomer.getId());

            RegisterResponse response = objectMapper.convertValue(newCustomer, RegisterResponse.class);
            response.setCustomerId(newCustomer.getId());

            emailService.sendEmail(response.getEmail(), Constant.SUBJECT_EMAIL_REGISTER,
                    commonUtils.generateOtpMessage(otp));
            log.info("OTP email sent to: {}", response.getEmail());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (EmailExistException | PhoneNumberExistException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error during registration: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> registerOauth(RegisterOauthRequest request) {
        try {
            log.info("Validating OAuth register request");
            if(commonUtils.isNull(request.getEmail())) {
                log.warn("Bad request - email is null");
                return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
            }

            if(customerRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                throw new EmailExistException();
            }

            CustomerEntity newCustomer = new CustomerEntity();
            newCustomer.setEmail(request.getEmail());
            newCustomer.setImage(request.getImage());
            newCustomer.setStatus("REGISTERED");
            newCustomer.setCreatedAt(LocalDateTime.now());
            newCustomer.setLastUpdateAt(LocalDateTime.now());

            customerRepository.save(newCustomer);
            log.info("OAuth customer registered successfully with ID: {}", newCustomer.getId());

            RegisterResponse response = objectMapper.convertValue(newCustomer, RegisterResponse.class);
            response.setCustomerId(newCustomer.getId());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (EmailExistException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error during OAuth registration: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> login(@Valid LoginRequest request) {
        try {
            log.info("Processing login request");
            List<CustomerEntity> customers = customerRepository.findByEmailOrPhoneNumber(
                    request.getEmail(), request.getPhoneNumber());

            if(customers.size() > 1) {
                log.warn("Multiple accounts found for email/phone: {}",
                        request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
                return commonUtils.setResponse(
                        "SIAP-SEWA-01-001",
                        "Multiple accounts found with the same email or phone number. Please contact support for assistance.",
                        HttpStatus.CONFLICT,
                        null
                );
            }

            if(customers.isEmpty()) {
                log.warn("No customer found for email/phone: {}",
                        request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
                throw new DataNotFoundException("Customer not found");
            }

            CustomerEntity customer = customers.get(0);
            log.info("Authenticating customer: {}", customer.getId());

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customer.getUsername(), request.getPassword()));

            if (!authentication.isAuthenticated()) {
                log.warn("Authentication failed for customer: {}", customer.getId());
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Failed to login");
            }

            LoginResponse response = LoginResponse.builder()
                    .customerId(customer.getId())
                    .username(customer.getUsername())
                    .email(customer.getEmail())
                    .phoneNumber(customer.getPhoneNumber())
                    .status(customer.getStatus())
                    .image(customer.getImage() == null ? "" : customer.getImage())
                    .token(jwtService.generateToken(new CustomerPrincipal(customer)))
                    .duration(1800)
                    .build();

            log.info("Login successful for customer: {}", customer.getId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error during login: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}

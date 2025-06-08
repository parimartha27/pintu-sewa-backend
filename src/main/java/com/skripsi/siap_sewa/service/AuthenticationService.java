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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
//    private final WhatsappService whatsappService;

    @Transactional
    public ResponseEntity<ApiResponse> register(@Valid RegisterRequest request) {
        
        Optional<CustomerEntity> validate = validateRegistrationRequest(request);
        if(validate != null){
            RegisterResponse response = objectMapper.convertValue(validate, RegisterResponse.class);
            response.setCustomerId(validate.get().getId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        CustomerEntity newCustomer = createNewCustomer(request);
        customerRepository.save(newCustomer);
        log.info("Customer entity saved successfully with ID: {}", newCustomer.getId());
        
        sendOtpNotification(newCustomer);

        RegisterResponse response = objectMapper.convertValue(newCustomer, RegisterResponse.class);
        response.setCustomerId(newCustomer.getId());
        
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse>  registerOauth(RegisterOauthRequest request) {

        if (commonUtils.isNull(request.getEmail())) {
            throw new IllegalArgumentException("Email tidak boleh kosong untuk registrasi OAuth.");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new EmailExistException("Email " + request.getEmail() + " sudah digunakan");
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
    }

    public ResponseEntity<ApiResponse> login(LoginRequest request) {

        List<CustomerEntity> customers = customerRepository.findByEmailOrPhoneNumber(
                request.getEmail(), request.getPhoneNumber());

        if (customers.isEmpty()) {
            log.warn("Login failed: Customer not found for identifier: {}", request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
            throw new DataNotFoundException("Email/Nomor Telepon atau password salah.");
        }

        CustomerEntity customer = customers.get(0);

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customer.getUsername(),
                            request.getPassword()));
            log.info("Authentication successful for customer: {}", customer.getId());
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for customer: {}. Reason: {}", customer.getId(), ex.getMessage());
            throw new DataNotFoundException("Email/Nomor Telepon atau password salah.");
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

        log.info("Login successful, token generated for customer: {}", customer.getId());
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    private Optional<CustomerEntity> validateRegistrationRequest(RegisterRequest request) {
        if (commonUtils.isNull(request.getEmail()) && commonUtils.isNull(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Email atau Nomor Handphone wajib diisi.");
        }

        if (!commonUtils.isNull(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            Optional<CustomerEntity> customer = customerRepository.findByEmail(request.getEmail());
            log.info("Email : {}", customer);
            if(customer.get().getUsername() != null){
                log.info("Email already used: {}", request.getEmail());
                throw new EmailExistException("Email sudah digunakan : " + request.getEmail());
            }else{
                return customer;
            }
        }
        if (!commonUtils.isNull(request.getPhoneNumber()) && customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            Optional<CustomerEntity> customer = customerRepository.findByPhoneNumber(request.getPhoneNumber());
            if(customer.get().getUsername() != null){
                log.info("Phone Number already used: {}", request.getPhoneNumber());
                throw new PhoneNumberExistException("No Handphone sudah digunakan: " + request.getPhoneNumber());
            }else{
                return customer;
            }
        }
        return null;
    }

    private CustomerEntity createNewCustomer(RegisterRequest request) {
        CustomerEntity newCustomer = new CustomerEntity();
        String otp = commonUtils.generateOtp();

        newCustomer.setEmail(request.getEmail());
        newCustomer.setPhoneNumber(request.getPhoneNumber());
        newCustomer.setOtp(otp);
        newCustomer.setStatus("VERIFY_OTP");
        newCustomer.setVerifyCount(0);
        newCustomer.setResendOtpCount(0);
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setLastUpdateAt(LocalDateTime.now());

        return newCustomer;
    }

    private void sendOtpNotification(CustomerEntity customer) {
        String otp = customer.getOtp();

        if (!commonUtils.isNull(customer.getEmail())) {
            log.info("Sending OTP to email: {}", customer.getEmail());
            emailService.sendEmail(customer.getEmail(), 0, otp);
        }

        // else if (!commonUtils.isNull(customer.getPhoneNumber())) {
        //     log.info("Sending OTP to WhatsApp: {}", customer.getPhoneNumber());
        //     whatsappService.sendOtpByWhatsapp(customer.getPhoneNumber(), otp);
        // }
    }

}

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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final CustomerRepository customerRepository;
    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final EmailService emailService;

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
            entity.setUsername(generateTemporaryUserName(request));

            customerRepository.save(entity);

            RegisterResponse response = objectMapper.convertValue(entity, RegisterResponse.class);

            emailService.sendEmail(Constant.SUBJECT_EMAIL_REGISTER, response.getEmail(), generateOtpMessage());

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

    public static String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    public static String generateOtpMessage() {
        String otp = generateOtp();
        return String.format(
                """
                        Hi, Sobat Sewa.
                        
                        Berikut merupakan one-time passcode (OTP) kamu : %s.
                        
                        OTP akan expired dalam 30 menit.
                        
                        Selamat menggunakan website Pintu Sewa
                        
                        Hormat kami,
                        Tim Pintu Sewa
                """, otp
        );
    }

}

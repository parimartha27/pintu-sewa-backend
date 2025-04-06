package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.authentication.otp.*;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.*;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;

    public ResponseEntity<ApiResponse> verifyOtp(@Valid OtpRequest request) {
        try {
            log.info("Verifying OTP for customer: {}", request.getCustomerId());

            CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.warn("Customer not found: {}", request.getCustomerId());
                        return new DataNotFoundException("Customer not found");
                    });

            if (customer.getVerifyCount() > Constant.MAX_OTP_VERIFY_ATTEMPTS ||
                    customer.getResendOtpCount() > Constant.MAX_OTP_RESEND_ATTEMPTS) {
                log.warn("OTP attempts exhausted for customer: {}", request.getCustomerId());
                throw new OtpAttemptsExceededException("OTP verification attempts exhausted");
            }

            if (!request.getOtpCode().equals(customer.getOtp())) {
                customer.setVerifyCount(customer.getVerifyCount() + 1);
                customerRepository.save(customer);
                log.warn("Invalid OTP provided for customer: {}", request.getCustomerId());
                throw new InvalidOtpException("Invalid OTP code");
            }

            // OTP verification successful
            customer.setStatus("REGISTERED");
            customer.setVerifyCount(customer.getVerifyCount() + 1);
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);
            log.info("OTP verified successfully for customer: {}", request.getCustomerId());

            OtpResponse response = OtpResponse.builder()
                    .customerId(customer.getId())
                    .username(customer.getUsername())
                    .email(customer.getEmail())
                    .phoneNumber(customer.getPhoneNumber())
                    .status(customer.getStatus())
                    .token(jwtService.generateToken(new CustomerPrincipal(customer)))
                    .duration(Constant.TOKEN_EXPIRATION_SECONDS)
                    .build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException | InvalidOtpException | OtpAttemptsExceededException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error verifying OTP: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> resendOtp(String customerId) {
        try {
            log.info("Resending OTP for customer: {}", customerId);

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.warn("Customer not found: {}", customerId);
                        return new DataNotFoundException("Customer not found");
                    });

            if (customer.getResendOtpCount() >= Constant.MAX_OTP_RESEND_ATTEMPTS ||
                    customer.getVerifyCount() >= Constant.MAX_OTP_VERIFY_ATTEMPTS) {
                log.warn("OTP resend attempts exhausted for customer: {}", customerId);
                throw new OtpAttemptsExceededException("OTP resend attempts exhausted");
            }

            // Reset counters if last attempt was more than 30 minutes ago
            if (!isLessThan30Minutes(customer.getLastUpdateAt())) {
                customer.setResendOtpCount(0);
                customer.setVerifyCount(0);
                log.debug("Resetting OTP counters for customer: {}", customerId);
            }

            String newOtp = commonUtils.generateOtp();
            customer.setOtp(newOtp);
            customer.setResendOtpCount(customer.getResendOtpCount() + 1);
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);
            log.info("New OTP generated for customer: {}", customerId);

            // Send OTP email
            emailService.sendEmail(
                    customer.getEmail(),
                    Constant.SUBJECT_EMAIL_REGISTER,
                    commonUtils.generateOtpMessage(newOtp)
            );
            log.debug("OTP email sent to: {}", customer.getEmail());

            ResendOtpResponse response = ResendOtpResponse.builder()
                    .customerId(customer.getId())
                    .verifyCount(customer.getVerifyCount())
                    .resendOtpCount(customer.getResendOtpCount())
                    .status(customer.getStatus())
                    .build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException | OtpAttemptsExceededException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error resending OTP: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private boolean isLessThan30Minutes(LocalDateTime lastUpdateAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastUpdateAt, now);
        return duration.toMinutes() < 30;
    }

    public ResponseEntity<ApiResponse> validateOtp(String customerId) {
        try {
            log.info("Validating OTP status for customer: {}", customerId);

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.warn("Customer not found: {}", customerId);
                        return new DataNotFoundException("Customer not found");
                    });

            ValidOtpResponse response = ValidOtpResponse.builder()
                    .customerId(customer.getId())
                    .status(customer.getStatus())
                    .verifyCount(customer.getVerifyCount())
                    .resendOtpCount(customer.getResendOtpCount())
                    .build();

            log.debug("OTP validation result for customer {}: {}", customerId, response);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error validating OTP status: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}
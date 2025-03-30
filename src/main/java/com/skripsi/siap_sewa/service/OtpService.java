package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.authentication.otp.OtpRequest;
import com.skripsi.siap_sewa.dto.authentication.otp.OtpResponse;
import com.skripsi.siap_sewa.dto.authentication.otp.ResendOtpRequest;
import com.skripsi.siap_sewa.dto.authentication.otp.ResendOtpResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;

    public ResponseEntity<ApiResponse> verifyOtp(@Valid OtpRequest request) {

        if (request.getVerifyCount() > 10 || request.getResendOtpCount() > 3) {
            return commonUtils.setResponse("PS-01-005", "The attempt has run out", HttpStatus.OK, null);
        }

        Optional<CustomerEntity> isPresentOtp = customerRepository.findById(request.getCustomerId());

        if (isPresentOtp.isPresent()) {
            CustomerEntity customerOtp = isPresentOtp.get();
            if (request.getOtpCode().equals(customerOtp.getOtp())) {
                OtpResponse response = OtpResponse.builder()
                        .customerId(customerOtp.getId())
                        .username(customerOtp.getUsername())
                        .email(customerOtp.getEmail())
                        .phoneNumber(customerOtp.getPhoneNumber())
                        .token(jwtService.generateToken(new CustomerPrincipal(customerOtp)))
                        .duration(1800)
                        .build();

                customerOtp.setStatus("REGISTERED");
                customerOtp.setVerifyCount(request.getVerifyCount() + 1);
                customerOtp.setLastUpdateAt(LocalDateTime.now());
                customerRepository.save(customerOtp);

                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
            } else {
                customerOtp.setVerifyCount(request.getVerifyCount() + 1);
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Invalid OTP");
            }
        } else {
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Credential invalid");
        }
    }

    public ResponseEntity<ApiResponse> resendOtp(@Valid ResendOtpRequest request) {
        Optional<CustomerEntity> isPresentOtp = customerRepository.findById(request.getCustomerId());

        if (isPresentOtp.isPresent()) {
            CustomerEntity updatedCustomerOtp = isPresentOtp.get();

            boolean isLessThan30Minutes = isLessThan30Minutes(updatedCustomerOtp.getLastUpdateAt());

            if (!isLessThan30Minutes) {
                updatedCustomerOtp.setResendOtpCount(0);
                updatedCustomerOtp.setVerifyCount(0);
                updatedCustomerOtp.setLastUpdateAt(LocalDateTime.now());
            }

            if (updatedCustomerOtp.getResendOtpCount() > 3) {
                return commonUtils.setResponse("PS-01-005", "The attempt has run out", HttpStatus.OK, null);
            }

            if (updatedCustomerOtp.getVerifyCount() > 10) {
                return commonUtils.setResponse("PS-01-005", "The attempt has run out", HttpStatus.OK, null);
            }

            String newOtp = commonUtils.generateOtp();

            updatedCustomerOtp.setOtp(newOtp);
            updatedCustomerOtp.setResendOtpCount(updatedCustomerOtp.getResendOtpCount() + 1);
            updatedCustomerOtp.setLastUpdateAt(LocalDateTime.now());

            ResendOtpResponse response = ResendOtpResponse.builder()
                    .customerId(updatedCustomerOtp.getId())
                    .verifyCount(updatedCustomerOtp.getVerifyCount())
                    .resendOtpCount(updatedCustomerOtp.getResendOtpCount())
                    .build();

            customerRepository.save(updatedCustomerOtp);

            emailService.sendEmail(updatedCustomerOtp.getEmail(), Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateOtpMessage(newOtp));

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        return commonUtils.setResponse(ErrorMessageEnum.FAILED, "OTP expired or invalid");
    }

    public boolean isLessThan30Minutes(LocalDateTime lastUpdateAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastUpdateAt, now);
        return duration.toMinutes() < 30;
    }

//    TODO: fix this
//    @Scheduled(fixedRate = 1800000)
//    public void deleteExpiredOtpHistory() {
//        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
//        otpHistoryRepository.deleteByCreatedAtAfter(thirtyMinutesAgo);
//    }
}

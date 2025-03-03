package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.OtpRequest;
import com.skripsi.siap_sewa.dto.authentication.OtpResponse;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final OtpHistoryRepository otpHistoryRepository;
    private final CustomerRepository customerRepository;

    public ResponseEntity<ApiResponse> verifyOtp(@Valid OtpRequest request) {

        if (request.getAttempt() > 3) {
            return commonUtils.setResponse("PS-01-005", "The attempt has run out", HttpStatus.OK, null);
        }

        Optional<OtpHistoryEntity> isPresentOtp = otpHistoryRepository.findById(request.getOtpId());

        if (isPresentOtp.isPresent()) {
            OtpHistoryEntity otpHistory = isPresentOtp.get();
            if (request.getOtpCode().equals(otpHistory.getOtp())) {
                List<CustomerEntity> customerEntityOptional = customerRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

                if (customerEntityOptional.size() == 1) {
                    CustomerEntity customer = customerEntityOptional.getFirst();

                    OtpResponse response = OtpResponse.builder()
                            .username(customer.getUsername())
                            .email(customer.getEmail())
                            .phoneNumber(customer.getPhoneNumber())
                            .token(jwtService.generateToken(customer.getUsername()))
                            .duration(1800)
                            .build();
                    return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
                }
                else {
                    return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Invalid Credentials");
                }
            } else {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Invalid OTP");
            }
        } else {
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "OTP expired or invalid");
        }
    }

    public ResponseEntity<ApiResponse> resendOtp(@Valid OtpRequest request) {

        if (request.getAttempt() > 3) {
            return commonUtils.setResponse("PS-01-005", "The attempt has run out", HttpStatus.OK, null);
        }

        Optional<OtpHistoryEntity> isPresentOtp = otpHistoryRepository.findById(request.getOtpId());

        if(isPresentOtp.isPresent()){
            OtpHistoryEntity updatedOtp = isPresentOtp.get();

            String newOtp = commonUtils.generateOtp();

           updatedOtp.setOtp(newOtp);
           updatedOtp.setCreatedAt(LocalDateTime.now());

            otpHistoryRepository.save(updatedOtp);

            emailService.sendEmail(request.getEmail(), Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateOtpMessage(updatedOtp.getOtp()));

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, request.getAttempt() + 1);
        }
        return commonUtils.setResponse(ErrorMessageEnum.FAILED, "OTP expired or invalid");
    }

//    TODO: fix this
//    @Scheduled(fixedRate = 1800000)
//    public void deleteExpiredOtpHistory() {
//        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
//        otpHistoryRepository.deleteByCreatedAtAfter(thirtyMinutesAgo);
//    }
}

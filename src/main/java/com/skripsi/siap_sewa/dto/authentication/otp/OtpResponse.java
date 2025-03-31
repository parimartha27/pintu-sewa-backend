package com.skripsi.siap_sewa.dto.authentication.otp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpResponse {
    private String customerId;
    private String username;
    private String email;
    private String phoneNumber;
    private String status;
    private int verifyCount;
    private int resendOtpCount;
    private String token;
    private int duration;
}

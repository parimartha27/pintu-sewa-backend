package com.skripsi.siap_sewa.dto.authentication.otp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResendOtpRequest {

    @NotNull(message = "Verify count tidak boleh kosong")
    private int verifyCount;

    @NotNull(message = "Resend OTP count tidak boleh kosong")
    private int resendOtpCount;

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;
}

package com.skripsi.siap_sewa.dto.authentication.otp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpRequest {

    @NotBlank(message = "OTP tidak boleh kosong")
    private String otpCode;

//    @NotNull(message = "Verify count tidak boleh kosong")
//    private int verifyCount;
//
//    @NotNull(message = "Resend OTP count tidak boleh kosong")
//    private int resendOtpCount;

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;
}

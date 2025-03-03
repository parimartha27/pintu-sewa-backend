package com.skripsi.siap_sewa.dto.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Email tidak boleh kosong")
    private String email;

    @NotBlank(message = "Nomor HP tidak boleh kosong")
    @Size(min = 10, max = 15, message = "Nomor HP tidak valid")
    private String phoneNumber;

    @NotBlank(message = "Attempt tidak boleh kosong")
    private int attempt;

    private String otpId;
}

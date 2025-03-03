package com.skripsi.siap_sewa.dto.authentication;

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

    @Nullable
    @Email(message = "Format email tidak valid")
    private String email;

    @Nullable
    @Pattern(regexp = "^(\\d{10,15})?$", message = "Nomor HP tidak valid")
    private String phoneNumber;

    @NotNull(message = "Attempt tidak boleh kosong")
    private int attempt;

    private String otpId;
}

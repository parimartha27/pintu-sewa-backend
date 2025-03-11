package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ForgetPasswordRequest {

    @Nullable
    @Email(message = "Format email tidak valid")
    private String email;

    @Nullable
    @Pattern(regexp = "^(\\d{10,15})?$", message = "Nomor HP tidak valid")
    private String phoneNumber;

    @NotBlank(message = "Password tidak boleh kosong")
    private String password;
}

package com.skripsi.siap_sewa.dto.admin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminLoginRequest {
    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    private String password;
}

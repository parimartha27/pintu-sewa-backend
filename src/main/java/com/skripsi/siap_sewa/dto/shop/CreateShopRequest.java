package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class CreateShopRequest {

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;

    @NotBlank(message = "Nama toko tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama toko harus terdiri dari 3 hingga 100 karakter")
    private String name;

    @NotBlank
    @Email(message = "Format email tidak valid")
    private String email;

    private String street;

    private String district;

    private String regency;

    private String province;

    private String postCode;

    @NotNull(message = "Tolong pilih alamat toko yang sesuai")
    private String isSameAddress;
}

package com.skripsi.siap_sewa.dto.shop;

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
public class CreateShopRequest {

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;

    @NotBlank(message = "Nama toko tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama toko harus terdiri dari 3 hingga 100 karakter")
    private String name;

    @NotBlank
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Lokasi toko tidak boleh kosong")
    @Size(min = 5, max = 255, message = "Lokasi toko harus terdiri dari 5 hingga 255 karakter")
    private String street;

    @NotBlank(message = "Kecamatan tidak boleh kosong")
    private String district;

    @NotBlank(message = "Kota tidak boleh kosong")
    private String regency;

    @NotBlank(message = "Provinsi tidak boleh kosong")
    private String province;

    @NotBlank(message = "Kode pos tidak boleh kosong")
    @Pattern(regexp = "^[0-9]{5}$", message = "Kode pos harus terdiri dari 5 angka")
    private String postCode;

    @NotNull(message = "Tolong pilih alamat toko yang sesuai")
    private boolean isSameAddress;
}

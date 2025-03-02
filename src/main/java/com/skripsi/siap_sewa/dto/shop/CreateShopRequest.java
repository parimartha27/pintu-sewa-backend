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

    @NotBlank(message = "Nama toko tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama toko harus terdiri dari 3 hingga 100 karakter")
    private String name;

    @NotBlank(message = "Deskripsi toko tidak boleh kosong")
    @Size(min = 10, max = 500, message = "Deskripsi toko harus terdiri dari 10 hingga 500 karakter")
    private String description;

    @Pattern(regexp = "^@?[a-zA-Z0-9._]+$", message = "Format username Instagram tidak valid")
    private String instagram;

    @Pattern(regexp = "^(https?:\\/\\/)?(www\\.)?facebook\\.com\\/[a-zA-Z0-9._-]+$", message = "URL Facebook tidak valid")
    private String facebook;

    @Min(value = 0, message = "Status toko minimal adalah 0")
    @Max(value = 1, message = "Status toko maksimal adalah 1")
    private int shopStatus;

    @NotBlank(message = "Lokasi toko tidak boleh kosong")
    @Size(min = 5, max = 255, message = "Lokasi toko harus terdiri dari 5 hingga 255 karakter")
    private String shopLocation;

    @NotBlank(message = "Provinsi tidak boleh kosong")
    private String province;

    @NotBlank(message = "Kota tidak boleh kosong")
    private String city;

    @NotBlank(message = "Kode pos tidak boleh kosong")
    @Pattern(regexp = "^[0-9]{5}$", message = "Kode pos harus terdiri dari 5 angka")
    private String postCode;

    @NotBlank(message = "Kecamatan tidak boleh kosong")
    private String subDistrict;

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;
}

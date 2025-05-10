package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class EditAddressRequest {
    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Jalan tidak boleh kosong")
    @Size(min = 5, max = 255, message = "Jalan harus terdiri dari 5 hingga 255 karakter")
    private String street;

    @NotBlank(message = "Kecamatan tidak boleh kosong")
    private String district;

    @NotBlank(message = "Kota tidak boleh kosong")
    private String regency;

    @NotBlank(message = "Provinsi tidak boleh kosong")
    private String province;

    private String postCode;

    private String notes;
}
package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EditShopRequest {

    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Nama toko tidak boleh kosong")
    private String name;

    private String description;

    private MultipartFile image;

    @NotBlank(message = "Jalan tidak boleh kosong")
    private String street;

    @NotBlank(message = "Kecamatan tidak boleh kosong")
    private String district;

    @NotBlank(message = "Kabupaten tidak boleh kosong")
    private String regency;

    @NotBlank(message = "Provinsi tidak boleh kosong")
    private String province;

    private String postCode;

    private String workHours;
}

package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nullable;
import
        jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EditCustomerRequest {

    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Jenis kelamin tidak boleh kosong")
    @Pattern(regexp = "^(Laki-laki|Perempuan)$", message = "Jenis kelamin harus 'Laki-laki' atau 'Perempuan'")
    private String gender;

    @NotNull(message = "Tanggal lahir tidak boleh kosong")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String image;

    @NotBlank(message = "Jalan tidak boleh kosong")
    @Size(min = 5, max = 255, message = "Jalan harus terdiri dari 5 hingga 255 karakter")
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

    private String password;

    private String note;
}

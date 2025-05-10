package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateNewCustomerRequest {

    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus terdiri dari 3 hingga 100 karakter")
    private String name;

    @Nullable
    @Email(message = "Format email tidak valid")
    private String email;

    @Nullable
    @Pattern(regexp = "^(\\d{10,15})?$", message = "Nomor HP tidak valid")
    private String phoneNumber;

    @NotBlank(message = "Jenis kelamin tidak boleh kosong")
    @Pattern(regexp = "^(Laki-Laki|Perempuan)$", message = "Jenis kelamin harus 'Laki-laki' atau 'Perempuan'")
    private String gender;

    @NotNull(message = "Tanggal lahir tidak boleh kosong")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "Gambar tidak boleh kosong")
    private MultipartFile image;

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

    private String password;

    private String notes;
}

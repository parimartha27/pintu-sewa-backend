package com.skripsi.siap_sewa.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EditCustomerRequest {

    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus terdiri dari 3 hingga 100 karakter")
    private String name;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 8, message = "Password minimal 8 karakter")
    private String password;

    @NotBlank(message = "Alamat tidak boleh kosong")
    @Size(min = 5, max = 255, message = "Alamat harus terdiri dari 5 hingga 255 karakter")
    private String address;

    @NotBlank(message = "Kota tidak boleh kosong")
    private String city;

    @NotBlank(message = "Provinsi tidak boleh kosong")
    private String province;

    @NotBlank(message = "Jenis kelamin tidak boleh kosong")
    @Pattern(regexp = "^(Laki-laki|Perempuan)$", message = "Jenis kelamin harus 'Laki-laki' atau 'Perempuan'")
    private String gender;

    @NotNull(message = "Tanggal lahir tidak boleh kosong")
    private LocalDateTime birthDate;

    @NotBlank(message = "Kode pos tidak boleh kosong")
    @Pattern(regexp = "^[0-9]{5}$", message = "Kode pos harus terdiri dari 5 angka")
    private String postCode;
}

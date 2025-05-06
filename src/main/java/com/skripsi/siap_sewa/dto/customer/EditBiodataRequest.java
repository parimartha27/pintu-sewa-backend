package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class EditBiodataRequest {
    @NotBlank(message = "ID tidak boleh kosong")
    private String id;

    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus 3-50 karakter")
    private String username;

    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama lengkap harus 3-100 karakter")
    private String name;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Email harus valid")
    private String email;

    @NotBlank(message = "Nomor telepon tidak boleh kosong")
    @Pattern(regexp = "^[0-9]+$", message = "Nomor telepon harus angka")
    private String phoneNumber;

    @NotBlank(message = "Jenis kelamin tidak boleh kosong")
    @Pattern(regexp = "^(Laki-laki|Perempuan)$", message = "Jenis kelamin harus 'Laki-laki' atau 'Perempuan'")
    private String gender;

    @NotNull(message = "Tanggal lahir tidak boleh kosong")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private MultipartFile image;
}

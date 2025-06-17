package com.skripsi.siap_sewa.dto.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddProductRequest {

    @NotBlank(message = "Shop ID tidak boleh kosong")
    private String shopId;

    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama produk wajib terdiri dari 3 hingga 100 karakter")
    private String name;

    @NotBlank(message = "Kategori produk tidak boleh kosong")
    private String category;

    @NotNull(message = "Durasi sewa tidak boleh kosong")
    @Min(value = 1, message = "Durasi sewa minimal harus 1")
    private Integer rentCategory;

    private String isRnb;

    @NotNull(message = "Berat produk tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Berat harus lebih dari 0")
    private BigDecimal weight;

    @NotNull(message = "Tinggi produk tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tinggi harus lebih dari 0")
    private BigDecimal height;

    @NotNull(message = "Lebar produk tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Lebar harus lebih dari 0")
    private BigDecimal width;

    @NotNull(message = "Panjang produk tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Panjang harus lebih dari 0")
    private BigDecimal length;

    @Builder.Default
    private BigDecimal dailyPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal weeklyPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deposit = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal buyPrice = BigDecimal.ZERO;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    private String description;
    
    private String conditionDescription;

    @Min(value = 0, message = "Stok tidak boleh 0, harus lebih dari 0")
    private int stock;

    private int minRented;

    private String status;

    private MultipartFile image;

}
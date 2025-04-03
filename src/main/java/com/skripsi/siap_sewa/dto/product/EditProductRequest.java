package com.skripsi.siap_sewa.dto.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EditProductRequest {

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

    private boolean isRnb;

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

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harian harus lebih dari 0")
    private BigDecimal dailyPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga mingguan harus lebih dari 0")
    private BigDecimal weeklyPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga bulanan harus lebih dari 0")
    private BigDecimal monthlyPrice;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    private String description;

    private String conditionDescription;

    @Min(value = 0, message = "Stok tidak boleh harus lebih dari 0")
    private int stock;

    private String status;

    private String image;

}
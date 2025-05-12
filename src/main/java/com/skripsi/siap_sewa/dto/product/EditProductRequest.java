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

    private String name;

    private String category;

    @Min(value = 1, message = "Durasi sewa minimal harus 1")
    private Integer rentCategory;

    private boolean isRnb;

    @DecimalMin(value = "0.0", inclusive = false, message = "Berat harus lebih dari 0")
    private BigDecimal weight;

    @DecimalMin(value = "0.0", inclusive = false, message = "Tinggi harus lebih dari 0")
    private BigDecimal height;

    @DecimalMin(value = "0.0", inclusive = false, message = "Lebar harus lebih dari 0")
    private BigDecimal width;

    @DecimalMin(value = "0.0", inclusive = false, message = "Panjang harus lebih dari 0")
    private BigDecimal length;

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harian harus lebih dari 0")
    private BigDecimal dailyPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga mingguan harus lebih dari 0")
    private BigDecimal weeklyPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Harga bulanan harus lebih dari 0")
    private BigDecimal monthlyPrice;

    private String description;

    private String conditionDescription;

    @Min(value = 0, message = "Stok tidak boleh 0, harus lebih dari 0")
    private int stock;

//    @Min(value = 0, message = "Minimal sewa tidak boleh 0, harus lebih dari 0")
    private int minRented;

    private String status;

    private String image;

}
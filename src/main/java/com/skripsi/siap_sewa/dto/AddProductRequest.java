package com.skripsi.siap_sewa.dto;

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
public class AddProductRequest {

    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Min(value = 3, message = "Nama produk wajib terdiri dari 3 karakter")
    @Max(value = 100, message = "Nama produk tidak boleh lebih dari 100 karakter")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "Biaya sewa per hari harus lebih besar dari 0")
    private BigDecimal priceInDay;

    @DecimalMin(value = "0.0", inclusive = false, message = "Biaya sewa per minggu harus lebih besar dari 0")
    private BigDecimal priceInWeek;

    @DecimalMin(value = "0.0", inclusive = false, message = "Biaya sewa per bulan harus lebih besar dari 0")
    private BigDecimal priceInMonth;

    @Min(value = 0, message = "Stock tidak boleh kurang dari 0")
    private int stock;

    private Boolean isRentToBuy = Boolean.FALSE;

    @Min(value = 1, message = "Minimal durasi sewa adalah 1 hari")
    private int minimumRentDay;

    @Min(value = 1, message = "Minimal quantity sewa adalah 1 barang")
    private int minimumRentQuantity;

    @Min(value = 1, message = "Maximal quantity sewa tidak boleh kosong")
    private int maxQuantityToRent;

    @NotBlank(message = "Gambar produk tidak boleh kososng")
    private String image;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    @Size(max = 1000, message = "Deskripsi produk tidak boleh lebih dari 1000 karakter")
    private String description;
}

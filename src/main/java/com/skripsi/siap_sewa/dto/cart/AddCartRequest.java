package com.skripsi.siap_sewa.dto.cart;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class AddCartRequest {

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;

    @NotBlank(message = "Product ID tidak boleh kosong")
    private String productId;

    @Min(value = 1, message = "Quantity harus minimal 1")
    private int quantity;

    @DecimalMin(value = "0.01", message = "Total amount harus lebih dari 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Start rent date tidak boleh kosong")
    private LocalDate startRentDate;

    @NotNull(message = "End rent date tidak boleh kosong")
    @FutureOrPresent(message = "End rent date harus di masa depan atau saat ini")
    private LocalDate endRentDate;

    @NotBlank(message = "Shipping address tidak boleh kosong")
    private String shippingAddress;

    @AssertTrue(message = "End rent date harus setelah start rent date")
    public boolean isValidRentalPeriod() {
        return startRentDate != null && endRentDate != null && endRentDate.isAfter(startRentDate);
    }
}

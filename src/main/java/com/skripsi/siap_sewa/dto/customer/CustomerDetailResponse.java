package com.skripsi.siap_sewa.dto.customer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerDetailResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate birthDate;
    private String image;
    private String status;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private String notes;
}

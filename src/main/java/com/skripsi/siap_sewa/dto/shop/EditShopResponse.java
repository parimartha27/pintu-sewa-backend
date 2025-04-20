package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EditShopResponse {
    private String name;
    private String description;
    private String instagram;
    private String facebook;
    private String image;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
}

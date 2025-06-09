package com.skripsi.siap_sewa.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductFilterDto {
    private String category;
    private Boolean isRnb;
    private String search;
    @Builder.Default
    private String sortBy = "name";
    @Builder.Default
    private String sortDirection = "asc";
    @Builder.Default
    private int page = 0;
}

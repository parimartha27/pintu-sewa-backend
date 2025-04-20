package com.skripsi.siap_sewa.dto.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductFilterRequest {
    private List<String> categories; 
    private String name;
    private List<Integer> rentDurations; 
    private List<String> locations; 
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<Boolean> isRnbOptions; 
    private List<Double> minRatings; 
    private String sortBy;
    private Sort.Direction sortDirection;
    private int page;
    private int size;
    private String shopId;


    public ProductFilterRequest() {
        this.sortBy = "name";
        this.sortDirection = Sort.Direction.ASC;
        this.page = 0;
        this.size = 16;
    }

    
    public String getSortBy() {
        
        if (sortBy != null && (sortBy.equals("name") ||
                sortBy.equals("dailyPrice") ||
                sortBy.equals("weeklyPrice") ||
                sortBy.equals("monthlyPrice"))) {
            return sortBy;
        }
        
        return "name";
    }
}
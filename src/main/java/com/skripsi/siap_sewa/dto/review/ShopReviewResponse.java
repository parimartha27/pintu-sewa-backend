package com.skripsi.siap_sewa.dto.review;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopReviewResponse {
    private String username;
    private String userProfile;
    private String comment;
    private List<String> images;
    private Double rating;
    private String createdAt;
    private String productImage;
    private String productName;
}

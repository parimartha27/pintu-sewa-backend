package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private BigDecimal priceInDay;
    private BigDecimal priceInWeek;
    private BigDecimal priceInMonth;
    private int stock;
    private Boolean isRentToBuy;
    private Boolean isDelete = Boolean.FALSE;
    private int minimumRentDay;
    private int minimumRentQuantity;
    private int maxQuantityToRent;
    private String image;
    private String description;
    private String slug;
    private LocalDateTime insertDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private StoreEntity store;




}


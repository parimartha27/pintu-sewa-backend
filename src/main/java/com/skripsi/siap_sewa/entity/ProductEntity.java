package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "PRODUCT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"shop", "transactions", "reviews"})
public class ProductEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String category;
    private int rentCategory;
    private boolean isRnb;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private BigDecimal dailyPrice;
    private BigDecimal weeklyPrice;
    private BigDecimal monthlyPrice;
    private String description;
    private String conditionDescription;
    private int stock;
    private int minRented;
    private String status;
    private String image;
    private BigDecimal deposit;

    @Version
    private Integer version;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastUpdateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id")
    private ShopEntity shop;

    @ManyToMany(mappedBy = "products")
    @JsonBackReference
    private Set<TransactionEntity> transactions;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ReviewEntity> reviews = new ArrayList<>();
}
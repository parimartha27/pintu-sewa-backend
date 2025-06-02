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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id")
    private ShopEntity shop;

    private String name;

    private String category;

    @Column(nullable = false)
    private int rentCategory;

    @Column(nullable = false)
    private boolean isRnb;

    @Column(precision = 38, scale = 2)
    private BigDecimal weight;

    @Column(precision = 38, scale = 2)
    private BigDecimal height;

    @Column(precision = 38, scale = 2)
    private BigDecimal width;

    @Column(precision = 38, scale = 2)
    private BigDecimal length;

    @Column(precision = 38, scale = 2)
    private BigDecimal dailyPrice;

    @Column(precision = 38, scale = 2)
    private BigDecimal weeklyPrice;

    @Column(precision = 38, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(precision = 38, scale = 2)
    private BigDecimal buyPrice;

    private String description;

    private String conditionDescription;

    private int stock;

    @Column(nullable = false)
    private int minRented;

    private String status;

    private String image;

    private BigDecimal deposit;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastUpdateAt;

    @ManyToMany(mappedBy = "products")
    @JsonBackReference
    private Set<TransactionEntity> transactions;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ReviewEntity> reviews = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.buyPrice == null) {
            this.buyPrice = BigDecimal.ZERO;
        }
    }
}
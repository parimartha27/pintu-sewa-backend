package com.skripsi.siap_sewa.entity;

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

@Entity
@Table(name = "SHOP")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"customer", "products"})
public class ShopEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // profile
    private String name;
    private String description;
    private String email;
    private String shopStatus;
    private String image;
    private String workHours;

    @Column(columnDefinition = "DECIMAL(19,2) DEFAULT 0")
    private BigDecimal balance;

    // Address
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastUpdateAt;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @JsonManagedReference
    private CustomerEntity customer;

    @Builder.Default
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductEntity> products = new ArrayList<>();


}


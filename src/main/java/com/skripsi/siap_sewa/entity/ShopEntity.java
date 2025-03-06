package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
//profile
    private String name;
    private String description;
    private String instagram;
    private String facebook;
    private int shopStatus;
    private String image;

//    Address
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @JsonBackReference
    private CustomerEntity customer;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductEntity> products = new ArrayList<>();

}


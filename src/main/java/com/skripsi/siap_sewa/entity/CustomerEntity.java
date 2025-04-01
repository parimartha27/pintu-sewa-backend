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
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "CUSTOMER")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String username;
    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;
    private String password;
    private String gender;
    private LocalDateTime birthDate;
    private String image;
    private String status;

//    Address
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private String notes;

//    OTP
    private String otp;
    private int verifyCount;
    private int resendOtpCount;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;

    private LocalDateTime lastLogin;
    private BigDecimal walletAmount;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ShopEntity shop;

    @OneToMany(mappedBy = "customer")
    private Set<TransactionEntity> transactions;
}



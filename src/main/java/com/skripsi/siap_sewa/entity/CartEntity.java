package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CART")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"product"})
public class CartEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonBackReference
    private ProductEntity product;

    @Column(nullable = false)
    private int quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private LocalDate startRentDate;

    private LocalDate endRentDate;

    @Column(length = 500)
    private String shippingAddress;

    private String shippingPartner;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastUpdateAt;
}
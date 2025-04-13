package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TRANSACTION")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"customer", "products"})
public class TransactionEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customer;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "transaction_products",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonManagedReference
    private Set<ProductEntity> products = new HashSet<>();

    private String transactionNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String shippingAddress;
    private int quantity;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private String isReturn;
    private String shippingCode;
    private String returnCode;

    @Version
    private Integer version;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastUpdateAt;

    @Column(name = "is_selled", columnDefinition = "boolean default false")
    private boolean isSelled;

    private String shopId;
    private String shopName;
    private BigDecimal totalDeposit;
    private boolean isDepositReturned;
    private LocalDateTime depositReturnedAt;
    private BigDecimal serviceFee;
    private String shippingPartner;
    private BigDecimal shippingPrice;
}
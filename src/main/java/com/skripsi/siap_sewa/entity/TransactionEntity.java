package com.skripsi.siap_sewa.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "TRANSASCTION")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customer;

    @ManyToMany
    @JoinTable(
            name = "transaction_products",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products;

    private String transactionNumber;
    private String startDate;
    private String endDate;
    private Integer shippingAddress;
    private Integer quantity;
    private Double amount;
    private String totalAmount;
    private String paymentMethod;
    private String status;
    private String isReturn;
    private String shippingCode;
    private String returnCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;

    @Column(name = "is_selled", columnDefinition = "boolean default false")
    private boolean isSelled;
}

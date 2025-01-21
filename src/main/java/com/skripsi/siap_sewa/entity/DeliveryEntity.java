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
@Table(name = "DELIVERY")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private int minDeliveryDuration;
    private int maxDeliveryDuration;
    private BigDecimal deliveryCost;
    private String providerName;
    private String deliveryType;
    private Boolean isPartnership = Boolean.TRUE;
    private LocalDateTime insertDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private StoreEntity store;

}

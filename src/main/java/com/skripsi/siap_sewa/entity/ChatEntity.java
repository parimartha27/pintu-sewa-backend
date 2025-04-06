package com.skripsi.siap_sewa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CHAT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatEntity {

    @EqualsAndHashCode.Include
    @Id
    @Column(columnDefinition = "UUID")
    private String id;

    @Column(name = "customer_id", nullable = true)
    private String customerId;

    @Column(name = "shop_id", nullable = true)
    private String shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = true)
    private SenderType senderType;

    @Column(nullable = true)
    private String message;

    @Column(name = "is_read_by_buyer")
    private boolean isReadByBuyer;

    @Column(name = "is_read_by_seller")
    private boolean isReadBySeller;

    @CreationTimestamp
    @Column(name = "created_dt", updatable = true)
    private LocalDateTime createdDt;

    public enum SenderType {
        BUYER, SELLER
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
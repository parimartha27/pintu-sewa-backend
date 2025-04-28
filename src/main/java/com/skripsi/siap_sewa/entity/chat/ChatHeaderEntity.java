package com.skripsi.siap_sewa.entity.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CHAT_HEADER")
public class ChatHeaderEntity {
    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "shop_id", nullable = false)
    private String shopId;

    @Column(name = "is_report", nullable = false)
    private Boolean isReport;
}
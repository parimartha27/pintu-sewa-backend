package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, String> {
    List<ChatEntity> findByCustomerIdAndShopIdOrderByCreatedDtAsc(String customerId, String shopId);

    int countByCustomerIdAndIsReadByBuyerFalse(String customerId);

    int countByShopIdAndIsReadBySellerFalse(String shopId);
}
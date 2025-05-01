package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.chat.ChatDetailEntity;
import com.skripsi.siap_sewa.entity.chat.ChatHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChatHeaderRepository extends JpaRepository<ChatHeaderEntity, String> {
    @Query("SELECT c FROM ChatHeaderEntity c WHERE c.shopId = :shopId and c.customerId = :customerId")
    Optional<ChatHeaderEntity> findByCustomerIdAndShopId(@Param("customerId") String customerId,@Param("shopId") String shopId);

    Optional<ChatHeaderEntity> findByCustomerIdAndIsReport(String customerId, Boolean isReport);

    @Query("SELECT c FROM ChatHeaderEntity c WHERE c.customerId = :customerId")
    List<ChatHeaderEntity> findByCustomerId(@Param("customerId") String customerId);

    List<ChatHeaderEntity> findByShopId(String shopId);
    List<ChatHeaderEntity> findByIsReport(Boolean isReport);
}


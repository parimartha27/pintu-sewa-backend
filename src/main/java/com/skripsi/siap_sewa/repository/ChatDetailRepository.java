package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.chat.ChatDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChatDetailRepository extends JpaRepository<ChatDetailEntity, String> {
    @Query("SELECT c FROM ChatDetailEntity c WHERE c.chatHeader.id = :chatHeaderId ORDER BY c.createdAt DESC")
    List<ChatDetailEntity> findByChatHeaderIdOrderByCreatedAtDesc(@Param("chatHeaderId") String chatHeaderId);

}


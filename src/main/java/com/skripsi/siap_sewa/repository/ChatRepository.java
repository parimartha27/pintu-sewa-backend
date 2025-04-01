package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, String> {
}


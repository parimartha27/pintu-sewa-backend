package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.chat.ChatHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChatHeaderRepository extends JpaRepository<ChatHeaderEntity, String> {


}


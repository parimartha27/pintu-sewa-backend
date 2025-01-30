package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository  extends JpaRepository<ShopEntity, String> {
}

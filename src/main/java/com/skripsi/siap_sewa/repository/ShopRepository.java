package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository  extends JpaRepository<ShopEntity, String> {

    boolean existsByName(String name);

    List<ShopEntity> findByRegency(String regency);
}

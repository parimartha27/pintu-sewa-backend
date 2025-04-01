package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {
    List<ReviewEntity> findByProductId(String productId);

}

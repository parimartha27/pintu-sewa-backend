package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {

//    @Query("SELECT r FROM ReviewEntity r WHERE " +
//            "r.product.id = :productId AND " +
//            "(:hasMedia IS NULL OR r.image IS NOT NULL) AND " +
//            "(:rating IS NULL OR r.rating BETWEEN :rating AND :rating + 0.9) AND " +
//            "(:reviewTopics IS NULL OR " +
//            "   (LOWER(r.comment) LIKE '%kondisi barang%' AND 'kondisi barang' IN :reviewTopics) OR " +
//            "   (LOWER(r.comment) LIKE '%durasi pengiriman%' AND 'durasi pengiriman' IN :reviewTopics))")
//    Page<ReviewEntity> findByProductIdWithFilters(
//            @Param("productId") String productId,
//            @Param("hasMedia") Boolean hasMedia,
//            @Param("rating") Integer rating,
//            @Param("reviewTopics") List<String> reviewTopics,
//            Pageable pageable);

    @Query("""
    SELECT r FROM ReviewEntity r
    WHERE r.product.id = :productId
      AND (:hasMedia IS NULL OR r.image IS NOT NULL)
      AND (:rating IS NULL OR r.rating BETWEEN :rating AND :rating + 0.9)
      AND (
           :reviewTopics IS NULL OR
           (LOWER(r.comment) LIKE '%kondisi barang%' AND 'kondisi barang' IN :reviewTopics) OR 
           (LOWER(r.comment) LIKE '%durasi pengiriman%' AND 'durasi pengiriman' IN :reviewTopics)
      )
    ORDER BY r.createdAt DESC
""")
    Page<ReviewEntity> findByProductIdWithFilters(
            @Param("productId") String productId,
            @Param("hasMedia") Boolean hasMedia,
            @Param("rating") Integer rating,
            @Param("reviewTopics") List<String> reviewTopics,
            Pageable pageable);

    @Query("SELECT r FROM ReviewEntity r WHERE " +
            "r.product.shop.id = :shopId AND " +
            "(:hasMedia IS NULL OR r.image IS NOT NULL) AND " +
            "(:rating IS NULL OR r.rating BETWEEN :rating AND :rating + 0.9) AND " +
            "(:reviewTopics IS NULL OR " +
            "   (LOWER(r.comment) LIKE '%kondisi barang%' AND 'kondisi barang' IN :reviewTopics) OR " +
            "   (LOWER(r.comment) LIKE '%durasi pengiriman%' AND 'durasi pengiriman' IN :reviewTopics))")
    Page<ReviewEntity> findByProduct_Shop_IdWithFilters(
            @Param("shopId") String shopId,
            @Param("hasMedia") Boolean hasMedia,
            @Param("rating") Integer rating,
            @Param("reviewTopics") List<String> reviewTopics,
            Pageable pageable);

    @Query("SELECT r FROM ReviewEntity r WHERE " + "r.product.shop.id = :shopId")
    List<ReviewEntity> findByProduct_Shop_Id(@Param("shopId") String shopId);
}

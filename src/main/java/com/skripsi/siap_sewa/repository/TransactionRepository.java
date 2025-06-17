package com.skripsi.siap_sewa.repository;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String>,
        JpaSpecificationExecutor<TransactionEntity> {

    @Query("SELECT t FROM TransactionEntity t JOIN t.products p WHERE p.id = :productId")
    List<TransactionEntity> findByProductId(@Param("productId") String productId);
    List<TransactionEntity> findByShopId(String shopId);

    @Query("SELECT t FROM TransactionEntity t JOIN t.products p WHERE t.shopId = :shopId ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByShopIdOrderByCreatedAtDesc(@Param("shopId") String shopId);

    List<TransactionEntity> findByTransactionNumber(String transactionNumber);

    @Query("SELECT t FROM TransactionEntity t WHERE " +
            "t.transactionNumber = :transactionNumber AND " +
            "(:customerId IS NULL OR t.customer.id = :customerId) AND " +
            "(:shopId IS NULL OR t.shopId = :shopId)")
    List<TransactionEntity> findByTransactionNumberAndFilters(
            @Param("transactionNumber") String transactionNumber,
            @Param("customerId") String customerId,
            @Param("shopId") String shopId);

    List<TransactionEntity> findByTransactionNumberIn(Collection<String> transactionNumbers);

    @Query("select t from TransactionEntity t where t.id = ?1 and t.customer = ?2 and t.transactionNumber = ?3")
    Optional<TransactionEntity> findByIdAndCustomerAndTransactionNumber(String id, CustomerEntity customer, String transactionNumber);


}


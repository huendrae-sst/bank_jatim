package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.StockLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {

    @Query("SELECT sl FROM StockLedger sl JOIN FETCH sl.item JOIN FETCH sl.warehouse WHERE sl.item.id = :itemId ORDER BY sl.createdAt DESC")
    Page<StockLedger> findByItemIdOrderByCreatedAtDesc(@Param("itemId") Long itemId, Pageable pageable);

    @Query("SELECT sl FROM StockLedger sl JOIN FETCH sl.item JOIN FETCH sl.warehouse LEFT JOIN FETCH sl.createdByUser WHERE (:warehouseId IS NULL OR sl.warehouse.id = :warehouseId) AND (:transactionType IS NULL OR sl.transactionType = :transactionType) ORDER BY sl.createdAt DESC")
    Page<StockLedger> findByWarehouseAndTransactionType(@Param("warehouseId") Long warehouseId, @Param("transactionType") String transactionType, Pageable pageable);

    List<StockLedger> findByReferenceNumber(String referenceNumber);
}

package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.StockLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {

    @Query("SELECT sl FROM StockLedger sl JOIN FETCH sl.item JOIN FETCH sl.warehouse WHERE sl.item.id = :itemId ORDER BY sl.createdAt DESC")
    Page<StockLedger> findByItemIdOrderByCreatedAtDesc(@Param("itemId") Long itemId, Pageable pageable);

    @Query("SELECT sl FROM StockLedger sl JOIN FETCH sl.item JOIN FETCH sl.warehouse LEFT JOIN FETCH sl.createdByUser WHERE (:warehouseId IS NULL OR sl.warehouse.id = :warehouseId) AND (:transactionType IS NULL OR sl.transactionType = :transactionType) ORDER BY sl.createdAt DESC")
    Page<StockLedger> findByWarehouseAndTransactionType(@Param("warehouseId") Long warehouseId, @Param("transactionType") String transactionType, Pageable pageable);

    List<StockLedger> findByReferenceNumber(String referenceNumber);

    @Query(value = """
            SELECT TO_CHAR(created_at, 'YYYY-MM') as month_key,
                   COALESCE(SUM(qty_in), 0) as total_in,
                   COALESCE(SUM(qty_out), 0) as total_out
            FROM stock_ledgers
            WHERE created_at >= :startDate
            GROUP BY TO_CHAR(created_at, 'YYYY-MM')
            ORDER BY month_key ASC
            """, nativeQuery = true)
    List<Object[]> getMonthlyMovementsSummary(@Param("startDate") LocalDateTime startDate);

    @Query(value = """
            SELECT i.name, i.sku, i.uom, SUM(sl.qty_out) as total_qty
            FROM stock_ledgers sl
            JOIN items i ON i.id = sl.item_id
            WHERE sl.qty_out > 0
            GROUP BY i.id, i.name, i.sku, i.uom
            ORDER BY total_qty DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTopFastMovingItems();

    @Query(value = """
            SELECT i.name, i.sku, i.uom, SUM(sb.on_hand) as total_qty
            FROM stock_balances sb
            JOIN items i ON i.id = sb.item_id
            WHERE sb.on_hand > 0
            GROUP BY i.id, i.name, i.sku, i.uom
            ORDER BY total_qty DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTopOnHandItems();
}

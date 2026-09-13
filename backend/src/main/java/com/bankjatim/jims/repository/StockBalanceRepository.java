package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.StockBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    Optional<StockBalance> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);

    List<StockBalance> findByWarehouseId(Long warehouseId);

    @Query(value = """
            SELECT sb FROM StockBalance sb
            JOIN FETCH sb.item item
            JOIN FETCH item.category
            JOIN FETCH sb.warehouse
            WHERE (:warehouseId IS NULL OR sb.warehouse.id = :warehouseId)
            """,
            countQuery = """
            SELECT COUNT(sb) FROM StockBalance sb
            WHERE (:warehouseId IS NULL OR sb.warehouse.id = :warehouseId)
            """)
    Page<StockBalance> findAllWithDetails(@Param("warehouseId") Long warehouseId, Pageable pageable);

    @Query("SELECT sb FROM StockBalance sb JOIN FETCH sb.item item LEFT JOIN FETCH item.category JOIN FETCH sb.warehouse WHERE sb.onHand <= item.minStock")
    List<StockBalance> findLowStockItems();

    @Query(value = """
            SELECT COALESCE(SUM(sb.on_hand * i.estimated_unit_price), 0)
            FROM stock_balances sb
            JOIN items i ON i.id = sb.item_id
            """, nativeQuery = true)
    BigDecimal calculateTotalInventoryValuation();

    @Query("SELECT COUNT(sb) FROM StockBalance sb WHERE sb.onHand <= 0")
    long countOutOfStockItems();

    @Query("""
            SELECT sb FROM StockBalance sb
            JOIN FETCH sb.item item
            JOIN FETCH item.category
            ORDER BY sb.onHand DESC
            """)
    List<StockBalance> findTopStockBalances(Pageable pageable);
}

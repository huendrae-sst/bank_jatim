package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.EmbossRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbossRecordRepository extends JpaRepository<EmbossRecord, Long> {
    List<EmbossRecord> findByEmbossFileId(Long embossFileId);

    @Query("""
            SELECT r FROM EmbossRecord r
            JOIN FETCH r.embossFile
            LEFT JOIN FETCH r.item
            LEFT JOIN FETCH r.pinEnvelopeItem
            LEFT JOIN FETCH r.order
            WHERE r.embossFile.id = :fileId
            AND (:status IS NULL OR r.status = :status)
            ORDER BY r.id ASC
            """)
    List<EmbossRecord> findByEmbossFileIdWithDetails(@Param("fileId") Long fileId, @Param("status") String status);

    @Query("""
            SELECT r FROM EmbossRecord r
            JOIN FETCH r.embossFile
            LEFT JOIN FETCH r.item
            LEFT JOIN FETCH r.pinEnvelopeItem
            LEFT JOIN FETCH r.order
            WHERE r.status = 'REJECTED'
            ORDER BY r.id DESC
            """)
    List<EmbossRecord> findRejectedWithDetails();

    List<EmbossRecord> findByProductionOrderIdOrderByIdAsc(Long productionOrderId);

    @Query("""
            SELECT r FROM EmbossRecord r
            JOIN FETCH r.embossFile
            LEFT JOIN FETCH r.item
            LEFT JOIN FETCH r.producedItem
            LEFT JOIN FETCH r.order
            WHERE r.productionOrder.id = :productionOrderId
            ORDER BY r.id ASC
            """)
    List<EmbossRecord> findByProductionOrderIdWithDetails(@Param("productionOrderId") Long productionOrderId);

    List<EmbossRecord> findByProductionOrderIdAndProductionStatus(Long productionOrderId, String productionStatus);

    @Query("SELECT r FROM EmbossRecord r WHERE r.order.id IN :orderIds AND r.status = 'VALID' AND r.productionOrder IS NULL")
    List<EmbossRecord> findValidUnassignedByOrderIds(@Param("orderIds") List<Long> orderIds);
}

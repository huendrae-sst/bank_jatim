package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {

    @Query("""
        SELECT DISTINCT po FROM ProductionOrder po
        JOIN FETCH po.warehouse
        JOIN FETCH po.createdByUser
        LEFT JOIN FETCH po.embossFile
        LEFT JOIN FETCH po.approvedByUser
        LEFT JOIN FETCH po.materialIssuedByUser
        ORDER BY po.createdAt DESC
    """)
    List<ProductionOrder> findAllWithDetails();

    @Query("""
        SELECT po FROM ProductionOrder po
        JOIN FETCH po.warehouse
        JOIN FETCH po.createdByUser
        LEFT JOIN FETCH po.embossFile
        LEFT JOIN FETCH po.approvedByUser
        LEFT JOIN FETCH po.materialIssuedByUser
        WHERE po.id = :id
    """)
    Optional<ProductionOrder> findByIdWithDetails(@Param("id") Long id);

    Optional<ProductionOrder> findByProductionNumber(String productionNumber);

    List<ProductionOrder> findByStatus(String status);

    List<ProductionOrder> findByEmbossFileId(Long embossFileId);
}

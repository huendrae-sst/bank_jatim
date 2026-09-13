package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.SwitchingStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwitchingStockRepository extends JpaRepository<SwitchingStock, Long> {

    @Query("""
            SELECT DISTINCT s FROM SwitchingStock s
            LEFT JOIN FETCH s.order
            JOIN FETCH s.sourceOrganization
            JOIN FETCH s.sourceWarehouse
            JOIN FETCH s.destinationOrganization
            JOIN FETCH s.destinationWarehouse
            JOIN FETCH s.proposedByUser
            LEFT JOIN FETCH s.approvedByUser
            LEFT JOIN FETCH s.items si
            LEFT JOIN FETCH si.item
            WHERE (:status IS NULL OR s.status = :status)
            ORDER BY s.id DESC
            """)
    List<SwitchingStock> findAllWithDetails(@Param("status") String status);

    @Query("""
            SELECT DISTINCT s FROM SwitchingStock s
            LEFT JOIN FETCH s.order
            JOIN FETCH s.sourceOrganization
            JOIN FETCH s.sourceWarehouse
            JOIN FETCH s.destinationOrganization
            JOIN FETCH s.destinationWarehouse
            JOIN FETCH s.proposedByUser
            LEFT JOIN FETCH s.approvedByUser
            LEFT JOIN FETCH s.items si
            LEFT JOIN FETCH si.item
            WHERE s.id = :id
            """)
    Optional<SwitchingStock> findByIdWithDetails(@Param("id") Long id);
}

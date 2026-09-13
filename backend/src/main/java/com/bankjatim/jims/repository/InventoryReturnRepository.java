package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.InventoryReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryReturnRepository extends JpaRepository<InventoryReturn, Long> {

    @Query("SELECT DISTINCT r FROM InventoryReturn r " +
            "LEFT JOIN FETCH r.organization " +
            "LEFT JOIN FETCH r.destinationWarehouse " +
            "LEFT JOIN FETCH r.createdByUser " +
            "LEFT JOIN FETCH r.approvedByUser " +
            "LEFT JOIN FETCH r.items i " +
            "LEFT JOIN FETCH i.item " +
            "ORDER BY r.id DESC")
    List<InventoryReturn> findAllWithDetails();

    @Query("SELECT r FROM InventoryReturn r " +
            "LEFT JOIN FETCH r.organization " +
            "LEFT JOIN FETCH r.destinationWarehouse " +
            "LEFT JOIN FETCH r.createdByUser " +
            "LEFT JOIN FETCH r.approvedByUser " +
            "LEFT JOIN FETCH r.items i " +
            "LEFT JOIN FETCH i.item " +
            "WHERE r.id = :id")
    Optional<InventoryReturn> findByIdWithDetails(@Param("id") Long id);

    List<InventoryReturn> findByStatusOrderByIdDesc(String status);
}

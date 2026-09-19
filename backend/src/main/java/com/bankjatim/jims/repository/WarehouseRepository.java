package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findByOrganizationId(Long organizationId);
    List<Warehouse> findByIsActiveTrue();
    Optional<Warehouse> findByType(String type);
    Optional<Warehouse> findFirstByType(String type);

    @Query("SELECT w FROM Warehouse w LEFT JOIN FETCH w.organization ORDER BY w.code ASC")
    List<Warehouse> findAllWithOrganization();
}

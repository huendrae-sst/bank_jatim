package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.WarehousePacking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousePackingRepository extends JpaRepository<WarehousePacking, Long> {
    List<WarehousePacking> findByOrderId(Long orderId);
}

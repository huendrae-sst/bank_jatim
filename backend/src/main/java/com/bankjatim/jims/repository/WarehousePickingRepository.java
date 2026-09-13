package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.WarehousePicking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousePickingRepository extends JpaRepository<WarehousePicking, Long> {
    List<WarehousePicking> findByOrderId(Long orderId);
}

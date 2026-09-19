package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ProductionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionOrderItemRepository extends JpaRepository<ProductionOrderItem, Long> {

    @Query("SELECT poi FROM ProductionOrderItem poi JOIN FETCH poi.item WHERE poi.productionOrder.id = :productionOrderId")
    List<ProductionOrderItem> findByProductionOrderIdWithItem(@Param("productionOrderId") Long productionOrderId);

    List<ProductionOrderItem> findByProductionOrderId(Long productionOrderId);
}

package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ProductionOrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionOrderFulfillmentRepository extends JpaRepository<ProductionOrderFulfillment, Long> {

    @Query("SELECT pof FROM ProductionOrderFulfillment pof JOIN FETCH pof.order o JOIN FETCH o.requestingOrganization WHERE pof.productionOrder.id = :productionOrderId")
    List<ProductionOrderFulfillment> findByProductionOrderIdWithOrder(@Param("productionOrderId") Long productionOrderId);

    List<ProductionOrderFulfillment> findByOrderId(Long orderId);
}

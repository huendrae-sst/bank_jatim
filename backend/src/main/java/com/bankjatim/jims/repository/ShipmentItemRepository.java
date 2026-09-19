package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    List<ShipmentItem> findByShipmentId(Long shipmentId);
    List<ShipmentItem> findByOrderItemId(Long orderItemId);
    List<ShipmentItem> findByPurchaseRequestItemId(Long purchaseRequestItemId);
}

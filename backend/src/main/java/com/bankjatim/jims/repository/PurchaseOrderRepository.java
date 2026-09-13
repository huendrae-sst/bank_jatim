package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findByVendorId(Long vendorId);

    List<PurchaseOrder> findByStatus(String status);

    @Query("""
            SELECT po FROM PurchaseOrder po
            JOIN FETCH po.vendor
            JOIN FETCH po.warehouse
            ORDER BY po.orderDate DESC, po.id DESC
            """)
    List<PurchaseOrder> findAllWithDetails();
}

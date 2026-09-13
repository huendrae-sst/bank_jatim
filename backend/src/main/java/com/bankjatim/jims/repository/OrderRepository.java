package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.requestingOrganization
            JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.item
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    Page<Order> findByRequestingOrganizationId(Long organizationId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.requestingOrganization
            JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            WHERE (:organizationId IS NULL OR o.requestingOrganization.id = :organizationId)
            """,
            countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE (:organizationId IS NULL OR o.requestingOrganization.id = :organizationId)
            """)
    Page<Order> findAllWithDetails(@Param("organizationId") Long organizationId, Pageable pageable);

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.requestingOrganization
            JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.item
            WHERE o.status IN ('SUBMITTED', 'WAITING_APPROVAL')
            """)
    List<Order> findPendingApprovals();

    @Query("SELECT o FROM Order o WHERE o.status = 'APPROVED'")
    List<Order> findApprovedOrdersQueue();

    @Query("SELECT o FROM Order o WHERE o.status IN ('PICKING', 'PACKING')")
    List<Order> findWarehouseProcessingOrders();

    List<Order> findByStatus(String status);
}

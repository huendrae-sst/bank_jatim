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

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.requestingOrganization
            LEFT JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            """,
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithDetails(Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.requestingOrganization
            LEFT JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            WHERE (:organizationId IS NULL OR o.requestingOrganization.id = :organizationId)
            """,
            countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE (:organizationId IS NULL OR o.requestingOrganization.id = :organizationId)
            """)
    Page<Order> findAllWithDetails(@Param("organizationId") Long organizationId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.requestingOrganization
            LEFT JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            WHERE o.requestingOrganization.id IN :organizationIds
            """,
            countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE o.requestingOrganization.id IN :organizationIds
            """)
    Page<Order> findAllByOrganizationIdsIn(@Param("organizationIds") java.util.Collection<Long> organizationIds, Pageable pageable);

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

    @Query("SELECT o FROM Order o WHERE o.status = 'APPROVED' AND (:orderType IS NULL OR o.orderType = :orderType) ORDER BY o.createdAt ASC")
    List<Order> findApprovedOrdersQueue(@Param("orderType") String orderType);

    default List<Order> findApprovedOrdersQueue() {
        return findApprovedOrdersQueue(null);
    }

    @Query("SELECT o FROM Order o WHERE o.status IN ('PICKING', 'PACKING') AND (:orderType IS NULL OR o.orderType = :orderType) ORDER BY o.createdAt ASC")
    List<Order> findWarehouseProcessingOrders(@Param("orderType") String orderType);

    default List<Order> findWarehouseProcessingOrders() {
        return findWarehouseProcessingOrders(null);
    }

    @Query("SELECT o FROM Order o WHERE o.status = 'READY_TO_SHIP' AND (:orderType IS NULL OR o.orderType = :orderType) ORDER BY o.createdAt ASC")
    List<Order> findReadyToShipOrders(@Param("orderType") String orderType);

    default List<Order> findReadyToShipOrders() {
        return findReadyToShipOrders(null);
    }

    List<Order> findByOrderTypeOrderByCreatedAtDesc(String orderType);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.requestingOrganization
            LEFT JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.item
            WHERE o.orderType = 'EMBOSS_ORDER'
            ORDER BY o.createdAt DESC
            """)
    List<Order> findEmbossOrdersWithDetails();

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.requestingOrganization
            LEFT JOIN FETCH o.createdByUser
            LEFT JOIN FETCH o.approvedByUser
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.item
            WHERE o.orderType = 'ROUTINE_PUSH'
            ORDER BY o.createdAt DESC
            """)
    List<Order> findRoutineDistributionsWithDetails();

    List<Order> findByStatus(String status);
}

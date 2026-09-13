package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    Optional<PurchaseRequest> findByPrNumber(String prNumber);

    Page<PurchaseRequest> findByOrganizationId(Long organizationId, Pageable pageable);

    List<PurchaseRequest> findByStatus(String status);

    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.status = 'APPROVED'")
    List<PurchaseRequest> findApprovedPool();

    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.status IN ('SUBMITTED', 'WAITING_APPROVAL')")
    List<PurchaseRequest> findPendingApprovals();
}

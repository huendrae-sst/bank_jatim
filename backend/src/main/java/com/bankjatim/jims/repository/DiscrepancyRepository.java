package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Discrepancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscrepancyRepository extends JpaRepository<Discrepancy, Long> {
    List<Discrepancy> findByReceivingId(Long receivingId);
    List<Discrepancy> findByResolutionStatus(String status);
}

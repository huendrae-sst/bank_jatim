package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findBySettlementNumber(String settlementNumber);
    Optional<Settlement> findByOrderId(Long orderId);
    List<Settlement> findByStatus(String status);
}

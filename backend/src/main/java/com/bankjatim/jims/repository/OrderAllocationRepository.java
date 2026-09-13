package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.OrderAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAllocationRepository extends JpaRepository<OrderAllocation, Long> {
    List<OrderAllocation> findByOrderItemId(Long orderItemId);
}

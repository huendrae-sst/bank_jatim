package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Receiving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivingRepository extends JpaRepository<Receiving, Long> {
    Optional<Receiving> findByReceivingNumber(String receivingNumber);
    Optional<Receiving> findByShipmentId(Long shipmentId);
    List<Receiving> findByOrganizationId(Long organizationId);
}

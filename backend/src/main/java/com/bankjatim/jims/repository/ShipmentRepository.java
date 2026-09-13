package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByManifestNumber(String manifestNumber);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByStatus(String status);
    List<Shipment> findByDestinationOrganizationId(Long organizationId);
}

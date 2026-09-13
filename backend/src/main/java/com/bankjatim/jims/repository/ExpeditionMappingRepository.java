package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ExpeditionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpeditionMappingRepository extends JpaRepository<ExpeditionMapping, Long> {
    Optional<ExpeditionMapping> findByDestinationOrganizationId(Long destinationOrganizationId);
}

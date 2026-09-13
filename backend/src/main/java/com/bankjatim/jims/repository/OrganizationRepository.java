package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    List<Organization> findByIsActiveTrueOrderByCodeAsc();
    List<Organization> findByType(String type);
}

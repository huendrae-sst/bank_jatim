package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    List<Organization> findByIsActiveTrueOrderByCodeAsc();
    List<Organization> findByType(String type);

    List<Organization> findByRegionId(Long regionId);

    @Query("""
        SELECT o.id FROM Organization o
        WHERE o.region.id = :regionId
           OR (o.parent IS NOT NULL AND o.parent.region.id = :regionId)
    """)
    List<Long> findAffiliatedOrgIdsByRegionId(@Param("regionId") Long regionId);

    @Query("""
        SELECT o FROM Organization o
        LEFT JOIN FETCH o.parent
        LEFT JOIN FETCH o.region
        ORDER BY o.code ASC
    """)
    List<Organization> findAllWithParentAndRegion();
}

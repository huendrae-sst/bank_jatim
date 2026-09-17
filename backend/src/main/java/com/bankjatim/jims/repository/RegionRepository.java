package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCode(String code);
    List<Region> findByIsActiveTrueOrderByCodeAsc();
    List<Region> findAllByOrderByCodeAsc();
}

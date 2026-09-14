package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Role> findAllByOrderByNameAscCodeAsc();
}

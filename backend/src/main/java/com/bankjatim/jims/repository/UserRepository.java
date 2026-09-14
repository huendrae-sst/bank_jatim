package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.role LEFT JOIN FETCH u.organization LEFT JOIN FETCH u.warehouse WHERE u.email = :email")
    Optional<User> findByEmailWithOrgAndWarehouse(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role LEFT JOIN FETCH u.organization LEFT JOIN FETCH u.warehouse ORDER BY u.name ASC")
    List<User> findAllWithOrgAndWarehouse();

    Optional<User> findByEmail(String email);

    Optional<User> findByNip(String nip);

    boolean existsByEmail(String email);

    boolean existsByNip(String nip);

    long countByRole_Code(String roleCode);
}

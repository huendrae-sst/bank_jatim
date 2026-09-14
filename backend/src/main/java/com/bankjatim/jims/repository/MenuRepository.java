package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderBySortOrderAsc();

    Optional<Menu> findByCode(String code);

    boolean existsByCode(String code);

    List<Menu> findAllByCodeIn(List<String> codes);

    void deleteByCode(String code);
}

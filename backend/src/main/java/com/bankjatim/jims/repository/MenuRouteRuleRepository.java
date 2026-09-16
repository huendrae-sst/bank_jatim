package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.MenuRouteRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MenuRouteRuleRepository extends JpaRepository<MenuRouteRule, Long> {

    List<MenuRouteRule> findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(Collection<Long> menuIds);
}

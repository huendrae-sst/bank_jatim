package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.domain.MenuRouteRule;
import com.bankjatim.jims.dto.NavigationItemResponse;
import com.bankjatim.jims.dto.NavigationRouteRuleResponse;
import com.bankjatim.jims.repository.MenuRepository;
import com.bankjatim.jims.repository.MenuRouteRuleRepository;
import com.bankjatim.jims.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final MenuRepository menuRepository;
    private final MenuRouteRuleRepository ruleRepository;
    private final RoleMenuService roleMenuService;

    @Transactional(readOnly = true)
    public List<NavigationItemResponse> getNavigation(UserPrincipal principal) {
        List<Menu> menus = menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF");
        if (menus.isEmpty()) {
            return List.of();
        }

        Set<String> allowedCodes = new HashSet<>(
                roleMenuService.getMenuCodesForRole(principal.getRole()));
        List<MenuRouteRule> rules = ruleRepository
                .findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(
                        menus.stream().map(Menu::getId).toList());
        Map<Long, List<NavigationRouteRuleResponse>> rulesByMenu = rules.stream()
                .collect(Collectors.groupingBy(
                        rule -> rule.getMenu().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                NavigationRouteRuleResponse::from,
                                Collectors.toList()
                        )
                ));

        return menus.stream()
                .map(menu -> NavigationItemResponse.from(
                        menu,
                        allowedCodes.contains(menu.getCode()),
                        rulesByMenu.getOrDefault(menu.getId(), List.of())
                ))
                .toList();
    }
}

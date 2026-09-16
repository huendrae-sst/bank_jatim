package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.domain.MenuRouteRule;
import com.bankjatim.jims.domain.RouteMatchType;
import com.bankjatim.jims.dto.NavigationItemResponse;
import com.bankjatim.jims.dto.NavigationRouteRuleResponse;
import com.bankjatim.jims.repository.MenuRepository;
import com.bankjatim.jims.repository.MenuRouteRuleRepository;
import com.bankjatim.jims.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NavigationServiceTest {

    private final MenuRepository menuRepository = mock(MenuRepository.class);
    private final MenuRouteRuleRepository ruleRepository = mock(MenuRouteRuleRepository.class);
    private final RoleMenuService roleMenuService = mock(RoleMenuService.class);
    private final NavigationService service = new NavigationService(
            menuRepository,
            ruleRepository,
            roleMenuService
    );

    @Test
    void marksOnlyAssignedActiveMenusAsAllowedAndGroupsRulesByOwner() {
        Menu orders = menu(10L, "ORD_LIST", "/orders", 10);
        Menu approvals = menu(11L, "ORD_APPROVAL", "/orders/approvals", 11);
        when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
                .thenReturn(List.of(orders, approvals));
        when(roleMenuService.getMenuCodesForRole("REQUESTER_CABANG"))
                .thenReturn(List.of("ORD_LIST"));
        when(ruleRepository.findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(List.of(10L, 11L)))
                .thenReturn(List.of(
                        rule(orders, "/orders", RouteMatchType.PREFIX, 1),
                        rule(approvals, "/orders/approvals", RouteMatchType.EXACT, 1)
                ));

        List<NavigationItemResponse> result = service.getNavigation(principal("REQUESTER_CABANG"));

        assertThat(result)
                .extracting(NavigationItemResponse::code, NavigationItemResponse::allowed)
                .containsExactly(
                        tuple("ORD_LIST", true),
                        tuple("ORD_APPROVAL", false)
                );
        assertThat(result.getFirst().routeRules())
                .extracting(NavigationRouteRuleResponse::path)
                .containsExactly("/orders");
        assertThat(result.get(1).routeRules())
                .extracting(NavigationRouteRuleResponse::path)
                .containsExactly("/orders/approvals");
    }

    @Test
    void superAdminReceivesEveryActiveMenuAsAllowed() {
        Menu dashboard = menu(1L, "DASH_EXEC", "/dashboard", 1);
        Menu orders = menu(10L, "ORD_LIST", "/orders", 10);
        when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
                .thenReturn(List.of(dashboard, orders));
        when(roleMenuService.getMenuCodesForRole("SUPER_ADMIN"))
                .thenReturn(List.of("DASH_EXEC", "ORD_LIST"));
        when(ruleRepository.findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(List.of(1L, 10L)))
                .thenReturn(List.of());

        assertThat(service.getNavigation(principal("SUPER_ADMIN")))
                .allMatch(NavigationItemResponse::allowed);
    }

    @Test
    void returnsEmptyNavigationWithoutLoadingPermissionsWhenNoActiveMenuExists() {
        when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
                .thenReturn(List.of());

        assertThat(service.getNavigation(principal("USER_ADMIN"))).isEmpty();
        verifyNoInteractions(ruleRepository, roleMenuService);
    }

    private static Menu menu(Long id, String code, String path, int order) {
        Menu menu = Menu.builder()
                .code(code)
                .title(code)
                .module("Operations")
                .path(path)
                .icon("bi-circle")
                .sortOrder(order)
                .status("AKTIF")
                .build();
        menu.setId(id);
        return menu;
    }

    private static MenuRouteRule rule(
            Menu menu,
            String path,
            RouteMatchType matchType,
            int order
    ) {
        MenuRouteRule rule = MenuRouteRule.builder()
                .menu(menu)
                .path(path)
                .matchType(matchType)
                .sortOrder(order)
                .build();
        rule.setId((long) order);
        return rule;
    }

    private static UserPrincipal principal(String role) {
        return UserPrincipal.create(
                7L,
                "Ayu",
                "ayu@bankjatim.co.id",
                "NIP-7",
                "secret",
                role,
                2L,
                3L,
                BigDecimal.ZERO,
                true
        );
    }
}

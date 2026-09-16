package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Menu;

import java.util.List;

public record NavigationItemResponse(
        String code,
        String title,
        String module,
        String parentCode,
        String path,
        String icon,
        Integer order,
        boolean allowed,
        List<NavigationRouteRuleResponse> routeRules
) {
    public static NavigationItemResponse from(
            Menu menu,
            boolean allowed,
            List<NavigationRouteRuleResponse> routeRules
    ) {
        return new NavigationItemResponse(
                menu.getCode(),
                menu.getTitle(),
                menu.getModule(),
                menu.getParentCode(),
                menu.getPath(),
                menu.getIcon(),
                menu.getSortOrder(),
                allowed,
                routeRules
        );
    }
}

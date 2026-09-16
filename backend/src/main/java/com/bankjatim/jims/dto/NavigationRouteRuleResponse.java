package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.MenuRouteRule;
import com.bankjatim.jims.domain.RouteMatchType;

public record NavigationRouteRuleResponse(
        String path,
        RouteMatchType matchType,
        Integer order
) {
    public static NavigationRouteRuleResponse from(MenuRouteRule rule) {
        return new NavigationRouteRuleResponse(
                rule.getPath(),
                rule.getMatchType(),
                rule.getSortOrder()
        );
    }
}

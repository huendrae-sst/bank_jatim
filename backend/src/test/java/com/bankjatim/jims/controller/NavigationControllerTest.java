package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.RouteMatchType;
import com.bankjatim.jims.dto.NavigationItemResponse;
import com.bankjatim.jims.dto.NavigationRouteRuleResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.NavigationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NavigationControllerTest {

    @Test
    void navigationUsesAuthenticatedPrincipalAndReturnsProjection() {
        NavigationService service = mock(NavigationService.class);
        UserPrincipal principal = UserPrincipal.create(
                7L,
                "Ayu",
                "ayu@bankjatim.co.id",
                "NIP-7",
                "secret",
                "REQUESTER_CABANG",
                2L,
                3L,
                BigDecimal.ZERO,
                true
        );
        NavigationItemResponse item = new NavigationItemResponse(
                "ORD_LIST",
                "Daftar Order Cabang",
                "Permintaan & Order",
                null,
                "/orders",
                "bi-cart3",
                10,
                true,
                List.of(new NavigationRouteRuleResponse(
                        "/orders",
                        RouteMatchType.PREFIX,
                        1
                ))
        );
        when(service.getNavigation(principal)).thenReturn(List.of(item));

        ResponseEntity<ApiResponse<List<NavigationItemResponse>>> response =
                new NavigationController(service).getNavigation(principal);

        verify(service).getNavigation(principal);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(item);
    }

    @Test
    void navigationRejectsMissingAuthenticatedPrincipal() {
        NavigationService service = mock(NavigationService.class);

        assertThatThrownBy(() -> new NavigationController(service).getNavigation(null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);

        verifyNoInteractions(service);
    }
}

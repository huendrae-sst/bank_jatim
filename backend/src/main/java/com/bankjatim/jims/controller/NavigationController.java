package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.dto.NavigationItemResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/navigation")
@RequiredArgsConstructor
public class NavigationController {

    private final NavigationService navigationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> getNavigation(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated principal is required");
        }
        return ResponseEntity.ok(ApiResponse.ok(navigationService.getNavigation(principal)));
    }
}

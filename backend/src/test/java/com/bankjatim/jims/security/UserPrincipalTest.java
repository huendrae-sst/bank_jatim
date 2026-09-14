package com.bankjatim.jims.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void customRoleCodeBecomesSpringSecurityAuthority() {
        UserPrincipal principal = UserPrincipal.create(
                1L,
                "Custom User",
                "custom@example.test",
                null,
                "hash",
                "CUSTOM_AUDITOR",
                null,
                null,
                BigDecimal.ZERO,
                true
        );

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOM_AUDITOR");
    }
}

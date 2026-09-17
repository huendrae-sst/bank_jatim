package com.bankjatim.jims.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String nip;
    @JsonIgnore
    private final String password;
    private final String role;
    private final Long organizationId;
    private final Long warehouseId;
    private final Long regionId;
    private final String regionName;
    private final BigDecimal approvalLimit;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(Long id, String name, String email, String nip, String password,
                                       String role, Long organizationId, Long warehouseId,
                                       BigDecimal approvalLimit, boolean active) {
        return create(id, name, email, nip, password, role, organizationId, warehouseId, null, null, approvalLimit, active);
    }

    public static UserPrincipal create(Long id, String name, String email, String nip, String password,
                                       String role, Long organizationId, Long warehouseId,
                                       Long regionId, String regionName,
                                       BigDecimal approvalLimit, boolean active) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return UserPrincipal.builder()
                .id(id)
                .name(name)
                .email(email)
                .nip(nip)
                .password(password)
                .role(role)
                .organizationId(organizationId)
                .warehouseId(warehouseId)
                .regionId(regionId)
                .regionName(regionName)
                .approvalLimit(approvalLimit != null ? approvalLimit : BigDecimal.ZERO)
                .active(active)
                .authorities(Collections.singletonList(authority))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

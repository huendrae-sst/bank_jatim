package com.bankjatim.jims.service;

import com.bankjatim.jims.common.ConflictException;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.dto.RoleCreateRequest;
import com.bankjatim.jims.dto.RoleResponse;
import com.bankjatim.jims.dto.RoleUpdateRequest;
import com.bankjatim.jims.repository.RoleRepository;
import com.bankjatim.jims.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceTest {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleService service = new RoleService(roleRepository, userRepository);

    @Test
    void createRoleNormalizesCodeAndAlwaysCreatesCustomRole() {
        when(roleRepository.existsByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(21L);
            return role;
        });

        RoleResponse response = service.createRole(
                new RoleCreateRequest(" custom_auditor ", "  Auditor Custom  ", "  Audit terbatas  "));

        assertThat(response.id()).isEqualTo(21L);
        assertThat(response.code()).isEqualTo("CUSTOM_AUDITOR");
        assertThat(response.name()).isEqualTo("Auditor Custom");
        assertThat(response.description()).isEqualTo("Audit terbatas");
        assertThat(response.systemRole()).isFalse();
    }

    @Test
    void updateRoleChangesOnlyMutableFields() {
        Role role = role("CUSTOM_AUDITOR", false);
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        RoleResponse response = service.updateRole(
                "custom_auditor", new RoleUpdateRequest("Auditor Cabang", "Pemeriksa cabang"));

        assertThat(response.code()).isEqualTo("CUSTOM_AUDITOR");
        assertThat(response.name()).isEqualTo("Auditor Cabang");
        assertThat(response.description()).isEqualTo("Pemeriksa cabang");
    }

    @Test
    void deleteRoleRejectsSystemRole() {
        Role role = role("SUPER_ADMIN", true);
        when(roleRepository.findByCodeIgnoreCase("SUPER_ADMIN")).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> service.deleteRole("SUPER_ADMIN"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("sistem");

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void deleteRoleRejectsRoleAssignedToUsers() {
        Role role = role("CUSTOM_AUDITOR", false);
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(userRepository.countByRole_Code("CUSTOM_AUDITOR")).thenReturn(2L);

        assertThatThrownBy(() -> service.deleteRole("CUSTOM_AUDITOR"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("2 pengguna");

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void deleteRoleRemovesUnusedCustomRole() {
        Role role = role("CUSTOM_AUDITOR", false);
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(userRepository.countByRole_Code("CUSTOM_AUDITOR")).thenReturn(0L);

        service.deleteRole("CUSTOM_AUDITOR");

        verify(roleRepository).delete(role);
    }

    private static Role role(String code, boolean systemRole) {
        Role role = Role.builder()
                .code(code)
                .name(code.replace('_', ' '))
                .description("Deskripsi")
                .systemRole(systemRole)
                .build();
        role.setId(7L);
        return role;
    }
}

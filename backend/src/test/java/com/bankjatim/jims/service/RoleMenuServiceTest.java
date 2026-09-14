package com.bankjatim.jims.service;

import com.bankjatim.jims.common.BadRequestException;
import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.domain.RoleMenuPermission;
import com.bankjatim.jims.repository.MenuRepository;
import com.bankjatim.jims.repository.RoleMenuPermissionRepository;
import com.bankjatim.jims.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleMenuServiceTest {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final MenuRepository menuRepository = mock(MenuRepository.class);
    private final RoleMenuPermissionRepository permissionRepository = mock(RoleMenuPermissionRepository.class);
    private final RoleMenuService service = new RoleMenuService(roleRepository, menuRepository, permissionRepository);

    @Test
    void replaceRoleMenusValidatesEveryMenuBeforeRemovingExistingAssignments() {
        Role role = role("CUSTOM_AUDITOR");
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(menuRepository.findAllByCodeIn(List.of("MST_USERS", "MISSING")))
                .thenReturn(List.of(menu(11L, "MST_USERS")));

        assertThatThrownBy(() -> service.replaceRoleMenus(
                "CUSTOM_AUDITOR", List.of("MST_USERS", "MISSING")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("MISSING");

        verify(permissionRepository, never()).deleteAllByRole_Id(5L);
    }

    @Test
    void replaceRoleMenusDeduplicatesCodesAndReturnsPersistedOrdering() {
        Role role = role("CUSTOM_AUDITOR");
        Menu users = menu(11L, "MST_USERS");
        Menu menus = menu(12L, "MST_MENUS");
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(menuRepository.findAllByCodeIn(List.of("MST_USERS", "MST_MENUS")))
                .thenReturn(List.of(users, menus));
        when(permissionRepository.findAllByRole_CodeOrderByMenu_SortOrderAsc("CUSTOM_AUDITOR"))
                .thenReturn(List.of(permission(role, users), permission(role, menus)));
        AtomicReference<Iterable<?>> saved = new AtomicReference<>();
        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            return List.of();
        }).when(permissionRepository).saveAll(anyList());

        List<String> result = service.replaceRoleMenus(
                "custom_auditor", List.of("MST_USERS", "MST_USERS", "MST_MENUS"));

        assertThat(StreamSupport.stream(saved.get().spliterator(), false)).hasSize(2);
        assertThat(result).containsExactly("MST_USERS", "MST_MENUS");
    }

    @Test
    void replaceRoleMenusAcceptsEmptyListAsClearAll() {
        Role role = role("CUSTOM_AUDITOR");
        when(roleRepository.findByCodeIgnoreCase("CUSTOM_AUDITOR")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllByRole_CodeOrderByMenu_SortOrderAsc("CUSTOM_AUDITOR"))
                .thenReturn(List.of());

        List<String> result = service.replaceRoleMenus("CUSTOM_AUDITOR", List.of());

        verify(permissionRepository).deleteAllByRole_Id(5L);
        verify(permissionRepository, never()).saveAll(anyList());
        assertThat(result).isEmpty();
    }

    private static Role role(String code) {
        Role role = Role.builder().code(code).name("Custom Auditor").systemRole(false).build();
        role.setId(5L);
        return role;
    }

    private static Menu menu(Long id, String code) {
        Menu menu = Menu.builder()
                .code(code)
                .title(code)
                .module("Master Data")
                .path("/" + code.toLowerCase())
                .sortOrder(id.intValue())
                .build();
        menu.setId(id);
        return menu;
    }

    private static RoleMenuPermission permission(Role role, Menu menu) {
        return RoleMenuPermission.of(role, menu);
    }
}

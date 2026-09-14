package com.bankjatim.jims.service;

import com.bankjatim.jims.common.BadRequestException;
import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.domain.RoleMenuPermission;
import com.bankjatim.jims.dto.RoleMenuResponse;
import com.bankjatim.jims.repository.MenuRepository;
import com.bankjatim.jims.repository.RoleMenuPermissionRepository;
import com.bankjatim.jims.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleMenuService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final RoleMenuPermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<String> getMenuCodesForRole(String roleCode) {
        Role role = findRole(roleCode);
        if ("SUPER_ADMIN".equals(role.getCode())) {
            return menuRepository.findAllByOrderBySortOrderAsc().stream().map(Menu::getCode).toList();
        }
        return permissionRepository.findAllByRole_CodeOrderByMenu_SortOrderAsc(role.getCode()).stream()
                .map(permission -> permission.getMenu().getCode())
                .toList();
    }

    @Transactional
    public List<String> replaceRoleMenus(String roleCode, List<String> selectedMenuCodes) {
        Role role = findRole(roleCode);
        List<Menu> menus = "SUPER_ADMIN".equals(role.getCode())
                ? menuRepository.findAllByOrderBySortOrderAsc()
                : resolveMenus(selectedMenuCodes);

        permissionRepository.deleteAllByRole_Id(role.getId());
        if (!menus.isEmpty()) {
            permissionRepository.saveAll(menus.stream()
                    .map(menu -> RoleMenuPermission.of(role, menu))
                    .toList());
        }
        return getMenuCodesForRole(role.getCode());
    }

    @Transactional(readOnly = true)
    public List<RoleMenuResponse> getAllMappings() {
        return permissionRepository.findAllByOrderByRole_CodeAscMenu_SortOrderAsc().stream()
                .map(RoleMenuResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> getRoleCodesByMenuIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Map.of();
        }
        return permissionRepository.findAllByMenu_IdIn(menuIds).stream()
                .collect(Collectors.groupingBy(
                        permission -> permission.getMenu().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(permission -> permission.getRole().getCode(), Collectors.toList())
                ));
    }

    @Transactional
    public List<String> replaceMenuRoles(Menu menu, List<String> selectedRoleCodes) {
        Set<String> codes = normalizeCodes(selectedRoleCodes);
        codes.add("SUPER_ADMIN");
        List<Role> roles = resolveRoles(codes);
        permissionRepository.deleteAllByMenu_Id(menu.getId());
        permissionRepository.saveAll(roles.stream()
                .map(role -> RoleMenuPermission.of(role, menu))
                .toList());
        return roles.stream().map(Role::getCode).sorted().toList();
    }

    @Transactional
    public void deleteMenuAssignments(Long menuId) {
        permissionRepository.deleteAllByMenu_Id(menuId);
    }

    private Role findRole(String roleCode) {
        String code = RoleService.normalizeCode(roleCode);
        return roleRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new com.bankjatim.jims.common.ResourceNotFoundException("Role tidak ditemukan: " + code));
    }

    private List<Menu> resolveMenus(List<String> selectedMenuCodes) {
        Set<String> codes = normalizeCodes(selectedMenuCodes);
        if (codes.isEmpty()) {
            return List.of();
        }
        List<String> requested = new ArrayList<>(codes);
        Map<String, Menu> found = menuRepository.findAllByCodeIn(requested).stream()
                .collect(Collectors.toMap(Menu::getCode, menu -> menu));
        List<String> missing = requested.stream().filter(code -> !found.containsKey(code)).toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("Menu tidak ditemukan: " + String.join(", ", missing));
        }
        return requested.stream().map(found::get).toList();
    }

    private List<Role> resolveRoles(Set<String> codes) {
        List<Role> roles = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String code : codes) {
            roleRepository.findByCodeIgnoreCase(code).ifPresentOrElse(roles::add, () -> missing.add(code));
        }
        if (!missing.isEmpty()) {
            throw new BadRequestException("Role tidak ditemukan: " + String.join(", ", missing));
        }
        return roles;
    }

    private static Set<String> normalizeCodes(List<String> values) {
        if (values == null) {
            return new LinkedHashSet<>();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

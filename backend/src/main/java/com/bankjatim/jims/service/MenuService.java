package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.dto.MenuRequest;
import com.bankjatim.jims.dto.MenuResponse;
import com.bankjatim.jims.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuService roleMenuService;

    @Transactional(readOnly = true)
    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuRepository.findAllByOrderBySortOrderAsc();
        Map<Long, List<String>> roleCodes = roleMenuService.getRoleCodesByMenuIds(
                menus.stream().map(Menu::getId).toList());
        return menus.stream()
                .map(menu -> toResponse(menu, roleCodes.getOrDefault(menu.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Menu findByIdOrCode(String idOrCode) {
        // Try finding by ID if numeric
        try {
            Long id = Long.parseLong(idOrCode.replace("m-", ""));
            Optional<Menu> byId = menuRepository.findById(id);
            if (byId.isPresent()) return byId.get();
        } catch (NumberFormatException ignored) {
        }
        return menuRepository.findByCode(idOrCode)
                .orElseThrow(() -> new IllegalArgumentException("Menu tidak ditemukan dengan identifier: " + idOrCode));
    }

    @Transactional
    public MenuResponse createMenu(MenuRequest request) {
        String code = request.getCode().trim().toUpperCase().replaceAll("\\s+", "_");
        if (menuRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Kode menu '" + code + "' sudah digunakan.");
        }

        Menu menu = Menu.builder()
                .code(code)
                .title(request.getTitle().trim())
                .module(request.getModule())
                .parentCode(request.getParentCode())
                .path(request.getPath().trim())
                .icon(request.getIcon() != null ? request.getIcon() : "bi-circle")
                .sortOrder(request.getOrder() != null ? request.getOrder() : 1)
                .status(request.getStatus() != null ? request.getStatus() : "AKTIF")
                .description(request.getDescription())
                .build();

        Menu saved = menuRepository.save(menu);
        List<String> roles = roleMenuService.replaceMenuRoles(saved, request.getRoles());
        return toResponse(saved, roles);
    }

    @Transactional
    public MenuResponse updateMenu(String idOrCode, MenuRequest request) {
        Menu menu = findByIdOrCode(idOrCode);

        menu.setTitle(request.getTitle().trim());
        menu.setModule(request.getModule());
        menu.setParentCode(request.getParentCode());
        menu.setPath(request.getPath().trim());
        if (request.getIcon() != null) menu.setIcon(request.getIcon());
        if (request.getOrder() != null) menu.setSortOrder(request.getOrder());
        if (request.getStatus() != null) menu.setStatus(request.getStatus());
        if (request.getDescription() != null) menu.setDescription(request.getDescription());
        Menu updated = menuRepository.save(menu);
        List<String> roles = request.getRoles() == null
                ? roleMenuService.getRoleCodesByMenuIds(List.of(updated.getId())).getOrDefault(updated.getId(), List.of())
                : roleMenuService.replaceMenuRoles(updated, request.getRoles());
        return toResponse(updated, roles);
    }

    @Transactional
    public void deleteMenu(String idOrCode) {
        Menu menu = findByIdOrCode(idOrCode);
        roleMenuService.deleteMenuAssignments(menu.getId());
        menuRepository.delete(menu);
    }

    @Transactional
    public String toggleStatus(String idOrCode) {
        Menu menu = findByIdOrCode(idOrCode);
        String nextStatus = "AKTIF".equalsIgnoreCase(menu.getStatus()) ? "NONAKTIF" : "AKTIF";
        menu.setStatus(nextStatus);
        menuRepository.save(menu);
        return nextStatus;
    }

    @Transactional
    public List<String> updateRoles(String idOrCode, List<String> roles) {
        Menu menu = findByIdOrCode(idOrCode);
        return roleMenuService.replaceMenuRoles(menu, roles);
    }

    @Transactional(readOnly = true)
    public List<String> getMenuCodesForRole(String roleCode) {
        return roleMenuService.getMenuCodesForRole(roleCode);
    }

    @Transactional
    public List<String> updateRoleMenus(String roleCode, List<String> selectedMenuCodes) {
        return roleMenuService.replaceRoleMenus(roleCode, selectedMenuCodes);
    }

    private MenuResponse toResponse(Menu menu, List<String> roles) {
        return MenuResponse.builder()
                .id(String.valueOf(menu.getId()))
                .code(menu.getCode())
                .title(menu.getTitle())
                .module(menu.getModule())
                .parentCode(menu.getParentCode())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .order(menu.getSortOrder())
                .status(menu.getStatus())
                .description(menu.getDescription())
                .roles(roles)
                .build();
    }
}

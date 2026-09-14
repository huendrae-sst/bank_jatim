package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.dto.MenuRequest;
import com.bankjatim.jims.dto.MenuResponse;
import com.bankjatim.jims.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuResponse> getAllMenus() {
        return menuRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toResponse)
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

        menu.setRoleList(request.getRoles() != null && !request.getRoles().isEmpty()
                ? request.getRoles()
                : List.of("SUPER_ADMIN"));

        Menu saved = menuRepository.save(menu);
        return toResponse(saved);
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
        if (request.getRoles() != null) menu.setRoleList(request.getRoles());

        Menu updated = menuRepository.save(menu);
        return toResponse(updated);
    }

    @Transactional
    public void deleteMenu(String idOrCode) {
        Menu menu = findByIdOrCode(idOrCode);
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
        menu.setRoleList(roles);
        menuRepository.save(menu);
        return menu.getRoleList();
    }

    @Transactional(readOnly = true)
    public List<String> getMenuCodesForRole(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanRole = roleCode.trim().toUpperCase();
        return menuRepository.findAll().stream()
                .filter(m -> "SUPER_ADMIN".equals(cleanRole) || m.getRoleList().contains(cleanRole))
                .map(Menu::getCode)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<String> updateRoleMenus(String roleCode, List<String> selectedMenuCodes) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Role code cannot be empty");
        }
        String cleanRole = roleCode.trim().toUpperCase();

        // Ensure SUPER_ADMIN cannot lose access
        if ("SUPER_ADMIN".equals(cleanRole)) {
            List<Menu> all = menuRepository.findAll();
            for (Menu menu : all) {
                List<String> roles = new ArrayList<>(menu.getRoleList());
                if (!roles.contains("SUPER_ADMIN")) {
                    roles.add("SUPER_ADMIN");
                    menu.setRoleList(roles);
                    menuRepository.save(menu);
                }
            }
            return all.stream().map(Menu::getCode).collect(Collectors.toList());
        }

        Set<String> selectedSet = selectedMenuCodes != null ? new HashSet<>(selectedMenuCodes) : Collections.emptySet();
        List<Menu> allMenus = menuRepository.findAll();
        List<String> resultAssignedCodes = new ArrayList<>();

        for (Menu menu : allMenus) {
            List<String> currentRoles = new ArrayList<>(menu.getRoleList());
            boolean hasRole = currentRoles.contains(cleanRole);
            boolean shouldHaveRole = selectedSet.contains(menu.getCode());

            if (shouldHaveRole && !hasRole) {
                currentRoles.add(cleanRole);
                menu.setRoleList(currentRoles);
                menuRepository.save(menu);
            } else if (!shouldHaveRole && hasRole) {
                currentRoles.remove(cleanRole);
                menu.setRoleList(currentRoles);
                menuRepository.save(menu);
            }

            if (shouldHaveRole) {
                resultAssignedCodes.add(menu.getCode());
            }
        }
        return resultAssignedCodes;
    }

    private MenuResponse toResponse(Menu menu) {
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
                .roles(menu.getRoleList())
                .build();
    }
}

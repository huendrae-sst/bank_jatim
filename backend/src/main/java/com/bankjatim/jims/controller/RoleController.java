package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.dto.RoleCreateRequest;
import com.bankjatim.jims.dto.RoleMenuResponse;
import com.bankjatim.jims.dto.RoleResponse;
import com.bankjatim.jims.dto.RoleUpdateRequest;
import com.bankjatim.jims.service.RoleMenuService;
import com.bankjatim.jims.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMenuService roleMenuService;

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getRoles()));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Role berhasil dibuat", roleService.createRole(request)));
    }

    @PutMapping("/roles/{code}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable String code, @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Role berhasil diperbarui", roleService.updateRole(code, request)));
    }

    @DeleteMapping("/roles/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String code) {
        roleService.deleteRole(code);
        return ResponseEntity.ok(ApiResponse.ok("Role berhasil dihapus", null));
    }

    @GetMapping("/roles/{roleCode}/menus")
    public ResponseEntity<ApiResponse<List<String>>> getRoleMenus(@PathVariable String roleCode) {
        return ResponseEntity.ok(ApiResponse.ok(roleMenuService.getMenuCodesForRole(roleCode)));
    }

    @PutMapping("/roles/{roleCode}/menus")
    public ResponseEntity<ApiResponse<List<String>>> updateRoleMenus(
            @PathVariable String roleCode, @RequestBody List<String> menuCodes) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Hak akses menu untuk peran " + roleCode + " berhasil disimpan",
                roleMenuService.replaceRoleMenus(roleCode, menuCodes)));
    }

    @GetMapping("/role-menus")
    public ResponseEntity<ApiResponse<List<RoleMenuResponse>>> getRoleMenuMappings() {
        return ResponseEntity.ok(ApiResponse.ok(roleMenuService.getAllMappings()));
    }
}

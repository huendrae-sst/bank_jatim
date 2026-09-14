package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.ExpeditionMappingRequest;
import com.bankjatim.jims.dto.ExpeditionMappingResponse;
import com.bankjatim.jims.dto.MenuRequest;
import com.bankjatim.jims.dto.MenuResponse;
import com.bankjatim.jims.dto.UserRequest;
import com.bankjatim.jims.dto.UserResponse;
import com.bankjatim.jims.repository.*;
import com.bankjatim.jims.service.ExpeditionMappingService;
import com.bankjatim.jims.service.MenuService;
import com.bankjatim.jims.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "Endpoints untuk Data Referensi: Barang, Kategori, Organisasi, Gudang, Pagu Anggaran, Vendor")
public class MasterDataController {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final WarehouseRepository warehouseRepository;
    private final BudgetRepository budgetRepository;
    private final VendorRepository vendorRepository;
    private final CourierRepository courierRepository;
    private final UserService userService;
    private final ExpeditionMappingService expeditionMappingService;
    private final MenuService menuService;

    @GetMapping("/items")
    @Operation(summary = "Daftar Master Barang")
    public ResponseEntity<ApiResponse<List<Item>>> getItems() {
        return ResponseEntity.ok(ApiResponse.ok(itemRepository.findAllWithCategory()));
    }

    @GetMapping("/categories")
    @Operation(summary = "Daftar Kategori Barang")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.findAll()));
    }

    @GetMapping("/organizations")
    @Operation(summary = "Daftar Unit Kerja & Cabang")
    public ResponseEntity<ApiResponse<List<Organization>>> getOrganizations() {
        return ResponseEntity.ok(ApiResponse.ok(organizationRepository.findAll()));
    }

    @GetMapping("/users")
    @Operation(summary = "Daftar Pengguna JIMS")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUsers()));
    }

    @PostMapping("/users")
    @Operation(summary = "Tambah Pengguna JIMS")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pengguna berhasil dibuat", userService.createUser(request)));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Ubah Pengguna JIMS")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pengguna berhasil diperbarui", userService.updateUser(id, request)));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Hapus Pengguna JIMS")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("Pengguna berhasil dihapus", null));
    }

    @GetMapping("/warehouses")
    @Operation(summary = "Daftar Gudang Logistik")
    public ResponseEntity<ApiResponse<List<Warehouse>>> getWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok(warehouseRepository.findAllWithOrganization()));
    }

    @GetMapping("/budgets")
    @Operation(summary = "Daftar Pagu Anggaran Unit")
    public ResponseEntity<ApiResponse<List<Budget>>> getBudgets() {
        return ResponseEntity.ok(ApiResponse.ok(budgetRepository.findAllWithOrganization()));
    }

    @GetMapping("/vendors")
    @Operation(summary = "Daftar Vendor Rekanan")
    public ResponseEntity<ApiResponse<List<Vendor>>> getVendors() {
        return ResponseEntity.ok(ApiResponse.ok(vendorRepository.findAll()));
    }

    @GetMapping("/couriers")
    @Operation(summary = "Daftar Mitra Ekspedisi")
    public ResponseEntity<ApiResponse<List<Courier>>> getCouriers() {
        return ResponseEntity.ok(ApiResponse.ok(courierRepository.findAll()));
    }

    @GetMapping("/expedition-mappings")
    @Operation(summary = "Daftar Pemetaan Ekspedisi Cabang")
    public ResponseEntity<ApiResponse<List<ExpeditionMappingResponse>>> getExpeditionMappings() {
        return ResponseEntity.ok(ApiResponse.ok(expeditionMappingService.getMappings()));
    }

    @PostMapping("/expedition-mappings")
    @Operation(summary = "Tambah Pemetaan Ekspedisi Cabang")
    public ResponseEntity<ApiResponse<ExpeditionMappingResponse>> createExpeditionMapping(
            @Valid @RequestBody ExpeditionMappingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pemetaan ekspedisi berhasil dibuat", expeditionMappingService.createMapping(request)));
    }

    @PutMapping("/expedition-mappings/{id}")
    @Operation(summary = "Ubah Pemetaan Ekspedisi Cabang")
    public ResponseEntity<ApiResponse<ExpeditionMappingResponse>> updateExpeditionMapping(
            @PathVariable Long id, @Valid @RequestBody ExpeditionMappingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pemetaan ekspedisi berhasil diperbarui",
                expeditionMappingService.updateMapping(id, request)));
    }

    @DeleteMapping("/expedition-mappings/{id}")
    @Operation(summary = "Hapus Pemetaan Ekspedisi Cabang")
    public ResponseEntity<ApiResponse<Void>> deleteExpeditionMapping(@PathVariable Long id) {
        expeditionMappingService.deleteMapping(id);
        return ResponseEntity.ok(ApiResponse.ok("Pemetaan ekspedisi berhasil dihapus", null));
    }

    @GetMapping("/menus")
    @Operation(summary = "Daftar Menu Sistem & Navigasi RBAC")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenus() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllMenus()));
    }

    @PostMapping("/menus")
    @Operation(summary = "Tambah Menu Sistem Baru")
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody MenuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Menu berhasil dibuat", menuService.createMenu(request)));
    }

    @PutMapping("/menus/{id}")
    @Operation(summary = "Ubah Konfigurasi Menu Sistem")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable String id, @Valid @RequestBody MenuRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Menu berhasil diperbarui", menuService.updateMenu(id, request)));
    }

    @DeleteMapping("/menus/{id}")
    @Operation(summary = "Hapus Menu Sistem")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.ok("Menu berhasil dihapus", null));
    }

    @PatchMapping("/menus/{id}/status")
    @Operation(summary = "Ubah Status Aktif/Nonaktif Menu")
    public ResponseEntity<ApiResponse<String>> toggleMenuStatus(@PathVariable String id) {
        String status = menuService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Status menu berhasil diubah", status));
    }

    @PutMapping("/menus/{id}/roles")
    @Operation(summary = "Ubah Matriks Hak Akses Peran untuk Menu")
    public ResponseEntity<ApiResponse<List<String>>> updateMenuRoles(
            @PathVariable String id, @RequestBody List<String> roles) {
        return ResponseEntity.ok(ApiResponse.ok("Hak akses menu berhasil diperbarui", menuService.updateRoles(id, roles)));
    }

}

package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.ExpeditionMappingRequest;
import com.bankjatim.jims.dto.ExpeditionMappingResponse;
import com.bankjatim.jims.dto.MenuRequest;
import com.bankjatim.jims.dto.MenuResponse;
import com.bankjatim.jims.dto.OrganizationResponse;
import com.bankjatim.jims.dto.UserRequest;
import com.bankjatim.jims.dto.UserResponse;
import com.bankjatim.jims.dto.WarehouseResponse;
import com.bankjatim.jims.repository.*;
import com.bankjatim.jims.service.ExpeditionMappingService;
import com.bankjatim.jims.service.MenuService;
import com.bankjatim.jims.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.bankjatim.jims.dto.RegionRequest;
import com.bankjatim.jims.dto.RegionResponse;
import com.bankjatim.jims.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/master")
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
    private final RegionRepository regionRepository;
    private final RegionService regionService;

    @Autowired
    public MasterDataController(ItemRepository itemRepository,
                                CategoryRepository categoryRepository,
                                OrganizationRepository organizationRepository,
                                WarehouseRepository warehouseRepository,
                                BudgetRepository budgetRepository,
                                VendorRepository vendorRepository,
                                CourierRepository courierRepository,
                                UserService userService,
                                ExpeditionMappingService expeditionMappingService,
                                MenuService menuService,
                                RegionRepository regionRepository,
                                RegionService regionService) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
        this.warehouseRepository = warehouseRepository;
        this.budgetRepository = budgetRepository;
        this.vendorRepository = vendorRepository;
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.expeditionMappingService = expeditionMappingService;
        this.menuService = menuService;
        this.regionRepository = regionRepository;
        this.regionService = regionService;
    }

    public MasterDataController(ItemRepository itemRepository,
                                CategoryRepository categoryRepository,
                                OrganizationRepository organizationRepository,
                                WarehouseRepository warehouseRepository,
                                BudgetRepository budgetRepository,
                                VendorRepository vendorRepository,
                                CourierRepository courierRepository,
                                UserService userService,
                                ExpeditionMappingService expeditionMappingService,
                                MenuService menuService) {
        this(itemRepository, categoryRepository, organizationRepository, warehouseRepository, budgetRepository, vendorRepository, courierRepository, userService, expeditionMappingService, menuService, null, null);
    }

    @GetMapping("/regions")
    @Operation(summary = "Daftar Master Wilayah & Pemetaan Cabang")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getRegions() {
        if (regionService != null) {
            return ResponseEntity.ok(ApiResponse.ok(regionService.getRegions()));
        }
        List<RegionResponse> regions = regionRepository != null
                ? regionRepository.findAllByOrderByCodeAsc().stream()
                .map(r -> RegionResponse.from(r, List.of()))
                .toList()
                : List.of();
        return ResponseEntity.ok(ApiResponse.ok(regions));
    }

    @GetMapping("/regions/{id}")
    @Operation(summary = "Detail Master Wilayah")
    public ResponseEntity<ApiResponse<RegionResponse>> getRegionById(@PathVariable Long id) {
        if (regionService == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.ok(regionService.getRegionById(id)));
    }

    @PostMapping("/regions")
    @Operation(summary = "Tambah Master Wilayah Baru")
    public ResponseEntity<ApiResponse<RegionResponse>> createRegion(@Valid @RequestBody RegionRequest request) {
        if (regionService == null) {
            throw new IllegalStateException("RegionService belum terkonfigurasi");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Wilayah berhasil dibuat", regionService.createRegion(request)));
    }

    @PutMapping("/regions/{id}")
    @Operation(summary = "Ubah Master Wilayah")
    public ResponseEntity<ApiResponse<RegionResponse>> updateRegion(
            @PathVariable Long id, @Valid @RequestBody RegionRequest request) {
        if (regionService == null) {
            throw new IllegalStateException("RegionService belum terkonfigurasi");
        }
        return ResponseEntity.ok(ApiResponse.ok("Wilayah berhasil diperbarui", regionService.updateRegion(id, request)));
    }

    @DeleteMapping("/regions/{id}")
    @Operation(summary = "Hapus Master Wilayah")
    public ResponseEntity<ApiResponse<Void>> deleteRegion(@PathVariable Long id) {
        if (regionService != null) {
            regionService.deleteRegion(id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Wilayah berhasil dihapus", null));
    }

    @PutMapping("/regions/{id}/branches")
    @Operation(summary = "Petakan Cabang ke Wilayah")
    public ResponseEntity<ApiResponse<RegionResponse>> assignBranches(
            @PathVariable Long id, @RequestBody List<Long> organizationIds) {
        if (regionService == null) {
            throw new IllegalStateException("RegionService belum terkonfigurasi");
        }
        return ResponseEntity.ok(ApiResponse.ok("Pemetaan cabang berhasil diperbarui", regionService.assignBranches(id, organizationIds)));
    }

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
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getOrganizations() {
        List<OrganizationResponse> responses = organizationRepository.findAllWithParentAndRegion().stream()
                .map(OrganizationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
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
    @Transactional
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehouses(
            @RequestParam(required = false) Long organizationId) {
        List<Warehouse> warehouses;
        if (organizationId != null) {
            warehouses = warehouseRepository.findByOrganizationId(organizationId);
        } else {
            warehouses = warehouseRepository.findAllWithOrganization();
            if (warehouses.isEmpty()) {
                warehouses = warehouseRepository.findAll();
            }
        }
        if (warehouses.isEmpty()) {
            List<Organization> orgs = organizationRepository.findAll();
            if (!orgs.isEmpty()) {
                List<Warehouse> toSave = new ArrayList<>();
                for (Organization org : orgs) {
                    String cleanCode = org.getCode().replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
                    String whCode = "WH_" + cleanCode;
                    if (warehouseRepository.findByCode(whCode).isEmpty()) {
                        Warehouse wh = Warehouse.builder()
                                .organization(org)
                                .code(whCode)
                                .name("Gudang Logistik " + org.getName())
                                .type("HEAD_OFFICE".equalsIgnoreCase(org.getType()) ? "CENTRAL_LOGISTICS" : "BRANCH_STORAGE")
                                .address(org.getAddress() != null ? org.getAddress() : "Surabaya, Jawa Timur")
                                .isActive(true)
                                .build();
                        toSave.add(wh);
                    }
                }
                if (!toSave.isEmpty()) {
                    warehouseRepository.saveAll(toSave);
                    warehouses = organizationId != null
                            ? warehouseRepository.findByOrganizationId(organizationId)
                            : warehouseRepository.findAllWithOrganization();
                    if (warehouses.isEmpty()) {
                        warehouses = warehouseRepository.findAll();
                    }
                }
            }
        }
        List<WarehouseResponse> responses = warehouses.stream()
                .map(WarehouseResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
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

<template>
  <div class="role-menus-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Pemetaan Hak Akses Menu per Peran</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/master/roles" class="text-decoration-none text-body">Manajemen Peran</router-link>
              </li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">
                Pemetaan Menu
              </li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Alert Banner -->
    <div v-if="alertMessage" class="alert alert-dismissible fade show shadow-xs fs-8 py-2 px-3 mb-3" :class="alertClass" role="alert">
      <i :class="alertIcon" class="me-2 fs-7"></i>
      <span>{{ alertMessage }}</span>
      <button type="button" class="btn-close py-2" @click="alertMessage = ''" aria-label="Close"></button>
    </div>

    <!-- 3. Split 2-Column Layout (Role Selector on Left, Menu Tree on Right) -->
    <!-- 3. Split 2-Column Layout (Role Selector on Left, Menu Tree on Right) -->
    <div class="row g-3">
      <!-- KOLOM KIRI: DAFTAR PERAN PENGGUNA (ROLE SELECTOR) -->
      <div class="col-12 col-md-5 col-lg-4 col-xl-4 col-xxl-3">
        <div class="card card-outline card-danger shadow-xs h-100">
          <!-- Card Header (Search & Category Filter Aligned Horizontally) -->
          <div class="card-header border-bottom p-2 bg-body">
            <div class="d-flex align-items-center gap-1.5 justify-content-between">
              <!-- Search Input (Menyesuaikan / Flex Grow) -->
              <div class="input-group input-group-sm flex-grow-1 min-w-0">
                <span class="input-group-text bg-body-tertiary border-end-0 text-secondary px-2 fs-9">
                  <i class="bi bi-search"></i>
                </span>
                <input
                  type="text"
                  v-model="roleSearch"
                  class="form-control form-control-sm border-start-0 border-end-0 fs-9 px-1.5"
                  placeholder="Cari kode..."
                />
                <button
                  v-if="roleSearch"
                  type="button"
                  class="btn btn-outline-secondary border-start-0 fs-9 px-1.5"
                  @click="roleSearch = ''"
                  title="Hapus"
                >
                  <i class="bi bi-x"></i>
                </button>
              </div>

              <!-- Category Filter Dropdown (Rata Kanan) -->
              <div class="flex-shrink-0" style="width: 140px;">
                <select v-model="roleCategoryFilter" class="form-select form-select-sm fs-9 text-truncate px-2">
                  <option value="">Semua Kategori</option>
                  <option v-for="cat in roleCategories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
              </div>

              <!-- Reset Button (Shown if filter active) -->
              <button
                v-if="roleCategoryFilter || roleSearch"
                type="button"
                class="btn btn-sm btn-outline-danger fs-9 px-1.5 py-1 flex-shrink-0"
                @click="resetRoleFilters"
                title="Reset Filter"
              >
                <i class="bi bi-arrow-counterclockwise"></i>
              </button>
            </div>
          </div>

          <!-- Role List Container: Clean Minimalist List without Icons, Labels, or Counts -->
          <div class="card-body p-0 role-list-scrollable" style="max-height: 700px; overflow-y: auto;">
            <div class="list-group list-group-flush">
              <div
                v-for="r in filteredRoles"
                :key="r.code"
                class="role-item list-group-item list-group-item-action py-2.5 px-3 border-bottom transition-all cursor-pointer d-flex align-items-center justify-content-between"
                :class="{
                  'active-role': activeRoleCode === r.code
                }"
                @click="selectRole(r.code)"
              >
                <!-- Role Details (Name & Code only) -->
                <div class="min-w-0 flex-grow-1 pe-2">
                  <div
                    class="fs-8 text-truncate"
                    :class="activeRoleCode === r.code ? 'text-danger fw-bold' : 'text-body fw-semibold'"
                    :title="r.name"
                  >
                    {{ r.name }}
                  </div>
                  <div class="font-monospace fs-9 text-secondary text-truncate" :title="r.code">
                    {{ r.code }}
                  </div>
                </div>

                <!-- Chevron Indicator -->
                <div class="flex-shrink-0" :class="activeRoleCode === r.code ? 'text-danger' : 'text-muted'">
                  <i class="bi bi-chevron-right fs-9"></i>
                </div>
              </div>

              <!-- Empty State -->
              <div v-if="filteredRoles.length === 0" class="text-center py-5 px-3 text-secondary fs-8">
                <i class="bi bi-search fs-3 text-secondary-emphasis d-block mb-2"></i>
                <div class="fw-semibold">Tidak ada peran yang cocok</div>
                <div class="fs-9 text-secondary mt-1">Coba kata kunci lain atau reset filter pencarian</div>
                <button
                  type="button"
                  class="btn btn-xs btn-outline-danger mt-2"
                  @click="resetRoleFilters"
                >
                  <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Filter
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- KOLOM KANAN: TREE MENU DENGAN CHECKLIST HAK AKSES -->
      <div class="col-12 col-md-7 col-lg-8 col-xl-8 col-xxl-9">
        <div class="card card-outline card-danger shadow-xs">
          <!-- Card Header & Batch Controls -->
          <div class="card-header border-bottom p-3 bg-body d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
            <div>
              <h5 class="mb-0 fw-bold fs-6 text-body">
                <i class="bi bi-list-check text-danger me-2"></i>
                Hak Akses Menu: <span class="text-danger">{{ activeRole?.name || activeRoleCode }}</span>
              </h5>
              <div class="text-secondary fs-9 mt-0.5">
                Centang sub-menu di bawah untuk memberikan hak akses navigasi kepada peran ini.
              </div>
            </div>

            <!-- Header Action Buttons (Rata Kanan) -->
            <div class="d-flex align-items-center justify-content-end gap-2 flex-wrap ms-auto">
              <div class="btn-group btn-group-sm">
                <button
                  type="button"
                  class="btn btn-outline-secondary fs-8"
                  :disabled="isSuperAdminActive"
                  @click="selectAllMenus"
                  title="Centang Seluruh Menu"
                >
                  <i class="bi bi-check-all me-1"></i> Pilih Semua
                </button>
                <button
                  type="button"
                  class="btn btn-outline-secondary fs-8"
                  :disabled="isSuperAdminActive"
                  @click="deselectAllMenus"
                  title="Batalkan Semua Pilihan"
                >
                  <i class="bi bi-dash-square me-1"></i> Batalkan
                </button>
              </div>

              <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-outline-secondary fs-8" @click="expandAllModules" title="Buka Semua Modul (Up)">
                  <i class="bi bi-chevron-up"></i>
                </button>
                <button type="button" class="btn btn-outline-secondary fs-8" @click="collapseAllModules" title="Tutup Semua Modul (Down)">
                  <i class="bi bi-chevron-down"></i>
                </button>
              </div>

              <!-- Save Button -->
              <button
                type="button"
                class="btn btn-sm btn-danger fw-bold shadow-xs fs-8 d-flex align-items-center position-relative"
                :disabled="!isDirty || isSuperAdminActive"
                @click="saveRolePermissions"
              >
                <i class="bi bi-check2-circle me-1.5"></i>
                <span>Simpan Perubahan</span>
                <span v-if="isDirty" class="position-absolute top-0 start-100 translate-middle p-1.5 bg-warning border border-light rounded-circle">
                  <span class="visually-hidden">Perubahan belum disimpan</span>
                </span>
              </button>
            </div>
          </div>

          <!-- Tree Search & Filter Toolbar -->
          <div class="card-body p-3 bg-body-tertiary border-bottom">
            <div class="row g-2 align-items-center justify-content-between">
              <div class="col-12 col-md-6">
                <div class="input-group input-group-sm">
                  <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                  <input
                    type="text"
                    v-model="menuTreeSearch"
                    class="form-control form-control-sm border-start-0 fs-8"
                    placeholder="Cari menu dalam pohon navigasi..."
                  />
                  <button v-if="menuTreeSearch" class="btn btn-outline-secondary btn-sm" @click="menuTreeSearch = ''">
                    <i class="bi bi-x-circle"></i>
                  </button>
                </div>
              </div>

              <div class="col-12 col-md-6 text-md-end fs-9 text-secondary">
                <span v-if="isSuperAdminActive" class="text-danger fw-semibold">
                  <i class="bi bi-lock-fill me-1"></i> Peran SUPER_ADMIN selalu memiliki akses ke seluruh menu sistem.
                </span>
                <span v-else>
                  Terpilih <strong>{{ selectedMenuCodes.size }}</strong> dari <strong>{{ menuStore.totalMenus }}</strong> menu navigasi.
                </span>
              </div>
            </div>
          </div>

          <!-- Tree Menu Container with Checklists -->
          <div class="p-3 bg-body" style="max-height: 680px; overflow-y: auto;">
            <div
              v-for="modGroup in treeModules"
              :key="modGroup.module"
              class="tree-module-block mb-3 border rounded-3 shadow-xs bg-body overflow-hidden"
              :class="{ 'border-danger': isExpanded(modGroup.module) }"
            >
              <!-- Module Header (Level 1) with Module Checkbox -->
              <div
                class="tree-module-header p-2.5 d-flex align-items-center justify-content-between user-select-none"
                :class="isExpanded(modGroup.module) ? 'bg-danger-subtle text-danger-emphasis' : 'bg-body-secondary text-body'"
                @click="toggleModule(modGroup.module)"
              >
                <div class="d-flex align-items-center gap-2">
                  <!-- Module Checkbox (Tri-state / Bulk check) -->
                  <div class="form-check m-0" @click.stop>
                    <input
                      type="checkbox"
                      class="form-check-input cursor-pointer"
                      :checked="isModuleAllChecked(modGroup)"
                      :indeterminate="isModuleIndeterminate(modGroup)"
                      :disabled="isSuperAdminActive"
                      @change="toggleModuleCheckAll(modGroup)"
                      :title="`Pilih seluruh menu dalam modul ${modGroup.module}`"
                    />
                  </div>

                  <!-- Expand/Collapse Chevron -->
                  <i
                    class="bi bi-chevron-right fs-8 tree-chevron transition-transform"
                    :class="{ 'rotate-90': isExpanded(modGroup.module) }"
                  ></i>

                  <!-- Module Icon -->
                  <div class="module-icon-box bg-danger text-white rounded-2 d-flex align-items-center justify-content-center shadow-xs">
                    <i :class="['bi', getModuleIcon(modGroup.module), 'fs-7']"></i>
                  </div>

                  <!-- Module Title -->
                  <span class="fw-bold fs-7">{{ modGroup.module }}</span>
                </div>

                <!-- Checked Count Badge -->
                <div class="d-flex align-items-center gap-2">
                  <span
                    class="badge rounded-pill font-monospace fs-9"
                    :class="getModuleCheckedCount(modGroup) > 0 ? 'text-bg-danger' : 'bg-body text-secondary border'"
                  >
                    {{ getModuleCheckedCount(modGroup) }} / {{ modGroup.items.length }} Menu
                  </span>
                </div>
              </div>

              <!-- Sub-Menu Leaf Nodes (Level 2) with Checkboxes -->
              <div
                v-show="isExpanded(modGroup.module)"
                class="tree-children-container p-2 space-y-1.5 border-top bg-body"
              >
                <div
                  v-for="item in modGroup.items"
                  :key="item.code"
                  class="tree-leaf-item p-2 rounded-2 border bg-body d-flex align-items-center justify-content-between gap-2 shadow-xs transition-all cursor-pointer"
                  :class="{
                    'border-start-danger bg-body-tertiary': selectedMenuCodes.has(item.code),
                    'opacity-75': !selectedMenuCodes.has(item.code)
                  }"
                  @click="toggleMenuCheck(item.code)"
                >
                  <div class="d-flex align-items-center gap-2.5 flex-grow-1 overflow-hidden">
                    <!-- Item Checkbox -->
                    <div class="form-check m-0" @click.stop>
                      <input
                        type="checkbox"
                        class="form-check-input cursor-pointer"
                        :checked="selectedMenuCodes.has(item.code)"
                        :disabled="isSuperAdminActive"
                        @change="toggleMenuCheck(item.code)"
                      />
                    </div>

                    <!-- Item Icon -->
                    <div
                      class="item-icon-box rounded-2 d-flex align-items-center justify-content-center"
                      :class="selectedMenuCodes.has(item.code) ? 'bg-danger-subtle text-danger' : 'bg-body-secondary text-secondary'"
                    >
                      <i :class="['bi', item.icon || 'bi-circle', 'fs-7']"></i>
                    </div>

                    <!-- Details: Title, Code, Path -->
                    <div class="min-w-0">
                      <div class="d-flex align-items-center gap-2">
                        <span class="fw-bold text-body fs-8">{{ item.title }}</span>
                        <span class="font-monospace text-secondary fs-9">({{ item.code }})</span>
                      </div>
                      <div class="d-flex align-items-center gap-2 mt-0.5">
                        <span class="font-monospace text-primary fs-9 bg-body-secondary px-1.5 py-0.5 rounded border border-secondary-subtle">
                          {{ item.path }}
                        </span>
                        <span v-if="item.description" class="fs-9 text-secondary text-truncate d-none d-md-inline" style="max-width: 340px;">
                          • {{ item.description }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- Right Badges -->
                  <div class="d-flex align-items-center gap-2 flex-shrink-0" @click.stop>
                    <span
                      class="badge fs-9"
                      :class="selectedMenuCodes.has(item.code) ? 'text-bg-success' : 'text-bg-secondary'"
                    >
                      {{ selectedMenuCodes.has(item.code) ? 'Diizinkan' : 'Dilarang' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="treeModules.length === 0" class="text-center py-5 text-secondary">
              <i class="bi bi-search fs-2 d-block mb-2 text-secondary-subtle"></i>
              <div class="fw-semibold">Tidak ada menu yang sesuai dengan pencarian "{{ menuTreeSearch }}".</div>
              <button class="btn btn-sm btn-outline-secondary mt-2 fs-8" @click="menuTreeSearch = ''">
                Bersihkan Pencarian
              </button>
            </div>
          </div>

          <!-- Card Footer with Save Reminder -->
          <div class="card-footer bg-body border-top p-3 d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
            <div class="fs-8 text-secondary">
              <template v-if="isDirty">
                <span class="text-warning fw-bold"><i class="bi bi-exclamation-triangle-fill me-1"></i> Ada perubahan yang belum disimpan.</span>
                Klik tombol <strong>Simpan Perubahan</strong> untuk menerapkan hak akses ke sistem.
              </template>
              <template v-else>
                <span class="text-success fw-semibold"><i class="bi bi-check-circle-fill me-1"></i> Seluruh konfigurasi hak akses telah tersimpan.</span>
              </template>
            </div>

            <div class="d-flex align-items-center justify-content-end gap-2 ms-auto">
              <button
                type="button"
                class="btn btn-sm btn-outline-secondary fs-8"
                :disabled="!isDirty"
                @click="resetToInitialSelection"
              >
                Batal Perubahan
              </button>
              <button
                type="button"
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                :disabled="!isDirty || isSuperAdminActive"
                @click="saveRolePermissions"
              >
                <i class="bi bi-check2-circle me-1"></i> Simpan Perubahan
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useMenuStore } from '@/stores/menu';
import { useRoleStore } from '@/stores/role';

const menuStore = useMenuStore();
const roleStore = useRoleStore();

// Alert state
const alertMessage = ref('');
const alertClass = ref('alert-success');
const alertIcon = ref('bi-check-circle');

const showAlert = (msg, type = 'success') => {
  alertMessage.value = msg;
  if (type === 'success') {
    alertClass.value = 'alert-success';
    alertIcon.value = 'bi-check-circle';
  } else {
    alertClass.value = 'alert-danger';
    alertIcon.value = 'bi-exclamation-triangle';
  }
  setTimeout(() => {
    if (alertMessage.value === msg) alertMessage.value = '';
  }, 4000);
};

// Left Column: Role Search & Filtering
const roleSearch = ref('');
const roleCategoryFilter = ref('');
const activeRoleCode = ref('REQUESTER_CABANG');

// Right Column: Menu Tree Search & State
const menuTreeSearch = ref('');
const expandedModules = ref(new Set());

// Set of selected menu codes for active role
const selectedMenuCodes = ref(new Set());
// Original set for dirty detection
const initialSelectedMenuCodes = ref(new Set());

onMounted(async () => {
  try {
    await Promise.all([
      menuStore.fetchMenus(),
      roleStore.fetchRoles()
    ]);
  } catch (error) {
    showAlert(error?.message || 'Gagal memuat role dan menu dari backend.', 'error');
    return;
  }

  // Set default active role
  if (roleStore.roles.length > 0) {
    const defaultRole = roleStore.roles.find((r) => r.code === 'REQUESTER_CABANG') || roleStore.roles[0];
    selectRole(defaultRole.code);
  }

  // Expand all tree modules initially
  expandAllModules();
});

const roleList = computed(() => roleStore.roles);

// Role Categories
const roleCategories = computed(() => {
  return [...new Set(roleList.value.map((r) => r.category).filter(Boolean))];
});

// Filtered Roles
const filteredRoles = computed(() => {
  return roleList.value.filter((r) => {
    if (roleSearch.value) {
      const q = roleSearch.value.toLowerCase().trim();
      const matchCode = r.code.toLowerCase().includes(q);
      const matchName = r.name.toLowerCase().includes(q);
      if (!matchCode && !matchName) return false;
    }
    if (roleCategoryFilter.value && r.category !== roleCategoryFilter.value) {
      return false;
    }
    return true;
  });
});

const resetRoleFilters = () => {
  roleSearch.value = '';
  roleCategoryFilter.value = '';
};

// Active Role Object
const activeRole = computed(() => {
  return roleList.value.find((r) => r.code === activeRoleCode.value) || null;
});

const isSuperAdminActive = computed(() => {
  return activeRoleCode.value === 'SUPER_ADMIN';
});

// Coverage percentage
const coveragePercentage = computed(() => {
  if (menuStore.totalMenus === 0) return 0;
  return Math.round((selectedMenuCodes.value.size / menuStore.totalMenus) * 100);
});

// Dirty State detection
const isDirty = computed(() => {
  if (isSuperAdminActive.value) return false;
  if (selectedMenuCodes.value.size !== initialSelectedMenuCodes.value.size) return true;
  for (const code of selectedMenuCodes.value) {
    if (!initialSelectedMenuCodes.value.has(code)) return true;
  }
  return false;
});

// Count of menus assigned to any role
const getRoleMenuCount = (code) => {
  if (code === 'SUPER_ADMIN') return menuStore.totalMenus;
  return menuStore.menus.filter((m) => Array.isArray(m.roles) && m.roles.includes(code)).length;
};

// Select a Role
const selectRole = (roleCode) => {
  if (isDirty.value) {
    if (!confirm('Ada perubahan yang belum disimpan untuk peran sebelumnya. Tetap berpindah peran?')) {
      return;
    }
  }

  activeRoleCode.value = roleCode;

  // Load current allowed menus for this role from menuStore
  const codes = new Set();
  if (roleCode === 'SUPER_ADMIN') {
    menuStore.menus.forEach((m) => codes.add(m.code));
  } else {
    menuStore.menus.forEach((m) => {
      if (Array.isArray(m.roles) && m.roles.includes(roleCode)) {
        codes.add(m.code);
      }
    });
  }

  selectedMenuCodes.value = new Set(codes);
  initialSelectedMenuCodes.value = new Set(codes);
};

// Reset to initial selection
const resetToInitialSelection = () => {
  selectedMenuCodes.value = new Set(initialSelectedMenuCodes.value);
  showAlert('Perubahan hak akses dibatalkan.');
};

// Save Role Permissions
const saveRolePermissions = async () => {
  if (!activeRoleCode.value || isSuperAdminActive.value) return;

  const codesArray = Array.from(selectedMenuCodes.value);
  await menuStore.updateRoleMenus(activeRoleCode.value, codesArray);
  initialSelectedMenuCodes.value = new Set(selectedMenuCodes.value);

  showAlert(`Hak akses menu untuk peran "${activeRole.value?.name || activeRoleCode.value}" berhasil disimpan!`);
};

// Tree Modules computed with search
const treeModules = computed(() => {
  const query = menuTreeSearch.value.toLowerCase().trim();
  const ordered = menuStore.orderedModules;

  if (!query) {
    return ordered;
  }

  const result = [];
  ordered.forEach((g) => {
    const modMatch = g.module.toLowerCase().includes(query);
    const matchingItems = g.items.filter((item) => {
      return (
        item.title?.toLowerCase().includes(query) ||
        item.code?.toLowerCase().includes(query) ||
        item.path?.toLowerCase().includes(query)
      );
    });

    if (modMatch || matchingItems.length > 0) {
      expandedModules.value.add(g.module);
      result.push({
        module: g.module,
        items: modMatch ? g.items : matchingItems
      });
    }
  });

  return result;
});

// Expand/Collapse controls
const isExpanded = (modName) => {
  return expandedModules.value.has(modName);
};

const toggleModule = (modName) => {
  if (expandedModules.value.has(modName)) {
    expandedModules.value.delete(modName);
  } else {
    expandedModules.value.add(modName);
  }
};

const expandAllModules = () => {
  menuStore.orderedModules.forEach((g) => expandedModules.value.add(g.module));
};

const collapseAllModules = () => {
  expandedModules.value.clear();
};

// Module-level checkbox helpers
const getModuleCheckedCount = (modGroup) => {
  return modGroup.items.filter((item) => selectedMenuCodes.value.has(item.code)).length;
};

const isModuleAllChecked = (modGroup) => {
  if (modGroup.items.length === 0) return false;
  return modGroup.items.every((item) => selectedMenuCodes.value.has(item.code));
};

const isModuleIndeterminate = (modGroup) => {
  const count = getModuleCheckedCount(modGroup);
  return count > 0 && count < modGroup.items.length;
};

const toggleModuleCheckAll = (modGroup) => {
  if (isSuperAdminActive.value) return;

  const allChecked = isModuleAllChecked(modGroup);
  const nextSet = new Set(selectedMenuCodes.value);

  if (allChecked) {
    // Uncheck all items in this module
    modGroup.items.forEach((item) => nextSet.delete(item.code));
  } else {
    // Check all items in this module
    modGroup.items.forEach((item) => nextSet.add(item.code));
  }

  selectedMenuCodes.value = nextSet;
};

// Item-level toggle
const toggleMenuCheck = (menuCode) => {
  if (isSuperAdminActive.value) return;

  const nextSet = new Set(selectedMenuCodes.value);
  if (nextSet.has(menuCode)) {
    nextSet.delete(menuCode);
  } else {
    nextSet.add(menuCode);
  }
  selectedMenuCodes.value = nextSet;
};

// Select all / Deselect all
const selectAllMenus = () => {
  if (isSuperAdminActive.value) return;
  const nextSet = new Set();
  menuStore.menus.forEach((m) => nextSet.add(m.code));
  selectedMenuCodes.value = nextSet;
};

const deselectAllMenus = () => {
  if (isSuperAdminActive.value) return;
  selectedMenuCodes.value = new Set();
};

// Module Icons
const MODULE_ICON_MAP = {
  'Dashboard': 'bi-speedometer2',
  'Permintaan & Order': 'bi-cart3',
  'Gudang & Distribusi': 'bi-box-seam',
  'Penerimaan': 'bi-truck',
  'Persediaan': 'bi-stack',
  'Pengadaan': 'bi-bag-check',
  'Finance': 'bi-cash-stack',
  'Master Data': 'bi-gear-wide-connected',
  'Audit & Keamanan': 'bi-shield-check',
  'Notifikasi': 'bi-bell',
  'Executive Support (ESS)': 'bi-speedometer2',
  'Laporan & Rekapitulasi': 'bi-bar-chart-line'
};

const getModuleIcon = (mod) => {
  return MODULE_ICON_MAP[mod] || 'bi-folder2-open';
};
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.hover-bg:hover {
  background-color: var(--bs-tertiary-bg) !important;
}

.transition-all {
  transition: all 0.2s ease;
}

.transition-transform {
  transition: transform 0.2s ease;
}

.rotate-90 {
  transform: rotate(90deg);
}

.module-icon-box {
  width: 28px;
  height: 28px;
}

.item-icon-box {
  width: 32px;
  height: 32px;
}

.role-item {
  border-left: 3px solid transparent !important;
  background-color: var(--bs-body-bg);
  border-radius: 0;
  transition: all 0.15s ease-in-out;
}

.role-item:hover {
  background-color: var(--bs-tertiary-bg) !important;
}

.role-item.active-role {
  border-left: 4px solid #dc3545 !important;
  background-color: rgba(220, 53, 69, 0.08) !important;
}

.tree-module-header {
  cursor: pointer;
  transition: background-color 0.15s ease-in-out;
}

.tree-module-header:hover {
  filter: brightness(0.98);
}

.tree-leaf-item {
  border-left: 3px solid var(--bs-border-color);
}

.tree-leaf-item.border-start-danger {
  border-left-color: #dc3545 !important;
}

.tree-leaf-item:hover {
  background-color: var(--bs-body-secondary) !important;
}

.btn-action-icon {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid var(--bs-border-color);
  background: var(--bs-body-bg);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all 0.15s ease-in-out;
  cursor: pointer;
}

.btn-action-icon:hover {
  background: var(--bs-tertiary-bg);
  border-color: var(--bs-border-color-translucent);
}

.btn-xs {
  font-size: 0.72rem;
  padding: 0.15rem 0.4rem;
  border-radius: 3px;
}

.role-list-scrollable::-webkit-scrollbar {
  width: 5px;
}

.role-list-scrollable::-webkit-scrollbar-thumb {
  background: var(--bs-border-color);
  border-radius: 3px;
}
</style>

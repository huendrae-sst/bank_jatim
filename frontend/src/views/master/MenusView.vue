<template>
  <div class="menus-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Manajemen Menu & Navigasi Sistem</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link>
              </li>
              <li class="breadcrumb-item text-secondary">
                Master Data
              </li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">
                Manajemen Menu
              </li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- 3. Main Tree Navigation Card -->
    <div class="card card-outline card-danger shadow-xs mb-4">
      <!-- Card Header: Title & Action Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h5 class="card-title fw-bold mb-0 fs-6 text-body">
            Struktur Tree & Urutan Menu
          </h5>
        </div>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2 flex-wrap">
          <button class="btn btn-sm btn-outline-secondary fs-8" @click="exportConfiguration" title="Ekspor JSON Konfigurasi Menu">
            Ekspor
          </button>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal(null)">
            Tambah
          </button>
        </div>
      </div>

      <!-- TREEVIEW STRUKTUR MENU DENGAN DRAG & DROP (TANPA PAGING) -->
      <div>
        <!-- Tree Toolbar: Expand/Collapse All, Filter Search, and Drag Hint -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center justify-content-between">
            <div class="col-12 col-md-5">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="searchQuery"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari nama menu, kode, rute URL dalam pohon..."
                />
                <button v-if="searchQuery" class="btn btn-outline-secondary btn-sm" @click="searchQuery = ''" title="Bersihkan">
                  <i class="bi bi-x-circle"></i>
                </button>
              </div>
            </div>

            <div class="col-12 col-md-7 d-flex align-items-center justify-content-md-end gap-2 flex-wrap">
              <span class="badge bg-secondary-subtle text-secondary border fs-9 py-1 px-2 d-none d-lg-inline-flex align-items-center">
                <i class="bi bi-arrows-move me-1 text-danger"></i> Drag &amp; drop modul parent atau baris menu untuk ubah urutan
              </span>
              <button type="button" class="btn btn-sm btn-outline-secondary fs-8" @click="expandAllModules">
                Buka Semua
              </button>
              <button type="button" class="btn btn-sm btn-outline-secondary fs-8" @click="collapseAllModules">
                Tutup Semua
              </button>
            </div>
          </div>
        </div>

        <!-- Tree Nodes Container (No Paging, Full Hierarchy) -->
        <div class="p-3 tree-container bg-body">
          <div
            v-for="(modGroup, mIdx) in treeModules"
            :key="modGroup.module"
            class="tree-module-block mb-3 border rounded-3 shadow-xs bg-body overflow-hidden transition-all"
            :class="{
              'border-danger': isExpanded(modGroup.module),
              'dragging-module': draggedModule?.module === modGroup.module,
              'drag-over-module': dragOverModuleIdx === mIdx
            }"
            draggable="true"
            @dragstart="onModuleDragStart($event, modGroup, mIdx)"
            @dragover.prevent="onModuleDragOver($event, mIdx)"
            @dragleave="onModuleDragLeave($event, mIdx)"
            @drop.prevent="onModuleDrop($event, mIdx)"
            @dragend="onModuleDragEnd($event)"
          >
            <!-- Module Parent Node (Level 1) -->
            <div
              class="tree-module-header p-2.5 d-flex align-items-center justify-content-between user-select-none"
              :class="isExpanded(modGroup.module) ? 'bg-danger-subtle text-danger-emphasis' : 'bg-body-secondary text-body'"
              @click="toggleModule(modGroup.module)"
            >
              <div class="d-flex align-items-center gap-2">
                <!-- Module Drag Handle Grip -->
                <div
                  class="drag-handle text-secondary cursor-grab p-1"
                  title="Tahan dan geser untuk memindahkan urutan modul ini"
                  @click.stop
                >
                  <i class="bi bi-grip-vertical fs-5"></i>
                </div>

                <!-- Expand/Collapse Chevron -->
                <i
                  class="bi bi-chevron-right fs-8 tree-chevron transition-transform"
                  :class="{ 'rotate-90': isExpanded(modGroup.module) }"
                ></i>

                <!-- Module Title (Hanya Label) -->
                <span class="fw-bold fs-7">{{ modGroup.module }}</span>
                <span class="badge bg-body text-secondary border fs-9 d-none d-sm-inline">
                  {{ modGroup.items.length }} Sub-Menu
                </span>
              </div>

              <!-- Module Header Tools -->
              <div class="d-flex align-items-center gap-1" @click.stop>
                <button
                  type="button"
                  class="btn btn-xs btn-outline-danger fw-semibold fs-9 d-flex align-items-center"
                  @click="openCreateModal(modGroup.module)"
                  title="Tambah Sub-Menu di Modul Ini"
                >
                  Tambah
                </button>
              </div>
            </div>

            <!-- Sub-Menu Leaf Nodes (Level 2) with Drag and Drop -->
            <div
              v-show="isExpanded(modGroup.module)"
              class="tree-children-container p-2 space-y-1.5 border-top"
            >
              <div
                v-if="modGroup.items.length === 0"
                class="text-center py-3 text-secondary fs-8 fst-italic"
              >
                Belum ada sub-menu dalam modul ini. Klik tombol "+ Sub-Menu" di atas untuk menambahkan.
              </div>

              <div
                v-for="(item, idx) in modGroup.items"
                :key="item.code"
                class="tree-leaf-item p-2 rounded-2 border bg-body d-flex flex-column flex-lg-row align-items-lg-center justify-content-between gap-2 shadow-xs transition-all"
                :class="{
                  'dragging': draggedItem?.code === item.code,
                  'drag-over-indicator': dragOverCode === item.code,
                  'border-start-danger': item.status === 'AKTIF'
                }"
                draggable="true"
                @dragstart="onDragStart($event, item, modGroup.module)"
                @dragover.prevent="onDragOver($event, item)"
                @dragleave="onDragLeave($event, item)"
                @drop="onDrop($event, item, modGroup.module)"
                @dragend="onDragEnd"
              >
                <!-- Left Details (Grip & Title/Path Label) -->
                <div class="d-flex align-items-center gap-2 flex-grow-1 overflow-hidden">
                  <!-- Drag Handle Grip -->
                  <div
                    class="drag-handle text-secondary cursor-grab p-1"
                    title="Tahan dan geser untuk memindahkan urutan"
                  >
                    <i class="bi bi-grip-vertical fs-6"></i>
                  </div>

                  <!-- Title, Description & Route Path (Hanya Label) -->
                  <div class="min-w-0">
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                      <span class="fw-bold text-body fs-8">{{ item.title }}</span>
                      <span class="font-monospace text-secondary fs-9">({{ item.code }})</span>
                    </div>
                    <div class="d-flex align-items-center gap-2 flex-wrap mt-0.5">
                      <router-link
                        :to="item.path"
                        class="font-monospace text-decoration-none text-primary fs-9 bg-body-secondary px-2 py-0.5 rounded border border-secondary-subtle"
                        title="Buka Halaman"
                      >
                        {{ item.path }} <i class="bi bi-box-arrow-up-right fs-9 ms-0.5"></i>
                      </router-link>
                      <span v-if="item.description" class="fs-9 text-secondary text-truncate d-none d-md-inline" style="max-width: 320px;">
                        • {{ item.description }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Right Tools: Status Switch, Edit Button, Delete Button -->
                <div class="d-flex align-items-center gap-2 ms-lg-auto flex-shrink-0">
                  <!-- Status Switch Toggle -->
                  <div class="d-flex align-items-center gap-1 px-1">
                    <div class="form-check form-switch m-0">
                      <input
                        class="form-check-input cursor-pointer"
                        type="checkbox"
                        role="switch"
                        :checked="item.status === 'AKTIF'"
                        @change="toggleMenuStatus(item)"
                        :title="item.status === 'AKTIF' ? 'Nonaktifkan Menu' : 'Aktifkan Menu'"
                      />
                    </div>
                    <span class="badge fs-9" :class="item.status === 'AKTIF' ? 'text-bg-success' : 'text-bg-secondary'">
                      {{ item.status }}
                    </span>
                  </div>

                  <!-- Edit & Delete Buttons -->
                  <div class="d-flex align-items-center gap-1">
                    <button
                      type="button"
                      class="btn btn-action-icon text-secondary"
                      @click="openEditModal(item)"
                      title="Edit Sub-Menu"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      type="button"
                      class="btn btn-action-icon text-danger"
                      @click="deleteMenuItem(item)"
                      title="Hapus Menu"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="treeModules.length === 0" class="text-center py-5 text-secondary">
            <i class="bi bi-search fs-2 d-block mb-2 text-secondary-subtle"></i>
            <div class="fw-semibold">Tidak ada menu atau modul yang cocok dengan pencarian "{{ searchQuery }}".</div>
            <button class="btn btn-sm btn-outline-secondary mt-2 fs-8" @click="searchQuery = ''">
              Bersihkan Pencarian
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. MODAL FORM: TAMBAH / EDIT MENU -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              {{ isEditMode ? 'Edit Konfigurasi Menu' : 'Tambah Menu Baru' }}
            </h6>
            <button type="button" class="btn-close" @click="showModal = false" aria-label="Tutup"></button>
          </div>

          <form @submit.prevent="saveMenu">
            <div class="modal-body p-3 fs-8 space-y-3" style="max-height: 70vh; overflow-y: auto;">
              <!-- Baris 1: Kode Menu & Judul -->
              <div class="row g-2 mb-2">
                <div class="col-12 col-md-5">
                  <label class="form-label fw-bold mb-1">Kode Menu (System Key) <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="menuForm.code"
                    class="form-control form-control-sm font-monospace text-uppercase"
                    placeholder="Contoh: MST_CUSTOM_MENU"
                    :disabled="isEditMode"
                    required
                  />
                  <div class="fs-9 text-secondary mt-1">Gunakan huruf kapital dan garis bawah (snake_case).</div>
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fw-bold mb-1">Nama / Judul Menu <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="menuForm.title"
                    class="form-control form-control-sm"
                    placeholder="Contoh: Manajemen Dokumen Khusus"
                    required
                  />
                </div>
              </div>

              <!-- Baris 2: Modul Induk & Rute URL -->
              <div class="row g-2 mb-2">
                <div class="col-12 col-md-5">
                  <label class="form-label fw-bold mb-1">Modul Induk <span class="text-danger">*</span></label>
                  <select v-model="menuForm.module" class="form-select form-select-sm" required>
                    <option v-for="cat in MODULE_CATEGORIES" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fw-bold mb-1">Rute URL (Path Vue Router) <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="menuForm.path"
                    class="form-control form-control-sm font-monospace"
                    placeholder="Contoh: /master/custom-module"
                    required
                  />
                  <div class="fs-9 text-secondary mt-1">Harus diawali tanda garis miring (/).</div>
                </div>
              </div>

              <!-- Baris 3: Ikon, Urutan & Status -->
              <div class="row g-2 mb-2">
                <div class="col-12 col-md-5">
                  <label class="form-label fw-bold mb-1">Ikon Bootstrap (Class) <span class="text-danger">*</span></label>
                  <div class="input-group input-group-sm">
                    <span class="input-group-text bg-body text-danger">
                      <i :class="['bi', menuForm.icon || 'bi-circle']"></i>
                    </span>
                    <select v-model="menuForm.icon" class="form-select form-select-sm font-monospace">
                      <option v-for="ic in ICON_PRESETS" :key="ic" :value="ic">{{ ic }}</option>
                    </select>
                  </div>
                </div>
                <div class="col-6 col-md-3">
                  <label class="form-label fw-bold mb-1">Nomor Urut <span class="text-danger">*</span></label>
                  <input
                    type="number"
                    v-model.number="menuForm.order"
                    class="form-control form-control-sm font-monospace"
                    min="1"
                    required
                  />
                </div>
                <div class="col-6 col-md-4">
                  <label class="form-label fw-bold mb-1">Status Menu <span class="text-danger">*</span></label>
                  <select v-model="menuForm.status" class="form-select form-select-sm" required>
                    <option value="AKTIF">AKTIF</option>
                    <option value="NONAKTIF">NONAKTIF</option>
                  </select>
                </div>
              </div>

              <!-- Baris 4: Deskripsi -->
              <div class="mb-3">
                <label class="form-label fw-bold mb-1">Keterangan / Deskripsi Fitur</label>
                <textarea
                  v-model="menuForm.description"
                  class="form-control form-control-sm"
                  rows="2"
                  placeholder="Jelaskan tujuan dan fungsi modul ini..."
                ></textarea>
              </div>

              <!-- Baris 5: Matriks Hak Akses Peran Pengguna -->
              <div class="border rounded-2 p-2 bg-body-tertiary">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <label class="form-label fw-bold mb-0">Hak Akses Peran Pengguna (RBAC)</label>
                  <div class="d-flex gap-1">
                    <button type="button" class="btn btn-xs btn-outline-secondary fs-9 py-0 px-1" @click="selectAllRoles">
                      Pilih Semua
                    </button>
                    <button type="button" class="btn btn-xs btn-outline-secondary fs-9 py-0 px-1" @click="deselectAllRoles">
                      Reset
                    </button>
                  </div>
                </div>
                <div class="row g-1">
                  <div v-for="r in allRoles" :key="r" class="col-6 col-md-4">
                    <div class="form-check fs-9">
                      <input
                        class="form-check-input cursor-pointer"
                        type="checkbox"
                        :id="'modal-role-' + r"
                        :value="r"
                        v-model="menuForm.roles"
                        :disabled="r === 'SUPER_ADMIN'"
                      />
                      <label class="form-check-label cursor-pointer font-monospace" :for="'modal-role-' + r">
                        {{ r }}
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body border-top p-2 d-flex justify-content-between">
              <button type="button" class="btn btn-sm btn-outline-secondary fs-8" @click="showModal = false">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold fs-8 px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useMenuStore, MODULE_CATEGORIES } from '@/stores/menu';
import { useRoleStore } from '@/stores/role';
import { toast } from '@/utils/toast';

const menuStore = useMenuStore();
const roleStore = useRoleStore();
const allRoles = computed(() => roleStore.roleCodes);

// Filter & search states
const searchQuery = ref('');

// Expanded module branches in treeview
const expandedModules = ref(new Set());

// Toast notification helper
const showAlert = (msg, type = 'success') => {
  if (type === 'success') {
    toast.success(msg, 'Berhasil');
  } else if (type === 'error') {
    toast.error(msg, 'Gagal');
  } else if (type === 'warn' || type === 'warning') {
    toast.warn(msg, 'Peringatan');
  } else {
    toast.info(msg, 'Informasi');
  }
};

// Modal State
const showModal = ref(false);
const isEditMode = ref(false);
const menuForm = ref({
  id: '',
  code: '',
  title: '',
  module: 'Master Data',
  parentCode: null,
  path: '',
  icon: 'bi-circle',
  order: 1,
  status: 'AKTIF',
  description: '',
  roles: ['SUPER_ADMIN']
});

// Drag and Drop States
const draggedItem = ref(null);
const draggedFromModule = ref(null);
const dragOverCode = ref(null);

// Preset icons for icon picker
const ICON_PRESETS = [
  'bi-circle',
  'bi-menu-button-wide',
  'bi-speedometer2',
  'bi-cart3',
  'bi-clipboard-check',
  'bi-credit-card',
  'bi-box-seam',
  'bi-boxes',
  'bi-truck',
  'bi-truck-flatbed',
  'bi-stack',
  'bi-arrow-left-right',
  'bi-journal-text',
  'bi-clipboard-data',
  'bi-bell',
  'bi-shuffle',
  'bi-bag-check',
  'bi-receipt',
  'bi-cash-stack',
  'bi-building',
  'bi-box',
  'bi-wallet2',
  'bi-calculator',
  'bi-map',
  'bi-people',
  'bi-shield-lock',
  'bi-shield-check',
  'bi-bar-chart-line',
  'bi-pie-chart',
  'bi-book'
];


onMounted(async () => {
  try {
    await Promise.all([menuStore.fetchMenus(), roleStore.fetchRoles()]);
    expandAllModules();
  } catch (error) {
    showAlert(error?.message || 'Gagal memuat konfigurasi menu dari backend.', 'error');
  }
});

// Expand / Collapse controls
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
  menuStore.orderedModules.forEach((g) => {
    expandedModules.value.add(g.module);
  });
};

const collapseAllModules = () => {
  expandedModules.value.clear();
};

// Tree Modules computed with search filtering
const treeModules = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  const ordered = menuStore.orderedModules;

  if (!query) {
    return ordered;
  }

  // Filter modules and items matching search
  const result = [];
  ordered.forEach((g) => {
    const modMatch = g.module.toLowerCase().includes(query);
    const matchingItems = g.items.filter((item) => {
      return (
        item.title?.toLowerCase().includes(query) ||
        item.code?.toLowerCase().includes(query) ||
        item.path?.toLowerCase().includes(query) ||
        item.description?.toLowerCase().includes(query)
      );
    });

    if (modMatch || matchingItems.length > 0) {
      // Auto-expand module if there's a match
      expandedModules.value.add(g.module);
      result.push({
        module: g.module,
        items: modMatch ? g.items : matchingItems
      });
    }
  });

  return result;
});

// Module Level Drag and Drop States
const draggedModule = ref(null);
const draggedModuleIdx = ref(-1);
const dragOverModuleIdx = ref(null);

const onModuleDragStart = (e, modGroup, mIdx) => {
  if (draggedItem.value) return;
  draggedModule.value = modGroup;
  draggedModuleIdx.value = mIdx;
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/plain', 'MODULE:' + modGroup.module);
  setTimeout(() => {
    if (e.target) e.target.classList.add('opacity-50');
  }, 0);
};

const onModuleDragOver = (e, mIdx) => {
  if (!draggedModule.value) return;
  if (draggedModuleIdx.value === mIdx) return;
  dragOverModuleIdx.value = mIdx;
};

const onModuleDragLeave = (e, mIdx) => {
  if (dragOverModuleIdx.value === mIdx) {
    dragOverModuleIdx.value = null;
  }
};

const onModuleDrop = async (e, targetIdx) => {
  e.preventDefault();
  dragOverModuleIdx.value = null;
  if (!draggedModule.value) return;
  const fromIdx = draggedModuleIdx.value;
  if (fromIdx === targetIdx || fromIdx === -1) return;

  const list = [...treeModules.value];
  const [removed] = list.splice(fromIdx, 1);
  list.splice(targetIdx, 0, removed);

  try {
    await menuStore.reorderModules(list);
    showAlert(`Urutan modul "${removed.module}" berhasil dipindahkan.`);
  } catch (err) {
    showAlert('Gagal memindahkan urutan modul: ' + (err?.message || err), 'error');
  } finally {
    draggedModule.value = null;
    draggedModuleIdx.value = -1;
  }
};

const onModuleDragEnd = (e) => {
  draggedModule.value = null;
  draggedModuleIdx.value = -1;
  dragOverModuleIdx.value = null;
  if (e.target) {
    e.target.classList.remove('opacity-50');
  }
};

// Drag and Drop Handlers (Native HTML5 Drag and Drop)
const onDragStart = (e, item, moduleName) => {
  e.stopPropagation();
  draggedItem.value = item;
  draggedFromModule.value = moduleName;
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/plain', item.code);
  // Add styling after small timeout
  setTimeout(() => {
    if (e.target) {
      e.target.classList.add('opacity-50');
    }
  }, 0);
};

const onDragOver = (e, item) => {
  if (!draggedItem.value) return;
  if (draggedItem.value.code === item.code) return;
  dragOverCode.value = item.code;
};

const onDragLeave = (e, item) => {
  if (dragOverCode.value === item.code) {
    dragOverCode.value = null;
  }
};

const onDrop = async (e, targetItem, targetModule) => {
  e.preventDefault();
  dragOverCode.value = null;

  if (!draggedItem.value) return;
  if (draggedItem.value.code === targetItem.code) return;

  const itemToMove = draggedItem.value;

  try {
    // Case 1: Reordering within the same module
    if (draggedFromModule.value === targetModule) {
      const moduleItems = [...menuStore.menus.filter((m) => m.module === targetModule)]
        .sort((a, b) => (a.order || 0) - (b.order || 0));

      const fromIndex = moduleItems.findIndex((m) => m.code === itemToMove.code);
      const toIndex = moduleItems.findIndex((m) => m.code === targetItem.code);

      if (fromIndex > -1 && toIndex > -1) {
        const [removed] = moduleItems.splice(fromIndex, 1);
        moduleItems.splice(toIndex, 0, removed);
        await menuStore.reorderModuleItems(targetModule, moduleItems);
        showAlert(`Urutan menu "${itemToMove.title}" berhasil diubah.`);
      }
    } else {
      // Case 2: Moving across modules
      const movedItem = { ...itemToMove, module: targetModule };
      const targetItems = [...menuStore.menus.filter((m) => m.module === targetModule)]
        .sort((a, b) => (a.order || 0) - (b.order || 0));
      const toIndex = targetItems.findIndex((m) => m.code === targetItem.code);
      if (toIndex > -1) targetItems.splice(toIndex, 0, movedItem);
      else targetItems.push(movedItem);
      await menuStore.reorderModuleItems(targetModule, targetItems);
      showAlert(`Menu "${itemToMove.title}" dipindahkan ke modul "${targetModule}".`);
    }
  } catch (error) {
    showAlert(error?.message || 'Gagal menyimpan urutan menu ke backend.', 'error');
  } finally {
    draggedItem.value = null;
    draggedFromModule.value = null;
  }
};

const onDragEnd = (e) => {
  draggedItem.value = null;
  draggedFromModule.value = null;
  dragOverCode.value = null;
  if (e.target) {
    e.target.classList.remove('opacity-50');
  }
};

// CRUD Operations
const openCreateModal = (defaultModule = null) => {
  isEditMode.value = false;
  menuForm.value = {
    id: '',
    code: '',
    title: '',
    module: defaultModule || 'Master Data',
    parentCode: null,
    path: '',
    icon: 'bi-circle',
    order: menuStore.menus.length + 1,
    status: 'AKTIF',
    description: '',
    roles: ['SUPER_ADMIN']
  };
  showModal.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  menuForm.value = {
    id: item.id,
    code: item.code,
    title: item.title,
    module: item.module,
    parentCode: item.parentCode || null,
    path: item.path,
    icon: item.icon || 'bi-circle',
    order: item.order || 1,
    status: item.status || 'AKTIF',
    description: item.description || '',
    roles: Array.isArray(item.roles) ? [...item.roles] : ['SUPER_ADMIN']
  };
  showModal.value = true;
};

const selectAllRoles = () => {
  menuForm.value.roles = [...allRoles.value];
};

const deselectAllRoles = () => {
  menuForm.value.roles = ['SUPER_ADMIN'];
};

const saveMenu = async () => {
  try {
    if (isEditMode.value) {
      await menuStore.updateMenu(menuForm.value.id || menuForm.value.code, menuForm.value);
      showAlert(`Menu "${menuForm.value.title}" berhasil diperbarui.`);
    } else {
      await menuStore.addMenu(menuForm.value);
      showAlert(`Menu baru "${menuForm.value.title}" berhasil ditambahkan.`);
      // Auto expand the module where it was added
      expandedModules.value.add(menuForm.value.module);
    }
    showModal.value = false;
  } catch (err) {
    showAlert(err.message || 'Gagal menyimpan data menu.', 'error');
  }
};

const deleteMenuItem = async (item) => {
  if (confirm(`Apakah Anda yakin ingin menghapus menu "${item.title}" (${item.code})?`)) {
    try {
      await menuStore.deleteMenu(item.id || item.code);
      showAlert(`Menu "${item.title}" berhasil dihapus.`);
    } catch (err) {
      showAlert(err.message || 'Gagal menghapus menu.', 'error');
    }
  }
};

const toggleMenuStatus = async (item) => {
  try {
    const newStatus = await menuStore.toggleStatus(item.id || item.code);
    showAlert(`Status menu "${item.title}" diubah menjadi ${newStatus}.`);
  } catch (err) {
    showAlert(err.message || 'Gagal mengubah status menu.', 'error');
  }
};


// Export Configuration as JSON
const exportConfiguration = () => {
  const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(menuStore.menus, null, 2));
  const downloadAnchor = document.createElement('a');
  downloadAnchor.setAttribute('href', dataStr);
  downloadAnchor.setAttribute('download', `jims_menu_tree_${new Date().toISOString().slice(0, 10)}.json`);
  document.body.appendChild(downloadAnchor);
  downloadAnchor.click();
  downloadAnchor.remove();
  showAlert('Konfigurasi pohon menu berhasil diekspor.');
};
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.cursor-grab {
  cursor: grab;
}

.cursor-grab:active {
  cursor: grabbing;
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


.tree-module-block.dragging-module {
  opacity: 0.45;
  border: 2px dashed #dc3545 !important;
}

.tree-module-block.drag-over-module {
  border-top: 3px solid #dc3545 !important;
  box-shadow: 0 -3px 8px rgba(220, 53, 69, 0.25) !important;
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

.tree-leaf-item.dragging {
  opacity: 0.4;
  border: 1px dashed #dc3545 !important;
}

.tree-leaf-item.drag-over-indicator {
  border-top: 2px solid #dc3545 !important;
  box-shadow: 0 -2px 6px rgba(220, 53, 69, 0.2);
}

.drag-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--bs-secondary);
}

.drag-handle:hover {
  color: #dc3545;
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

.matrix-table-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.matrix-table-container::-webkit-scrollbar-thumb {
  background: var(--bs-border-color);
  border-radius: 3px;
}
</style>

<template>
  <div class="regions-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Data Wilayah</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/master/items" class="text-decoration-none text-body">Master Data</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Master Wilayah</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Alert -->
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show fs-8 mb-3" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMessage }}
      <button type="button" class="btn-close" @click="errorMessage = ''" aria-label="Close"></button>
    </div>

    <!-- Success Alert -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show fs-8 mb-3" role="alert">
      <i class="bi bi-check-circle-fill me-1"></i> {{ successMessage }}
      <button type="button" class="btn-close" @click="successMessage = ''" aria-label="Close"></button>
    </div>

    <!-- Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs mb-3">
      <!-- Card Header with Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            Daftar Wilayah Operasional
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-outline-secondary fs-8" @click="loadData" :disabled="loading" title="Refresh Data">
            Refresh
          </button>
          <button v-if="canManage" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            Tambah
          </button>
        </div>
      </div>
      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
              <select v-model="statusFilter" class="form-select form-select-sm border-start-0 fs-8" @change="currentPage = 1">
                <option value="ALL">Semua Status</option>
                <option value="ACTIVE">Aktif</option>
                <option value="INACTIVE">Non-Aktif</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || statusFilter !== 'ALL'">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari kode, nama wilayah, cabang..."
                @keyup.enter="currentPage = 1"
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Regions Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th style="width: 140px;" class="ps-3 py-2 text-uppercase fs-9">Kode Wilayah</th>
              <th style="width: 250px;" class="py-2 text-uppercase fs-9">Nama Wilayah</th>
              <th class="py-2 text-uppercase fs-9">Cabang Terafiliasi</th>
              <th style="width: 110px;" class="text-center py-2 text-uppercase fs-9">Jml Cabang</th>
              <th style="width: 110px;" class="text-center py-2 text-uppercase fs-9">Status</th>
              <th v-if="canManage" style="width: 130px;" class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && filteredRegions.length === 0">
              <td :colspan="canManage ? 6 : 5" class="text-center py-4 text-secondary fs-8">
                <i class="bi bi-geo-alt fs-3 d-block mb-1 text-muted"></i>
                Tidak ada data wilayah yang sesuai kriteria pencarian.
              </td>
            </tr>
            <tr v-for="reg in paginatedRegions" :key="reg.id">
              <td class="ps-3">
                <span class="badge bg-danger-subtle text-danger border border-danger-subtle font-monospace fw-bold px-2 py-1">
                  {{ reg.code }}
                </span>
              </td>
              <td>
                <div class="fw-bold text-body">{{ reg.name }}</div>
                <div v-if="reg.description" class="text-muted fs-8 text-truncate" style="max-width: 300px;" :title="reg.description">
                  {{ reg.description }}
                </div>
              </td>
              <td>
                <div class="d-flex flex-wrap gap-1 align-items-center">
                  <template v-if="reg.branches && reg.branches.length > 0">
                    <span
                      v-for="branch in reg.branches"
                      :key="branch.id"
                      class="badge bg-light text-body border font-monospace fs-9 fw-semibold px-2 py-1"
                      :title="branch.code + ' - ' + branch.name"
                    >
                     {{ branch.name }}
                    </span>
                  </template>
                  <span v-else class="text-muted fs-8 fst-italic">
                    Belum ada cabang terpetakan
                  </span>
                </div>
              </td>
              <td class="text-center">
                <span class="badge" :class="(reg.branchCount || 0) > 0 ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-secondary-subtle text-secondary'">
                  {{ reg.branchCount || 0 }} Cabang
                </span>
              </td>
              <td class="text-center">
                <span class="badge" :class="reg.isActive ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle'">
                  {{ reg.isActive ? 'Aktif' : 'Non-Aktif' }}
                </span>
              </td>
              <td v-if="canManage" class="text-center pe-3">
                <div class="d-inline-flex gap-1">
                  <button
                    type="button"
                    class="btn btn-xs btn-outline-primary"
                    @click="openBranchMappingModal(reg)"
                    title="Petakan Cabang"
                  >
                    <i class="bi bi-diagram-3"></i>
                  </button>
                  <button
                    type="button"
                    class="btn btn-xs btn-outline-secondary"
                    @click="openEditModal(reg)"
                    title="Edit Wilayah"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button
                    type="button"
                    class="btn btn-xs btn-outline-danger"
                    @click="confirmDelete(reg)"
                    title="Hapus Wilayah"
                  >
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <PaginationFooter
        v-if="filteredRegions.length > 0"
        :total="filteredRegions.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Tambah / Edit Wilayah) -->
    <div v-if="showFormModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content shadow-lg border-0">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h5 class="modal-title fs-7 fw-bold">
              {{ isEditMode ? 'Ubah Data Wilayah' : 'Tambah Wilayah Baru' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="showFormModal = false"></button>
          </div>
          <form @submit.prevent="saveRegion">
            <div class="modal-body p-3">
              <div class="row g-3">
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold fs-8 mb-1">Kode Wilayah <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.code"
                    class="form-control form-control-sm font-monospace text-uppercase"
                    placeholder="Contoh: WIL_05"
                    required
                    :disabled="isEditMode"
                  />
                  <div class="form-text fs-9 text-muted">Format standar kode wilayah JIMS.</div>
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fw-bold fs-8 mb-1">Nama Wilayah <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.name"
                    class="form-control form-control-sm"
                    placeholder="Contoh: Wilayah V (Madura Raya)"
                    required
                  />
                </div>
                <div class="col-12">
                  <label class="form-label fw-bold fs-8 mb-1">Deskripsi / Catatan Wilayah</label>
                  <textarea
                    v-model="form.description"
                    class="form-control form-control-sm"
                    rows="2"
                    placeholder="Keterangan koordinator wilayah dan cakupan operasional..."
                  ></textarea>
                </div>
                <div class="col-12">
                  <div class="form-check form-switch">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      role="switch"
                      id="regionActiveSwitch"
                      v-model="form.isActive"
                    />
                    <label class="form-check-label fw-semibold fs-8" for="regionActiveSwitch">
                      Status Wilayah Aktif
                    </label>
                  </div>
                </div>

                <!-- Assigned Branches Checklist inside Form -->
                <div class="col-12">
                  <div class="border rounded p-2 bg-body-tertiary">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <label class="form-label fw-bold fs-8 mb-0">
                        <i class="bi bi-building me-1"></i> Pilih Kantor Cabang Anggota
                      </label>
                      <div class="d-flex gap-2">
                        <button type="button" class="btn btn-xs btn-outline-secondary" @click="selectAllFormBranches">
                          Pilih Semua
                        </button>
                        <button type="button" class="btn btn-xs btn-outline-secondary" @click="clearAllFormBranches">
                          Batal Semua
                        </button>
                      </div>
                    </div>
                    <div class="input-group input-group-sm mb-2">
                      <span class="input-group-text bg-body text-secondary border-end-0 fs-9"><i class="bi bi-search"></i></span>
                      <input
                        type="text"
                        v-model="formBranchSearch"
                        class="form-control form-control-sm border-start-0 fs-9"
                        placeholder="Cari cabang..."
                      />
                    </div>
                    <div class="branch-selection-container" style="max-height: 180px; overflow-y: auto;">
                      <div v-if="filteredAvailableBranches.length === 0" class="text-center py-2 text-muted fs-9">
                        Tidak ada cabang ditemukan.
                      </div>
                      <div class="row g-2">
                        <div
                          v-for="branch in filteredAvailableBranches"
                          :key="branch.id"
                          class="col-12 col-md-6"
                        >
                          <label class="form-check d-flex align-items-center gap-2 p-1 border rounded bg-body cursor-pointer mb-0">
                            <input
                              class="form-check-input m-0 ms-1"
                              type="checkbox"
                              :value="branch.id"
                              v-model="form.organizationIds"
                            />
                            <span class="fs-9 flex-grow-1 text-truncate" :title="branch.code + ' - ' + branch.name">
                              <span class="fw-bold">{{ branch.code }}</span> - {{ branch.name }}
                            </span>
                            <span v-if="branch.region && branch.region.id !== editingRegionId" class="badge bg-warning-subtle text-warning border fs-9 text-nowrap">
                              Wilayah Lain
                            </span>
                          </label>
                        </div>
                      </div>
                    </div>
                    <div class="fs-9 text-muted mt-2">
                      Terpilih: <strong>{{ form.organizationIds.length }}</strong> kantor cabang utama.
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-secondary" @click="showFormModal = false" :disabled="submitting">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-3" :disabled="submitting">
                <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
                <span v-else>Simpan</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Form (Quick Branch Mapping) -->
    <div v-if="showMappingModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content shadow-lg border-0">
          <div class="modal-header bg-primary text-white py-2 px-3">
            <h5 class="modal-title fs-7 fw-bold">
              Petakan Cabang: {{ selectedRegion?.name }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="showMappingModal = false"></button>
          </div>
          <div class="modal-body p-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <div class="input-group input-group-sm w-50">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-9"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="mappingSearch"
                  class="form-control form-control-sm border-start-0 fs-9"
                  placeholder="Cari cabang..."
                />
              </div>
              <div class="d-flex gap-2">
                <button type="button" class="btn btn-xs btn-outline-secondary" @click="selectAllMappingBranches">
                  Pilih Semua
                </button>
                <button type="button" class="btn btn-xs btn-outline-secondary" @click="clearAllMappingBranches">
                  Batal Semua
                </button>
              </div>
            </div>

            <div class="branch-selection-container p-2 border rounded bg-body-tertiary" style="max-height: 320px; overflow-y: auto;">
              <div v-if="filteredMappingBranches.length === 0" class="text-center py-3 text-muted fs-8">
                Tidak ada kantor cabang yang cocok.
              </div>
              <div class="row g-2">
                <div
                  v-for="branch in filteredMappingBranches"
                  :key="branch.id"
                  class="col-12 col-md-6"
                >
                  <label class="form-check d-flex align-items-center gap-2 p-2 border rounded bg-body cursor-pointer mb-0">
                    <input
                      class="form-check-input m-0 ms-1"
                      type="checkbox"
                      :value="branch.id"
                      v-model="mappingBranchIds"
                    />
                    <div class="fs-9 flex-grow-1 text-truncate">
                      <div class="fw-bold text-body">{{ branch.name }}</div>
                      <div class="text-secondary">{{ branch.code }} &bull; {{ branch.city || '-' }}</div>
                    </div>
                    <span
                      v-if="branch.region && branch.region.id !== selectedRegion?.id"
                      class="badge bg-warning-subtle text-warning border fs-9 text-nowrap"
                      :title="'Saat ini di ' + branch.region.name"
                    >
                      {{ branch.region.code }}
                    </span>
                    <span
                      v-else-if="branch.region && branch.region.id === selectedRegion?.id"
                      class="badge bg-success-subtle text-success border fs-9 text-nowrap"
                    >
                      Terpetakan
                    </span>
                  </label>
                </div>
              </div>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2 fs-9 text-muted">
              <div>Cabang terpilih: <strong>{{ mappingBranchIds.length }}</strong> cabang</div>
              <div>Sub-cabang (Capem) akan otomatis mengikuti cabang induknya.</div>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-secondary" @click="showMappingModal = false" :disabled="submitting">
              Batal
            </button>
            <button type="button" class="btn btn-sm btn-primary fw-bold" @click="saveBranchMapping" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              Terapkan Pemetaan
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';
import { useAuthStore } from '@/stores/auth';
import PaginationFooter from '@/components/PaginationFooter.vue';

const authStore = useAuthStore();

// Permission check
const canManage = computed(() => {
  const role = authStore.userRole;
  return ['SUPER_ADMIN', 'USER_ADMIN', 'MASTER_MAKER'].includes(role);
});

// State
const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const regions = ref([]);
const branches = ref([]);

// Filters
const searchQuery = ref('');
const statusFilter = ref('ALL');
const currentPage = ref(1);
const perPage = ref(10);

const resetFilters = () => {
  searchQuery.value = '';
  statusFilter.value = 'ALL';
  currentPage.value = 1;
};

// Form Modal State
const showFormModal = ref(false);
const isEditMode = ref(false);
const editingRegionId = ref(null);
const formBranchSearch = ref('');
const form = reactive({
  code: '',
  name: '',
  description: '',
  isActive: true,
  organizationIds: []
});

// Quick Mapping Modal State
const showMappingModal = ref(false);
const selectedRegion = ref(null);
const mappingSearch = ref('');
const mappingBranchIds = ref([]);

// Filtered Regions
const filteredRegions = computed(() => {
  return regions.value.filter(reg => {
    // Status Filter
    if (statusFilter.value === 'ACTIVE' && !reg.isActive) return false;
    if (statusFilter.value === 'INACTIVE' && reg.isActive) return false;

    // Search Query
    if (searchQuery.value.trim()) {
      const q = searchQuery.value.trim().toLowerCase();
      const matchCode = reg.code && reg.code.toLowerCase().includes(q);
      const matchName = reg.name && reg.name.toLowerCase().includes(q);
      const matchDesc = reg.description && reg.description.toLowerCase().includes(q);
      const matchBranch = reg.branches && reg.branches.some(b => 
        (b.name && b.name.toLowerCase().includes(q)) || (b.code && b.code.toLowerCase().includes(q))
      );
      return matchCode || matchName || matchDesc || matchBranch;
    }
    return true;
  });
});

// Paginated Regions
const paginatedRegions = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredRegions.value.slice(start, start + perPage.value);
});

// Available branches for Form
const filteredAvailableBranches = computed(() => {
  if (!formBranchSearch.value.trim()) return branches.value;
  const q = formBranchSearch.value.trim().toLowerCase();
  return branches.value.filter(b => 
    (b.name && b.name.toLowerCase().includes(q)) || (b.code && b.code.toLowerCase().includes(q))
  );
});

// Available branches for Mapping Modal
const filteredMappingBranches = computed(() => {
  if (!mappingSearch.value.trim()) return branches.value;
  const q = mappingSearch.value.trim().toLowerCase();
  return branches.value.filter(b => 
    (b.name && b.name.toLowerCase().includes(q)) || (b.code && b.code.toLowerCase().includes(q))
  );
});

// Fetch Data
const loadData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [regRes, orgRes] = await Promise.all([
      api.get('/master/regions'),
      api.get('/master/organizations')
    ]);

    const regData = regRes?.data || regRes || [];
    regions.value = Array.isArray(regData) ? regData : [];

    const orgData = orgRes?.data || orgRes || [];
    const allOrgs = Array.isArray(orgData) ? orgData : [];
    // Only take MAIN_BRANCH for region mapping (SUB_BRANCH inherits from parent)
    branches.value = allOrgs.filter(o => o.type === 'MAIN_BRANCH' || o.type === 'CABANG_UTAMA');
  } catch (err) {
    console.error('Failed to load regions or organizations:', err);
    errorMessage.value = err?.message || err?.error || 'Gagal memuat data master wilayah.';
  } finally {
    loading.value = false;
  }
};

// Open Create Modal
const openCreateModal = () => {
  isEditMode.value = false;
  editingRegionId.value = null;
  formBranchSearch.value = '';
  Object.assign(form, {
    code: '',
    name: '',
    description: '',
    isActive: true,
    organizationIds: []
  });
  showFormModal.value = true;
};

// Open Edit Modal
const openEditModal = (reg) => {
  isEditMode.value = true;
  editingRegionId.value = reg.id;
  formBranchSearch.value = '';
  const currentBranchIds = reg.branches ? reg.branches.map(b => b.id) : [];
  Object.assign(form, {
    code: reg.code,
    name: reg.name,
    description: reg.description || '',
    isActive: reg.isActive !== false,
    organizationIds: [...currentBranchIds]
  });
  showFormModal.value = true;
};

// Save Region (Create / Edit)
const saveRegion = async () => {
  submitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    const payload = {
      code: form.code,
      name: form.name,
      description: form.description,
      isActive: form.isActive,
      organizationIds: form.organizationIds
    };

    if (isEditMode.value) {
      await api.put(`/master/regions/${editingRegionId.value}`, payload);
      successMessage.value = `Wilayah ${form.name} berhasil diperbarui.`;
    } else {
      await api.post('/master/regions', payload);
      successMessage.value = `Wilayah ${form.name} berhasil ditambahkan.`;
    }

    showFormModal.value = false;
    await loadData();
  } catch (err) {
    console.error('Failed to save region:', err);
    errorMessage.value = err?.message || err?.error || 'Gagal menyimpan data wilayah.';
  } finally {
    submitting.value = false;
  }
};

// Open Branch Mapping Modal
const openBranchMappingModal = (reg) => {
  selectedRegion.value = reg;
  mappingSearch.value = '';
  mappingBranchIds.value = reg.branches ? reg.branches.map(b => b.id) : [];
  showMappingModal.value = true;
};

// Save Branch Mapping
const saveBranchMapping = async () => {
  if (!selectedRegion.value) return;
  submitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await api.put(`/master/regions/${selectedRegion.value.id}/branches`, mappingBranchIds.value);
    successMessage.value = `Pemetaan cabang untuk ${selectedRegion.value.name} berhasil disimpan.`;
    showMappingModal.value = false;
    await loadData();
  } catch (err) {
    console.error('Failed to assign branches:', err);
    errorMessage.value = err?.message || err?.error || 'Gagal menyimpan pemetaan cabang.';
  } finally {
    submitting.value = false;
  }
};

// Delete Region
const confirmDelete = async (reg) => {
  const confirmMsg = `Yakin ingin menghapus wilayah "${reg.name}" (${reg.code})?\nSemua cabang dan user yang terafiliasi akan dilepaskan pemetaannya.`;
  if (!window.confirm(confirmMsg)) return;

  loading.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await api.delete(`/master/regions/${reg.id}`);
    successMessage.value = `Wilayah ${reg.name} berhasil dihapus.`;
    await loadData();
  } catch (err) {
    console.error('Failed to delete region:', err);
    errorMessage.value = err?.message || err?.error || 'Gagal menghapus wilayah.';
  } finally {
    loading.value = false;
  }
};

// Select / Clear all helpers
const selectAllFormBranches = () => {
  form.organizationIds = branches.value.map(b => b.id);
};

const clearAllFormBranches = () => {
  form.organizationIds = [];
};

const selectAllMappingBranches = () => {
  mappingBranchIds.value = branches.value.map(b => b.id);
};

const clearAllMappingBranches = () => {
  mappingBranchIds.value = [];
};

onMounted(loadData);
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.btn-xs {
  padding: 0.15rem 0.4rem;
  font-size: 0.75rem;
  border-radius: 0.2rem;
}
</style>

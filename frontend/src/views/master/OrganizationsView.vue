<template>
  <div class="organizations-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Unit Kerja & Gudang Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/master/items" class="text-decoration-none text-body">Master Data</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Unit Kerja & Gudang</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-building"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total Entitas Unit</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ orgList.length }} Unit</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Struktur Organisasi Bank Jatim</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-diagram-3"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Jaringan Cabang</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">{{ branchCount }} Cabang</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 80%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">KC Surabaya, Malang, Sidoarjo, Jember</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-inboxes-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Gudang Logistik</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ warehouseCount }} Hub</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Gudang Margomulyo Surabaya</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-check2-circle"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Status Operasional</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">{{ activePercent }}% Aktif</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Semua Unit Kerja Siap Melayani</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            <i class="bi bi-building-gear text-danger me-2"></i>Daftar Entitas Unit Kerja & Gudang Terdaftar
          </h3>
          <span class="badge text-bg-danger fs-9">{{ orgList.length }} Unit</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Tambah Unit Kerja
          </button>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
              <select v-model="filterType" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Tipe Entitas</option>
                <option v-for="type in typeOptions" :key="type" :value="type">{{ type }}</option>
                <option value="CENTRAL_WAREHOUSE">CENTRAL_WAREHOUSE</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterType">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Kode, Nama Unit, Kota..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                <i class="bi bi-search me-1"></i> Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">Kode Unit</th>
              <th class="py-2 text-uppercase fs-9">Nama Kantor / Unit Kerja</th>
              <th class="py-2 text-uppercase fs-9">Tipe Entitas</th>
              <th class="py-2 text-uppercase fs-9">Kota / Wilayah</th>
              <th class="py-2 text-uppercase fs-9">Alamat Operasional</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="org in paginatedOrgs" :key="org.id">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ org.code }}</td>
              <td class="py-2 fw-semibold text-body">{{ org.name }}</td>
              <td class="py-2"><span class="badge text-bg-light border text-secondary">{{ org.type }}</span></td>
              <td class="py-2">{{ org.city }}</td>
              <td class="py-2 fs-8 text-secondary">{{ org.address }}</td>
              <td class="py-2"><span class="badge text-bg-success">{{ org.status }}</span></td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(org)" title="Edit Unit"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteOrg(org)" title="Hapus Unit"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredOrgs.length === 0">
              <td colspan="7" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data unit kerja yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredOrgs.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Modal Form (Tambah / Edit Unit Kerja) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              {{ isEditMode ? 'Edit Unit Kerja' : 'Tambah Unit Kerja Baru' }}
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup"><i class="bi bi-x-lg"></i></button>
          </div>
          <form @submit.prevent="saveOrg">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kode Unit <span class="text-danger">*</span></label>
                  <input type="text" v-model="orgForm.code" class="form-control form-control-sm font-monospace" placeholder="Contoh: KC-SBY-02" required />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fw-bold mb-1">Nama Kantor / Unit Kerja <span class="text-danger">*</span></label>
                  <input type="text" v-model="orgForm.name" class="form-control form-control-sm" placeholder="Contoh: KC Surabaya Darmo" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Tipe Entitas</label>
                  <select v-model="orgForm.type" class="form-select form-select-sm">
                    <option value="HEAD_OFFICE">HEAD_OFFICE</option>
                    <option value="BRANCH">BRANCH (KANTOR CABANG)</option>
                    <option value="SUB_BRANCH">SUB_BRANCH (KCP)</option>
                    <option value="CASH_OFFICE">CASH_OFFICE (KAS)</option>
                    <option value="CENTRAL_WAREHOUSE">CENTRAL_WAREHOUSE</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kota / Wilayah <span class="text-danger">*</span></label>
                  <input type="text" v-model="orgForm.city" class="form-control form-control-sm" placeholder="Contoh: Surabaya" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Status</label>
                  <select v-model="orgForm.status" class="form-select form-select-sm">
                    <option value="AKTIF">AKTIF</option>
                    <option value="NONAKTIF">NONAKTIF</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label fw-bold mb-1">Alamat Operasional <span class="text-danger">*</span></label>
                  <textarea v-model="orgForm.address" class="form-control form-control-sm" rows="2" placeholder="Alamat lengkap gedung/kantor..." required></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                <i class="bi bi-check2-circle me-1"></i> Simpan Unit Kerja
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';

const showModal = ref(false);
const isEditMode = ref(false);
const editingId = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterType = ref('');

const orgList = ref([]);

onMounted(() => {
  loadOrganizations();
});

const loadOrganizations = async () => {
  const response = await api.get('/master/organizations');
  orgList.value = (response.data || []).map((org) => ({
    id: org.id,
    code: org.code,
    name: org.name,
    type: org.type,
    city: org.city || '-',
    address: org.address || '-',
    status: org.isActive ? 'AKTIF' : 'NONAKTIF'
  }));
};

const typeOptions = computed(() => Array.from(new Set(orgList.value.map((org) => org.type))).filter(Boolean).sort());
const branchCount = computed(() => orgList.value.filter((org) => org.type?.includes('BRANCH')).length);
const warehouseCount = computed(() => orgList.value.filter((org) => org.type?.includes('WAREHOUSE')).length);
const activePercent = computed(() => {
  if (orgList.value.length === 0) return 0;
  return Math.round((orgList.value.filter((org) => org.status === 'AKTIF').length / orgList.value.length) * 100);
});

const orgForm = reactive({
  code: '',
  name: '',
  type: 'BRANCH',
  city: '',
  address: '',
  status: 'AKTIF'
});

const resetFilters = () => {
  searchQuery.value = '';
  filterType.value = '';
  currentPage.value = 1;
};

const filteredOrgs = computed(() => {
  return orgList.value.filter(org => {
    const matchQuery = !searchQuery.value ||
      org.code.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      org.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      org.city.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchT = !filterType.value || org.type === filterType.value;
    return matchQuery && matchT;
  });
});

const paginatedOrgs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredOrgs.value.slice(start, start + perPage.value);
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingId.value = null;
  Object.assign(orgForm, {
    code: '',
    name: '',
    type: 'BRANCH',
    city: '',
    address: '',
    status: 'AKTIF'
  });
  showModal.value = true;
};

const openEditModal = (org) => {
  isEditMode.value = true;
  editingId.value = org.id;
  Object.assign(orgForm, {
    code: org.code,
    name: org.name,
    type: org.type,
    city: org.city,
    address: org.address,
    status: org.status
  });
  showModal.value = true;
};

const saveOrg = () => {
  alert('Penyimpanan unit kerja belum tersedia di backend production.');
  showModal.value = false;
};

const deleteOrg = (org) => {
  alert(`Penghapusan unit kerja "${org.name}" belum tersedia di backend production.`);
};
</script>

<style scoped>
.btn-close-modal {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  color: var(--bs-secondary);
  cursor: pointer;
}
.btn-close-modal:hover {
  color: var(--bs-dark);
}
</style>

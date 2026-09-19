<template>
  <div class="switching-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Switching Stock Antar-Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Persediaan</li>
              <li class="breadcrumb-item active" aria-current="page">Switching Stock</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua Pengajuan
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'waiting' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'waiting'"
            >
              Menunggu Approval
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/inventory/switching/approvals" class="btn btn-sm btn-outline-secondary fs-8">
            Antrean Approval
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            Tambah
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-geo-alt"></i>
              </span>
              <select v-model="filterFrom" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Cabang Asal</option>
                <option v-for="org in organizations" :key="org.id" :value="org.name">{{ org.name }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterTo" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Cabang Tujuan</option>
                <option v-for="org in organizations" :key="org.id" :value="org.name">{{ org.name }}</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterFrom !== 'ALL' || filterTo !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              Reset
            </button>
          </div>
          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-search"></i>
              </span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No Pengajuan, Barang, atau Cabang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. Pengajuan</th>
              <th>Nama Barang / SKU</th>
              <th>Cabang Asal (Surplus)</th>
              <th>Cabang Tujuan (Defisit)</th>
              <th class="text-end">Jumlah Transfer</th>
              <th class="text-center">Status Alur</th>
              <th class="text-center pe-3" style="width: 100px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="s in paginatedSwitchingList" :key="s.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ s.switchNo }}</td>
              <td class="fw-semibold text-body">{{ s.itemName }}</td>
              <td><span class="badge text-bg-light border fs-9">{{ s.fromBranch }}</span></td>
              <td><span class="badge text-bg-light border text-primary fs-9">{{ s.toBranch }}</span></td>
              <td class="text-end font-monospace fw-bold text-body">{{ s.qty.toLocaleString('id-ID') }}</td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="s.status.includes('WAITING') ? 'text-bg-warning' : 'text-bg-success'">{{ s.status.replace('_', ' ') }}</span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-secondary" @click="openDetailModal(s)" title="Detail"><i class="bi bi-eye"></i></button>
              </td>
            </tr>
            <tr v-if="filteredSwitchingList.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data pengajuan switching stock yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredSwitchingList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form Usulkan Switching Baru -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Form Pengajuan Switching Stock Antar-Cabang
            </h6>
            <button type="button" class="btn-close" @click="showModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="saveSwitching">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Barang Persediaan <span class="text-danger">*</span></label>
                  <select v-model="switchForm.itemId" class="form-select form-select-sm fs-8" required>
                    <option value="">Pilih barang</option>
                    <option v-for="item in items" :key="item.id" :value="item.id">
                      {{ item.name }} [{{ item.sku }}]
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Unit Transfer <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="switchForm.qty" class="form-control form-control-sm fs-8 font-monospace" min="1" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Cabang Asal (Stok Surplus) <span class="text-danger">*</span></label>
                  <select v-model="switchForm.sourceOrganizationId" class="form-select form-select-sm fs-8" required>
                    <option value="">Pilih cabang asal</option>
                    <option v-for="org in organizations" :key="org.id" :value="org.id">{{ org.code }} - {{ org.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Cabang Tujuan (Stok Defisit) <span class="text-danger">*</span></label>
                  <select v-model="switchForm.destinationOrganizationId" class="form-select form-select-sm fs-8" required>
                    <option value="">Pilih cabang tujuan</option>
                    <option v-for="org in organizations" :key="org.id" :value="org.id">{{ org.code }} - {{ org.name }}</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alasan Rekomendasi</label>
                  <textarea v-model="switchForm.reason" rows="2" class="form-control form-control-sm fs-8"></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                Kirim Pengajuan Switching
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Detail Switching -->
    <div v-if="showDetailModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showDetailModal = false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Rincian Pengajuan Switching Stock
            </h6>
            <button type="button" class="btn-close" @click="showDetailModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-3 fs-8" v-if="selectedSwitch">
            <div class="mb-3">
              <div class="text-secondary fs-9 fw-bold text-uppercase">Nomor Dokumen Pengajuan:</div>
              <div class="font-monospace fw-bold fs-6 text-danger">{{ selectedSwitch.switchNo }}</div>
            </div>
            <div class="mb-3">
              <div class="text-secondary fs-9 fw-bold text-uppercase">Nama Barang:</div>
              <div class="fw-bold text-body fs-7">{{ selectedSwitch.itemName }}</div>
            </div>
            <div class="row g-2 pt-2 border-top">
              <div class="col-6">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Cabang Asal (Surplus):</div>
                <div class="badge text-bg-secondary fs-8 mt-1">{{ selectedSwitch.fromBranch }}</div>
              </div>
              <div class="col-6">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Cabang Tujuan (Defisit):</div>
                <div class="badge text-bg-primary fs-8 mt-1">{{ selectedSwitch.toBranch }}</div>
              </div>
              <div class="col-6 mt-3">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Jumlah Transfer:</div>
                <div class="font-monospace fw-bold fs-6 text-body mt-1">{{ selectedSwitch.qty.toLocaleString('id-ID') }} Unit</div>
              </div>
              <div class="col-6 mt-3">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Status Persetujuan:</div>
                <div class="badge text-bg-warning fs-8 mt-1">{{ selectedSwitch.status }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showDetailModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';
import { toast } from '@/utils/toast';

const showModal = ref(false);
const showDetailModal = ref(false);
const selectedSwitch = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterFrom = ref('ALL');
const filterTo = ref('ALL');
const organizations = ref([]);
const warehouses = ref([]);
const items = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterFrom.value = 'ALL';
  filterTo.value = 'ALL';
  currentPage.value = 1;
};

const firstItem = (switching) => switching.items?.[0] || {};

const mapSwitching = (switching) => ({
  id: switching.id,
  switchNo: switching.switchNo || `SW-${switching.id}`,
  itemName: firstItem(switching).item?.name || '-',
  fromBranch: switching.sourceOrganization?.name || '-',
  toBranch: switching.destinationOrganization?.name || '-',
  qty: Number(firstItem(switching).qtyRequested || 0),
  status: switching.status || '-',
  reason: switching.recommendationReason || '-'
});

const switchingList = ref([]);

const loadOptions = async () => {
  try {
    const [organizationResponse, warehouseResponse, itemResponse] = await Promise.allSettled([
      api.get('/master/organizations'),
      api.get('/master/warehouses'),
      api.get('/master/items')
    ]);
    organizations.value = organizationResponse.status === 'fulfilled' && Array.isArray(organizationResponse.value.data) ? organizationResponse.value.data : [];
    warehouses.value = warehouseResponse.status === 'fulfilled' && Array.isArray(warehouseResponse.value.data) ? warehouseResponse.value.data : [];
    items.value = itemResponse.status === 'fulfilled' && Array.isArray(itemResponse.value.data) ? itemResponse.value.data : [];
  } catch (e) {
    console.warn('Failed loading options', e);
    organizations.value = [];
    warehouses.value = [];
    items.value = [];
  }
};

const loadSwitching = async () => {
  try {
    const response = await api.get('/inventory/switching');
    const data = response.data || [];
    switchingList.value = Array.isArray(data) ? data.map(mapSwitching) : [];
  } catch (e) {
    console.warn('Failed loading switching from server:', e);
    switchingList.value = [];
  }
};

const loadPage = async () => {
  errorMessage.value = '';
  await Promise.allSettled([loadOptions(), loadSwitching()]);
};

const waitingCount = computed(() => {
  return switchingList.value.filter(s => s.status.includes('WAITING')).length;
});

const completedCount = computed(() => {
  return switchingList.value.filter(s => s.status === 'COMPLETED').length;
});

const totalQty = computed(() => {
  return switchingList.value.reduce((acc, curr) => acc + (curr.qty || 0), 0);
});

const switchForm = reactive({
  itemId: '',
  sourceOrganizationId: '',
  destinationOrganizationId: '',
  qty: 100,
  reason: ''
});

const openCreateModal = () => {
  Object.assign(switchForm, {
    itemId: '',
    sourceOrganizationId: '',
    destinationOrganizationId: '',
    qty: 100,
    reason: ''
  });
  showModal.value = true;
};

const openDetailModal = (s) => {
  selectedSwitch.value = s;
  showDetailModal.value = true;
};

const warehouseForOrg = (organizationId) => {
  return warehouses.value.find((warehouse) => String(warehouse.organization?.id) === String(organizationId));
};

const saveSwitching = async () => {
  const sourceWarehouse = warehouseForOrg(switchForm.sourceOrganizationId);
  const destinationWarehouse = warehouseForOrg(switchForm.destinationOrganizationId);
  if (!sourceWarehouse || !destinationWarehouse) {
    toast.warn('Gudang asal/tujuan belum tersedia untuk cabang yang dipilih.');
    return;
  }

  try {
    await api.post('/inventory/switching', {
      sourceOrganizationId: switchForm.sourceOrganizationId,
      sourceWarehouseId: sourceWarehouse.id,
      destinationOrganizationId: switchForm.destinationOrganizationId,
      destinationWarehouseId: destinationWarehouse.id,
      recommendationReason: switchForm.reason,
      items: [
        {
          itemId: switchForm.itemId,
          qty: switchForm.qty
        }
      ]
    });
    await loadSwitching();
    toast.success('Pengajuan switching antar-cabang berhasil dikirim.');
    showModal.value = false;
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal mengirim pengajuan switching.');
  }
};

const filteredSwitchingList = computed(() => {
  return switchingList.value.filter(s => {
    if (activeTab.value === 'waiting' && !s.status.includes('WAITING')) return false;

    if (filterFrom.value !== 'ALL' && s.fromBranch !== filterFrom.value) return false;
    if (filterTo.value !== 'ALL' && s.toBranch !== filterTo.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNo = s.switchNo.toLowerCase().includes(q);
      const matchItem = s.itemName.toLowerCase().includes(q);
      const matchFrom = s.fromBranch.toLowerCase().includes(q);
      const matchTo = s.toBranch.toLowerCase().includes(q);
      if (!matchNo && !matchItem && !matchFrom && !matchTo) return false;
    }

    return true;
  });
});

const paginatedSwitchingList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredSwitchingList.value.slice(start, start + perPage.value);
});

onMounted(loadPage);
</script>

<style scoped>
</style>

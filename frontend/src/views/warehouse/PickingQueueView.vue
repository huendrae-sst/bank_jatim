<template>
  <div class="picking-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Antrean Picking Gudang (Margomulyo)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Pergudangan</li>
              <li class="breadcrumb-item active" aria-current="page">Antrean Picking</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- Picking Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'queue' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'queue'"
            >
              Antrean Picking
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'history' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'history'"
            >
              Riwayat Antrean
            </button>
          </li>
        </ul>

        <div v-if="activeTab === 'queue'" class="d-flex align-items-center gap-2">
          <button
            type="button"
            class="btn btn-sm btn-danger fw-bold shadow-xs"
            :disabled="selectedOrderIds.length === 0 || isBatchProcessing"
            @click="processBatchPicking"
          >
            Proses Batch Picking ({{ selectedOrderIds.length }} Terpilih)
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-tag"></i>
              </span>
              <select v-model="filterOrderType" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Jalur Order</option>
                <option value="INTERNAL_REQUEST">Order Permintaan</option>
                <option value="PURCHASE_REQUEST">Order Pembelian (PR)</option>
                <option value="EMBOSS_ORDER">Order Cetak / Emboss</option>
                <option value="ROUTINE_PUSH">Distribusi Rutin</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Cabang Peminta</option>
                <option v-for="branch in branchOptions" :key="branch" :value="branch">
                  {{ branch }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-flag"></i>
              </span>
              <select v-model="filterPriority" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Prioritas</option>
                <option value="URGENT">URGENT</option>
                <option value="NORMAL">NORMAL</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBranch !== 'ALL' || filterPriority !== 'ALL' || filterOrderType !== 'ALL'">
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
                placeholder="Cari No Batch, Order, Rak, atau Deskripsi Item..."
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
              <th v-if="activeTab === 'queue'" class="ps-3 text-center" style="width: 40px;">
                <input
                  type="checkbox"
                  class="form-check-input"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  title="Pilih Semua"
                />
              </th>
              <th :class="activeTab === 'queue' ? '' : 'ps-3'">No. Batch Picking</th>
              <th>Nomor Order &amp; Tipe</th>
              <th>Cabang Peminta</th>
              <th>Lokasi Rak Gudang</th>
              <th>Item &amp; Kuantitas</th>
              <th class="text-center">Prioritas</th>
              <th class="text-center">Status</th>
              <th class="text-center pe-3" style="width: 140px;">Aksi Petugas</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="item in paginatedQueue" :key="item.id">
              <td v-if="activeTab === 'queue'" class="ps-3 text-center">
                <input
                  type="checkbox"
                  class="form-check-input"
                  :value="item.id"
                  v-model="selectedOrderIds"
                />
              </td>
              <td :class="activeTab === 'queue' ? '' : 'ps-3'" class="fw-bold font-monospace text-danger">{{ item.batchNo }}</td>
              <td>
                <div class="fw-bold font-monospace">{{ item.orderNumber }}</div>
                <span class="badge fs-9 text-uppercase" :class="getOrderTypeBadgeClass(item.orderType)">
                  {{ getOrderTypeLabel(item.orderType) }}
                </span>
              </td>
              <td>
                <span class="fw-semibold text-body">{{ item.branch }}</span>
              </td>
              <td><span class="badge text-bg-light border fs-9 font-monospace">{{ item.binLocation }}</span></td>
              <td class="fs-8 text-body">{{ item.itemDescription }}</td>
              <td class="text-center">
                <span :class="['badge fs-9', item.priority === 'URGENT' ? 'text-bg-danger' : 'text-bg-secondary']">
                  {{ item.priority }}
                </span>
              </td>
              <td class="text-center">
                <span class="badge text-bg-warning fs-9">{{ item.status }}</span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-danger" @click="processPicking(item)" title="Ambil / Picking">
                  <i class="bi bi-check2-square"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredQueue.length === 0">
              <td :colspan="activeTab === 'queue' ? 9 : 8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada antrean picking yang sesuai dengan filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredQueue.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Scan Rak / Barcode -->
    <div v-if="scanBarcodeModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body mb-0">
              Scan Barcode Rak / Bin Location
            </h6>
            <button type="button" class="btn-close" @click="scanBarcodeModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitBarcodeScan">
            <div class="modal-body p-4 fs-8 space-y-3">
              <div class="text-center p-3 bg-body-tertiary rounded-3 border mb-3">
                <i class="bi bi-qr-code-scan display-4 text-danger d-block mb-2"></i>
                <div class="fw-bold fs-7">Arahkan Scanner Barcode ke Label Rak</div>
                <div class="text-secondary fs-9">Atau ketik manual kode lokasi rak di bawah:</div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Barcode Rak / Bin Location <span class="text-danger">*</span></label>
                <div class="input-group input-group-sm">
                  <input
                    type="text"
                    v-model="scannedBarcode"
                    class="form-control form-control-sm font-monospace text-uppercase"
                    placeholder="Contoh: RAK-A2-LVL1"
                    autofocus
                    required
                  />
                  <button type="submit" class="btn btn-danger fw-bold">Verifikasi</button>
                </div>
              </div>
              <div v-if="scanResult" class="alert py-2 px-3 fs-8 mt-2" :class="scanResult.success ? 'alert-success' : 'alert-danger'">
                <i :class="scanResult.success ? 'bi bi-check-circle me-1' : 'bi bi-x-circle me-1'"></i>
                {{ scanResult.message }}
              </div>
            </div>
            <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="scanBarcodeModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs">
                Verifikasi Lokasi Rak
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
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';
import { toast } from '@/utils/toast';

const scanBarcodeModal = ref(false);
const scannedBarcode = ref('');
const scanResult = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('queue');
const filterBranch = ref('ALL');
const filterPriority = ref('ALL');
const filterOrderType = ref('ALL');

const selectedOrderIds = ref([]);
const isBatchProcessing = ref(false);

const resetFilters = () => {
  searchQuery.value = '';
  filterBranch.value = 'ALL';
  filterPriority.value = 'ALL';
  filterOrderType.value = 'ALL';
  selectedOrderIds.value = [];
  currentPage.value = 1;
};

const describeItems = (items = []) => items
  .map(line => {
    const name = line.item?.name || '-';
    const qty = Number(line.qtyApproved || line.qtyRequested || 0).toLocaleString('id-ID');
    const uom = line.item?.uom || 'Unit';
    return `${name} (${qty} ${uom})`;
  })
  .join(', ');

const mapQueue = (item) => ({
  id: item.orderId || item.id,
  batchNo: item.batchNo || `PCK-${item.orderNumber || item.orderId || item.id}`,
  orderNumber: item.orderNumber || '-',
  orderType: item.orderType || 'INTERNAL_REQUEST',
  branch: item.branch || item.requestingOrganization?.name || '-',
  binLocation: item.binLocation || item.warehouse || 'RAK-A1-02',
  itemDescription: item.itemDescription || describeItems(item.items),
  priority: item.priority || 'NORMAL',
  status: item.status || 'MENUNGGU PICKING'
});

const pendingQueue = ref([]);

const loadPickingQueue = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/warehouse/picking', {
      params: {
        type: filterOrderType.value !== 'ALL' ? filterOrderType.value : undefined
      }
    });
    const list = response.data?.data ?? response.data ?? [];
    if (Array.isArray(list)) {
      pendingQueue.value = list.map(mapQueue);
    }
  } catch (error) {
    pendingQueue.value = [];
    console.warn('Failed loading picking queue from backend:', error);
  }
};

const urgentCount = computed(() => {
  return pendingQueue.value.filter(p => p.priority === 'URGENT').length;
});

const branchOptions = computed(() => [...new Set(pendingQueue.value.map(item => item.branch).filter(Boolean))]);

const submitBarcodeScan = () => {
  const code = scannedBarcode.value.trim().toUpperCase();
  const match = pendingQueue.value.find(p => p.binLocation.toUpperCase() === code);
  if (match) {
    scanResult.value = {
      success: true,
      message: `Lokasi terverifikasi! Batch ${match.batchNo} (${match.itemDescription}) siap diambil.`
    };
  } else {
    scanResult.value = {
      success: false,
      message: `Kode rak "${code}" tidak ditemukan pada antrean order saat ini.`
    };
  }
};

const filteredQueue = computed(() => {
  return pendingQueue.value.filter(p => {
    const isCompleted = p.status === 'SELESAI' || p.status === 'COMPLETED' || p.status === 'PICKED';
    if (activeTab.value === 'queue' && isCompleted) return false;
    if (activeTab.value === 'history' && !isCompleted) return false;

    if (filterOrderType.value !== 'ALL' && p.orderType !== filterOrderType.value) return false;
    if (filterBranch.value !== 'ALL' && p.branch !== filterBranch.value) return false;
    if (filterPriority.value !== 'ALL' && p.priority !== filterPriority.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchBatch = p.batchNo.toLowerCase().includes(q);
      const matchOrder = p.orderNumber.toLowerCase().includes(q);
      const matchBranch = p.branch.toLowerCase().includes(q);
      const matchBin = p.binLocation.toLowerCase().includes(q);
      const matchDesc = p.itemDescription.toLowerCase().includes(q);
      if (!matchBatch && !matchOrder && !matchBranch && !matchBin && !matchDesc) return false;
    }

    return true;
  });
});

const isAllSelected = computed(() => {
  const currentIds = paginatedQueue.value.map(item => item.id);
  return currentIds.length > 0 && currentIds.every(id => selectedOrderIds.value.includes(id));
});

const toggleSelectAll = () => {
  const currentIds = paginatedQueue.value.map(item => item.id);
  if (isAllSelected.value) {
    selectedOrderIds.value = selectedOrderIds.value.filter(id => !currentIds.includes(id));
  } else {
    const newSelected = new Set([...selectedOrderIds.value, ...currentIds]);
    selectedOrderIds.value = Array.from(newSelected);
  }
};

const paginatedQueue = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredQueue.value.slice(start, start + perPage.value);
});

const processPicking = async (item) => {
  if (confirm(`Konfirmasi pengambilan fisik untuk ${item.batchNo}?`)) {
    try {
      await api.post(`/warehouse/picking/${item.id}/process`);
      await loadPickingQueue();
      toast.success(`Batch ${item.batchNo} sukses diambil dan dialihkan ke Antrean Packing.`);
    } catch (error) {
      toast.error(error?.message || error?.error || 'Gagal memproses picking ke backend.');
    }
  }
};

const processBatchPicking = async () => {
  if (selectedOrderIds.value.length === 0) return;
  if (!confirm(`Konfirmasi batch picking untuk ${selectedOrderIds.value.length} order sekaligus?`)) return;

  isBatchProcessing.value = true;
  try {
    const res = await api.post('/warehouse/picking/batch-process', {
      orderIds: selectedOrderIds.value
    });
    toast.success(res.data?.message || `${selectedOrderIds.value.length} order berhasil di-picking dan diteruskan ke packing!`);
    selectedOrderIds.value = [];
    await loadPickingQueue();
  } catch (error) {
    toast.error('Gagal memproses batch picking: ' + (error?.response?.data?.message || error.message));
  } finally {
    isBatchProcessing.value = false;
  }
};

const getOrderTypeLabel = (type) => {
  switch (type) {
    case 'INTERNAL_REQUEST': return 'Permintaan';
    case 'PURCHASE_REQUEST': return 'Pembelian (PR)';
    case 'EMBOSS_ORDER': return 'Cetak/Emboss';
    case 'ROUTINE_PUSH': return 'Distribusi Rutin';
    default: return type || 'Permintaan';
  }
};

const getOrderTypeBadgeClass = (type) => {
  switch (type) {
    case 'INTERNAL_REQUEST': return 'bg-danger-subtle text-danger border border-danger-subtle';
    case 'PURCHASE_REQUEST': return 'bg-info-subtle text-info-emphasis border border-info-subtle';
    case 'EMBOSS_ORDER': return 'bg-warning-subtle text-dark border border-warning-subtle';
    case 'ROUTINE_PUSH': return 'bg-success-subtle text-success-emphasis border border-success-subtle';
    default: return 'bg-secondary-subtle text-secondary';
  }
};

onMounted(loadPickingQueue);
</script>

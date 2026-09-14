<template>
  <div class="production-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Bon Permintaan Produksi Warkat &amp; Cetakan</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Percetakan</li>
              <li class="breadcrumb-item active" aria-current="page">SPK Produksi</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- Main Table Card -->
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
              Semua SPK
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'process' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'process'"
            >
              Proses Cetak
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'completed'"
            >
              Selesai QC
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Buat Bon Produksi
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-file-earmark-text"></i>
              </span>
              <select v-model="filterType" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Jenis Warkat</option>
                <option value="Bilyet Giro">Bilyet Giro</option>
                <option value="Bilyet Deposito">Bilyet Deposito</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-funnel"></i>
              </span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Status</option>
                <option value="PROSES CETAK">PROSES CETAK</option>
                <option value="PERSIAPAN BLANKO">PERSIAPAN BLANKO</option>
                <option value="SELESAI">SELESAI</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterType !== 'ALL' || filterStatus !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
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
                placeholder="Cari No. SPK, Jenis Cetakan, No Seri..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. SPK Produksi</th>
              <th>Jenis Cetakan Warkat</th>
              <th>Rentang Nomor Seri (Security Printing)</th>
              <th class="text-end">Jumlah Eksemplar</th>
              <th>Target Selesai</th>
              <th class="text-center">Status Alur</th>
              <th class="text-center pe-3" style="width: 120px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="p in paginatedProductionList" :key="p.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                <router-link :to="'/production/' + p.id" class="text-decoration-none text-danger">
                  {{ p.spkNo }}
                </router-link>
              </td>
              <td class="fw-semibold text-body">{{ p.productType }}</td>
              <td class="font-monospace text-secondary fs-8">{{ p.serialRange }}</td>
              <td class="text-end font-monospace fw-bold text-body">{{ p.qty.toLocaleString('id-ID') }}</td>
              <td class="fs-8 text-secondary">{{ p.targetDate }}</td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(p.status)">{{ p.status }}</span>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <router-link :to="'/production/' + p.id" class="btn-action-icon text-secondary" title="Detail">
                    <i class="bi bi-eye"></i>
                  </router-link>
                  <router-link :to="'/production/' + p.id + '/manifest'" target="_blank" class="btn-action-icon text-dark" title="Cetak Manifest Produksi">
                    <i class="bi bi-printer"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredProductionList.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data SPK produksi yang sesuai kriteria pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredProductionList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Buat Bon Produksi -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Penerbitan Surat Perintah Kerja (SPK) Baru
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="saveProduction">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor SPK Produksi <span class="text-danger">*</span></label>
                  <input type="text" v-model="prodForm.spkNo" class="form-control form-control-sm font-monospace fs-8" placeholder="SPK-PROD-2026-XXXX" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jenis Cetakan Warkat <span class="text-danger">*</span></label>
                  <input type="text" v-model="prodForm.productType" class="form-control form-control-sm fs-8" placeholder="Contoh: Bilyet Giro 50 Lembar" required />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Rentang Nomor Seri (Security Printing) <span class="text-danger">*</span></label>
                  <input type="text" v-model="prodForm.serialRange" class="form-control form-control-sm font-monospace fs-8" placeholder="Contoh: BG-JATIM-0985001 s/d BG-JATIM-0990000" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Eksemplar / Cetak <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="prodForm.qty" class="form-control form-control-sm font-monospace fs-8" min="1" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Target Selesai Cetak <span class="text-danger">*</span></label>
                  <input type="text" v-model="prodForm.targetDate" class="form-control form-control-sm fs-8" placeholder="Contoh: 25 Sep 2026" required />
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                <i class="bi bi-check2-circle me-1"></i> Terbitkan SPK
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
import PaginationFooter from '@/components/PaginationFooter.vue';

const showModal = ref(false);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterType = ref('ALL');
const filterStatus = ref('ALL');
const isLoading = ref(false);

const resetFilters = () => {
  searchQuery.value = '';
  filterType.value = 'ALL';
  filterStatus.value = 'ALL';
  currentPage.value = 1;
};

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch {
    return val;
  }
};

const mapProduction = (p, idx) => ({
  id: p.id,
  spkNo: p.productionNumber || p.spkNo || `SPK-PROD-2026-${String(p.id || idx + 1).padStart(4, '0')}`,
  productType: p.itemName ? `Pencetakan ${p.itemName}` : (p.productType || `Personalisasi Kartu (${p.filename || 'Berkas Emboss'})`),
  serialRange: p.serialRange || `BATCH-${p.id} (${p.qtyToProduce || p.qty || 1500} unit)`,
  qty: p.qtyToProduce || p.qtyFinished || p.qty || 1500,
  targetDate: formatDate(p.startDate || p.createdAt || '2026-03-25'),
  status: p.status === 'COMPLETED' || p.status === 'READY_TO_SHIP' ? 'SELESAI' : 'PROSES CETAK'
});

const productionList = ref([]);

const fetchProductionList = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/emboss');
    const files = res.data?.data || res.data?.content || res.data || [];
    if (Array.isArray(files) && files.length > 0) {
      productionList.value = files.map((f, idx) => ({
        id: f.id,
        spkNo: `SPK-PROD-2026-${String(f.id || idx + 1).padStart(4, '0')}`,
        productType: `Personalisasi Kartu (${f.filename || 'Berkas Emboss'})`,
        serialRange: `BATCH-EMB-${f.id} (${f.validRecords || 0} valid / ${f.totalRecords || 0} total)`,
        qty: f.totalRecords || 0,
        targetDate: formatDate(f.createdAt),
        status: f.status === 'COMPLETED' ? 'SELESAI' : 'PROSES CETAK'
      }));
    }
  } catch (err) {
    productionList.value = [];
    console.warn('Failed to load production orders from backend:', err);
  } finally {
    isLoading.value = false;
  }
};

const inProcessCount = computed(() => {
  return productionList.value.filter(p => ['PROSES CETAK', 'PERSIAPAN BLANKO'].includes(p.status)).length;
});

const completedCount = computed(() => {
  return productionList.value.filter(p => p.status === 'SELESAI').length;
});

const prodForm = reactive({
  spkNo: '',
  productType: '',
  serialRange: '',
  qty: 100,
  targetDate: ''
});

const openCreateModal = () => {
  const nextNum = String(productionList.value.length + 1).padStart(4, '0');
  Object.assign(prodForm, {
    spkNo: `SPK-PROD-2026-${nextNum}`,
    productType: '',
    serialRange: '',
    qty: 100,
    targetDate: '28 Sep 2026'
  });
  showModal.value = true;
};

const saveProduction = async () => {
  productionList.value.unshift({
    id: Date.now(),
    spkNo: prodForm.spkNo,
    productType: prodForm.productType,
    serialRange: prodForm.serialRange,
    qty: prodForm.qty,
    targetDate: prodForm.targetDate,
    status: 'PROSES CETAK'
  });
  showModal.value = false;
  alert('SPK Produksi ' + prodForm.spkNo + ' berhasil diterbitkan.');
};

const filteredProductionList = computed(() => {
  return productionList.value.filter(p => {
    if (activeTab.value === 'process' && !['PROSES CETAK', 'PERSIAPAN BLANKO'].includes(p.status)) return false;
    if (activeTab.value === 'completed' && p.status !== 'SELESAI') return false;

    if (filterType.value !== 'ALL' && !p.productType.includes(filterType.value)) return false;
    if (filterStatus.value !== 'ALL' && p.status !== filterStatus.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchSpk = p.spkNo.toLowerCase().includes(q);
      const matchType = p.productType.toLowerCase().includes(q);
      const matchSerial = p.serialRange.toLowerCase().includes(q);
      if (!matchSpk && !matchType && !matchSerial) return false;
    }

    return true;
  });
});

const paginatedProductionList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredProductionList.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  switch (status) {
    case 'SELESAI': return 'text-bg-success';
    case 'PROSES CETAK': return 'text-bg-warning';
    case 'PERSIAPAN BLANKO': return 'text-bg-info';
    default: return 'text-bg-secondary';
  }
};

onMounted(() => {
  fetchProductionList();
});
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

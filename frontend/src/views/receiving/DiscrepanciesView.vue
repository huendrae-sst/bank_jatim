<template>
  <div class="discrepancies-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Berita Acara Selisih &amp; Kerusakan Barang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Penerimaan</li>
              <li class="breadcrumb-item active" aria-current="page">Selisih & Kerusakan</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

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
              Semua Kasus
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'investigation' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'investigation'"
            >
              Dalam Investigasi
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'resolved' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'resolved'"
            >
              Selesai
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Buat BA Selisih
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Unit Pelapor</option>
                <option v-for="branch in branchOptions" :key="branch" :value="branch">
                  {{ branch }}
                </option>
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
                <option value="INVESTIGASI LOGISTIK">INVESTIGASI LOGISTIK</option>
                <option value="SELESAI / KLAIM">SELESAI / KLAIM</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBranch !== 'ALL' || filterStatus !== 'ALL'">
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
                placeholder="Cari No. BA, Surat Jalan, Cabang, Barang..."
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
              <th class="ps-3">No. Berita Acara</th>
              <th>Surat Jalan Asal</th>
              <th>Unit Pelapor</th>
              <th>Nama Barang</th>
              <th class="text-end">Fisik Tercatat</th>
              <th class="text-end">Fisik Aktual</th>
              <th class="text-end">Selisih</th>
              <th>Penyebab / Keterangan</th>
              <th class="text-center">Status Investigasi</th>
              <th class="text-center pe-3" style="width: 110px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="r in paginatedRecords" :key="r.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                <a href="javascript:void(0)" class="text-decoration-none text-danger" @click="openDetailModal(r)">
                  {{ r.bapNo }}
                </a>
              </td>
              <td class="font-monospace text-body">{{ r.shipmentNo }}</td>
              <td class="fw-semibold text-body">{{ r.branch }}</td>
              <td class="fs-8 text-body">{{ r.item }}</td>
              <td class="text-end font-monospace text-body">{{ r.qtyExpected.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-primary fw-bold">{{ r.qtyActual.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-danger fw-bold">{{ r.discrepancy }}</td>
              <td class="fs-8 text-secondary">{{ r.reason }}</td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(r.status)">{{ r.status }}</span>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="btn-action-icon text-secondary" @click="openDetailModal(r)" title="Detail"><i class="bi bi-eye"></i></button>
                  <router-link :to="'/receiving/discrepancies/' + r.id + '/print'" target="_blank" class="btn-action-icon text-dark" title="Cetak Berita Acara"><i class="bi bi-printer"></i></router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRecords.length === 0">
              <td colspan="10" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data berita acara selisih yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredRecords.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Buat BA Selisih) -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showCreateModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Form Berita Acara Selisih Baru
            </h6>
            <button type="button" class="btn-close-modal" @click="showCreateModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="saveDiscrepancy">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Surat Jalan Asal <span class="text-danger">*</span></label>
                  <input type="text" v-model="form.shipmentNo" class="form-control form-control-sm fs-8 font-monospace" placeholder="Contoh: SJ-BJ-2026-0085" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Unit Kerja Pelapor <span class="text-danger">*</span></label>
                  <input type="text" v-model="form.branch" class="form-control form-control-sm fs-8" placeholder="Contoh: KC Kediri" required />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Barang Persediaan <span class="text-danger">*</span></label>
                  <input type="text" v-model="form.item" class="form-control form-control-sm fs-8" placeholder="Contoh: Blanko Kartu ATM GPN" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Tercatat Surat Jalan <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="form.qtyExpected" class="form-control form-control-sm fs-8 font-monospace" min="1" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Fisik Aktual Diterima <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="form.qtyActual" class="form-control form-control-sm fs-8 font-monospace" min="0" required />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Penyebab / Keterangan Selisih <span class="text-danger">*</span></label>
                  <textarea v-model="form.reason" class="form-control form-control-sm fs-8" rows="2" placeholder="Uraikan kronologi kerusakan fisik atau selisih kuantitas saat unboxing..." required></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showCreateModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                <i class="bi bi-check2-circle me-1"></i> Terbitkan Berita Acara
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Detail BA Selisih -->
    <div v-if="showDetailModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showDetailModal = false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Rincian Berita Acara Selisih
            </h6>
            <button type="button" class="btn-close-modal" @click="showDetailModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body p-3 fs-8" v-if="selectedRecord">
            <div class="mb-3">
              <div class="text-secondary fs-9 fw-bold text-uppercase">Nomor BA Selisih:</div>
              <div class="font-monospace fw-bold fs-6 text-danger">{{ selectedRecord.bapNo }}</div>
            </div>
            <div class="row g-2 mb-3">
              <div class="col-6">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Surat Jalan:</div>
                <div class="font-monospace fw-semibold text-body">{{ selectedRecord.shipmentNo }}</div>
              </div>
              <div class="col-6">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Unit Pelapor:</div>
                <div class="fw-semibold text-body">{{ selectedRecord.branch }}</div>
              </div>
            </div>
            <div class="mb-3">
              <div class="text-secondary fs-9 fw-bold text-uppercase">Barang Persediaan:</div>
              <div class="fw-bold text-body">{{ selectedRecord.item }}</div>
            </div>
            <div class="row g-2 py-3 border-top border-bottom my-3 bg-body-tertiary rounded-2">
              <div class="col-4 text-center">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Tercatat</div>
                <div class="font-monospace fw-bold fs-6 text-body">{{ selectedRecord.qtyExpected }}</div>
              </div>
              <div class="col-4 text-center">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Aktual</div>
                <div class="font-monospace fw-bold fs-6 text-primary">{{ selectedRecord.qtyActual }}</div>
              </div>
              <div class="col-4 text-center">
                <div class="text-secondary fs-9 fw-bold text-uppercase">Selisih</div>
                <div class="font-monospace fw-bold fs-6 text-danger">{{ selectedRecord.discrepancy }}</div>
              </div>
            </div>
            <div>
              <div class="text-secondary fs-9 fw-bold text-uppercase">Keterangan Investigasi:</div>
              <div class="text-body mt-1">{{ selectedRecord.reason }}</div>
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

const showCreateModal = ref(false);
const showDetailModal = ref(false);
const selectedRecord = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterBranch = ref('ALL');
const filterStatus = ref('ALL');
const errorMessage = ref('');

const resetFilters = () => {
  searchQuery.value = '';
  filterBranch.value = 'ALL';
  filterStatus.value = 'ALL';
  currentPage.value = 1;
};

const mapStatus = (status) => {
  if (status === 'RESOLVED' || status === 'CLAIMED' || status === 'SELESAI') return 'SELESAI / KLAIM';
  return 'INVESTIGASI LOGISTIK';
};

const mapDiscrepancy = (record) => ({
  id: record.id,
  bapNo: record.beritaAcaraNumber || record.bapNumber || `BAP-${record.id}`,
  shipmentNo: record.receivingNumber || record.shipmentNumber || record.shipmentNo || '-',
  branch: record.branchName || record.branch || record.orderNumber || '-',
  item: record.item?.name || record.itemName || record.item || '-',
  qtyExpected: Number(record.qtyExpected || 0),
  qtyActual: Number(record.qtyActual || 0),
  discrepancy: Number(record.qtyDiscrepancy || (Number(record.qtyActual || 0) - Number(record.qtyExpected || 0))),
  reason: record.resolutionNotes || record.notes || record.reason || record.discrepancyType || '-',
  status: mapStatus(record.resolutionStatus || record.status)
});

const records = ref([]);

const loadDiscrepancies = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/receiving/discrepancies');
    const data = response.data?.content || response.data || [];
    if (Array.isArray(data) && data.length > 0) {
      records.value = data.map(mapDiscrepancy);
    }
  } catch (error) {
    records.value = [];
    console.warn('Failed loading discrepancies from backend:', error);
  }
};

const inInvestigationCount = computed(() => {
  return records.value.filter(r => r.status.includes('INVESTIGASI')).length;
});

const resolvedCount = computed(() => {
  return records.value.filter(r => r.status.includes('SELESAI')).length;
});

const branchOptions = computed(() => [...new Set(records.value.map(r => r.branch).filter(Boolean))]);

const form = reactive({
  shipmentNo: '',
  branch: '',
  item: '',
  qtyExpected: 100,
  qtyActual: 95,
  reason: ''
});

const openCreateModal = () => {
  Object.assign(form, {
    shipmentNo: '',
    branch: '',
    item: '',
    qtyExpected: 100,
    qtyActual: 95,
    reason: ''
  });
  showCreateModal.value = true;
};

const openDetailModal = (r) => {
  selectedRecord.value = r;
  showDetailModal.value = true;
};

const saveDiscrepancy = () => {
  errorMessage.value = 'Pembuatan BA selisih manual belum memiliki endpoint backend persistent. Gunakan alur konfirmasi penerimaan dengan diskrepansi agar tercatat di database.';
  showCreateModal.value = false;
};

const filteredRecords = computed(() => {
  return records.value.filter(r => {
    if (activeTab.value === 'investigation' && !r.status.includes('INVESTIGASI')) return false;
    if (activeTab.value === 'resolved' && !r.status.includes('SELESAI')) return false;

    if (filterBranch.value !== 'ALL' && r.branch !== filterBranch.value) return false;
    if (filterStatus.value !== 'ALL' && r.status !== filterStatus.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNo = r.bapNo.toLowerCase().includes(q);
      const matchSj = r.shipmentNo.toLowerCase().includes(q);
      const matchBranch = r.branch.toLowerCase().includes(q);
      const matchItem = r.item.toLowerCase().includes(q);
      if (!matchNo && !matchSj && !matchBranch && !matchItem) return false;
    }

    return true;
  });
});

const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredRecords.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  if (status.includes('SELESAI')) return 'text-bg-success';
  if (status.includes('INVESTIGASI')) return 'text-bg-warning';
  return 'text-bg-secondary';
};

onMounted(loadDiscrepancies);
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

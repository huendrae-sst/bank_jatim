<template>
  <div class="purchase-requests-page">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Purchase Requests (PR)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/procurement/pr" class="text-decoration-none text-danger">Pengadaan</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Purchase Requests</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <div v-if="errorMessage" class="alert alert-danger fs-8">
      {{ errorMessage }}
    </div>

    <!-- 3. Main PR Data Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Section Tombol Tambah -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua PR
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'waiting' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'waiting'"
            >
              Menunggu Review
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'approved' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'approved'"
            >
              Approved
            </button>
          </li>
        </ul>

        <!-- Section Tombol Tambah & Aksi -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/procurement/consolidation" class="btn btn-sm btn-outline-secondary shadow-xs">
            Konsolidasi PR
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateModal">
            Tambah
          </button>
        </div>
      </div>

      <!-- 4. Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterOrg" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Unit Kerja</option>
                <option value="Divisi Umum & Logistik Pusat">Divisi Umum & Logistik Pusat</option>
                <option value="KC Surabaya Utama">KC Surabaya Utama</option>
                <option value="KC Malang">KC Malang</option>
                <option value="KC Sidoarjo">KC Sidoarjo</option>
                <option value="KC Jember">KC Jember</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-gear"></i></span>
              <select v-model="filterMethod" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Metode</option>
                <option value="DIRECT">Pengadaan Langsung</option>
                <option value="E_CATALOG">E-Katalog</option>
                <option value="TENDER">Tender / Lelang</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterOrg !== 'ALL' || filterMethod !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              Reset
            </button>
          </div>
          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Nomor PR / Keperluan..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- PR Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Nomor PR</th>
              <th>Unit Pemohon</th>
              <th>Metode</th>
              <th>Tujuan Pengadaan</th>
              <th class="text-end">Estimasi Biaya</th>
              <th class="text-center">Validasi Anggaran</th>
              <th class="text-center">Status</th>
              <th class="text-center" style="width: 140px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="pr in paginatedPrList" :key="pr.id">
              <td>
                <router-link :to="`/procurement/pr/${pr.id}`" class="font-monospace fw-bold text-danger text-decoration-none">
                  {{ pr.prNumber }}
                </router-link>
                <div class="fs-9 text-secondary">{{ pr.date }}</div>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ pr.organization }}</div>
                <div class="fs-9 text-secondary font-monospace">{{ pr.costCenter }}</div>
              </td>
              <td>
                <span class="badge text-bg-light border fs-9">{{ pr.method }}</span>
              </td>
              <td>
                <div class="text-truncate text-body" style="max-width: 260px;" :title="pr.purpose">
                  {{ pr.purpose }}
                </div>
              </td>
              <td class="text-end font-monospace fw-bold text-body">
                Rp {{ pr.estimatedCost.toLocaleString('id-ID') }}
              </td>
              <td class="text-center">
                <span class="badge text-bg-success fs-9">
                  Tersedia
                </span>
              </td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(pr.status)">
                  {{ pr.status ? pr.status.replace('_', ' ') : 'SUBMITTED' }}
                </span>
                <div v-if="pr.fulfillmentStatus" class="mt-1">
                  <span class="badge fs-9" :class="fulfillmentBadgeClass(pr.fulfillmentStatus)">
                    {{ fulfillmentLabel(pr.fulfillmentStatus) }}
                  </span>
                </div>
              </td>
              <td class="text-center">
                <div class="d-inline-flex align-items-center gap-1">
                  <router-link
                    :to="`/procurement/pr/${pr.id}`"
                    class="btn-action-icon text-secondary"
                    title="Lihat Detail PR"
                  >
                    <i class="bi bi-eye"></i>
                  </router-link>
                  <router-link
                    :to="`/procurement/pr/${pr.id}/print`"
                    target="_blank"
                    class="btn-action-icon text-secondary"
                    title="Cetak Dokumen PR Resmi"
                  >
                    <i class="bi bi-printer"></i>
                  </router-link>
                  <button
                    v-if="['SUBMITTED', 'WAITING_APPROVAL'].includes(pr.status)"
                    class="btn-action-icon text-success"
                    @click="approvePr(pr.id)"
                    title="Persetujuan Otorisator"
                  >
                    <i class="bi bi-check-circle-fill"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPrList.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data Purchase Request yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPrList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Create PR Modal -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold text-body">
              Form Pengajuan Purchase Request (PR) Baru
            </h5>
            <button type="button" class="btn-close-modal" @click="showCreateModal = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="submitCreatePr">
            <div class="modal-body p-4 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Unit Kerja Pemohon <span class="text-danger">*</span></label>
                  <select v-model="newPr.organization" class="form-select fs-7" required>
                    <option value="Divisi Umum & Logistik Pusat">Divisi Umum & Logistik Pusat</option>
                    <option value="KC Surabaya Utama">KC Surabaya Utama</option>
                    <option value="KC Malang">KC Malang</option>
                    <option value="KC Sidoarjo">KC Sidoarjo</option>
                    <option value="KC Jember">KC Jember</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Metode Pengadaan <span class="text-danger">*</span></label>
                  <select v-model="newPr.method" class="form-select fs-7" required>
                    <option value="DIRECT">Pengadaan Langsung (Direct Purchase)</option>
                    <option value="E_CATALOG">E-Katalog Bank Jatim</option>
                    <option value="TENDER">Tender / Lelang Terbuka</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tujuan / Keperluan Pengadaan <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="newPr.purpose"
                    class="form-control fs-7"
                    placeholder="Contoh: Pengadaan Blanko Kartu ATM GPN Chip Batch Q1 2026"
                    required
                  />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Estimasi Total Biaya (Rp) <span class="text-danger">*</span></label>
                  <input
                    type="number"
                    v-model.number="newPr.estimatedCost"
                    class="form-control fs-7 font-monospace"
                    placeholder="0"
                    min="1000"
                    required
                  />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Target Penyelesaian Pengadaan</label>
                  <input type="date" v-model="newPr.targetDate" class="form-control fs-7" />
                </div>
              </div>
            </div>
            <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="showCreateModal = false">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
                Simpan & Kirim Pengajuan
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
import { extractList } from '@/utils/responseParser';

const activeTab = ref('all');
const searchQuery = ref('');
const filterOrg = ref('ALL');
const filterMethod = ref('ALL');
const showCreateModal = ref(false);
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');

const resetFilters = () => {
  searchQuery.value = '';
  filterOrg.value = 'ALL';
  filterMethod.value = 'ALL';
  currentPage.value = 1;
};

const mapPr = (pr) => ({
  id: pr.id,
  prNumber: pr.prNumber,
  date: pr.createdAt ? new Date(pr.createdAt).toLocaleDateString('id-ID', { dateStyle: 'medium' }) : '-',
  organization: pr.organization?.name || pr.organizationName || 'Kantor Pusat',
  costCenter: pr.organization?.code || pr.costCenter || '-',
  method: pr.procurementMethod || pr.method || 'DIRECT',
  purpose: pr.purpose || '-',
  estimatedCost: Number(pr.estimatedTotalCost || pr.estimatedCost || 0),
  budgetStatus: pr.budgetStatus || 'VALIDATED',
  status: pr.status || 'DRAFT',
  fulfillmentStatus: pr.fulfillmentStatus || 'UNFULFILLED'
});

const prList = ref([]);

const newPr = ref({
  organization: 'Divisi Umum & Logistik Pusat',
  method: 'DIRECT',
  purpose: '',
  estimatedCost: null,
  targetDate: ''
});

const waitingApprovalCount = computed(() => {
  return prList.value.filter(p => ['SUBMITTED', 'WAITING_APPROVAL'].includes(p.status)).length;
});

const approvedCount = computed(() => {
  return prList.value.filter(p => p.status === 'APPROVED').length;
});

const poIssuedCount = computed(() => {
  return prList.value.filter(p => p.status === 'PO_ISSUED').length;
});

const filteredPrList = computed(() => {
  return prList.value.filter(pr => {
    // Tab filtering
    if (activeTab.value === 'waiting' && !['SUBMITTED', 'WAITING_APPROVAL'].includes(pr.status)) return false;
    if (activeTab.value === 'approved' && pr.status !== 'APPROVED') return false;
    if (activeTab.value === 'po_issued' && pr.status !== 'PO_ISSUED') return false;

    // Unit Kerja Filter
    if (filterOrg.value !== 'ALL' && pr.organization !== filterOrg.value) return false;

    // Method Filter
    if (filterMethod.value !== 'ALL' && pr.method !== filterMethod.value) return false;

    // Search Query
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNum = pr.prNumber?.toLowerCase().includes(q);
      const matchPurpose = pr.purpose?.toLowerCase().includes(q);
      if (!matchNum && !matchPurpose) return false;
    }

    return true;
  });
});

const totalRows = computed(() => filteredPrList.value.length);
const totalPages = computed(() => Math.ceil(totalRows.value / perPage.value) || 1);

const paginatedPrList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPrList.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  switch (status) {
    case 'APPROVED': return 'text-bg-success';
    case 'WAITING_APPROVAL':
    case 'SUBMITTED': return 'text-bg-warning';
    case 'REJECTED': return 'text-bg-danger';
    case 'PO_ISSUED': return 'text-bg-primary';
    default: return 'text-bg-secondary';
  }
};

const fulfillmentLabel = (status) => {
  switch (status) {
    case 'FULFILLED': return 'Dipenuhi';
    case 'PARTIALLY_FULFILLED': return 'Sebagian';
    case 'UNFULFILLED': return 'Belum Dipenuhi';
    default: return status || '-';
  }
};

const fulfillmentBadgeClass = (status) => {
  switch (status) {
    case 'FULFILLED': return 'bg-success text-white';
    case 'PARTIALLY_FULFILLED': return 'bg-warning text-dark';
    case 'UNFULFILLED': return 'bg-secondary text-white';
    default: return 'bg-light text-dark';
  }
};

onMounted(() => {
  loadPurchaseRequests();
});

const loadPurchaseRequests = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/procurement/pr', {
      params: { page: 0, size: 100, sort: 'createdAt,desc' }
    });
    const list = extractList(response);
    if (list !== null) {
      prList.value = list.map(mapPr);
    }
  } catch (error) {
    console.warn('Failed loading purchase requests from server:', error);
    prList.value = [];
  }
};

const approvePr = async (id) => {
  try {
    await api.post(`/procurement/pr/${id}/approve`);
    await loadPurchaseRequests();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menyetujui PR.';
  }
};

const openCreateModal = () => {
  newPr.value = {
    organization: 'Divisi Umum & Logistik Pusat',
    method: 'DIRECT',
    purpose: '',
    estimatedCost: null,
    targetDate: ''
  };
  showCreateModal.value = true;
};

const submitCreatePr = () => {
  errorMessage.value = 'Pembuatan PR belum memiliki endpoint backend persistent.';
};

onMounted(loadPurchaseRequests);
</script>

<template>
  <div class="card-personalization-page">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Personalisasi Kartu ATM (Emboss Operations)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Personalisasi Kartu</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Hidden File Input -->
    <input type="file" ref="fileInputRef" accept=".csv,.txt" class="d-none" @change="handleFileInputChange" />

    <div v-if="errorMessage" class="alert alert-danger fs-8 py-2 px-3 mb-3">{{ errorMessage }}</div>

    <!-- Main Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
          Riwayat Berkas Personalisasi Kartu Diproses
        </h3>
      </div>

      <!-- Filter & Search Toolbar (UI seperti orders/emboss) -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <!-- Status Filter -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8" @change="currentPage = 1">
                <option value="ALL">Semua Status</option>
                <option value="VALIDATED">VALIDATED</option>
                <option value="ORDERS_GENERATED">ORDER TERBIT</option>
                <option value="PROCESSING">PROCESSING</option>
                <option value="COMPLETED">COMPLETED</option>
              </select>
            </div>
          </div>

          <!-- Sort Field Filter -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-sort-down"></i></span>
              <select v-model="sortBy" class="form-select form-select-sm border-start-0 fs-8">
                <option value="id">ID Berkas (Terbaru)</option>
                <option value="filename">Nama Berkas</option>
                <option value="total">Total Kartu</option>
                <option value="valid">Kartu Valid</option>
                <option value="rejected">Kartu Reject</option>
                <option value="status">Status Pemrosesan</option>
              </select>
            </div>
          </div>

          <!-- Reset Button -->
          <div class="col-auto" v-if="isFiltered">
            <button type="button" @click="resetFilters" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
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
                placeholder="Cari Nama Berkas, Batch ID..."
                @input="currentPage = 1"
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button" @click="currentPage = 1">
                Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Nama File Berkas Core Banking</th>
              <th class="text-center">Total Nasabah</th>
              <th class="text-center">Valid (Stok Siap)</th>
              <th class="text-center">Reject Queue</th>
              <th class="text-center">Status Pemrosesan</th>
              <th class="text-center" style="width: 200px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <template v-if="!isLoading || paginatedFiles.length > 0">
              <tr v-for="file in paginatedFiles" :key="file.id">
                <td>
                  <span class="font-monospace fw-bold text-dark">
                    {{ file.filename }}
                  </span>
                  <div class="fs-9 text-secondary">Batch ID: #EB-202609-00{{ file.id }}</div>
                </td>
                <td class="text-center font-monospace fw-bold text-body">
                  {{ file.total }} Kartu
                </td>
                <td class="text-center font-monospace">
                  <span class="badge text-bg-success fs-9">{{ file.valid }} Kartu</span>
                </td>
                <td class="text-center font-monospace">
                  <span v-if="file.rejected > 0" class="badge text-bg-danger fs-9">{{ file.rejected }} Kartu</span>
                  <span v-else class="text-secondary fs-9">-</span>
                </td>
                <td class="text-center">
                  <span class="badge fs-9 text-uppercase" :class="badgeClass(file.status)">
                    {{ file.status }}
                  </span>
                </td>
                <td class="text-center">
                  <div class="d-inline-flex align-items-center gap-1.5">
                    <router-link
                      :to="`/emboss/${file.id}`"
                      class="btn btn-sm btn-outline-danger py-1 px-2.5 fs-8 fw-semibold d-inline-flex align-items-center gap-1 shadow-xs"
                      title="Buka Detail Berkas & Order"
                    >
                      <span>Buka Detail</span>
                    </router-link>
                    <button
                      v-if="file.status === 'VALIDATED'"
                      type="button"
                      class="btn btn-sm btn-danger py-1 px-2 fs-8 fw-bold d-inline-flex align-items-center gap-1 shadow-xs"
                      @click="generateOrders(file)"
                      title="Terbitkan Pesanan Cabang Otomatis"
                    >
                      <span>Order</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="!isLoading && filteredFiles.length === 0">
                <td colspan="6" class="text-center py-5 text-secondary">
                  <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                  Tidak ada riwayat berkas emboss yang sesuai.
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        v-if="!isLoading && filteredFiles.length > 0"
        :total="filteredFiles.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

const router = useRouter();
const searchQuery = ref('');
const filterStatus = ref('ALL');
const sortBy = ref('id');
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');

const isFiltered = computed(() => {
  return searchQuery.value.trim() !== '' || filterStatus.value !== 'ALL' || sortBy.value !== 'id';
});

const resetFilters = () => {
  searchQuery.value = '';
  filterStatus.value = 'ALL';
  sortBy.value = 'id';
  currentPage.value = 1;
};

const mapEmbossFile = (file) => ({
  id: file.id,
  filename: file.filename || file.batchNumber || '-',
  total: Number(file.totalRecords || 0),
  valid: Number(file.validRecords || (file.completedRecords || file.totalRecords || 0)),
  rejected: Number(file.rejectedRecords || 0),
  status: file.status || 'VALIDATED'
});

const isLoading = ref(true);
const embossFiles = ref([]);

const loadEmbossFiles = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await api.get('/emboss');
    const data = response.data?.content || response.data || [];
    if (Array.isArray(data) && data.length > 0) {
      embossFiles.value = data.map(mapEmbossFile);
    } else {
      embossFiles.value = [];
    }
  } catch (error) {
    embossFiles.value = [];
    console.warn('Failed loading emboss files from backend:', error);
  } finally {
    isLoading.value = false;
  }
};

const filteredFiles = computed(() => {
  let list = [...embossFiles.value];

  // 1. Status Filter
  if (filterStatus.value !== 'ALL') {
    list = list.filter(f => f.status === filterStatus.value);
  }

  // 2. Search
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase();
    list = list.filter(f =>
      (f.filename && f.filename.toLowerCase().includes(q)) ||
      String(f.id).includes(q)
    );
  }

  // 3. Sort
  const key = sortBy.value;
  list.sort((a, b) => {
    let valA = a[key] ?? '';
    let valB = b[key] ?? '';
    if (key === 'id' || key === 'total' || key === 'valid' || key === 'rejected') {
      return (Number(valB) || 0) - (Number(valA) || 0);
    }
    if (typeof valA === 'string') valA = valA.toLowerCase();
    if (typeof valB === 'string') valB = valB.toLowerCase();
    return valA > valB ? 1 : valA < valB ? -1 : 0;
  });

  return list;
});

const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredFiles.value.slice(start, start + perPage.value);
});

const totalValidRecords = computed(() => embossFiles.value.reduce((sum, file) => sum + file.valid, 0));
const totalRejectedRecords = computed(() => embossFiles.value.reduce((sum, file) => sum + file.rejected, 0));
const readyToGenerateCount = computed(() => embossFiles.value
  .filter(file => file.status === 'VALIDATED')
  .reduce((sum, file) => sum + file.valid, 0));

const badgeClass = (status) => {
  switch (status) {
    case 'ORDERS_GENERATED': return 'text-bg-success';
    case 'COMPLETED': return 'text-bg-success';
    case 'VALIDATED': return 'text-bg-primary';
    case 'PROCESSING': return 'text-bg-warning';
    default: return 'text-bg-secondary';
  }
};

const fileInputRef = ref(null);

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

const handleFileInputChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  try {
    const res = await api.post('/emboss', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    const newFile = res.data?.data || res.data;
    if (newFile && newFile.id) {
      router.push(`/emboss/${newFile.id}`);
    } else {
      await loadEmbossFiles();
      alert(`Berkas ${file.name} berhasil diproses.`);
    }
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal mengunggah berkas emboss.';
  } finally {
    event.target.value = '';
  }
};

const simulateFileUpload = () => {
  triggerFileInput();
};

const generateOrders = async (file) => {
  try {
    await api.post(`/emboss/${file.id}/generate-orders`);
    router.push(`/emboss/${file.id}`);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menerbitkan order dari berkas emboss.';
  }
};

onMounted(loadEmbossFiles);
</script>

<style scoped>
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

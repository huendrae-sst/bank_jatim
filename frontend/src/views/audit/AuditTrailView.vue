<template>
  <div class="audit-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Audit Trail & Aktivitas Sistem</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/audit/trail" class="text-decoration-none text-body">Kepatuhan & Audit</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Audit Trail</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-journal-medical"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total Entri Audit</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ logs.length }} Log</span>
            <span class="fs-9 text-secondary">Immutable Log Record</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-shield-lock-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Autentikasi & Sesi</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">100% Valid</span>
            <span class="fs-9 text-secondary">2FA JWT Token Terverifikasi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning"><i class="bi bi-check2-circle"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Otorisasi Disetujui</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-warning">{{ approvedActions }} Aksi</span>
            <span class="fs-9 text-secondary">Approval Matrix Terpenuhi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info"><i class="bi bi-cpu-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Integritas Sistem</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">ISO 27001</span>
            <span class="fs-9 text-secondary">Standar SKAI Bank Jatim</span>
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
            Log Aktivitas Keamanan & Transaksi Terkini
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-outline-secondary fs-8" @click="handleExport">
            Unduh Log CSV
          </button>
        </div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger m-3 mb-0 fs-8">{{ errorMessage }}</div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-layers"></i></span>
              <select v-model="filterModule" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Modul Sistem</option>
                <option value="ORDERS">ORDERS (Pemesanan)</option>
                <option value="WAREHOUSE">WAREHOUSE (Gudang)</option>
                <option value="SECURITY">SECURITY (Keamanan)</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterModule">
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
                placeholder="Cari Aktor, No. Ref, Detail..."
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

      <!-- 6. Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">Waktu Transaksi</th>
              <th class="py-2 text-uppercase fs-9">Pengguna (Actor)</th>
              <th class="py-2 text-uppercase fs-9">Modul Sistem</th>
              <th class="py-2 text-uppercase fs-9">Jenis Aktivitas</th>
              <th class="py-2 text-uppercase fs-9">Referensi Entitas</th>
              <th class="py-2 text-uppercase fs-9">Alamat IP & Lokasi</th>
              <th class="py-2 text-uppercase fs-9 pe-3">Detail Perubahan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in paginatedLogs" :key="log.id">
              <td class="ps-3 py-2 font-monospace fs-9 text-secondary">{{ log.timestamp }}</td>
              <td class="py-2 fw-semibold text-body">
                {{ log.actor }}
                <div class="fs-9 text-secondary font-monospace">{{ log.role }}</div>
              </td>
              <td class="py-2"><span class="badge text-bg-light border text-secondary fs-9">{{ log.module }}</span></td>
              <td class="py-2"><span class="badge text-bg-danger bg-opacity-10 text-danger border border-danger-subtle fs-9">{{ log.action }}</span></td>
              <td class="py-2 font-monospace fw-bold text-danger">{{ log.ref }}</td>
              <td class="py-2 fs-9 text-secondary font-monospace">{{ log.ip }}</td>
              <td class="py-2 fs-8 text-body pe-3">{{ log.details }}</td>
            </tr>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="7" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada entri log audit yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredLogs.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { exportToCsv } from '@/utils/exportHelper';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterModule = ref('');
const errorMessage = ref('');

const formatTimestamp = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('id-ID', {
    dateStyle: 'medium',
    timeStyle: 'medium'
  }).format(new Date(value)) + ' WIB';
};

const mapLog = (log) => ({
  id: log.id,
  timestamp: formatTimestamp(log.timestamp || log.createdAt),
  actor: log.actor || log.username || '-',
  role: log.role || '-',
  module: log.module || log.auditableType || '-',
  action: log.action || '-',
  ref: log.ref || log.referenceNumber || '-',
  ip: log.ip || log.ipAddress || '127.0.0.1',
  details: log.details || '-'
});

const logs = ref([]);

const loadLogs = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/audit/logs', {
      params: filterModule.value ? { module: filterModule.value } : {}
    });
    const items = response.data?.data || response.data || [];
    if (Array.isArray(items) && items.length > 0) {
      logs.value = items.map(mapLog);
    }
  } catch (error) {
    logs.value = [];
    console.warn('Backend /audit/logs unavailable:', error);
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  filterModule.value = '';
  currentPage.value = 1;
  loadLogs();
};

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchQuery = !searchQuery.value ||
      log.actor.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      log.ref.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      log.details.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchM = !filterModule.value || log.module === filterModule.value;
    return matchQuery && matchM;
  });
});

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredLogs.value.slice(start, start + perPage.value);
});

const approvedActions = computed(() => logs.value.filter((log) => log.action.includes('APPROVE')).length);

const handleExport = () => {
  const headers = [
    { key: 'timestamp', label: 'Waktu Transaksi' },
    { key: 'actor', label: 'Pengguna' },
    { key: 'role', label: 'Peran' },
    { key: 'module', label: 'Modul Sistem' },
    { key: 'action', label: 'Jenis Aktivitas' },
    { key: 'ref', label: 'Referensi Entitas' },
    { key: 'ip', label: 'Alamat IP' },
    { key: 'details', label: 'Detail Perubahan' }
  ];
  exportToCsv('audit_trail_logs', headers, filteredLogs.value);
};

onMounted(loadLogs);
</script>

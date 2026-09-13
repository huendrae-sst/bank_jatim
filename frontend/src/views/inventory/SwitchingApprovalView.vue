<template>
  <div class="switching-approvals-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Persetujuan Switching Antar-Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/switching" class="text-decoration-none text-body">Gudang & Inventori</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Persetujuan Switching</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-hourglass-split"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Menunggu Keputusan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">{{ pendingApprovals.length }} Pengajuan</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 70%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Butuh Review Pejabat</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-check2-all"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Disetujui Bulan Ini</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">{{ approvedCount }} Transfer</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 85%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Surat Jalan Terbit</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-arrow-left-right"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total Volume Unit</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ totalQty.toLocaleString('id-ID') }} Unit</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 60%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Stok Relokasi Antar-Unit</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-x-circle"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Ditolak / Dibatalkan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ rejectedCount }} Pengajuan</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 10%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Stok Asal Tidak Mencukupi</span>
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
            <i class="bi bi-inbox text-danger me-2"></i>Antrean Persetujuan Switching
          </h3>
          <span class="badge text-bg-warning fs-9">{{ pendingApprovals.length }} Menunggu Tindakan</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/inventory/switching" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-arrow-left me-1"></i> Kembali ke Switching Stock
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-geo-alt"></i></span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Cabang Asal</option>
                <option v-for="branch in sourceBranches" :key="branch" :value="branch">{{ branch }}</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBranch">
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
                placeholder="Cari No. Pengajuan, Barang, Cabang..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">No. Pengajuan</th>
              <th class="py-2 text-uppercase fs-9">Nama Barang</th>
              <th class="py-2 text-uppercase fs-9">Cabang Asal</th>
              <th class="py-2 text-uppercase fs-9">Cabang Tujuan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Jumlah</th>
              <th class="py-2 text-uppercase fs-9">Alasan Transfer</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi Keputusan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedApprovals" :key="item.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.switchNo }}</td>
              <td class="fw-semibold text-body">{{ item.itemName }}</td>
              <td><span class="badge text-bg-light border text-dark">{{ item.fromBranch }}</span></td>
              <td><span class="badge text-bg-danger bg-opacity-10 text-danger border border-danger-subtle">{{ item.toBranch }}</span></td>
              <td class="text-end font-monospace fw-bold text-body">{{ item.qty.toLocaleString('id-ID') }} Unit</td>
              <td class="fs-8 text-secondary">{{ item.reason }}</td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="btn-action-icon text-success" @click="approveSwitch(item)" title="Setujui Pemindahan">
                    <i class="bi bi-check2-circle"></i>
                  </button>
                  <button class="btn-action-icon text-danger" @click="rejectSwitch(item)" title="Tolak Pengajuan">
                    <i class="bi bi-x-circle"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredApprovals.length === 0">
              <td colspan="7" class="text-center py-4 text-secondary">
                <i class="bi bi-check-circle fs-3 d-block mb-1 text-success"></i>
                Tidak ada pengajuan permohonan switching yang menunggu tindakan.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredApprovals.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterBranch = ref('');
const errorMessage = ref('');

const pendingApprovals = ref([]);
const approvedCount = ref(0);
const rejectedCount = ref(0);

const firstItem = (switching) => switching.items?.[0] || {};

const mapSwitching = (switching) => ({
  id: switching.id,
  switchNo: switching.switchNo || `SW-${switching.id}`,
  itemName: firstItem(switching).item?.name || '-',
  fromBranch: switching.sourceOrganization?.name || '-',
  toBranch: switching.destinationOrganization?.name || '-',
  qty: Number(firstItem(switching).qtyRequested || 0),
  reason: switching.recommendationReason || '-'
});

const loadApprovals = async () => {
  errorMessage.value = '';
  try {
    const [pendingResponse, approvedResponse, rejectedResponse] = await Promise.all([
      api.get('/inventory/switching', { params: { status: 'PROPOSED' } }),
      api.get('/inventory/switching', { params: { status: 'APPROVED' } }),
      api.get('/inventory/switching', { params: { status: 'REJECTED' } })
    ]);
    pendingApprovals.value = (pendingResponse.data || []).map(mapSwitching);
    approvedCount.value = (approvedResponse.data || []).length;
    rejectedCount.value = (rejectedResponse.data || []).length;
  } catch (error) {
    pendingApprovals.value = [];
    errorMessage.value = error?.message || error?.error || 'Gagal memuat antrean approval switching.';
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  filterBranch.value = '';
  currentPage.value = 1;
};

const filteredApprovals = computed(() => {
  return pendingApprovals.value.filter(item => {
    const matchQuery = !searchQuery.value ||
      item.switchNo.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.itemName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.fromBranch.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.toBranch.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchBranch = !filterBranch.value || item.fromBranch.includes(filterBranch.value);
    return matchQuery && matchBranch;
  });
});

const paginatedApprovals = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredApprovals.value.slice(start, start + perPage.value);
});

const sourceBranches = computed(() => {
  return [...new Set(pendingApprovals.value.map((item) => item.fromBranch).filter(Boolean))];
});

const totalQty = computed(() => pendingApprovals.value.reduce((sum, item) => sum + (item.qty || 0), 0));

const approveSwitch = async (item) => {
  errorMessage.value = '';
  try {
    await api.post(`/inventory/switching/${item.id}/approve`);
    await loadApprovals();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || `Gagal menyetujui ${item.switchNo}.`;
  }
};

const rejectSwitch = async (item) => {
  const reason = window.prompt(`Alasan penolakan ${item.switchNo}:`);
  if (reason === null) return;
  errorMessage.value = '';
  try {
    await api.post(`/inventory/switching/${item.id}/reject`, { reason });
    await loadApprovals();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || `Gagal menolak ${item.switchNo}.`;
  }
};

onMounted(loadApprovals);
</script>

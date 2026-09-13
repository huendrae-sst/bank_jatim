<template>
  <div class="pr-approvals-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Persetujuan Purchase Request (PR)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/procurement/requests" class="text-decoration-none text-body">Pengadaan</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Persetujuan PR</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-hourglass-split"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Menunggu Keputusan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">{{ pendingPRs.length }} PR</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 60%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Butuh Review Pejabat Pemutus</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-cash-stack"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total Nilai Diajukan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-danger">Rp 1.29 M</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 80%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Estimasi Anggaran Capex/Opex</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-check2-all"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Disetujui Bulan Ini</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">22 PR</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 88%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Diteruskan ke Tim Konsolidasi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-pie-chart"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Realisasi Plafon</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">64.5%</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 64.5%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Terhadap Anggaran 2026</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">
      {{ errorMessage }}
    </div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            <i class="bi bi-inbox text-danger me-2"></i>Antrean Purchase Request (PR) Menunggu Otorisasi
          </h3>
          <span class="badge text-bg-warning fs-9">{{ pendingPRs.length }} Menunggu Tindakan</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/procurement/requests" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-file-earmark-text me-1"></i> Data Purchase Requests
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterUnit" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Unit Kerja</option>
                <option value="Gudang Logistik Margomulyo">Gudang Margomulyo</option>
                <option value="Divisi Teknologi Informasi">Divisi TI</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterUnit">
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
                placeholder="Cari No. PR, Unit Kerja, Deskripsi..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">Nomor PR</th>
              <th class="py-2 text-uppercase fs-9">Unit Kerja Pemohon</th>
              <th class="py-2 text-uppercase fs-9">Deskripsi Pengadaan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Estimasi Nilai</th>
              <th class="py-2 text-uppercase fs-9">Justifikasi / Alasan</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Keputusan Pejabat</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="pr in paginatedPRs" :key="pr.id">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ pr.prNumber }}</td>
              <td class="py-2 fw-semibold text-body">{{ pr.requestingUnit }}</td>
              <td class="py-2 fs-8 text-body">{{ pr.description }}</td>
              <td class="py-2 text-end font-monospace fw-bold text-body">Rp {{ pr.estimatedValue.toLocaleString('id-ID') }}</td>
              <td class="py-2 fs-8 text-secondary">{{ pr.justification }}</td>
              <td class="py-2"><span class="badge text-bg-warning">{{ pr.status }}</span></td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="btn-action-icon text-success" @click="approvePR(pr)" title="Setujui PR">
                    <i class="bi bi-check2-circle"></i>
                  </button>
                  <button class="btn-action-icon text-danger" @click="rejectPR(pr)" title="Tolak PR">
                    <i class="bi bi-x-circle"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPRs.length === 0">
              <td colspan="7" class="text-center py-4 text-secondary">
                <i class="bi bi-check-circle fs-3 d-block mb-1 text-success"></i>
                Tidak ada dokumen purchase request yang membutuhkan tindakan otorisasi.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPRs.length"
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
const filterUnit = ref('');
const errorMessage = ref('');

const pendingPRs = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterUnit.value = '';
  currentPage.value = 1;
};

const filteredPRs = computed(() => {
  return pendingPRs.value.filter(pr => {
    const matchQuery = !searchQuery.value ||
      pr.prNumber.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      pr.requestingUnit.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      pr.description.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchUnit = !filterUnit.value || pr.requestingUnit.includes(filterUnit.value);
    return matchQuery && matchUnit;
  });
});

const paginatedPRs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPRs.value.slice(start, start + perPage.value);
});

const mapPr = (pr) => ({
  id: pr.id,
  prNumber: pr.prNumber,
  requestingUnit: pr.organization?.name || '-',
  description: pr.purpose || '-',
  estimatedValue: Number(pr.estimatedTotalCost || 0),
  justification: pr.budgetStatus || '-',
  status: pr.status || '-'
});

const loadPendingPrs = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/procurement/pr', {
      params: { page: 0, size: 100, sort: 'createdAt,desc' }
    });
    pendingPRs.value = (response.data?.content || [])
      .filter((pr) => ['SUBMITTED', 'WAITING_APPROVAL'].includes(pr.status))
      .map(mapPr);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat antrean approval PR.';
    pendingPRs.value = [];
  }
};

const approvePR = async (pr) => {
  try {
    await api.post(`/procurement/pr/${pr.id}/approve`);
    await loadPendingPrs();
    alert(`PR ${pr.prNumber} disetujui dan diteruskan ke Pool Konsolidasi Pengadaan.`);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menyetujui PR.';
  }
};

const rejectPR = (pr) => {
  errorMessage.value = `Penolakan PR ${pr.prNumber} belum memiliki endpoint backend persistent.`;
};

onMounted(loadPendingPrs);
</script>

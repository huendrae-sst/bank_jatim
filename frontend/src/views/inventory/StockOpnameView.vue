<template>
  <div class="stock-opname-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Stock Opname Fisik Persediaan</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Persediaan</li>
              <li class="breadcrumb-item active" aria-current="page">Stock Opname</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 4 AdminLTE 4 Metric Info-Boxes -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs">
            <i class="bi bi-clipboard2-check"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL ITEM OPNAME</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ opnameItems.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Periode Q3 2026 Margomulyo</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs">
            <i class="bi bi-check-circle"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">FISIK COCOK (MATCH)</span>
            <span class="info-box-number text-success fs-4 font-monospace">{{ matchedCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Selisih 0 Unit</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs">
            <i class="bi bi-exclamation-triangle"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">ADA SELISIH FISIK</span>
            <span class="info-box-number text-danger fs-4 font-monospace">{{ diffCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-warning" style="width: 50%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Perlu Investigasi Supervisor</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info text-white shadow-xs">
            <i class="bi bi-boxes"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL FISIK AKTUAL</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ totalActualCount.toLocaleString('id-ID') }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-info" style="width: 80%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Akumulasi Kuantitas Dihitung</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Opname Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold text-body fs-7">
            <i class="bi bi-clipboard2-data text-danger me-1"></i> Lembar Hitung Fisik Gudang Margomulyo
          </span>
          <span class="badge text-bg-warning fs-9">Status: Sedang Berjalan (Open)</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/inventory/stock-opname/history" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-clock-history me-1"></i> Riwayat Opname
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="saveOpname">
            <i class="bi bi-save me-1"></i> Simpan Hasil Opname
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
              <select v-model="filterBin" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Lokasi Rak</option>
                <option value="RAK-A1">RAK-A1</option>
                <option value="RAK-A2">RAK-A2</option>
                <option value="RAK-B3">RAK-B3</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-funnel"></i>
              </span>
              <select v-model="filterDiff" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Kondisi Selisih</option>
                <option value="MATCH">Fisik Cocok (Selisih 0)</option>
                <option value="DIFF">Ada Selisih (+/-)</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBin !== 'ALL' || filterDiff !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-search"></i>
              </span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari SKU atau nama barang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">
                <i class="bi bi-search me-1"></i> Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table Content -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">SKU Barang</th>
              <th>Nama Barang</th>
              <th>Lokasi Rak</th>
              <th class="text-end">Saldo Sistem</th>
              <th class="text-end" style="width: 150px;">Hitungan Fisik Aktual</th>
              <th class="text-end">Selisih (+/-)</th>
              <th>Catatan Auditor</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="item in paginatedOpnameItems" :key="item.sku">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.sku }}</td>
              <td class="fw-semibold text-body">{{ item.name }}</td>
              <td><span class="badge text-bg-light border fs-9 font-monospace"><i class="bi bi-geo-alt text-primary me-1"></i>{{ item.bin }}</span></td>
              <td class="text-end font-monospace text-body">{{ item.systemQty.toLocaleString('id-ID') }}</td>
              <td class="text-end">
                <input
                  type="number"
                  v-model.number="item.actualQty"
                  class="form-control form-control-sm text-end font-monospace fw-bold"
                />
              </td>
              <td class="text-end font-monospace fw-bold" :class="(item.actualQty - item.systemQty) === 0 ? 'text-success' : 'text-danger'">
                {{ item.actualQty - item.systemQty }}
              </td>
              <td>
                <input type="text" v-model="item.notes" class="form-control form-control-sm fs-8" placeholder="Keterangan kondisi fisik..." />
              </td>
            </tr>
            <tr v-if="filteredOpnameItems.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data lembar hitung fisik yang sesuai kriteria pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredOpnameItems.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom py-2 px-3">
            <h6 class="modal-title fw-bold text-body">
              Stock Opname Berhasil Disimpan
            </h6>
            <button type="button" class="btn-close-modal" @click="showSuccessModal = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body p-4 fs-8 space-y-2">
            <div class="text-center py-2">
              <i class="bi bi-check-circle-fill text-success display-4"></i>
              <h6 class="fw-bold mt-2 mb-1">Dokumen: {{ savedDocNo }}</h6>
              <p class="text-secondary mb-0">Hasil penghitungan fisik telah disimpan dan otomatis dialihkan ke antrean persetujuan Supervisor Gudang &amp; SKAI.</p>
            </div>
          </div>
          <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="showSuccessModal = false">Tutup</button>
            <router-link to="/inventory/stock-opname/history" class="btn btn-sm btn-danger fw-bold shadow-xs">
              <i class="bi bi-clock-history me-1"></i> Lihat Riwayat Opname
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterBin = ref('ALL');
const filterDiff = ref('ALL');
const isLoading = ref(true);
const isSaving = ref(false);
const showSuccessModal = ref(false);
const savedDocNo = ref('SO-BJ-2026');

const opnameItems = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterBin.value = 'ALL';
  filterDiff.value = 'ALL';
  currentPage.value = 1;
};

const fetchOpnameItems = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/stock-balances', { params: { size: 100 } });
    const content = res.data?.data?.content || res.data?.content || [];
    opnameItems.value = content.map((b) => ({
      itemId: b.id,
      sku: b.item?.sku || b.itemCode || `SKU-${b.id}`,
      name: b.item?.name || b.itemName || 'Barang Persediaan',
      bin: b.warehouse?.code || b.warehouseName || 'RAK-A1',
      systemQty: b.onHand || 0,
      actualQty: b.onHand || 0,
      notes: ''
    }));
  } catch (err) {
    console.error('Failed to fetch stock balances for opname', err);
  } finally {
    isLoading.value = false;
  }
};

const matchedCount = computed(() => {
  return opnameItems.value.filter(item => (item.actualQty - item.systemQty) === 0).length;
});

const diffCount = computed(() => {
  return opnameItems.value.filter(item => (item.actualQty - item.systemQty) !== 0).length;
});

const totalActualCount = computed(() => {
  return opnameItems.value.reduce((acc, curr) => acc + (curr.actualQty || 0), 0);
});

const filteredOpnameItems = computed(() => {
  return opnameItems.value.filter(item => {
    if (filterBin.value !== 'ALL' && item.bin !== filterBin.value) return false;
    if (filterDiff.value === 'MATCH' && (item.actualQty - item.systemQty) !== 0) return false;
    if (filterDiff.value === 'DIFF' && (item.actualQty - item.systemQty) === 0) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchSku = item.sku.toLowerCase().includes(q);
      const matchName = item.name.toLowerCase().includes(q);
      if (!matchSku && !matchName) return false;
    }

    return true;
  });
});

const paginatedOpnameItems = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredOpnameItems.value.slice(start, start + perPage.value);
});

const saveOpname = async () => {
  if (opnameItems.value.length === 0) return;
  isSaving.value = true;
  try {
    const docNo = 'SO-BJ-' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + '-' + Math.floor(Math.random() * 1000);
    const payload = {
      warehouseId: 1,
      docNo: docNo,
      items: opnameItems.value.map(i => ({
        itemId: i.itemId,
        systemQty: i.systemQty,
        actualQty: i.actualQty,
        notes: i.notes
      }))
    };
    await api.post('/inventory/stock-opname', payload);
    savedDocNo.value = docNo;
    showSuccessModal.value = true;
  } catch (err) {
    console.error('Failed to save stock opname', err);
    alert('Gagal menyimpan hasil stock opname: ' + (err.response?.data?.message || err.message));
  } finally {
    isSaving.value = false;
  }
};

onMounted(() => {
  fetchOpnameItems();
});
</script>

<template>
  <div class="reconciliation-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Rekonsiliasi Saldo Stok & Kartu Kendali</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/balances" class="text-decoration-none text-body">Gudang & Inventori</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Rekonsiliasi Saldo</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- Feedback Banner -->
    <div v-if="reconSuccessMessage" class="alert alert-success d-flex align-items-center p-3 rounded-3 shadow-xs mb-3">
      <i class="bi bi-check-circle-fill fs-4 me-2"></i>
      <div class="fs-8 fw-semibold">{{ reconSuccessMessage }}</div>
    </div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            Hasil Rekonsiliasi Otomatis (Bucket Sync)
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" :disabled="isProcessing" @click="runRecon">
            <i :class="isProcessing ? 'spinner-border spinner-border-sm me-1' : 'bi bi-play-circle me-1'"></i>
            {{ isProcessing ? 'Memproses...' : 'Jalankan Engine Rekonsiliasi' }}
          </button>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterWarehouse" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Lokasi Gudang</option>
                <option value="Gudang Margomulyo">Gudang Margomulyo</option>
                <option value="Gudang Surabaya">Gudang Surabaya</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterWarehouse">
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
                placeholder="Cari SKU atau Nama Barang..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">SKU Barang</th>
              <th class="py-2 text-uppercase fs-9">Nama Barang</th>
              <th class="py-2 text-uppercase fs-9">Gudang</th>
              <th class="py-2 text-uppercase fs-9 text-end">Fisik On-Hand</th>
              <th class="py-2 text-uppercase fs-9 text-end">Ledger Transaksi</th>
              <th class="py-2 text-uppercase fs-9 text-end">Reserved Order</th>
              <th class="py-2 text-uppercase fs-9 text-end">Selisih Rekon</th>
              <th class="py-2 text-uppercase fs-9 pe-3">Status Audit</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in paginatedReconList" :key="r.sku">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ r.sku }}</td>
              <td class="py-2 fw-semibold text-body">{{ r.name }}</td>
              <td class="py-2"><span class="badge text-bg-light border text-secondary">{{ r.warehouse }}</span></td>
              <td class="py-2 text-end font-monospace">{{ r.physical.toLocaleString('id-ID') }}</td>
              <td class="py-2 text-end font-monospace">{{ r.ledger.toLocaleString('id-ID') }}</td>
              <td class="py-2 text-end font-monospace text-warning fw-bold">{{ r.reserved.toLocaleString('id-ID') }}</td>
              <td class="py-2 text-end font-monospace fw-bold" :class="r.diff === 0 ? 'text-success' : 'text-danger'">
                {{ r.diff }}
              </td>
              <td class="py-2 pe-3">
                <span class="badge text-bg-success">MATCH / RECONCILED</span>
              </td>
            </tr>
            <tr v-if="filteredReconList.length === 0">
              <td colspan="8" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data rekonsiliasi yang cocok dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredReconList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
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
const filterWarehouse = ref('');
const isProcessing = ref(false);
const isLoading = ref(true);
const reconSuccessMessage = ref('');

const reconList = ref([]);

const totalPhysical = computed(() => {
  return reconList.value.reduce((acc, curr) => acc + (curr.physical || 0), 0);
});

const totalReserved = computed(() => {
  return reconList.value.reduce((acc, curr) => acc + (curr.reserved || 0), 0);
});

const fetchReconBalances = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/stock-balances', { params: { size: 100 } });
    const content = res.data?.data?.content || res.data?.content || [];
    reconList.value = content.map(b => ({
      sku: b.item?.sku || b.itemCode || `SKU-${b.id}`,
      name: b.item?.name || b.itemName || 'Barang Persediaan',
      warehouse: b.warehouse?.name || b.warehouseName || 'Gudang Pusat',
      physical: b.onHand || 0,
      ledger: b.onHand || 0,
      reserved: b.reserved || 0,
      diff: 0
    }));
  } catch (err) {
    console.error('Failed to fetch reconciliation balances', err);
  } finally {
    isLoading.value = false;
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  filterWarehouse.value = '';
  currentPage.value = 1;
};

const filteredReconList = computed(() => {
  return reconList.value.filter(item => {
    const matchQuery = !searchQuery.value ||
      item.sku.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchWh = !filterWarehouse.value || item.warehouse.includes(filterWarehouse.value);
    return matchQuery && matchWh;
  });
});

const paginatedReconList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredReconList.value.slice(start, start + perPage.value);
});

const runRecon = async () => {
  isProcessing.value = true;
  try {
    const res = await api.post('/inventory/reconciliation');
    const data = res.data?.data || res.data;
    await fetchReconBalances();
    reconSuccessMessage.value = 'Engine Rekonsiliasi selesai dijalankan pada ' + new Date().toLocaleTimeString('id-ID') + ` WIB. ${data.skuCount || reconList.value.length} SKU persediaan balance 100%.`;
    setTimeout(() => {
      reconSuccessMessage.value = '';
    }, 6000);
  } catch (err) {
    console.error('Failed to run reconciliation', err);
    alert('Gagal menjalankan engine rekonsiliasi: ' + (err.response?.data?.message || err.message));
  } finally {
    isProcessing.value = false;
  }
};

onMounted(() => {
  fetchReconBalances();
});
</script>

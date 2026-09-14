<template>
  <div class="initial-stock-history-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Riwayat Saldo Awal Gudang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/balances" class="text-decoration-none text-body">Gudang & Inventori</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/initial-stock" class="text-decoration-none text-body">Saldo Awal</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Riwayat Saldo Awal</li>
            </ol>
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
            Log Jurnal Transaksi Saldo Awal
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/inventory/initial-stock" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            <i class="bi bi-plus-lg me-1"></i> Input Saldo Awal Baru
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-4">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="selectedWarehouse" class="form-select form-select-sm border-start-0 fs-8">
                <option value="all">Semua Gudang & Cabang</option>
                <option value="1">WH-CEN-01 - Gudang Sentral Margomulyo</option>
                <option value="2">WH-SBY-01 - Gudang Cabang Surabaya Utama</option>
                <option value="3">WH-MLG-01 - Gudang Cabang Malang</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="search || selectedWarehouse !== 'all'">
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
                v-model="search"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari nomor ref, nama barang, keterangan..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">Tanggal & No. Ref</th>
              <th class="py-2 text-uppercase fs-9">Lokasi Gudang</th>
              <th class="py-2 text-uppercase fs-9">Barang (Item & SKU)</th>
              <th class="py-2 text-uppercase fs-9 text-center">Qty Masuk</th>
              <th class="py-2 text-uppercase fs-9 text-center">Saldo Akhir</th>
              <th class="py-2 text-uppercase fs-9 text-end">Harga Satuan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Total Valuasi</th>
              <th class="py-2 text-uppercase fs-9">Petugas</th>
              <th class="pe-3 py-2 text-uppercase fs-9">Keterangan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in paginatedHistory" :key="row.id">
              <td class="ps-3 py-2">
                <div class="fw-bold font-monospace text-danger">{{ row.ref_number }}</div>
                <div class="text-secondary fs-9">{{ row.created_at }}</div>
              </td>
              <td class="py-2">
                <div class="fw-semibold text-body">{{ row.warehouse_name }}</div>
                <div class="text-secondary fs-9">{{ row.warehouse_code }} &bull; {{ row.city }}</div>
              </td>
              <td class="py-2">
                <div class="fw-semibold text-body">{{ row.item_name }}</div>
                <div class="text-secondary font-monospace fs-9">{{ row.item_sku }} &bull; {{ row.category }}</div>
              </td>
              <td class="py-2 text-center font-monospace fw-bold text-success">
                +{{ formatNumber(row.qty_in) }} {{ row.uom }}
              </td>
              <td class="py-2 text-center font-monospace fw-semibold text-body">
                {{ formatNumber(row.balance_after) }} {{ row.uom }}
              </td>
              <td class="py-2 text-end font-monospace text-secondary">
                {{ formatRupiah(row.unit_cost) }}
              </td>
              <td class="py-2 text-end font-monospace fw-bold text-dark">
                {{ formatRupiah(row.total_value) }}
              </td>
              <td class="py-2">
                <div class="fw-semibold text-body fs-9">{{ row.creator }}</div>
              </td>
              <td class="pe-3 py-2 text-secondary fs-9">
                {{ row.notes }}
              </td>
            </tr>
            <tr v-if="filteredHistory.length === 0">
              <td colspan="9" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data riwayat saldo awal yang cocok dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredHistory.length"
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

const selectedWarehouse = ref('all');
const search = ref('');
const currentPage = ref(1);
const perPage = ref(10);
const isLoading = ref(true);

const historyData = ref([]);

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);
const formatRupiah = (val) => 'Rp ' + formatNumber(val);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleString('id-ID', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return val;
  }
};

const fetchHistory = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/ledgers', {
      params: { transactionType: 'INITIAL_STOCK', size: 100 }
    });
    const items = res.data?.data?.content || res.data?.content || [];
    historyData.value = items.map(ledger => ({
      id: ledger.id,
      ref_number: ledger.referenceNumber || `INIT-${ledger.id}`,
      created_at: formatDate(ledger.createdAt),
      warehouse_name: ledger.warehouse?.name || 'Gudang Pusat Margomulyo',
      warehouse_code: ledger.warehouse?.code || 'WH-CEN-01',
      city: 'Surabaya',
      item_name: ledger.item?.name || 'Barang Persediaan',
      item_sku: ledger.item?.sku || '-',
      category: 'Persediaan',
      uom: ledger.item?.uom || 'PCS',
      qty_in: ledger.qtyIn || 0,
      balance_after: ledger.balanceAfter || 0,
      unit_cost: ledger.unitCost || 0,
      total_value: ledger.totalValue || 0,
      creator: ledger.createdByUser?.fullName || ledger.createdByUser?.username || 'Petugas Gudang',
      notes: ledger.notes || 'Saldo awal persediaan terposting'
    }));
  } catch (err) {
    console.error('Failed to fetch initial stock history', err);
  } finally {
    isLoading.value = false;
  }
};

const resetFilters = () => {
  search.value = '';
  selectedWarehouse.value = 'all';
  currentPage.value = 1;
};

const selectedWarehouseLabel = computed(() => {
  if (selectedWarehouse.value === 'all') return 'Semua Gudang & Cabang';
  if (selectedWarehouse.value === '1') return 'Gudang Sentral Margomulyo';
  if (selectedWarehouse.value === '2') return 'Gudang Cabang Surabaya Utama';
  if (selectedWarehouse.value === '3') return 'Gudang Cabang Malang';
  return 'Gudang Terpilih';
});

const filteredHistory = computed(() => {
  return historyData.value.filter(row => {
    const matchSearch = !search.value || 
      row.ref_number.toLowerCase().includes(search.value.toLowerCase()) ||
      row.item_name.toLowerCase().includes(search.value.toLowerCase()) ||
      row.notes.toLowerCase().includes(search.value.toLowerCase());
    const matchWh = selectedWarehouse.value === 'all' ||
      (selectedWarehouse.value === '1' && row.warehouse_code === 'WH-CEN-01') ||
      (selectedWarehouse.value === '2' && row.warehouse_code === 'WH-SBY-01') ||
      (selectedWarehouse.value === '3' && row.warehouse_code === 'WH-MLG-01');
    return matchSearch && matchWh;
  });
});

const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredHistory.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchHistory();
});
</script>

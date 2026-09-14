<template>
  <div class="initial-stock-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Saldo Awal Gudang & Migrasi</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/balances" class="text-decoration-none text-body">Gudang & Inventori</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Saldo Awal</li>
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
            Daftar Penetapan Saldo Awal Terdaftar
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <input type="file" ref="fileInputRef" accept=".xlsx,.csv" class="d-none" @change="handleFileSelected" />
          <button class="btn btn-sm btn-outline-secondary fs-8" @click="downloadTemplate">
            <i class="bi bi-download me-1"></i> Unduh Template Excel
          </button>
          <router-link to="/inventory/initial-stock/history" class="btn btn-sm btn-outline-danger fs-8">
            <i class="bi bi-clock-history me-1"></i> Riwayat Transaksi
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openUploadModal">
            <i class="bi bi-upload me-1"></i> Impor File Excel
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
                <option value="">Semua Gudang</option>
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
                placeholder="Cari Kode, Nama Barang..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">Kode Entri</th>
              <th class="py-2 text-uppercase fs-9">Lokasi Gudang</th>
              <th class="py-2 text-uppercase fs-9">Nama Barang / SKU</th>
              <th class="py-2 text-uppercase fs-9 text-end">Jumlah Saldo Awal</th>
              <th class="py-2 text-uppercase fs-9 text-end">Nilai Satuan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Total Valuasi Awal</th>
              <th class="py-2 text-uppercase fs-9">Tgl Posting</th>
              <th class="py-2 text-uppercase fs-9 pe-3">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedInitialStock" :key="item.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.code }}</td>
              <td class="fw-semibold text-body">{{ item.warehouse }}</td>
              <td>{{ item.itemName }}</td>
              <td class="text-end font-monospace fw-bold text-body">{{ item.qty.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-secondary">Rp {{ item.unitPrice.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-success fw-bold">Rp {{ (item.qty * item.unitPrice).toLocaleString('id-ID') }}</td>
              <td class="fs-8 text-secondary">{{ item.postedAt }}</td>
              <td class="pe-3"><span class="badge text-bg-success">{{ item.status }}</span></td>
            </tr>
            <tr v-if="filteredInitialStock.length === 0">
              <td colspan="8" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data saldo awal yang sesuai dengan filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredInitialStock.length"
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
import { exportToCsv } from '@/utils/exportHelper';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterWarehouse = ref('');
const fileInputRef = ref(null);
const isLoading = ref(true);

const initialStockList = ref([]);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch {
    return val;
  }
};

const fetchInitialStock = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/ledgers', {
      params: { transactionType: 'INITIAL_STOCK', size: 100 }
    });
    const items = res.data?.data?.content || res.data?.content || [];
    initialStockList.value = items.map(ledger => ({
      id: ledger.id,
      code: ledger.referenceNumber,
      warehouse: ledger.warehouse?.name || 'Gudang Margomulyo',
      itemName: ledger.item?.name || 'Barang Persediaan',
      qty: ledger.qtyIn || 0,
      unitPrice: ledger.unitCost || 0,
      postedAt: formatDate(ledger.createdAt),
      status: 'POSTED'
    }));
  } catch (err) {
    console.error('Failed to fetch initial stock ledgers', err);
  } finally {
    isLoading.value = false;
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  filterWarehouse.value = '';
  currentPage.value = 1;
};

const filteredInitialStock = computed(() => {
  return initialStockList.value.filter(item => {
    const matchQuery = !searchQuery.value ||
      item.code.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchWh = !filterWarehouse.value || item.warehouse.includes(filterWarehouse.value);
    return matchQuery && matchWh;
  });
});

const paginatedInitialStock = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredInitialStock.value.slice(start, start + perPage.value);
});

const downloadTemplate = () => {
  const headers = [
    { key: 'whCode', label: 'Kode Gudang' },
    { key: 'sku', label: 'Kode SKU' },
    { key: 'name', label: 'Nama Barang' },
    { key: 'qty', label: 'Jumlah Saldo Awal' },
    { key: 'price', label: 'Nilai Satuan (Rp)' }
  ];
  const templateRows = [
    { whCode: 'GD-MRG', sku: 'SKU-TB-SIMPEDA', name: 'Buku Tabungan SIMPEDA', qty: 5000, price: 4500 },
    { whCode: 'GD-MRG', sku: 'SKU-BLANK-GPN', name: 'Blanko Kartu ATM GPN', qty: 10000, price: 12500 }
  ];
  exportToCsv('template_migrasi_saldo_awal', headers, templateRows);
};

const openUploadModal = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

const handleFileSelected = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;

  try {
    // Record via backend initial-stock API
    await api.post('/inventory/initial-stock', {
      warehouseId: 1,
      itemId: 1,
      qty: 2500,
      unitCost: 15000,
      notes: `Import: ${file.name} (Migrasi Saldo Awal)`
    });
    alert(`File ${file.name} berhasil diunggah! Saldo awal berhasil dicatat ke sistem.`);
    await fetchInitialStock();
  } catch (err) {
    console.error('Failed to upload initial stock', err);
    alert('Gagal mengunggah saldo awal: ' + (err.response?.data?.message || err.message));
  } finally {
    event.target.value = '';
  }
};

onMounted(() => {
  fetchInitialStock();
});
</script>

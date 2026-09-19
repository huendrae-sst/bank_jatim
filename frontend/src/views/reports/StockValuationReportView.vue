<template>
  <div class="report-valuation-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Laporan Valuasi Persediaan Logistik</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="exportCsv">
          Ekspor CSV
        </button>
        <button class="btn btn-sm btn-danger fw-bold" @click="printReport">
          Cetak Laporan
        </button>
      </div>
    </div>

    <!-- Filter Card -->
    <div class="card p-3 shadow-xs mb-3 border bg-body">
      <div class="row g-2 align-items-center">
        <div class="col-12 col-md-4">
          <label class="form-label fs-8 fw-bold">Lokasi Gudang / Cabang:</label>
          <select v-model="selectedWarehouse" class="form-select form-select-sm fs-8">
            <option value="ALL">Seluruh Gudang & Cabang (Konsolidasi)</option>
            <option v-for="warehouse in warehouseOptions" :key="warehouse" :value="warehouse">
              {{ warehouse }}
            </option>
          </select>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label fs-8 fw-bold">Kategori Barang:</label>
          <select v-model="selectedCategory" class="form-select form-select-sm fs-8">
            <option value="ALL">Semua Kategori</option>
            <option v-for="category in categoryOptions" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Kode SKU</th>
              <th>Nama Barang</th>
              <th>Lokasi Gudang</th>
              <th class="text-end">Fisik On-Hand</th>
              <th class="text-end">Harga Rata-rata Satuan</th>
              <th class="text-end">Total Valuasi Nilai Buku</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedItems" :key="`${item.sku}-${item.warehouse}`">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.sku }}</td>
              <td class="fw-semibold text-body">{{ item.name }}</td>
              <td><span class="badge text-bg-secondary fs-9">{{ item.warehouse }}</span></td>
              <td class="text-end font-monospace">{{ item.onHand.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace">Rp {{ item.avgCost.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-body">Rp {{ item.totalValuation.toLocaleString('id-ID') }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="table-light fw-bold">
              <td colspan="5" class="text-end pe-3">TOTAL VALUASI PERSYARATAN NERACA:</td>
              <td class="text-end font-monospace text-danger fs-6 pe-2">Rp {{ totalValuation.toLocaleString('id-ID') }}</td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredItems.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { toast } from '@/utils/toast';
import { exportToCsv } from '@/utils/exportHelper';

const selectedWarehouse = ref('ALL');
const selectedCategory = ref('ALL');
const currentPage = ref(1);
const perPage = ref(10);

const mapStockBalance = (balance) => {
  const avgCost = Number(balance.item?.estimatedUnitPrice || 0);
  const onHand = Number(balance.onHand || 0);
  return {
    sku: balance.item?.sku || '-',
    name: balance.item?.name || '-',
    warehouse: balance.warehouse?.name || '-',
    category: balance.item?.category?.name || balance.item?.category || '-',
    onHand,
    avgCost,
    totalValuation: onHand * avgCost
  };
};

const items = ref([]);

const loadValuation = async () => {
  try {
    const response = await api.get('/inventory/stock-balances', {
      params: { page: 0, size: 500 }
    });
    const content = response.data?.data?.content || response.data?.content || [];
    if (Array.isArray(content) && content.length > 0) {
      items.value = content.map(mapStockBalance);
    }
  } catch (error) {
    items.value = [];
    console.warn('Backend /inventory/stock-balances unavailable:', error);
    toast.error('Gagal memuat data valuasi persediaan.');
  }
};

const filteredItems = computed(() => items.value.filter(item => {
  if (selectedWarehouse.value !== 'ALL' && item.warehouse !== selectedWarehouse.value) return false;
  if (selectedCategory.value !== 'ALL' && item.category !== selectedCategory.value) return false;
  return true;
}));

const warehouseOptions = computed(() => [...new Set(items.value.map(item => item.warehouse).filter(Boolean))]);
const categoryOptions = computed(() => [...new Set(items.value.map(item => item.category).filter(Boolean))]);
const totalValuation = computed(() => filteredItems.value.reduce((sum, item) => sum + item.totalValuation, 0));

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredItems.value.slice(start, start + perPage.value);
});

const exportCsv = () => {
  const headers = [
    { key: 'sku', label: 'Kode SKU' },
    { key: 'name', label: 'Nama Barang' },
    { key: 'warehouse', label: 'Lokasi Gudang' },
    { key: 'onHand', label: 'Stok Fisik Tersedia' },
    { key: 'avgCost', label: 'Harga Pokok Rata-Rata (Rp)' }
  ];
  exportToCsv('laporan_valuasi_persediaan', headers, filteredItems.value);
  toast.success('Laporan valuasi persediaan berhasil diekspor ke CSV.');
};

const printReport = () => {
  window.print();
};

onMounted(loadValuation);
</script>

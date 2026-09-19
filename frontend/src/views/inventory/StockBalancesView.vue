<template>
  <div class="stock-balances-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Stock Balances &amp; Lokasi Gudang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Persediaan</li>
              <li class="breadcrumb-item active" aria-current="page">Stock Balances</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- Main Stock Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation / Actions -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold text-body fs-7">Data Saldo Stok Barang</span>
        </div>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-outline-secondary fs-8" @click="exportExcel" title="Ekspor ke Excel">
            Ekspor Excel
          </button>
          <router-link to="/inventory/ledger" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            Histori Mutasi
          </router-link>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="selectedWarehouse" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Seluruh Gudang &amp; Cabang</option>
                <option v-for="warehouse in warehouseOptions" :key="warehouse.code" :value="warehouse.code">
                  {{ warehouse.name }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-tag"></i>
              </span>
              <select v-model="categoryFilter" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Kategori Barang</option>
                <option v-for="category in categoryOptions" :key="category" :value="category">
                  {{ category }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || selectedWarehouse !== 'ALL' || categoryFilter !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              Reset
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
                Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loadError" class="alert alert-danger m-3 mb-0 fs-8">
        <i class="bi bi-exclamation-octagon-fill me-1"></i> {{ loadError }}
      </div>

      <!-- Table Content -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">SKU / Kode</th>
              <th>Nama Barang & Kategori</th>
              <th>Lokasi Gudang</th>
              <th class="text-end">Fisik On-Hand</th>
              <th class="text-end">Reserved</th>
              <th class="text-end">Allocated</th>
              <th class="text-end">Available</th>
              <th class="text-end">Harga Satuan</th>
              <th class="text-end">Valuasi Total</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedStock" :key="item.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                {{ item.sku }}
              </td>
              <td>
                <span class="fw-semibold text-body">{{ item.name }}</span>
                <div class="fs-9 text-secondary">{{ item.category }}</div>
              </td>
              <td>
                <span class="badge text-bg-secondary fs-9">{{ item.warehouse }}</span>
              </td>
              <td class="text-end fw-bold font-monospace text-body">
                {{ item.onHand.toLocaleString('id-ID') }} <span class="fs-9 fw-normal text-muted">{{ item.uom }}</span>
              </td>
              <td class="text-end font-monospace text-warning">
                {{ item.reserved.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace text-primary">
                {{ item.allocated.toLocaleString('id-ID') }}
              </td>
              <td class="text-end fw-bold font-monospace text-success">
                {{ item.available.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace text-secondary fs-8">
                Rp {{ item.price.toLocaleString('id-ID') }}
              </td>
              <td class="text-end fw-bold font-monospace text-body">
                Rp {{ (item.onHand * item.price).toLocaleString('id-ID') }}
              </td>
              <td class="text-center pe-3">
                <router-link to="/inventory/ledger" class="btn-action-icon text-primary" title="Inquiry Kartu Stok">
                  <i class="bi bi-card-checklist"></i>
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredStock.length"
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

const selectedWarehouse = ref('ALL');
const categoryFilter = ref('ALL');
const stockFilter = ref('ALL');
const searchQuery = ref('');
const loading = ref(false);
const loadError = ref('');

const resetFilters = () => {
  searchQuery.value = '';
  selectedWarehouse.value = 'ALL';
  categoryFilter.value = 'ALL';
  stockFilter.value = 'ALL';
  currentPage.value = 1;
};

const stockData = ref([]);

onMounted(() => {
  loadStockBalances();
});

const loadStockBalances = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const response = await api.get('/inventory/stock-balances', {
      params: {
        page: 0,
        size: 1000,
        sort: 'item.sku,asc'
      }
    });

    stockData.value = (response.data?.content || []).map((balance) => {
      const onHand = Number(balance.onHand || 0);
      const reserved = Number(balance.reserved || 0);
      const allocated = Number(balance.allocated || 0);
      const hold = Number(balance.hold || 0);
      const damaged = Number(balance.damaged || 0);
      const price = Number(balance.item?.estimatedUnitPrice || 0);

      return {
        id: balance.id,
        sku: balance.item?.sku || '-',
        name: balance.item?.name || '-',
        category: balance.item?.category?.name || '-',
        warehouse: balance.warehouse?.name || '-',
        whCode: balance.warehouse?.code || '-',
        uom: balance.item?.uom || '',
        price,
        onHand,
        reserved,
        allocated,
        available: onHand - reserved - allocated - hold - damaged
      };
    });
  } catch (err) {
    loadError.value = err?.message || err?.error || 'Gagal memuat saldo stok dari backend.';
  } finally {
    loading.value = false;
  }
};

const selectedWarehouseLabel = computed(() => {
  if (selectedWarehouse.value === 'ALL') return 'Seluruh Gudang & Cabang (Konsolidasi Total Bank Jatim)';
  return warehouseOptions.value.find((warehouse) => warehouse.code === selectedWarehouse.value)?.name || 'Gudang Terpilih';
});

const warehouseOptions = computed(() => {
  const warehouses = new Map();
  stockData.value.forEach((item) => {
    if (item.whCode && item.whCode !== '-') {
      warehouses.set(item.whCode, item.warehouse);
    }
  });
  return Array.from(warehouses.entries()).map(([code, name]) => ({ code, name }));
});

const categoryOptions = computed(() => {
  return Array.from(new Set(stockData.value.map((item) => item.category).filter((category) => category && category !== '-'))).sort();
});

const filteredStock = computed(() => {
  let list = stockData.value;
  if (selectedWarehouse.value !== 'ALL') {
    list = list.filter(s => s.whCode === selectedWarehouse.value);
  }
  if (categoryFilter.value !== 'ALL') {
    list = list.filter(s => s.category === categoryFilter.value);
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(s => s.sku.toLowerCase().includes(q) || s.name.toLowerCase().includes(q));
  }
  return list;
});

const currentPage = ref(1);
const perPage = ref(10);

const paginatedStock = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredStock.value.slice(start, start + perPage.value);
});

const totalOnHand = computed(() => filteredStock.value.reduce((s, it) => s + it.onHand, 0));
const totalReserved = computed(() => filteredStock.value.reduce((s, it) => s + (it.reserved + it.allocated), 0));
const totalAvailable = computed(() => filteredStock.value.reduce((s, it) => s + it.available, 0));
const totalValuation = computed(() => filteredStock.value.reduce((s, it) => s + (it.onHand * it.price), 0));

const exportExcel = () => {
  const headers = [
    { key: 'sku', label: 'Kode SKU' },
    { key: 'name', label: 'Nama Barang' },
    { key: 'category', label: 'Kategori' },
    { key: 'warehouse', label: 'Gudang' },
    { key: 'onHand', label: 'Stok Fisik' },
    { key: 'reserved', label: 'Reserved' },
    { key: 'allocated', label: 'Allocated' },
    { key: 'price', label: 'Harga Satuan' }
  ];
  exportToCsv('stock_balances_report', headers, filteredStock.value);
};
</script>

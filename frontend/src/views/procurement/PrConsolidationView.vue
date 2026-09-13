<template>
  <div class="pr-consolidation-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Konsolidasi PR ke Purchase Order</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item"><router-link to="/procurement/pr">Pengadaan</router-link></li>
              <li class="breadcrumb-item active" aria-current="page">Konsolidasi PR</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Success PO Generated Alert Card -->
    <div v-if="generatedPo" class="card border-0 shadow-sm mb-3 text-bg-success">
      <div class="card-body p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
        <div class="d-flex align-items-center gap-3">
          <div class="rounded-circle bg-white text-success d-flex align-items-center justify-content-center p-2 flex-shrink-0" style="width: 46px; height: 46px;">
            <i class="bi bi-check-circle-fill fs-3"></i>
          </div>
          <div>
            <h5 class="fw-bold mb-0 text-white">Purchase Order Berhasil Diterbitkan: {{ generatedPo.poNumber }}</h5>
            <p class="mb-0 text-white-50 fs-8">
              Total Nilai PO: <strong>Rp {{ generatedPo.total.toLocaleString('id-ID') }}</strong> (Termasuk PPN 11%) &bull; Estimasi Cost Saving Konsolidasi: <strong>Rp 18.500.000</strong>.
            </p>
          </div>
        </div>
        <div class="d-flex gap-2">
          <router-link to="/procurement/po" class="btn btn-sm btn-light fw-bold text-success shadow-xs">
            <i class="bi bi-file-earmark-ruled me-1"></i> Buka Daftar PO
          </router-link>
          <button type="button" class="btn btn-sm btn-outline-light" @click="generatedPo = null">
            Tutup
          </button>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- 4 AdminLTE 4 Metric Info-Boxes -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-primary shadow-xs">
            <i class="bi bi-layers-half"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">POOL ITEM TERSEDIA</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ poolItems.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-primary" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Tervalidasi &amp; Siap Digabung</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs">
            <i class="bi bi-check-all"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">ITEM TERPILIH</span>
            <span class="info-box-number text-danger fs-4 font-monospace">{{ selectedItems.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-danger" style="width: 65%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Akan Masuk Kontrak PO</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs">
            <i class="bi bi-piggy-bank"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">ESTIMASI COST SAVING</span>
            <span class="info-box-number text-success fs-4 font-monospace">Rp 18.5<span class="fs-7 fw-normal">jt</span></span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Diskon Volume Multi-Cabang</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info text-white shadow-xs">
            <i class="bi bi-wallet2"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL SUBPOKOK</span>
            <span class="info-box-number text-body fs-4 font-monospace">Rp {{ (selectedSubtotal / 1000000).toFixed(1) }}<span class="fs-7 fw-normal">jt</span></span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-info" style="width: 80%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Item Terpilih (Sebelum PPN)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pool Items Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/procurement/pr" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-arrow-left me-1"></i> Kembali ke PR
          </router-link>
          <span class="fw-bold text-body fs-7">
            <i class="bi bi-inboxes text-danger me-1"></i> Item Siap Konsolidasi (Pool PR Approved)
          </span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <span class="fs-8 text-secondary d-none d-sm-inline">Subtotal:</span>
          <strong class="fs-7 font-monospace text-danger me-2">Rp {{ selectedSubtotal.toLocaleString('id-ID') }}</strong>
          <select v-model="selectedVendorId" class="form-select form-select-sm fs-8 w-auto">
            <option :value="null">Pilih Vendor</option>
            <option v-for="vendor in vendorOptions" :key="vendor.id" :value="vendor.id">
              {{ vendor.name }}
            </option>
          </select>
          <select v-model="selectedWarehouseId" class="form-select form-select-sm fs-8 w-auto">
            <option :value="null">Pilih Gudang</option>
            <option v-for="warehouse in warehouseOptions" :key="warehouse.id" :value="warehouse.id">
              {{ warehouse.name }}
            </option>
          </select>
          <button
            class="btn btn-sm btn-danger fw-bold shadow-xs fs-8"
            :disabled="selectedItems.length === 0 || !selectedVendorId || !selectedWarehouseId"
            @click="consolidateToPo"
          >
            <i class="bi bi-check2-square me-1"></i> Terbitkan PO ({{ selectedItems.length }} Item)
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-shop"></i>
              </span>
              <select v-model="filterVendor" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Rekomendasi Vendor</option>
                <option v-for="vendor in vendorOptions" :key="vendor.id" :value="vendor.name">
                  {{ vendor.name }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Unit Pemohon</option>
                <option v-for="branch in branchOptions" :key="branch" :value="branch">
                  {{ branch }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterVendor !== 'ALL' || filterBranch !== 'ALL'">
            <button
              type="button"
              class="btn btn-sm btn-outline-danger fs-8"
              @click="resetFilters"
              title="Reset Filter"
            >
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
                placeholder="Cari No PR, Unit Pemohon, atau Nama Barang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">
                <i class="bi bi-search me-1"></i> Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th width="40" class="text-center">
                <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" class="form-check-input" />
              </th>
              <th>No PR Asal</th>
              <th>Unit Pemohon</th>
              <th>Nama Barang</th>
              <th class="text-center">Kuantitas</th>
              <th class="text-end">Estimasi Satuan</th>
              <th class="text-end">Subtotal</th>
              <th>Rekomendasi Vendor</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="item in paginatedPoolItems" :key="item.id">
              <td class="text-center">
                <input type="checkbox" :value="item.id" v-model="selectedItems" class="form-check-input" />
              </td>
              <td>
                <span class="font-monospace fw-bold text-danger">{{ item.prNumber }}</span>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ item.branch }}</div>
              </td>
              <td>
                <strong class="text-body">{{ item.itemName }}</strong>
              </td>
              <td class="text-center font-monospace">
                <span class="badge text-bg-light border text-body fs-9">{{ item.qty.toLocaleString('id-ID') }} {{ item.uom }}</span>
              </td>
              <td class="text-end font-monospace text-secondary">
                Rp {{ item.unitPrice.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace fw-bold text-body">
                Rp {{ (item.qty * item.unitPrice).toLocaleString('id-ID') }}
              </td>
              <td>
                <span class="badge text-bg-light border fs-9">
                  <i class="bi bi-building me-1 text-primary"></i> {{ item.vendor }}
                </span>
              </td>
            </tr>
            <tr v-if="filteredPoolItems.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada item pool konsolidasi yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPoolItems.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />

      <div class="card-footer bg-body border-top p-3 d-flex flex-column flex-sm-row justify-content-between align-items-center gap-2">
        <div class="fs-8 text-secondary">
          <strong>{{ selectedItems.length }}</strong> dari {{ poolItems.length }} item dipilih untuk digabungkan menjadi PO baru
        </div>
        <button
          class="btn btn-sm btn-danger fw-bold shadow-xs"
          :disabled="selectedItems.length === 0"
          @click="consolidateToPo"
        >
          <i class="bi bi-file-earmark-plus me-1"></i> Terbitkan Purchase Order
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const selectedItems = ref([]);
const selectAll = ref(false);
const generatedPo = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterVendor = ref('ALL');
const filterBranch = ref('ALL');
const errorMessage = ref('');
const selectedVendorId = ref(null);
const selectedWarehouseId = ref(null);
const vendorOptions = ref([]);
const warehouseOptions = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterVendor.value = 'ALL';
  filterBranch.value = 'ALL';
  currentPage.value = 1;
};

const poolItems = ref([]);

const mapPoolItems = (purchaseRequests = []) => purchaseRequests.flatMap(pr =>
  (pr.items || [])
    .filter(line => Number(line.qtyApproved || 0) > Number(line.qtyOrdered || 0))
    .map(line => ({
      id: line.id,
      prNumber: pr.prNumber || '-',
      branch: pr.organization?.name || '-',
      itemName: line.item?.name || '-',
      qty: Number(line.qtyApproved || 0) - Number(line.qtyOrdered || 0),
      uom: line.item?.uom || 'Unit',
      unitPrice: Number(line.estimatedUnitPrice || 0),
      vendor: selectedVendorName.value
    }))
);

const loadOptions = async () => {
  const [vendorResponse, warehouseResponse] = await Promise.all([
    api.get('/master/vendors'),
    api.get('/master/warehouses')
  ]);
  vendorOptions.value = vendorResponse.data || [];
  warehouseOptions.value = warehouseResponse.data || [];
  selectedVendorId.value = vendorOptions.value[0]?.id || null;
  selectedWarehouseId.value = warehouseOptions.value[0]?.id || null;
};

const selectedVendorName = computed(() => {
  return vendorOptions.value.find(vendor => vendor.id === selectedVendorId.value)?.name || '-';
});

const branchOptions = computed(() => [...new Set(poolItems.value.map(item => item.branch).filter(Boolean))]);

const loadPool = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/procurement/consolidation');
    poolItems.value = mapPoolItems(response.data || []);
    selectedItems.value = [];
    selectAll.value = false;
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat pool PR approved dari server.';
    poolItems.value = [];
  }
};

const filteredPoolItems = computed(() => {
  return poolItems.value.filter(item => {
    if (filterVendor.value !== 'ALL' && item.vendor !== filterVendor.value) return false;
    if (filterBranch.value !== 'ALL' && item.branch !== filterBranch.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchPr = item.prNumber.toLowerCase().includes(q);
      const matchBranch = item.branch.toLowerCase().includes(q);
      const matchItem = item.itemName.toLowerCase().includes(q);
      if (!matchPr && !matchBranch && !matchItem) return false;
    }

    return true;
  });
});

const paginatedPoolItems = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPoolItems.value.slice(start, start + perPage.value);
});

const selectedSubtotal = computed(() => {
  return poolItems.value
    .filter(i => selectedItems.value.includes(i.id))
    .reduce((acc, curr) => acc + (curr.qty * curr.unitPrice), 0);
});

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedItems.value = poolItems.value.map(i => i.id);
  } else {
    selectedItems.value = [];
  }
};

const consolidateToPo = async () => {
  errorMessage.value = '';
  try {
    const response = await api.post('/procurement/consolidate', {
      vendorId: selectedVendorId.value,
      warehouseId: selectedWarehouseId.value,
      prItemIds: selectedItems.value
    });
    generatedPo.value = {
      poNumber: response.data?.poNumber || '-',
      total: Number(response.data?.totalAmount || 0)
    };
    await loadPool();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menerbitkan PO dari pool PR.';
  }
};

onMounted(async () => {
  try {
    await loadOptions();
    await loadPool();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat data master konsolidasi.';
  }
});
</script>

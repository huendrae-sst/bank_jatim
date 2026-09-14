<template>
  <div class="packing-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Antrean Packing &amp; Penimbangan Koli</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Pergudangan</li>
              <li class="breadcrumb-item active" aria-current="page">Antrean Packing</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Success Banner -->
    <div v-if="packingSuccessMessage" class="alert alert-success d-flex align-items-center justify-content-between p-3 rounded-3 shadow-xs mb-3">
      <div class="d-flex align-items-center">
        <i class="bi bi-check-circle-fill fs-4 me-2"></i>
        <div class="fs-8 fw-semibold">{{ packingSuccessMessage }}</div>
      </div>
      <router-link to="/distribution/shipments" class="btn btn-sm btn-success fw-bold text-nowrap ms-2">
        Lihat Pengiriman
      </router-link>
    </div>


    <!-- Packing Table Card -->
    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Packing Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'queue' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'queue'"
            >
              Antrean Packing
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'history' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'history'"
            >
              Riwayat Antrean
            </button>
          </li>
        </ul>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-geo-alt"></i>
              </span>
              <select v-model="filterDestination" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Tujuan Cabang</option>
                <option v-for="destination in destinationOptions" :key="destination" :value="destination">
                  {{ destination }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-funnel"></i>
              </span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Status Kemas</option>
                <option value="SIAP PACKING">SIAP PACKING</option>
                <option value="PROSES SEAL">PROSES SEAL</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterDestination !== 'ALL' || filterStatus !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-search"></i>
              </span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No Packing, Order, Tujuan, atau Item..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. Packing Batch</th>
              <th>Nomor Order</th>
              <th>Tujuan Pengiriman</th>
              <th>Item Isi Paket</th>
              <th>Estimasi Berat</th>
              <th class="text-center">Jumlah Koli</th>
              <th class="text-center">Status Kemas</th>
              <th class="text-center pe-3" style="width: 170px;">Aksi Petugas</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="item in paginatedQueue" :key="item.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.packNo }}</td>
              <td class="fw-bold font-monospace">{{ item.orderNumber }}</td>
              <td><span class="fw-semibold text-body">{{ item.destination }}</span></td>
              <td class="fs-8 text-body">{{ item.items }}</td>
              <td class="font-monospace text-body">{{ item.weight }} Kg</td>
              <td class="text-center">
                <span class="badge text-bg-light border font-monospace fs-9">{{ item.koli }} Koli</span>
              </td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="item.status === 'SIAP PACKING' ? 'text-bg-warning' : 'text-bg-info'">{{ item.status }}</span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-danger" @click="completePacking(item)" title="Cetak Label &amp; Seal Koli">
                  <i class="bi bi-box-seam"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredQueue.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada antrean packing yang sesuai filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredQueue.length"
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
const activeTab = ref('queue');
const filterDestination = ref('ALL');
const filterStatus = ref('ALL');
const errorMessage = ref('');

const resetFilters = () => {
  searchQuery.value = '';
  filterDestination.value = 'ALL';
  filterStatus.value = 'ALL';
  currentPage.value = 1;
};

const describeItems = (items = []) => items
  .map(line => {
    const name = line.item?.name || '-';
    const qty = Number(line.qtyPicked || line.qtyApproved || 0).toLocaleString('id-ID');
    const uom = line.item?.uom || 'Unit';
    return `${name} (${qty} ${uom})`;
  })
  .join(', ');

const mapQueue = (item) => ({
  id: item.orderId || item.id,
  packNo: item.packNo || `PCK-KOLI-${item.orderNumber || item.orderId || item.id}`,
  orderNumber: item.orderNumber || '-',
  destination: item.destination || item.branch || '-',
  items: typeof item.items === 'string' ? item.items : describeItems(item.items || []),
  weight: item.weight || '1.50',
  koli: item.koli || 1,
  status: item.status === 'PICKING' ? 'SIAP PACKING' : (item.status || 'SIAP PACKING')
});

const packingQueue = ref([]);

const loadPackingQueue = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/warehouse/packing');
    const list = response.data?.data ?? response.data ?? [];
    if (Array.isArray(list) && list.length > 0) {
      packingQueue.value = list.map(mapQueue);
    }
  } catch (error) {
    packingQueue.value = [];
    console.warn('Failed loading packing queue from backend:', error);
  }
};

const readyPackingCount = computed(() => {
  return packingQueue.value.filter(p => p.status === 'SIAP PACKING').length;
});

const inSealCount = computed(() => {
  return packingQueue.value.filter(p => p.status === 'PROSES SEAL').length;
});

const destinationOptions = computed(() => [...new Set(packingQueue.value.map(item => item.destination).filter(Boolean))]);

const filteredQueue = computed(() => {
  return packingQueue.value.filter(p => {
    const isCompleted = p.status === 'SELESAI' || p.status === 'COMPLETED' || p.status === 'PACKED' || p.status === 'READY_TO_SHIP';
    if (activeTab.value === 'queue' && isCompleted) return false;
    if (activeTab.value === 'history' && !isCompleted) return false;

    if (filterDestination.value !== 'ALL' && !p.destination.includes(filterDestination.value)) return false;
    if (filterStatus.value !== 'ALL' && p.status !== filterStatus.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchPack = p.packNo.toLowerCase().includes(q);
      const matchOrder = p.orderNumber.toLowerCase().includes(q);
      const matchDest = p.destination.toLowerCase().includes(q);
      const matchItems = p.items.toLowerCase().includes(q);
      if (!matchPack && !matchOrder && !matchDest && !matchItems) return false;
    }

    return true;
  });
});

const paginatedQueue = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredQueue.value.slice(start, start + perPage.value);
});

const packingSuccessMessage = ref('');

const completePacking = async (item) => {
  errorMessage.value = '';
  try {
    const response = await api.post(`/warehouse/packing/${item.id}/process`, {
      koliCount: item.koli,
      totalWeightKg: item.weight,
      dimensionsCm: '30x20x15'
    });
    const packingNumber = response.data?.packingNumber || item.packNo;
    packingSuccessMessage.value = `Label barcode koli dan Surat Jalan untuk ${packingNumber} (${item.destination}) berhasil dicetak. Paket siap diserahkan ke Ekspedisi.`;
    await loadPackingQueue();
    setTimeout(() => {
      packingSuccessMessage.value = '';
    }, 6000);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memproses packing ke backend.';
  }
};

onMounted(loadPackingQueue);
</script>

<template>
  <div class="receiving-branch-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Penerimaan Barang Kiriman di Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/receiving" class="text-decoration-none text-body">Penerimaan & QC</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Penerimaan Cabang</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'incoming' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'incoming'"
            >
              Penerimaan Cabang
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'history' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'history'"
            >
              Riwayat Penerimaan
            </button>
          </li>
        </ul>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/receiving/discrepancies" class="btn btn-sm btn-outline-danger fs-8">
            <i class="bi bi-exclamation-triangle me-1"></i> Berita Acara Selisih
          </router-link>
          <router-link to="/receiving/confirmation" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            <i class="bi bi-check2-circle me-1"></i> Form Konfirmasi BAP
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
              <select v-model="filterCourier" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Ekspedisi</option>
                <option v-for="courier in courierOptions" :key="courier" :value="courier">
                  {{ courier }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterCourier">
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
                placeholder="Cari Resi, Pengirim, Kurir..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">No. Surat Jalan / Resi</th>
              <th class="py-2 text-uppercase fs-9">Pengirim</th>
              <th class="py-2 text-uppercase fs-9">Ekspedisi & Kurir</th>
              <th class="py-2 text-uppercase fs-9">Jumlah Koli</th>
              <th class="py-2 text-uppercase fs-9">Status Kirim</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi Petugas Cabang</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedList" :key="item.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.shipmentNo }}</td>
              <td class="fw-semibold text-body">{{ item.origin }}</td>
              <td>{{ item.courier }} <span class="text-secondary font-monospace">({{ item.plateNo }})</span></td>
              <td><span class="badge text-bg-secondary">{{ item.koli }} Koli</span></td>
              <td>
                <span :class="['badge', item.status === 'DELIVERED' ? 'text-bg-success' : 'text-bg-warning']">{{ item.status }}</span>
              </td>
              <td class="text-center pe-3">
                <button v-if="item.status !== 'DELIVERED'" class="btn-action-icon text-success" @click="confirmReceipt(item)" title="Konfirmasi Terima (BAP)">
                  <i class="bi bi-box-arrow-in-down"></i>
                </button>
                <span v-else class="text-success fs-8"><i class="bi bi-check2-circle me-1"></i> Diterima</span>
              </td>
            </tr>
            <tr v-if="filteredList.length === 0">
              <td colspan="6" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data paket inbound yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredList.length"
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
const filterCourier = ref('');
const activeTab = ref('incoming');
const errorMessage = ref('');

const mapInbound = (shipment) => ({
  id: shipment.id,
  shipmentNo: shipment.trackingNumber || shipment.manifestNumber || shipment.deliveryNote || '-',
  origin: shipment.originWarehouse?.name || shipment.origin || 'Gudang Pusat Surabaya (Margomulyo)',
  courier: shipment.courier?.name || shipment.courier || '-',
  plateNo: shipment.serviceType || shipment.vehiclePlate || 'L-9821-X',
  koli: Number(shipment.koliCount || shipment.koli || 1),
  status: shipment.status || 'IN_TRANSIT'
});

const inboundList = ref([]);

const loadInboundShipments = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/distribution/shipments');
    const data = response.data?.content || response.data || [];
    if (Array.isArray(data) && data.length > 0) {
      inboundList.value = data.map(mapInbound);
    }
  } catch (error) {
    inboundList.value = [];
    console.warn('Failed loading inbound shipments from backend:', error);
  }
};

const courierOptions = computed(() => [...new Set(inboundList.value.map(item => item.courier).filter(Boolean))]);

const resetFilters = () => {
  searchQuery.value = '';
  filterCourier.value = '';
  currentPage.value = 1;
};

const filteredList = computed(() => {
  return inboundList.value.filter(item => {
    if (activeTab.value === 'incoming' && (item.status === 'DELIVERED' || item.status === 'RECEIVED')) return false;
    if (activeTab.value === 'history' && (item.status !== 'DELIVERED' && item.status !== 'RECEIVED')) return false;

    const matchQuery = !searchQuery.value ||
      item.shipmentNo.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.origin.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.courier.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchCourier = !filterCourier.value || item.courier.includes(filterCourier.value);
    return matchQuery && matchCourier;
  });
});

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredList.value.slice(start, start + perPage.value);
});

const confirmReceipt = async (item) => {
  if (confirm(`Konfirmasi penerimaan paket ${item.shipmentNo}? BAP elektronik akan diterbitkan.`)) {
    try {
      await api.post(`/receiving/confirm/${item.id}`, {
        podSignature: 'Confirmed from Receiving Branch',
        notes: 'Dikonfirmasi dari halaman penerimaan cabang',
        discrepancies: []
      });
      await loadInboundShipments();
      alert(`Paket ${item.shipmentNo} berhasil diverifikasi. Stok cabang telah bertambah secara otomatis.`);
    } catch (error) {
      errorMessage.value = error?.message || error?.error || 'Gagal mengonfirmasi penerimaan paket.';
    }
  }
};

onMounted(loadInboundShipments);
</script>

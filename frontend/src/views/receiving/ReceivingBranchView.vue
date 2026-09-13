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

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-truck"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Paket Dalam Perjalanan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">{{ inboundList.length }} Paket</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 75%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Menuju KC Surabaya Utama</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-box-seam-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Diterima Bulan Ini</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">38 Paket</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 90%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">BAP Lengkap & Sah</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-boxes"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total Koli Masuk</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">112 Koli</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 80%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Barang Operasional & Warkat</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-exclamation-triangle"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Insiden / Selisih</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">0 Kasus</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 0%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Kondisi Kemasan Utuh</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            <i class="bi bi-box-arrow-in-down text-danger me-2"></i>Paket In-Transit Menuju KC Surabaya Utama
          </h3>
          <span class="badge text-bg-warning fs-9">{{ inboundList.length }} Paket Dalam Perjalanan</span>
        </div>
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
                <span class="badge text-bg-warning">{{ item.status }}</span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-success" @click="confirmReceipt(item)" title="Konfirmasi Terima (BAP)">
                  <i class="bi bi-box-arrow-in-down"></i>
                </button>
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

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterCourier = ref('');
const errorMessage = ref('');

const inboundList = ref([]);

const mapInbound = (shipment) => ({
  id: shipment.id,
  shipmentNo: shipment.trackingNumber || shipment.manifestNumber || '-',
  origin: shipment.originWarehouse?.name || '-',
  courier: shipment.courier?.name || '-',
  plateNo: shipment.serviceType || '-',
  koli: Number(shipment.koliCount || 0),
  status: shipment.status || '-'
});

const loadInboundShipments = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/distribution/shipments');
    inboundList.value = (response.data || [])
      .filter(shipment => ['IN_TRANSIT', 'DISPATCHED'].includes(shipment.status))
      .map(mapInbound);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat paket inbound dari server.';
    inboundList.value = [];
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

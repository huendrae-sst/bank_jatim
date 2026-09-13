<template>
  <div class="shipments-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Manifest Distribusi &amp; Pelacakan Ekspedisi</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Distribusi</li>
              <li class="breadcrumb-item active" aria-current="page">Manifest Pengiriman</li>
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
            <i class="bi bi-send-check"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL MANIFEST</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ shipments.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Semua Surat Jalan</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs">
            <i class="bi bi-truck-flatbed"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">DALAM PERJALANAN</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ inTransitCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-warning" style="width: 65%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Menuju Kantor Cabang</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs">
            <i class="bi bi-check2-circle"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TIBA DI CABANG</span>
            <span class="info-box-number text-success fs-4 font-monospace">{{ deliveredCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">POD Terkonfirmasi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info text-white shadow-xs">
            <i class="bi bi-box-seam"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL KOLI / PAKET</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ totalKoli }} <span class="fs-7 fw-normal">Koli</span></span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-info" style="width: 80%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">{{ totalWeight.toLocaleString('id-ID') }} Kg Total Muatan</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Main Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'transit' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'transit'"
            >
              Dalam Perjalanan
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'delivered' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'delivered'"
            >
              Tiba di Cabang
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/master/expedition-mappings" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-map me-1"></i> Pemetaan Wilayah
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateShipmentModal">
            <i class="bi bi-plus-lg me-1"></i> Terbitkan Manifest Baru
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
              <select v-model="filterDestination" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Unit Cabang</option>
                <option v-for="destination in destinationOptions" :key="destination" :value="destination">
                  {{ destination }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-truck"></i>
              </span>
              <select v-model="filterCourier" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Ekspedisi / Armada</option>
                <option v-for="courier in courierOptions" :key="courier" :value="courier">
                  {{ courier }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterDestination !== 'ALL' || filterCourier !== 'ALL'">
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
                placeholder="Cari Manifest / Resi / Cabang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Nomor Manifest</th>
              <th>Nomor Resi / AWB</th>
              <th>Unit Tujuan</th>
              <th>Ekspedisi & Layanan</th>
              <th class="text-center">Koli & Berat</th>
              <th class="text-center">Status</th>
              <th class="text-center" style="width: 140px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="shipment in paginatedShipments" :key="shipment.id">
              <td>
                <router-link :to="`/distribution/manifest/${shipment.id}/print`" target="_blank" class="font-monospace fw-bold text-danger text-decoration-none">
                  {{ shipment.manifestNumber }}
                </router-link>
              </td>
              <td>
                <span class="font-monospace fw-bold text-body">{{ shipment.trackingNumber }}</span>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ shipment.destination }}</div>
              </td>
              <td>
                <div>{{ shipment.courier }}</div>
                <span class="badge text-bg-light border fs-9">{{ shipment.serviceType }}</span>
              </td>
              <td class="text-center font-monospace">
                <span class="badge text-bg-secondary fs-9">{{ shipment.koli }} Koli</span>
                <span class="fs-8 text-secondary ms-1">({{ shipment.weight }} Kg)</span>
              </td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(shipment.status)">
                  {{ shipment.status ? shipment.status.replace('_', ' ') : 'PENDING' }}
                </span>
              </td>
              <td class="text-center">
                <div class="d-inline-flex align-items-center gap-1">
                  <router-link
                    :to="`/distribution/manifest/${shipment.id}/print`"
                    target="_blank"
                    class="btn-action-icon text-secondary"
                    title="Cetak Surat Jalan / Manifest"
                  >
                    <i class="bi bi-file-earmark-text"></i>
                  </router-link>
                  <router-link
                    :to="`/distribution/label/${shipment.id}/print`"
                    target="_blank"
                    class="btn-action-icon text-danger"
                    title="Cetak Label Pengiriman Barcode"
                  >
                    <i class="bi bi-upc-scan"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredShipments.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data manifest pengiriman yang sesuai dengan kriteria filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredShipments.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Create Shipment Modal -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold text-body">
              Terbitkan Manifest Pengiriman Baru
            </h5>
            <button type="button" class="btn-close-modal" @click="showCreateModal = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="submitCreateShipment">
            <div class="modal-body p-4 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Order Siap Kirim <span class="text-danger">*</span></label>
                  <select v-model="newShipment.orderId" class="form-select fs-7" required>
                    <option :value="null">Pilih order READY_TO_SHIP</option>
                    <option v-for="order in readyOrders" :key="order.id" :value="order.id">
                      {{ order.orderNumber }} - {{ order.requestingOrganization?.name || '-' }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Ekspedisi / Armada <span class="text-danger">*</span></label>
                  <select v-model="newShipment.courierId" class="form-select fs-7" required>
                    <option :value="null">Pilih kurir</option>
                    <option v-for="courier in masterCouriers" :key="courier.id" :value="courier.id">
                      {{ courier.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Layanan</label>
                  <select v-model="newShipment.serviceType" class="form-select fs-7">
                    <option value="INTERNAL">INTERNAL (Kurir Internal)</option>
                    <option value="EXPRESS">EXPRESS (1 Hari Kerja)</option>
                    <option value="REGULER">REGULER (2-3 Hari Kerja)</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Resi / AWB</label>
                  <input
                    type="text"
                    v-model="newShipment.trackingNumber"
                    class="form-control fs-7 font-monospace"
                    placeholder="Contoh: AWB-BJ-2026-0105"
                  />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Koli (Box) <span class="text-danger">*</span></label>
                  <input
                    type="number"
                    v-model.number="newShipment.koli"
                    class="form-control fs-7"
                    min="1"
                    required
                  />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Total Berat (Kg)</label>
                  <input
                    type="number"
                    v-model.number="newShipment.weight"
                    class="form-control fs-7"
                    step="0.5"
                    min="0.1"
                  />
                </div>
              </div>
            </div>
            <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="showCreateModal = false">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs">
                <i class="bi bi-file-earmark-check me-1"></i> Terbitkan Manifest & Surat Jalan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const searchQuery = ref('');
const activeTab = ref('all');
const filterDestination = ref('ALL');
const filterCourier = ref('ALL');
const showCreateModal = ref(false);
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');
const readyOrders = ref([]);
const masterCouriers = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterDestination.value = 'ALL';
  filterCourier.value = 'ALL';
  currentPage.value = 1;
};

const shipments = ref([]);

const newShipment = ref({
  orderId: null,
  courierId: null,
  serviceType: 'REGULER',
  trackingNumber: '',
  koli: 1,
  weight: 5.0
});

const mapShipment = (shipment) => ({
  id: shipment.id,
  manifestNumber: shipment.manifestNumber || '-',
  trackingNumber: shipment.trackingNumber || '-',
  destination: shipment.destinationOrganization?.name || '-',
  courier: shipment.courier?.name || '-',
  serviceType: shipment.serviceType || '-',
  koli: Number(shipment.koliCount || 0),
  weight: Number(shipment.totalWeightKg || 0),
  status: shipment.status || '-'
});

const loadShipments = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/distribution/shipments');
    shipments.value = (response.data || []).map(mapShipment);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat manifest pengiriman dari server.';
    shipments.value = [];
  }
};

const loadCreateOptions = async () => {
  const [ordersResponse, couriersResponse] = await Promise.all([
    api.get('/orders', { params: { page: 0, size: 200 } }),
    api.get('/master/couriers')
  ]);
  readyOrders.value = (ordersResponse.data?.content || []).filter(order => order.status === 'READY_TO_SHIP');
  masterCouriers.value = couriersResponse.data || [];
};

const inTransitCount = computed(() => {
  return shipments.value.filter(s => s.status === 'IN_TRANSIT').length;
});

const deliveredCount = computed(() => {
  return shipments.value.filter(s => s.status === 'DELIVERED').length;
});

const destinationOptions = computed(() => [...new Set(shipments.value.map(s => s.destination).filter(Boolean))]);
const courierOptions = computed(() => [...new Set(shipments.value.map(s => s.courier).filter(Boolean))]);
const totalKoli = computed(() => shipments.value.reduce((sum, shipment) => sum + shipment.koli, 0));
const totalWeight = computed(() => shipments.value.reduce((sum, shipment) => sum + shipment.weight, 0));

const filteredShipments = computed(() => {
  return shipments.value.filter(s => {
    if (activeTab.value === 'transit' && s.status !== 'IN_TRANSIT') return false;
    if (activeTab.value === 'delivered' && s.status !== 'DELIVERED') return false;

    if (filterDestination.value !== 'ALL' && s.destination !== filterDestination.value) return false;
    if (filterCourier.value !== 'ALL' && s.courier !== filterCourier.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchMan = s.manifestNumber.toLowerCase().includes(q);
      const matchTrk = s.trackingNumber.toLowerCase().includes(q);
      const matchDest = s.destination.toLowerCase().includes(q);
      if (!matchMan && !matchTrk && !matchDest) return false;
    }
    return true;
  });
});

const paginatedShipments = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredShipments.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  switch (status) {
    case 'DELIVERED': return 'text-bg-success';
    case 'IN_TRANSIT': return 'text-bg-info';
    case 'PENDING': return 'text-bg-warning';
    default: return 'text-bg-secondary';
  }
};

const openCreateShipmentModal = () => {
  newShipment.value = { orderId: null, courierId: null, serviceType: 'REGULER', trackingNumber: '', koli: 1, weight: 5.0 };
  loadCreateOptions();
  showCreateModal.value = true;
};

const submitCreateShipment = async () => {
  errorMessage.value = '';
  try {
    await api.post('/distribution/shipments', {
      orderId: newShipment.value.orderId,
      courierId: newShipment.value.courierId,
      serviceType: newShipment.value.serviceType,
      trackingNumber: newShipment.value.trackingNumber,
      shippingCost: 0
    });
    await loadShipments();
    showCreateModal.value = false;
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menerbitkan manifest pengiriman.';
  }
};

onMounted(loadShipments);
</script>

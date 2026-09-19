<template>
  <div class="shipments-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Manifest Distribusi &amp; Pelacakan Ekspedisi</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/distribution/routine" class="btn btn-sm btn-outline-danger">
          Distribusi Rutin
        </router-link>
        <button class="btn btn-sm btn-outline-danger fw-semibold" @click="openBulkManifestModal">
          Terbitkan Batch Manifest (Bulk)
        </button>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateShipmentModal">
          Tambah
        </button>
      </div>
    </div>

    <!-- 4 KPI Cards -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-danger text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-truck fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Total Manifest Terbit</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ shipments.length }} Surat Jalan</div>
            <div class="fs-9 text-secondary">{{ totalKoli }} Koli ({{ totalWeight }} Kg)</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-send-check fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Dalam Perjalanan</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ inTransitCount }} Manifest</div>
            <div class="fs-9 text-secondary">In Transit ke Cabang</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-check2-all fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Sampai di Cabang</div>
            <div class="fs-4 fw-bold font-monospace text-success">{{ deliveredCount }} Manifest</div>
            <div class="fs-9 text-secondary">POD Delivered</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-primary text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-box-seam-fill fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Siap Kirim (Packed)</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ readyOrders.length }} Order</div>
            <div class="fs-9 text-secondary">Menunggu Manifest Terbit</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>
    <div v-if="successMessage" class="alert alert-success fs-8">{{ successMessage }}</div>

    <!-- Main Table Card -->
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
              Antrean Pengiriman (In Transit)
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'delivered' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'delivered'"
            >
              Riwayat Pengiriman (Delivered)
            </button>
          </li>
        </ul>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tag"></i></span>
              <select v-model="filterDistType" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Jenis Distribusi</option>
                <option value="ORDER_REQUEST">Order Permintaan</option>
                <option value="PURCHASE_REQUEST">Order Pembelian (PR)</option>
                <option value="EMBOSS_ORDER">Order Emboss Kartu</option>
                <option value="ROUTINE_PUSH">Distribusi Rutin</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-geo-alt"></i></span>
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
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-truck"></i></span>
              <select v-model="filterCourier" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Ekspedisi / Armada</option>
                <option v-for="courier in courierOptions" :key="courier" :value="courier">{{ courier }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Manifest / Resi / Batch..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8" type="button">Cari</button>
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
              <th>Tipe Distribusi</th>
              <th>Ekspedisi &amp; Layanan</th>
              <th class="text-center">Koli &amp; Berat</th>
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
                <div v-if="shipment.batchManifestNumber" class="fs-9 text-secondary font-monospace">
                 {{ shipment.batchManifestNumber }}
                </div>
              </td>
              <td>
                <span class="font-monospace fw-bold text-body">{{ shipment.trackingNumber }}</span>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ shipment.destination }}</div>
              </td>
              <td>
                <span class="badge fs-9" :class="distTypeBadgeClass(shipment.distributionType)">
                  {{ distTypeLabel(shipment.distributionType) }}
                </span>
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
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data manifest pengiriman yang sesuai dengan kriteria filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationFooter
        :total="filteredShipments.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal 1: Create Single Shipment Modal -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold text-body">
              Terbitkan Manifest Pengiriman Satuan
            </h5>
            <button type="button" class="btn-close" @click="showCreateModal = false"></button>
          </div>
          <form @submit.prevent="submitCreateShipment">
            <div class="modal-body p-4 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Order Siap Kirim (Packed) <span class="text-danger">*</span></label>
                  <select v-model="newShipment.orderId" class="form-select fs-7" required>
                    <option :value="null">Pilih order READY_TO_SHIP</option>
                    <option v-for="order in readyOrders" :key="order.id" :value="order.id">
                      {{ order.orderNumber }} - {{ order.requestingOrganization?.name || '-' }} ({{ distTypeLabel(order.orderType) }})
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
                    <option value="INTERNAL">INTERNAL (Kurir Internal Bank Jatim)</option>
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
                    placeholder="Otomatis digenerate bila dikosongkan"
                  />
                </div>
              </div>
            </div>
            <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="showCreateModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs">
                Terbitkan Manifest &amp; Surat Jalan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal 2: Create Batch Manifest Modal (Bulk Multi-Cabang) -->
    <div v-if="showBulkModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold text-body">
              Terbitkan Batch Manifest Pengiriman (Multi-Cabang Bulk)
            </h5>
            <button type="button" class="btn-close" @click="showBulkModal = false"></button>
          </div>
          <form @submit.prevent="submitBulkShipment">
            <div class="modal-body p-4 space-y-4">
              <div class="alert alert-info fs-8 py-2 px-3">
                <i class="bi bi-info-circle-fill me-1"></i>
                Pilih beberapa order yang sudah selesai dikemas (READY_TO_SHIP). Sistem akan menerbitkan Surat Jalan dan Resi untuk seluruh cabang tujuan dalam 1 Batch Manifest pengiriman ekspedisi.
              </div>

              <!-- Pilihan Ekspedisi Batch -->
              <div class="row g-3">
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Ekspedisi / Mitra Kurir <span class="text-danger">*</span></label>
                  <select v-model="bulkForm.courierId" class="form-select fs-7" required>
                    <option :value="null">Pilih Kurir</option>
                    <option v-for="courier in masterCouriers" :key="courier.id" :value="courier.id">
                      {{ courier.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Layanan</label>
                  <select v-model="bulkForm.serviceType" class="form-select fs-7">
                    <option value="REGULER">REGULER (Kargo Logistik)</option>
                    <option value="EXPRESS">EXPRESS (1 Hari)</option>
                    <option value="INTERNAL">INTERNAL (Kurir Kantor Pusat)</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Batch Manifest</label>
                  <input
                    type="text"
                    v-model="bulkForm.batchManifestNumber"
                    class="form-control fs-7 font-monospace"
                    placeholder="Contoh: BATCH-KORWIL-SBY-01"
                  />
                </div>
              </div>

              <!-- Tabel Daftar Order Ready to Ship -->
              <div class="border rounded-3 p-3 bg-body-tertiary">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="fs-8 fw-bold text-secondary text-uppercase">Pilih Order Siap Kirim ({{ selectedBulkOrderIds.length }} Terpilih):</span>
                  <div class="d-flex gap-2">
                    <button type="button" class="btn btn-xs btn-outline-danger" @click="selectAllBulkOrders">Pilih Semua</button>
                    <button type="button" class="btn btn-xs btn-outline-secondary" @click="selectedBulkOrderIds = []">Batal</button>
                  </div>
                </div>
                <div class="table-responsive" style="max-height: 250px; overflow-y: auto;">
                  <table class="table table-sm table-hover bg-body mb-0 align-middle">
                    <thead class="table-light fs-8 text-secondary">
                      <tr>
                        <th style="width: 40px;" class="text-center">#</th>
                        <th>Nomor Order</th>
                        <th>Cabang Tujuan</th>
                        <th>Tipe Order</th>
                        <th class="text-center">Total Item</th>
                      </tr>
                    </thead>
                    <tbody class="fs-8">
                      <tr v-for="order in readyOrders" :key="order.id">
                        <td class="text-center">
                          <input
                            type="checkbox"
                            class="form-check-input mt-0"
                            :value="order.id"
                            v-model="selectedBulkOrderIds"
                          />
                        </td>
                        <td class="font-monospace fw-bold text-danger">{{ order.orderNumber }}</td>
                        <td class="fw-semibold text-body">{{ order.requestingOrganization?.name || '-' }}</td>
                        <td>
                          <span class="badge fs-9" :class="distTypeBadgeClass(order.orderType)">
                            {{ distTypeLabel(order.orderType) }}
                          </span>
                        </td>
                        <td class="text-center font-monospace">{{ order.totalItems }} Pcs</td>
                      </tr>
                      <tr v-if="readyOrders.length === 0">
                        <td colspan="5" class="text-center py-4 text-secondary">
                          Tidak ada order dengan status READY_TO_SHIP. Silakan selesaikan proses Packing terlebih dahulu di modul Pergudangan.
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="showBulkModal = false">Batal</button>
              <button
                type="submit"
                class="btn btn-sm btn-danger fw-bold shadow-xs"
                :disabled="submittingBulk || selectedBulkOrderIds.length === 0"
              >
                <span v-if="submittingBulk" class="spinner-border spinner-border-sm me-1"></span>
                Terbitkan Batch Manifest untuk {{ selectedBulkOrderIds.length }} Cabang
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
import PaginationFooter from '@/components/PaginationFooter.vue';

const searchQuery = ref('');
const activeTab = ref('queue');
const filterDestination = ref('ALL');
const filterCourier = ref('ALL');
const filterDistType = ref('ALL');
const showCreateModal = ref(false);
const showBulkModal = ref(false);
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');
const successMessage = ref('');
const readyOrders = ref([]);
const masterCouriers = ref([]);
const selectedBulkOrderIds = ref([]);
const submittingBulk = ref(false);

const bulkForm = ref({
  courierId: null,
  serviceType: 'REGULER',
  batchManifestNumber: '',
  trackingNumberPrefix: 'AWB-BATCH'
});

const mapShipment = (shipment) => ({
  id: shipment.id,
  manifestNumber: shipment.manifestNumber || '-',
  batchManifestNumber: shipment.batchManifestNumber || '',
  distributionType: shipment.distributionType || shipment.order?.orderType || 'ORDER_REQUEST',
  trackingNumber: shipment.trackingNumber || '-',
  destination: shipment.destinationOrganization?.name || shipment.destination || '-',
  courier: shipment.courier?.name || shipment.courier || '-',
  serviceType: shipment.serviceType || '-',
  koli: Number(shipment.koliCount || shipment.koli || 0),
  weight: Number(shipment.totalWeightKg || shipment.weight || 0),
  status: shipment.status || '-'
});

const shipments = ref([]);

const newShipment = ref({
  orderId: null,
  courierId: null,
  serviceType: 'REGULER',
  trackingNumber: '',
  koli: 1,
  weight: 5.0
});

const loadShipments = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/distribution/shipments');
    const data = response.data?.content || response.data?.data || response.data || [];
    if (Array.isArray(data)) {
      shipments.value = data.map(mapShipment);
    }
  } catch (error) {
    shipments.value = [];
    console.warn('Failed loading shipments from backend:', error);
  }
};

const loadCreateOptions = async () => {
  try {
    const [ordersResponse, couriersResponse] = await Promise.all([
      api.get('/orders', { params: { page: 0, size: 200 } }),
      api.get('/master/couriers')
    ]);
    const ordList = ordersResponse.data?.content || ordersResponse.data?.data || ordersResponse.data || [];
    readyOrders.value = (Array.isArray(ordList) ? ordList : []).filter(order => order.status === 'READY_TO_SHIP');
    
    const courList = couriersResponse.data?.data || couriersResponse.data || [];
    masterCouriers.value = Array.isArray(courList) ? courList : [];
    if (masterCouriers.value.length > 0 && !bulkForm.value.courierId) {
      bulkForm.value.courierId = masterCouriers.value[0].id;
    }
  } catch (err) {
    console.error('Failed to load options:', err);
  }
};

const inTransitCount = computed(() => {
  return shipments.value.filter(s => s.status === 'IN_TRANSIT' || s.status === 'DISPATCHED').length;
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
    if (activeTab.value === 'queue' && s.status === 'DELIVERED') return false;
    if (activeTab.value === 'delivered' && s.status !== 'DELIVERED') return false;

    if (filterDistType.value !== 'ALL' && s.distributionType !== filterDistType.value) return false;
    if (filterDestination.value !== 'ALL' && s.destination !== filterDestination.value) return false;
    if (filterCourier.value !== 'ALL' && s.courier !== filterCourier.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchMan = s.manifestNumber.toLowerCase().includes(q);
      const matchTrk = s.trackingNumber.toLowerCase().includes(q);
      const matchDest = s.destination.toLowerCase().includes(q);
      const matchBatch = s.batchManifestNumber ? s.batchManifestNumber.toLowerCase().includes(q) : false;
      if (!matchMan && !matchTrk && !matchDest && !matchBatch) return false;
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
    case 'IN_TRANSIT':
    case 'DISPATCHED': return 'text-bg-info';
    case 'PENDING': return 'text-bg-warning';
    default: return 'text-bg-secondary';
  }
};

const distTypeLabel = (type) => {
  switch (type) {
    case 'PURCHASE_REQUEST': return 'Pembelian PR';
    case 'EMBOSS_ORDER': return 'Order Emboss';
    case 'ROUTINE_PUSH': return 'Distribusi Rutin';
    default: return 'Permintaan';
  }
};

const distTypeBadgeClass = (type) => {
  switch (type) {
    case 'PURCHASE_REQUEST': return 'bg-primary text-white';
    case 'EMBOSS_ORDER': return 'bg-info text-dark';
    case 'ROUTINE_PUSH': return 'bg-danger text-white';
    default: return 'bg-secondary text-white';
  }
};

const openCreateShipmentModal = () => {
  newShipment.value = { orderId: null, courierId: null, serviceType: 'REGULER', trackingNumber: '', koli: 1, weight: 5.0 };
  loadCreateOptions();
  showCreateModal.value = true;
};

const openBulkManifestModal = () => {
  bulkForm.value = {
    courierId: masterCouriers.value[0]?.id || null,
    serviceType: 'REGULER',
    batchManifestNumber: 'BATCH-MAN-' + Date.now().toString().slice(-6),
    trackingNumberPrefix: 'AWB-BJ'
  };
  selectedBulkOrderIds.value = readyOrders.value.map(o => o.id);
  loadCreateOptions();
  showBulkModal.value = true;
};

const selectAllBulkOrders = () => {
  selectedBulkOrderIds.value = readyOrders.value.map(o => o.id);
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
    successMessage.value = 'Manifest pengiriman satuan berhasil diterbitkan!';
    await loadShipments();
    await loadCreateOptions();
    showCreateModal.value = false;
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || 'Gagal menerbitkan manifest pengiriman.';
  }
};

const submitBulkShipment = async () => {
  if (selectedBulkOrderIds.value.length === 0) {
    alert('Pilih minimal 1 order untuk diterbitkan manifest');
    return;
  }
  submittingBulk.value = true;
  try {
    const payload = {
      orderIds: selectedBulkOrderIds.value,
      courierId: bulkForm.value.courierId,
      serviceType: bulkForm.value.serviceType,
      batchManifestNumber: bulkForm.value.batchManifestNumber,
      trackingNumberPrefix: bulkForm.value.trackingNumberPrefix,
      shippingCostPerOrder: 0
    };
    const res = await api.post('/distribution/shipments/bulk', payload);
    const created = res.data?.data || [];
    successMessage.value = `Berhasil menerbitkan Batch Manifest (${bulkForm.value.batchManifestNumber}) untuk ${created.length} cabang tujuan!`;
    showBulkModal.value = false;
    await loadShipments();
    await loadCreateOptions();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || 'Gagal menerbitkan batch manifest pengiriman.';
  } finally {
    submittingBulk.value = false;
  }
};

onMounted(() => {
  loadShipments();
  loadCreateOptions();
});
</script>

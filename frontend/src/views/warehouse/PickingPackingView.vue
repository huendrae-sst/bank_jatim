<template>
  <div class="picking-packing-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Operasional Gudang: Picking &amp; Packing</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/distribution/routine" class="btn btn-sm btn-outline-danger">
          Distribusi Rutin
        </router-link>
        <router-link to="/distribution/shipments" class="btn btn-sm btn-danger fw-bold shadow-xs">
          Manifest Pengiriman
        </router-link>
      </div>
    </div>

    <!-- 4 KPI Info Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-warning text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-hourglass-split fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Antrean Picking</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ pickingQueue.length }} Order</div>
            <div class="fs-9 text-secondary">Menunggu Ambil di Rak</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-primary text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-boxes fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Antrean Packing</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ packingQueue.length }} Order</div>
            <div class="fs-9 text-secondary">Siap Masuk Koli / Boks</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-check2-circle fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Tipe Order Terlayani</div>
            <div class="fs-4 fw-bold font-monospace text-success">4 Jalur</div>
            <div class="fs-9 text-secondary">Permintaan, PR, Emboss, Rutin</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-speedometer fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Mode Operasional</div>
            <div class="fs-4 fw-bold font-monospace text-body">Bulk Ready</div>
            <div class="fs-9 text-secondary">Batch Wave Picking &amp; Packing</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Category Filter Tabs Bar -->
    <div class="card shadow-xs border-0 p-2 mb-3 bg-body">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div class="d-flex flex-wrap align-items-center gap-1">
          <span class="fs-8 fw-bold text-secondary me-2 text-uppercase">Filter Kategori:</span>
          <button
            type="button"
            class="btn btn-sm py-1 px-3 fw-semibold rounded-pill"
            :class="selectedType === 'ALL' ? 'btn-danger text-white' : 'btn-outline-secondary'"
            @click="setTypeFilter('ALL')"
          >
            Semua ({{ pickingQueueAll.length + packingQueueAll.length }})
          </button>
          <button
            type="button"
            class="btn btn-sm py-1 px-3 fw-semibold rounded-pill"
            :class="selectedType === 'INTERNAL_REQUEST' ? 'btn-danger text-white' : 'btn-outline-secondary'"
            @click="setTypeFilter('INTERNAL_REQUEST')"
          >
            Order Permintaan
          </button>
          <button
            type="button"
            class="btn btn-sm py-1 px-3 fw-semibold rounded-pill"
            :class="selectedType === 'PURCHASE_REQUEST' ? 'btn-danger text-white' : 'btn-outline-secondary'"
            @click="setTypeFilter('PURCHASE_REQUEST')"
          >
            Order Pembelian (PR)
          </button>
          <button
            type="button"
            class="btn btn-sm py-1 px-3 fw-semibold rounded-pill"
            :class="selectedType === 'EMBOSS_ORDER' ? 'btn-danger text-white' : 'btn-outline-secondary'"
            @click="setTypeFilter('EMBOSS_ORDER')"
          >
            Order Emboss Kartu
          </button>
          <button
            type="button"
            class="btn btn-sm py-1 px-3 fw-semibold rounded-pill"
            :class="selectedType === 'ROUTINE_PUSH' ? 'btn-danger text-white' : 'btn-outline-secondary'"
            @click="setTypeFilter('ROUTINE_PUSH')"
          >
            Distribusi Rutin
          </button>
        </div>
        <div class="text-secondary fs-8">
          <i class="bi bi-info-circle me-1"></i> Mendukung pemrosesan per order atau massal (bulk)
        </div>
      </div>
    </div>

    <!-- Dual Column Kanban Board -->
    <div class="row g-3">
      <!-- Col 1: Picking Queue -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-warning shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
            <div>
              <h3 class="card-title fs-6 fw-bold mb-0 text-body">
                Antrean Picking (Ambil Barang di Rak)
              </h3>
              <div class="fs-9 text-secondary">{{ pickingQueue.length }} order siap diambil</div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button
                v-if="pickingQueue.length > 0"
                type="button"
                class="btn btn-xs btn-outline-secondary"
                @click="toggleSelectAllPicking"
              >
                {{ selectedPickingIds.length === pickingQueue.length ? 'Batal Pilih' : 'Pilih Semua' }}
              </button>
              <button
                v-if="selectedPickingIds.length > 0"
                type="button"
                class="btn btn-xs btn-warning fw-bold text-dark shadow-xs"
                @click="processBatchPicking"
              >
                Batch Picking ({{ selectedPickingIds.length }})
              </button>
            </div>
          </div>
          <div class="card-body p-3 space-y-3">
            <div
              v-for="order in pickingQueue"
              :key="order.id"
              class="card border shadow-xs p-3 bg-body"
              :class="{ 'border-warning bg-warning-subtle': selectedPickingIds.includes(order.id) }"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <div class="d-flex align-items-center gap-2">
                  <input
                    type="checkbox"
                    class="form-check-input mt-0"
                    :value="order.id"
                    v-model="selectedPickingIds"
                  />
                  <div>
                    <router-link :to="`/orders/${order.id}`" class="font-monospace fw-bold text-danger fs-7 text-decoration-none">
                      {{ order.orderNumber }}
                    </router-link>
                    <div class="fs-9 text-secondary">Tujuan: <strong class="text-body">{{ order.branch }}</strong></div>
                  </div>
                </div>
                <div class="d-flex flex-column align-items-end gap-1">
                  <span class="badge fs-9" :class="orderTypeBadgeClass(order.orderType)">
                    {{ orderTypeLabel(order.orderType) }}
                  </span>
                  <span class="badge text-bg-secondary fs-9">Menunggu Rak</span>
                </div>
              </div>
              <p class="fs-8 text-secondary mb-3">
                <i class="bi bi-list-check me-1"></i> {{ order.itemsSummary }}
              </p>
              <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                <span class="fs-9 text-secondary font-monospace">Lokasi: RAK-LOGISTIK-PUSAT</span>
                <button
                  class="btn btn-sm btn-warning fw-bold shadow-xs py-1 px-2.5 text-dark"
                  @click="processPicking(order)"
                >
                  Selesaikan Picking
                </button>
              </div>
            </div>

            <div v-if="pickingQueue.length === 0" class="text-center py-5 text-secondary">
              <i class="bi bi-check-circle fs-1 d-block mb-2 text-success"></i>
              Tidak ada antrean picking tersisa untuk kategori ini.
            </div>
          </div>
        </div>
      </div>

      <!-- Col 2: Packing Queue -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-success shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
            <div>
              <h3 class="card-title fs-6 fw-bold mb-0 text-body">
                Antrean Packing (Pengemasan Koli)
              </h3>
              <div class="fs-9 text-secondary">{{ packingQueue.length }} order siap dikemas</div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button
                v-if="packingQueue.length > 0"
                type="button"
                class="btn btn-xs btn-outline-secondary"
                @click="toggleSelectAllPacking"
              >
                {{ selectedPackingIds.length === packingQueue.length ? 'Batal Pilih' : 'Pilih Semua' }}
              </button>
              <button
                v-if="selectedPackingIds.length > 0"
                type="button"
                class="btn btn-xs btn-success fw-bold text-white shadow-xs"
                @click="processBatchPacking"
              >
                Batch Packing ({{ selectedPackingIds.length }})
              </button>
            </div>
          </div>
          <div class="card-body p-3 space-y-3">
            <div
              v-for="order in packingQueue"
              :key="order.id"
              class="card border shadow-xs p-3 bg-body"
              :class="{ 'border-success bg-success-subtle': selectedPackingIds.includes(order.id) }"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <div class="d-flex align-items-center gap-2">
                  <input
                    type="checkbox"
                    class="form-check-input mt-0"
                    :value="order.id"
                    v-model="selectedPackingIds"
                  />
                  <div>
                    <router-link :to="`/orders/${order.id}`" class="font-monospace fw-bold text-danger fs-7 text-decoration-none">
                      {{ order.orderNumber }}
                    </router-link>
                    <div class="fs-9 text-secondary">Tujuan: <strong class="text-body">{{ order.branch }}</strong></div>
                  </div>
                </div>
                <div class="d-flex flex-column align-items-end gap-1">
                  <span class="badge fs-9" :class="orderTypeBadgeClass(order.orderType)">
                    {{ orderTypeLabel(order.orderType) }}
                  </span>
                  <span class="badge text-bg-success fs-9">Picked</span>
                </div>
              </div>
              <p class="fs-8 text-secondary mb-2">
                <i class="bi bi-list-check me-1"></i> {{ order.itemsSummary }}
              </p>
              
              <!-- Input Koli & Berat -->
              <div class="row g-2 p-2 bg-body-tertiary rounded-2 mb-3 border">
                <div class="col-6">
                  <label class="form-label fs-9 fw-bold text-secondary text-uppercase mb-0.5">Jumlah Koli (Box):</label>
                  <input
                    type="number"
                    v-model.number="order.koli"
                    class="form-control form-control-sm fs-8 font-monospace"
                    min="1"
                  />
                </div>
                <div class="col-6">
                  <label class="form-label fs-9 fw-bold text-secondary text-uppercase mb-0.5">Total Berat (Kg):</label>
                  <input
                    type="number"
                    v-model.number="order.weight"
                    class="form-control form-control-sm fs-8 font-monospace"
                    step="0.5"
                    min="0.1"
                  />
                </div>
              </div>

              <div class="d-flex justify-content-end">
                <button
                  class="btn btn-sm btn-danger fw-bold shadow-xs py-1 px-2.5"
                  @click="processPacking(order)"
                >
                  Selesaikan Packing &amp; Label Barcode
                </button>
              </div>
            </div>

            <div v-if="packingQueue.length === 0" class="text-center py-5 text-secondary">
              <i class="bi bi-box2-heart fs-1 d-block mb-2 text-muted"></i>
              Tidak ada antrean packing saat ini untuk kategori ini.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { toast } from '@/utils/toast';

const selectedType = ref('ALL');

const pickingQueueAll = ref([]);
const packingQueueAll = ref([]);
const selectedPickingIds = ref([]);
const selectedPackingIds = ref([]);

const describeItems = (items = []) => items
  .map(line => {
    const name = line.item?.name || '-';
    const qty = Number(line.qtyPicked || line.qtyApproved || 0).toLocaleString('id-ID');
    const uom = line.item?.uom || 'Unit';
    return `${qty} ${uom} ${name}`;
  })
  .join(', ');

const mapQueue = (order) => ({
  id: order.orderId || order.id,
  orderNumber: order.orderNumber || '-',
  orderType: order.orderType || 'INTERNAL_REQUEST',
  fulfillmentStatus: order.fulfillmentStatus || 'UNFULFILLED',
  branch: order.branch || order.destination || order.requestingOrganization?.name || '-',
  itemsSummary: typeof order.items === 'string' ? order.items : (order.itemDescription || describeItems(order.items || [])),
  koli: order.koli || 1,
  weight: order.weight ? Number(order.weight) : 2.5
});

const loadQueues = async () => {
  try {
    const [pickingRes, packingRes] = await Promise.allSettled([
      api.get('/warehouse/picking'),
      api.get('/warehouse/packing')
    ]);

    if (pickingRes.status === 'fulfilled') {
      const list = pickingRes.value.data?.data || pickingRes.value.data || [];
      pickingQueueAll.value = Array.isArray(list) ? list.map(mapQueue) : [];
    }
    if (packingRes.status === 'fulfilled') {
      const list = packingRes.value.data?.data || packingRes.value.data || [];
      packingQueueAll.value = Array.isArray(list) ? list.map(mapQueue) : [];
    }
  } catch (err) {
    console.error('Failed to load queues:', err);
  }
};

const setTypeFilter = (type) => {
  selectedType.value = type;
  selectedPickingIds.value = [];
  selectedPackingIds.value = [];
};

const pickingQueue = computed(() => {
  if (selectedType.value === 'ALL') return pickingQueueAll.value;
  return pickingQueueAll.value.filter(o => o.orderType === selectedType.value);
});

const packingQueue = computed(() => {
  if (selectedType.value === 'ALL') return packingQueueAll.value;
  return packingQueueAll.value.filter(o => o.orderType === selectedType.value);
});

const toggleSelectAllPicking = () => {
  if (selectedPickingIds.value.length === pickingQueue.value.length) {
    selectedPickingIds.value = [];
  } else {
    selectedPickingIds.value = pickingQueue.value.map(o => o.id);
  }
};

const toggleSelectAllPacking = () => {
  if (selectedPackingIds.value.length === packingQueue.value.length) {
    selectedPackingIds.value = [];
  } else {
    selectedPackingIds.value = packingQueue.value.map(o => o.id);
  }
};

const processPicking = async (order) => {
  try {
    await api.post(`/warehouse/picking/${order.id}/process`);
    toast.success(`Order ${order.orderNumber} berhasil di-picking dan berpindah ke antrean Packing.`);
    await loadQueues();
  } catch (err) {
    toast.error(err?.response?.data?.message || err?.message || 'Gagal memproses picking');
  }
};

const processBatchPicking = async () => {
  if (selectedPickingIds.value.length === 0) return;
  try {
    await api.post('/warehouse/picking/batch-process', { orderIds: selectedPickingIds.value });
    toast.success(`Berhasil menyelesaikan Picking secara massal untuk ${selectedPickingIds.value.length} order!`);
    selectedPickingIds.value = [];
    await loadQueues();
  } catch (err) {
    toast.error(err?.response?.data?.message || err?.message || 'Gagal memproses batch picking');
  }
};

const processPacking = async (order) => {
  try {
    await api.post(`/warehouse/packing/${order.id}/process`, {
      koliCount: order.koli,
      totalWeightKg: order.weight,
      dimensionsCm: '30x20x15'
    });
    toast.success(`Order ${order.orderNumber} selesai dikemas (${order.koli} Koli, ${order.weight} Kg) dan siap diterbitkan Manifest Ekspedisi.`);
    await loadQueues();
  } catch (err) {
    toast.error(err?.response?.data?.message || err?.message || 'Gagal memproses packing');
  }
};

const processBatchPacking = async () => {
  if (selectedPackingIds.value.length === 0) return;
  const itemsToPack = packingQueue.value
    .filter(o => selectedPackingIds.value.includes(o.id))
    .map(o => ({
      orderId: o.id,
      koliCount: o.koli || 1,
      totalWeightKg: o.weight || 2.5,
      dimensionsCm: '30x20x15'
    }));

  try {
    await api.post('/warehouse/packing/batch-process', { items: itemsToPack });
    toast.success(`Berhasil menyelesaikan Packing massal untuk ${itemsToPack.length} order! Siap terbit Manifest.`);
    selectedPackingIds.value = [];
    await loadQueues();
  } catch (err) {
    toast.error(err?.response?.data?.message || err?.message || 'Gagal memproses batch packing');
  }
};

const orderTypeLabel = (type) => {
  switch (type) {
    case 'PURCHASE_REQUEST': return 'Pembelian PR';
    case 'EMBOSS_ORDER': return 'Order Emboss';
    case 'ROUTINE_PUSH': return 'Distribusi Rutin';
    default: return 'Permintaan';
  }
};

const orderTypeBadgeClass = (type) => {
  switch (type) {
    case 'PURCHASE_REQUEST': return 'bg-primary text-white';
    case 'EMBOSS_ORDER': return 'bg-info text-dark';
    case 'ROUTINE_PUSH': return 'bg-danger text-white';
    default: return 'bg-secondary text-white';
  }
};

onMounted(loadQueues);
</script>

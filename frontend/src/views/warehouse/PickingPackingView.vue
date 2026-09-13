<template>
  <div class="picking-packing-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Operasional Gudang: Picking & Packing</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/warehouse/picking" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-box-seam me-1"></i> Mode Picking
        </router-link>
        <router-link to="/warehouse/packing" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-boxes me-1"></i> Mode Packing
        </router-link>
        <router-link to="/distribution/shipments" class="btn btn-sm btn-danger fw-bold shadow-xs">
          <i class="bi bi-truck me-1"></i> Manifest Pengiriman
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
            <div class="fs-8 text-secondary fw-bold text-uppercase">Selesai Dikemas</div>
            <div class="fs-4 fw-bold font-monospace text-success">8 Order</div>
            <div class="fs-9 text-secondary">Hari Ini (Margomulyo)</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-speedometer fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">SLA Fulfillment</div>
            <div class="fs-4 fw-bold font-monospace text-body">99.2%</div>
            <div class="fs-9 text-secondary">Standar &lt; 24 Jam Kerja</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Dual Column Kanban Board -->
    <div class="row g-3">
      <!-- Col 1: Picking Queue -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-warning shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-hand-index-thumb text-warning"></i>
              <span>Antrean Picking (Ambil Barang di Rak)</span>
            </h3>
            <span class="badge text-bg-warning fs-9">{{ pickingQueue.length }} Order Menunggu</span>
          </div>
          <div class="card-body p-3 space-y-3">
            <div
              v-for="order in pickingQueue"
              :key="order.id"
              class="card border shadow-xs p-3 bg-body"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <div>
                  <router-link :to="`/orders/${order.id}`" class="font-monospace fw-bold text-danger fs-7 text-decoration-none">
                    {{ order.orderNumber }}
                  </router-link>
                  <div class="fs-9 text-secondary">Tujuan: <strong class="text-body">{{ order.branch }}</strong></div>
                </div>
                <span class="badge text-bg-secondary fs-9">Menunggu Rak</span>
              </div>
              <p class="fs-8 text-secondary mb-3">
                <i class="bi bi-list-check me-1"></i> {{ order.itemsSummary }}
              </p>
              <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                <span class="fs-9 text-secondary font-monospace">Lokasi: RAK-A01 s/d A04</span>
                <button
                  class="btn btn-sm btn-warning fw-bold shadow-xs py-1 px-2.5 text-dark"
                  @click="processPicking(order)"
                >
                  <i class="bi bi-check2-circle me-1"></i> Selesaikan Picking
                </button>
              </div>
            </div>

            <div v-if="pickingQueue.length === 0" class="text-center py-5 text-secondary">
              <i class="bi bi-check-circle fs-1 d-block mb-2 text-success"></i>
              Tidak ada antrean picking tersisa. Semua barang sudah berhasil diambil dari rak.
            </div>
          </div>
        </div>
      </div>

      <!-- Col 2: Packing Queue -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-success shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-box-seam text-success"></i>
              <span>Antrean Packing (Pengemasan Koli)</span>
            </h3>
            <span class="badge text-bg-success fs-9">{{ packingQueue.length }} Siap Koli</span>
          </div>
          <div class="card-body p-3 space-y-3">
            <div
              v-for="order in packingQueue"
              :key="order.id"
              class="card border shadow-xs p-3 bg-body"
            >
              <div class="d-flex justify-content-between align-items-start mb-2">
                <div>
                  <router-link :to="`/orders/${order.id}`" class="font-monospace fw-bold text-danger fs-7 text-decoration-none">
                    {{ order.orderNumber }}
                  </router-link>
                  <div class="fs-9 text-secondary">Tujuan: <strong class="text-body">{{ order.branch }}</strong></div>
                </div>
                <span class="badge text-bg-success fs-9">Picked</span>
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
                  <i class="bi bi-upc-scan me-1"></i> Selesaikan Packing & Label Barcode
                </button>
              </div>
            </div>

            <div v-if="packingQueue.length === 0" class="text-center py-5 text-secondary">
              <i class="bi bi-box2-heart fs-1 d-block mb-2 text-muted"></i>
              Tidak ada antrean packing saat ini.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/client';

const pickingQueue = ref([]);
const packingQueue = ref([]);
const errorMessage = ref('');

const describeItems = (items = []) => items
  .map(line => {
    const name = line.item?.name || '-';
    const qty = Number(line.qtyPicked || line.qtyApproved || 0).toLocaleString('id-ID');
    const uom = line.item?.uom || 'Unit';
    return `${qty} ${uom} ${name}`;
  })
  .join(', ');

const mapQueue = (order) => ({
  id: order.orderId,
  orderNumber: order.orderNumber || '-',
  branch: order.branch || '-',
  itemsSummary: describeItems(order.items),
  koli: 1,
  weight: 2.5
});

const loadQueues = async () => {
  errorMessage.value = '';
  try {
    const [pickingResponse, packingResponse] = await Promise.all([
      api.get('/warehouse/picking'),
      api.get('/warehouse/packing')
    ]);
    const pickingList = pickingResponse.data?.data ?? pickingResponse.data ?? [];
    const packingList = packingResponse.data?.data ?? packingResponse.data ?? [];
    pickingQueue.value = Array.isArray(pickingList) ? pickingList.map(mapQueue) : [];
    packingQueue.value = Array.isArray(packingList) ? packingList.map(mapQueue) : [];
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat antrean picking/packing dari server.';
    pickingQueue.value = [];
    packingQueue.value = [];
  }
};

const processPicking = async (order) => {
  try {
    await api.post(`/warehouse/picking/${order.id}/process`);
    await loadQueues();
    alert('Barang untuk order ' + order.orderNumber + ' berhasil diambil dan dialihkan ke antrean Packing.');
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memproses picking.';
  }
};

const processPacking = async (order) => {
  try {
    await api.post(`/warehouse/packing/${order.id}/process`, {
      koliCount: order.koli,
      totalWeightKg: order.weight,
      dimensionsCm: '30x20x15'
    });
    await loadQueues();
    alert('Order ' + order.orderNumber + ' selesai dikemas (' + order.koli + ' Koli, ' + order.weight + ' Kg) dan siap diterbitkan Manifest Ekspedisi.');
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memproses packing.';
  }
};

onMounted(loadQueues);
</script>

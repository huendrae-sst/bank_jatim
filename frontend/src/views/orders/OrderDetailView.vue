<template>
  <div class="order-detail-page space-y-3">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <div class="d-flex align-items-center flex-wrap gap-2">
              <h3 class="mb-0 text-body fw-bold">{{ order.order_number }}</h3>
              <span class="badge fs-8 text-uppercase" :class="badgeClass(order.status)">
                {{ getStatusLabel(order.status) }}
              </span>
              <span class="badge fs-8" :class="orderTypeBadgeClass(order.order_type)">
                {{ orderTypeLabel(order.order_type) }}
              </span>
              <span class="badge fs-8" :class="fulfillmentBadgeClass(order.fulfillment_status)">
                <i class="bi bi-box-seam me-1"></i> Pemenuhan: {{ order.fulfillment_status || 'UNFULFILLED' }}
              </span>
            </div>
            <p class="fs-8 text-secondary mb-0 mt-0.5">
              Dibuat oleh: <strong class="text-body">{{ order.requester_name }}</strong> &bull; {{ order.created_at_formatted }} WIB &bull; Unit: {{ order.org_name }} ({{ order.org_code }})
              <span v-if="order.routine_period" class="ms-1 badge bg-secondary-subtle text-secondary font-monospace">{{ order.routine_period }}</span>
            </p>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/orders" class="text-decoration-none text-danger">Orders</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">{{ order.order_number }}</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Action Bar / Buttons Header -->
    <div class="card p-2.5 mb-3 bg-body shadow-xs d-flex flex-row justify-content-between align-items-center flex-wrap gap-2">
      <div class="d-flex align-items-center gap-2">
        <router-link to="/orders" class="btn btn-sm btn-outline-secondary">
          Kembali ke Daftar Order
        </router-link>
        <router-link :to="`/orders/${order.id}/print`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold">
          Cetak Dokumen Order
        </router-link>
      </div>
      <div class="d-flex align-items-center gap-2">
        <template v-if="['SUBMITTED', 'WAITING_APPROVAL'].includes(order.status)">
          <button type="button" @click="showRejectModal = true" class="btn btn-sm btn-outline-danger fw-bold">
            Tolak Order
          </button>
          <button type="button" @click="approveOrder" class="btn btn-sm btn-success fw-bold shadow-xs">
            Setujui & Reservasi Stok
          </button>
        </template>
        <router-link v-else-if="order.status === 'ALLOCATED'" to="/warehouse/picking" class="btn btn-sm btn-primary fw-bold shadow-xs">
          Proses Picking Gudang
        </router-link>
      </div>
    </div>

    <!-- Workflow Progress Bar Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom">
        <h3 class="card-title fs-7 fw-bold mb-0 text-uppercase text-secondary">
          Status Alur Transaksi (Workflow Timeline)
        </h3>
      </div>
      <div class="card-body py-3 border-bottom bg-body-tertiary">
        <div class="row text-center g-2">
          <div v-for="(step, idx) in steps" :key="step.code" class="col-6 col-sm-3 col-md">
            <div class="d-flex flex-column align-items-center">
              <div 
                class="rounded-circle d-flex align-items-center justify-content-center fw-bold fs-7 mb-1" 
                :class="isPassed(idx) ? 'bg-danger text-white shadow-xs' : 'bg-body-secondary text-secondary'" 
                style="width: 32px; height: 32px;"
              >
                {{ idx + 1 }}
              </div>
              <span class="fs-8 fw-bold" :class="isPassed(idx) ? 'text-body' : 'text-secondary'">{{ step.label }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <!-- Detailed Timeline Table -->
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-7">
            <thead class="bg-body-tertiary border-bottom fs-8 text-uppercase text-secondary">
              <tr>
                <th class="ps-3" style="width: 50px;">No</th>
                <th style="min-width: 170px;">Status Alur</th>
                <th style="min-width: 160px;">Tanggal & Waktu</th>
                <th style="min-width: 180px;">Siapa yang Memproses</th>
                <th>Keterangan / Catatan</th>
                <th class="text-center pe-3" style="width: 130px;">Kondisi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tl in timeline" :key="tl.step" :class="{ 'table-warning': tl.status_state === 'CURRENT' }">
                <td class="ps-3 text-center fw-bold">
                  <span 
                    class="badge rounded-pill" 
                    :class="tl.status_state === 'DONE' ? 'bg-danger' : (tl.status_state === 'CURRENT' ? 'bg-warning text-dark' : 'bg-secondary')"
                    style="width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center;"
                  >
                    {{ tl.step }}
                  </span>
                </td>
                <td>
                  <div class="fw-bold text-body">{{ tl.label }}</div>
                  <span class="fs-8 font-monospace text-secondary">{{ tl.code }}</span>
                </td>
                <td>
                  <div class="fw-semibold font-monospace fs-8 text-body">
                   {{ tl.date_formatted }}
                  </div>
                </td>
                <td>
                  <div class="fw-bold text-body">{{ tl.actor_name }}</div>
                  <div class="fs-8 text-secondary">{{ tl.actor_role }}</div>
                </td>
                <td>
                  <div class="fs-8 text-body-secondary leading-relaxed">
                    {{ tl.description }}
                  </div>
                </td>
                <td class="text-center pe-3">
                  <span class="badge fs-8" :class="tl.badge_class">
                    {{ tl.badge_label }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Switching Stock Recommendation Banner -->
    <div v-if="switchingRecommendations.length > 0" class="alert alert-warning border-warning shadow-xs">
      <div class="d-flex align-items-center gap-2 fw-bold fs-7 mb-1">
        <i class="bi bi-lightbulb-fill text-warning fs-5"></i>
        <span>Rekomendasi Cerdas Switching Stock Antar-Cabang (Intelligent Fulfillment)</span>
      </div>
      <p class="fs-8 mb-3">
        Stok pada Gudang Logistik Utama tidak mencukupi untuk memenuhi seluruh permintaan. Sistem mendeteksi cabang lain dengan kelebihan stok (di atas safety stock):
      </p>

      <div class="space-y-2">
        <div v-for="rec in switchingRecommendations" :key="rec.item.id" class="card p-3 border-warning-subtle bg-body mb-2">
          <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
            <div>
              <div class="fs-7 fw-bold text-body">{{ rec.item.name }}</div>
              <div class="fs-8 text-secondary">
                Defisit: <strong class="text-danger">{{ rec.deficit }} {{ rec.item.uom }}</strong> (Stok Pusat: {{ rec.central_available }} {{ rec.item.uom }})
              </div>
              <div class="fs-8 text-primary fw-semibold mt-1">
                Sumber Alternatif: {{ rec.alternatives[0]?.organization_name }} (Stok Lebih: {{ rec.alternatives[0]?.excess_stock }} {{ rec.item.uom }})
              </div>
            </div>
            <button @click="openSwitchingModal(rec)" class="btn btn-sm btn-warning text-dark fw-bold">
              Ajukan Switching Stock
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Items Table Card -->
    <div class="card card-outline card-secondary shadow-xs">
      <div class="card-header border-bottom">
        <h3 class="card-title fs-6 fw-bold mb-0 text-body">
          Rincian Barang yang Diminta
        </h3>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-7">
            <thead class="border-bottom fs-8 text-uppercase text-secondary bg-body-tertiary">
              <tr>
                <th class="ps-3">Item & SKU</th>
                <th class="text-center">Diminta</th>
                <th class="text-center">Dialokasikan</th>
                <th class="text-center">Dipick/Pack</th>
                <th class="text-center">Dikirim</th>
                <th class="text-center">Diterima</th>
                <th class="text-end pe-3">Est. Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in order.items" :key="it.id">
                <td class="ps-3">
                  <div class="fw-bold text-body">{{ it.item.name }}</div>
                  <div class="fs-8 font-monospace text-secondary">{{ it.item.sku }} &bull; {{ it.item.uom }}</div>
                </td>
                <td class="text-center fw-bold text-body">{{ it.qty_requested }}</td>
                <td class="text-center fw-bold text-primary">{{ it.qty_allocated }}</td>
                <td class="text-center fw-bold text-info">{{ it.qty_packed }}</td>
                <td class="text-center fw-bold text-warning">{{ it.qty_shipped }}</td>
                <td class="text-center fw-bold text-success">{{ it.qty_received }}</td>
                <td class="text-end pe-3 fw-bold font-monospace text-body-emphasis">
                  {{ formatRupiah(it.subtotal_ref) }}
                </td>
              </tr>
            </tbody>
            <tfoot class="bg-body-tertiary border-top">
              <tr>
                <th colspan="6" class="text-end ps-3 fw-bold text-body">Total Estimasi Nilai Order:</th>
                <th class="text-end pe-3 fw-bold font-monospace text-danger fs-6">
                  {{ formatRupiah(order.total_estimated_value) }}
                </th>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div v-if="showRejectModal" class="modal-backdrop-custom" @click.self="showRejectModal = false">
      <div class="modal-dialog-custom modal-sm card shadow-lg">
        <div class="card-header bg-danger-subtle d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
          <h6 class="modal-title fw-bold text-danger mb-0">Konfirmasi Penolakan Order</h6>
          <button type="button" class="btn-close" @click="showRejectModal = false" aria-label="Close"></button>
        </div>
        <div class="card-body p-3.5 fs-8">
          <p class="text-secondary mb-2">Tuliskan alasan penolakan permintaan logistik ini:</p>
          <textarea v-model="rejectReason" rows="3" required class="form-control fs-8" placeholder="Contoh: Plafon anggaran cabang tidak mencukupi atau stok sedang kosong..."></textarea>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2.5 px-4 border-top">
          <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="showRejectModal = false">Batal</button>
          <button type="button" @click="confirmReject" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">Tolak Order</button>
        </div>
      </div>
    </div>

    <!-- Switching Stock Modal -->
    <div v-if="showSwitchingModal" class="modal-backdrop-custom" @click.self="showSwitchingModal = false">
      <div class="modal-dialog-custom card shadow-lg">
        <div class="card-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
          <h6 class="modal-title fw-bold mb-0 text-body">Form Pengajuan Switching Stock</h6>
          <button type="button" class="btn-close" @click="showSwitchingModal = false" aria-label="Close"></button>
        </div>
        <div class="card-body p-3.5 fs-8 space-y-3">
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Barang</label>
            <div class="p-2 rounded bg-body-secondary fw-bold fs-7">{{ selectedSwitchItem?.item?.name }}</div>
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Pilih Unit Sumber Alternatif</label>
            <select v-model="switchSourceWarehouseId" class="form-select fs-8" required>
              <option v-for="alt in selectedSwitchItem?.alternatives" :key="alt.warehouse_id" :value="alt.warehouse_id">
                {{ alt.organization_name }} (Stok Lebih: {{ alt.excess_stock }})
              </option>
            </select>
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Switching Stock (Qty)</label>
            <input type="number" v-model.number="switchQty" min="1" class="form-control form-control-sm fw-bold font-monospace fs-8" />
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alasan Rekomendasi</label>
            <textarea v-model="switchReason" rows="2" class="form-control form-control-sm fs-8"></textarea>
          </div>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2.5 px-4 border-top">
          <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="showSwitchingModal = false">Batal</button>
          <button type="button" @click="submitSwitching" class="btn btn-sm btn-warning text-dark fw-bold shadow-xs px-3">Ajukan Proposal Switching</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();

const showRejectModal = ref(false);
const rejectReason = ref('');
const showSwitchingModal = ref(false);
const selectedSwitchItem = ref(null);
const switchSourceWarehouseId = ref('');
const switchQty = ref(0);
const switchReason = ref('Pemenuhan kekurangan stok pusat dari kelebihan stok cabang regional.');
const errorMessage = ref('');

const steps = [
  { code: 'SUBMITTED', label: 'Diajukan' },
  { code: 'APPROVED', label: 'Disetujui' },
  { code: 'ALLOCATED', label: 'Teralokasi' },
  { code: 'PICKING', label: 'Picking' },
  { code: 'READY_TO_SHIP', label: 'Siap Kirim' },
  { code: 'IN_TRANSIT', label: 'Dalam Perjalanan' },
  { code: 'RECEIVED', label: 'Diterima' },
  { code: 'COMPLETED', label: 'Selesai' }
];

const statusMap = {
  DRAFT: 'Draft',
  SUBMITTED: 'Diajukan',
  WAITING_APPROVAL: 'Menunggu Approval',
  APPROVED: 'Disetujui',
  ALLOCATED: 'Teralokasi',
  PICKING: 'Picking',
  READY_TO_SHIP: 'Siap Kirim',
  IN_TRANSIT: 'Dalam Perjalanan',
  RECEIVED: 'Diterima',
  COMPLETED: 'Selesai',
  REJECTED: 'Ditolak',
  CANCELLED: 'Dibatalkan'
};

const getStatusLabel = (st) => {
  return statusMap[st] || (st ? st.replace(/_/g, ' ') : '');
};

const emptyOrder = () => ({
  id: route.params.id || null,
  order_number: '-',
  status: '',
  requester_name: '-',
  created_at_formatted: '-',
  org_name: '-',
  org_code: '-',
  total_estimated_value: 0,
  items: []
});

const order = ref(emptyOrder());
const switchingRecommendations = ref([]);

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('id-ID', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
};

const mapOrder = (data) => ({
  id: data.id,
  order_number: data.orderNumber || '-',
  status: data.status || '',
  order_type: data.orderType || 'INTERNAL_REQUEST',
  fulfillment_status: data.fulfillmentStatus || 'UNFULFILLED',
  batch_manifest_number: data.batchManifestNumber || '',
  routine_period: data.routinePeriod || '',
  requester_name: data.createdByUser?.name || '-',
  created_at_formatted: formatDate(data.createdAt || data.submittedAt),
  org_name: data.requestingOrganization?.name || '-',
  org_code: data.requestingOrganization?.code || '-',
  total_estimated_value: Number(data.totalEstimatedValue || 0),
  items: (data.items || []).map((line) => ({
    id: line.id,
    item: {
      name: line.item?.name || '-',
      sku: line.item?.sku || '-',
      uom: line.item?.uom || 'Unit'
    },
    qty_requested: Number(line.qtyRequested || 0),
    qty_allocated: Number(line.qtyAllocated || 0),
    qty_packed: Number(line.qtyPacked || 0),
    qty_shipped: Number(line.qtyShipped || 0),
    qty_received: Number(line.qtyReceived || 0),
    subtotal_ref: Number(line.subtotalRef || 0)
  }))
});

const orderTypeLabel = (type) => {
  switch (type) {
    case 'PURCHASE_REQUEST': return 'Pembelian PR';
    case 'EMBOSS_ORDER': return 'Order Emboss';
    case 'ROUTINE_PUSH': return 'Distribusi Rutin';
    default: return 'Order Permintaan';
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

const fulfillmentBadgeClass = (fStatus) => {
  switch (fStatus) {
    case 'RECEIVED': return 'bg-success text-white';
    case 'FULLY_FULFILLED': return 'bg-primary text-white';
    case 'PARTIALLY_FULFILLED': return 'bg-warning text-dark';
    default: return 'bg-secondary text-white';
  }
};

const buildTimeline = (currentOrder) => {
  const currentIndex = steps.findIndex((step) => step.code === currentOrder.status);
  const normalizedIndex = currentIndex >= 0 ? currentIndex : 0;

  return steps.map((step, index) => {
    const done = currentOrder.status !== 'REJECTED' && index < normalizedIndex;
    const current = currentOrder.status !== 'REJECTED' && index === normalizedIndex;

    return {
      step: index + 1,
      code: step.code,
      label: step.label,
      date_formatted: index === 0 ? currentOrder.created_at_formatted : '-',
      actor_name: index === 0 ? currentOrder.requester_name : '-',
      actor_role: index === 0 ? 'Pemohon Order' : '-',
      description: current ? 'Status order saat ini berdasarkan data backend.' : (done ? 'Tahap sudah dilalui.' : 'Menunggu proses berikutnya.'),
      status_state: done ? 'DONE' : (current ? 'CURRENT' : 'PENDING'),
      badge_class: done ? 'text-bg-success' : (current ? 'text-bg-warning' : 'text-bg-secondary'),
      badge_label: done ? 'SELESAI' : (current ? 'AKTIF' : 'MENUNGGU')
    };
  });
};

const timeline = computed(() => buildTimeline(order.value));

const currentStatusIdx = computed(() => {
  const all = steps.map(s => s.code);
  const found = all.indexOf(order.value.status);
  return found >= 0 ? found : 0;
});

const isPassed = (idx) => {
  return idx <= currentStatusIdx.value && order.value.status !== 'REJECTED';
};

const badgeClass = (st) => {
  switch (st) {
    case 'COMPLETED':
    case 'RECEIVED':
      return 'text-bg-success';
    case 'IN_TRANSIT':
      return 'text-bg-info';
    case 'READY_TO_SHIP':
    case 'ALLOCATED':
      return 'text-bg-primary';
    case 'WAITING_APPROVAL':
    case 'SUBMITTED':
      return 'text-bg-warning';
    case 'CANCELLED':
    case 'REJECTED':
      return 'text-bg-danger';
    default:
      return 'text-bg-light border';
  }
};

const formatRupiah = (val) => {
  return 'Rp ' + new Intl.NumberFormat('id-ID').format(Math.round(val || 0));
};

const loadOrder = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get(`/orders/${route.params.id}`);
    order.value = mapOrder(response.data);
  } catch (error) {
    order.value = emptyOrder();
    errorMessage.value = error?.message || error?.error || 'Gagal memuat detail order.';
  }
};

const approveOrder = async () => {
  errorMessage.value = '';
  try {
    await api.post(`/orders/${route.params.id}/approve`);
    await loadOrder();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menyetujui order.';
  }
};

const confirmReject = () => {
  showRejectModal.value = false;
  errorMessage.value = 'Penolakan order belum memiliki endpoint backend persistent.';
};

const openSwitchingModal = (rec) => {
  selectedSwitchItem.value = rec;
  switchSourceWarehouseId.value = rec.alternatives[0]?.warehouse_id || '';
  switchQty.value = rec.deficit;
  showSwitchingModal.value = true;
};

const submitSwitching = () => {
  showSwitchingModal.value = false;
  errorMessage.value = 'Pengajuan switching stock belum memiliki endpoint backend persistent.';
};

onMounted(loadOrder);
</script>

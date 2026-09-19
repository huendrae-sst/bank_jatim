<template>
  <div class="receiving-po-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Penerimaan Barang PO Vendor</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/receiving" class="text-decoration-none text-body">Penerimaan & QC</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Penerimaan PO</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'queue' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'queue'"
            >
              Penerimaan PO
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
          <router-link to="/procurement/po" class="btn btn-sm btn-outline-secondary fs-8">
            Data Purchase Orders
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Status PO</option>
                <option value="MENUNGGU KEDATANGAN">MENUNGGU KEDATANGAN</option>
                <option value="PARTIAL">PARTIAL</option>
                <option value="COMPLETED">COMPLETED</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterStatus">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No. PO, Vendor, Nama Barang..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                Cari
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
              <th class="ps-3 py-2 text-uppercase fs-9">Nomor PO</th>
              <th class="py-2 text-uppercase fs-9">Vendor / Rekanan</th>
              <th class="py-2 text-uppercase fs-9">Deskripsi Barang</th>
              <th class="py-2 text-uppercase fs-9 text-end">Jumlah Dipesan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Jumlah Diterima</th>
              <th class="py-2 text-uppercase fs-9">Tgl Estimasi Tiba</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi QC & Penerimaan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="po in paginatedPoList" :key="po.id">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ po.poNumber }}</td>
              <td class="py-2">
                <span class="fw-semibold text-body">{{ po.vendorName }}</span>
              </td>
              <td class="py-2 fs-8 text-body">{{ po.itemName }}</td>
              <td class="py-2 text-end font-monospace">{{ po.qtyOrdered.toLocaleString('id-ID') }} {{ po.uom }}</td>
              <td class="py-2 text-end font-monospace text-success fw-bold">{{ po.qtyReceived.toLocaleString('id-ID') }} {{ po.uom }}</td>
              <td class="py-2 fs-8 text-secondary">{{ po.expectedDate }}</td>
              <td class="py-2">
                <span :class="['badge', po.status === 'PARTIAL' ? 'text-bg-warning' : (po.status === 'COMPLETED' ? 'text-bg-success' : 'text-bg-primary')]">
                  {{ po.status }}
                </span>
              </td>
              <td class="text-center pe-3 py-2">
                <button class="btn-action-icon text-primary" @click="openReceiptForm(po)" title="Terima Fisik & QC">
                  <i class="bi bi-clipboard-check"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredPoList.length === 0">
              <td colspan="8" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data PO vendor yang cocok dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPoList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Receipt Modal -->
    <div v-if="showModal" class="modal-backdrop-custom" @click.self="showModal = false">
      <div class="modal-dialog-custom card shadow-lg">
        <div class="card-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
          <h6 class="mb-0 fw-bold text-body">
            Konfirmasi Penerimaan Fisik Barang PO
          </h6>
          <button type="button" class="btn-close" @click="showModal = false" aria-label="Close"></button>
        </div>
        <div class="card-body p-3 fs-8" v-if="selectedPo">
          <div class="alert alert-light border d-flex flex-column gap-1 mb-3">
            <div><strong>Nomor PO:</strong> <span class="font-monospace text-danger fw-bold">{{ selectedPo.poNumber }}</span></div>
            <div><strong>Rekanan Vendor:</strong> {{ selectedPo.vendorName }}</div>
            <div><strong>Barang / SKU:</strong> {{ selectedPo.itemName }}</div>
            <div><strong>Total Pesanan:</strong> {{ selectedPo.qtyOrdered.toLocaleString('id-ID') }} {{ selectedPo.uom }}</div>
          </div>
          <div class="mb-3">
            <label class="form-label fs-8 fw-bold">Jumlah Fisik Masuk Gudang (Good / Lolos QC) <span class="text-danger">*</span></label>
            <input type="number" v-model.number="receiveQty" class="form-control form-control-sm font-monospace" min="1" :max="selectedPo.qtyOrdered - selectedPo.qtyReceived" />
            <small class="text-secondary fs-9">Maksimal sisa penerimaan: {{ (selectedPo.qtyOrdered - selectedPo.qtyReceived).toLocaleString('id-ID') }} {{ selectedPo.uom }}</small>
          </div>
          <div class="mb-3">
            <label class="form-label fs-8 fw-bold">Nomor Surat Jalan / Delivery Order Vendor <span class="text-danger">*</span></label>
            <input type="text" v-model="deliveryNote" class="form-control form-control-sm" placeholder="Contoh: SJ-PERURI-2026-11" required />
          </div>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2.5 px-3 border-top">
          <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
          <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" @click="submitReceipt">
            Simpan & Update Stok
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';
import { toast } from '@/utils/toast';

const showModal = ref(false);
const selectedPo = ref(null);
const receiveQty = ref(0);
const deliveryNote = ref('');
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterStatus = ref('');
const activeTab = ref('queue');

const toReceivingStatus = (status, qtyOrdered, qtyReceived) => {
  if (status === 'RECEIVED' || qtyReceived >= qtyOrdered && qtyOrdered > 0) return 'COMPLETED';
  if (qtyReceived > 0) return 'PARTIAL';
  return 'MENUNGGU KEDATANGAN';
};

const mapPo = (po) => {
  const items = po.items || [];
  const qtyOrdered = items.reduce((sum, line) => sum + Number(line.qtyOrdered || 0), 0);
  const qtyReceived = items.reduce((sum, line) => sum + Number(line.qtyReceived || 0), 0);
  const itemName = items
    .map(line => {
      const name = line.item?.name || '-';
      const qty = Number(line.qtyOrdered || 0).toLocaleString('id-ID');
      const uom = line.item?.uom || 'Unit';
      return `${name} (${qty} ${uom})`;
    })
    .join(', ');

  return {
    id: po.id,
    poNumber: po.poNumber || '-',
    vendorName: po.vendor?.name || po.vendor || '-',
    itemName: itemName || '-',
    qtyOrdered,
    qtyReceived,
    expectedDate: po.expectedDeliveryDate || po.orderDate || '-',
    uom: items[0]?.item?.uom || 'Unit',
    status: toReceivingStatus(po.status, qtyOrdered, qtyReceived)
  };
};

const poList = ref([]);

const loadPurchaseOrders = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/procurement/po');
    const data = response.data?.content || response.data || [];
    const valid = data.filter(po => !['DRAFT', 'ISSUED', 'REJECTED'].includes(po.status));
    if (valid.length > 0) {
      poList.value = valid.map(mapPo);
    }
  } catch (error) {
    poList.value = [];
    console.warn('Failed loading purchase orders from backend:', error);
  }
};

const waitingCount = computed(() => poList.value.filter(po => po.status === 'MENUNGGU KEDATANGAN').length);
const partialCount = computed(() => poList.value.filter(po => po.status === 'PARTIAL').length);
const completedCount = computed(() => poList.value.filter(po => po.status === 'COMPLETED').length);
const openPoCount = computed(() => poList.value.filter(po => po.status !== 'COMPLETED').length);
const totalReceivedQty = computed(() => poList.value.reduce((sum, po) => sum + Number(po.qtyReceived || 0), 0));

const resetFilters = () => {
  searchQuery.value = '';
  filterStatus.value = '';
  currentPage.value = 1;
};

const filteredPoList = computed(() => {
  return poList.value.filter(po => {
    if (activeTab.value === 'queue' && po.status === 'COMPLETED') return false;
    if (activeTab.value === 'history' && po.status !== 'COMPLETED') return false;

    const matchQuery = !searchQuery.value ||
      po.poNumber.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      po.vendorName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      po.itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchStatus = !filterStatus.value || po.status === filterStatus.value;
    return matchQuery && matchStatus;
  });
});

const paginatedPoList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPoList.value.slice(start, start + perPage.value);
});

const openReceiptForm = (po) => {
  selectedPo.value = po;
  receiveQty.value = po.qtyOrdered - po.qtyReceived;
  deliveryNote.value = '';
  showModal.value = true;
};

const submitReceipt = async () => {
  if (!receiveQty.value || receiveQty.value <= 0) {
    toast.warn('Jumlah fisik masuk harus lebih besar dari 0.');
    return;
  }
  if (!deliveryNote.value) {
    toast.warn('Nomor Surat Jalan / DO Vendor wajib diisi.');
    return;
  }
  if (!selectedPo.value) {
    return;
  }

  try {
    await api.post(`/procurement/po/${selectedPo.value.id}/receive-goods`, null, {
      params: { deliveryNoteNumber: deliveryNote.value }
    });
    await loadPurchaseOrders();
    showModal.value = false;
    toast.success('Penerimaan barang sukses dicatat. Saldo stok Gudang Margomulyo telah bertambah.');
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal mencatat penerimaan PO ke backend.');
  }
};

onMounted(loadPurchaseOrders);
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}
.modal-dialog-custom {
  width: 500px;
  max-width: 100%;
}
</style>

<template>
  <div class="purchase-orders-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Purchase Orders (PO)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Pengadaan</li>
              <li class="breadcrumb-item active" aria-current="page">Purchase Orders</li>
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
            <i class="bi bi-file-earmark-ruled"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL KONTRAK PO</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ poList.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Tahun Anggaran 2026</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs">
            <i class="bi bi-truck"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">DALAM PENGIRIMAN</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ inDeliveryCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-warning" style="width: 60%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Proses Logistik Rekanan</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs">
            <i class="bi bi-check2-all"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">SELESAI DITERIMA (GRN)</span>
            <span class="info-box-number text-success fs-4 font-monospace">{{ completedCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Stok Masuk Margomulyo</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info text-white shadow-xs">
            <i class="bi bi-wallet2"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">KOMITMEN BELANJA</span>
            <span class="info-box-number text-body fs-4 font-monospace">Rp {{ formatCompact(totalCommitment) }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-info" style="width: 75%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Termasuk PPN 11%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main PO Data Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Tools -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua PO
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'delivery' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'delivery'"
            >
              Dalam Pengiriman
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'completed'"
            >
              Selesai GRN
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/procurement/consolidation" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-layers text-primary me-1"></i> Konsolidasi PR
          </router-link>
          <router-link to="/reports/procurement-coverage" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            <i class="bi bi-diagram-3 me-1"></i> Matriks Coverage
          </router-link>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterWarehouse" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Gudang Tujuan</option>
                <option v-for="warehouse in warehouseOptions" :key="warehouse" :value="warehouse">{{ warehouse }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-shop"></i>
              </span>
              <select v-model="filterVendor" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Rekanan Vendor</option>
                <option v-for="vendor in vendorOptions" :key="vendor" :value="vendor">{{ vendor }}</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterWarehouse !== 'ALL' || filterVendor !== 'ALL'">
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
                placeholder="Cari Nomor PO, Ref PR, atau Vendor..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- PO Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Nomor PO</th>
              <th>Rekanan Vendor</th>
              <th>Gudang Tujuan</th>
              <th>Tanggal Terbit</th>
              <th class="text-end">Total Nominal (PPN 11%)</th>
              <th class="text-center">Status</th>
              <th class="text-center" style="width: 140px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="po in paginatedPoList" :key="po.id">
              <td>
                <router-link :to="`/procurement/po/${po.id}/print`" target="_blank" class="font-monospace fw-bold text-danger text-decoration-none">
                  {{ po.poNumber }}
                </router-link>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ po.vendor }}</div>
                <div class="fs-9 text-secondary font-monospace">{{ po.refPr }}</div>
              </td>
              <td>
                <span class="badge text-bg-light border fs-9">
                  <i class="bi bi-building me-1"></i> {{ po.warehouse }}
                </span>
              </td>
              <td>
                <span class="fs-8 text-secondary">{{ po.orderDate }}</span>
              </td>
              <td class="text-end font-monospace fw-bold text-body">
                Rp {{ po.totalAmount.toLocaleString('id-ID') }}
              </td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(po.status)">
                  {{ po.status ? po.status.replace('_', ' ') : 'ISSUED' }}
                </span>
              </td>
              <td class="text-center">
                <div class="d-inline-flex align-items-center gap-1">
                  <button
                    v-if="['ISSUED', 'VENDOR_PROCESS', 'IN_DELIVERY'].includes(po.status)"
                    class="btn-action-icon text-danger"
                    @click="openGoodsReceiptModal(po)"
                    title="Penerimaan Fisik Barang Masuk (GRN)"
                  >
                    <i class="bi bi-box-arrow-in-down"></i>
                  </button>
                  <router-link
                    :to="`/procurement/po/${po.id}/print`"
                    target="_blank"
                    class="btn-action-icon text-secondary"
                    title="Cetak PO Resmi"
                  >
                    <i class="bi bi-printer"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPoList.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data Purchase Order yang sesuai dengan kriteria filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPoList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Goods Receipt Modal -->
    <div v-if="activeGrnPo" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-md modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold text-body">
              Penerimaan Fisik (GRN) - {{ activeGrnPo.poNumber }}
            </h5>
            <button type="button" class="btn-close-modal" @click="activeGrnPo = null">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body p-4 space-y-3">
            <div class="alert alert-light border shadow-xs p-3 fs-8 mb-3">
              <div class="fw-bold text-dark mb-1">Detail Rekanan Vendor:</div>
              <div>Vendor: <strong class="text-body">{{ activeGrnPo.vendor }}</strong></div>
              <div>Gudang: <strong class="text-body">{{ activeGrnPo.warehouse }}</strong></div>
              <div>Total Nilai: <strong class="text-danger font-monospace">Rp {{ activeGrnPo.totalAmount.toLocaleString('id-ID') }}</strong></div>
            </div>

            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Surat Jalan Vendor (Delivery Note) <span class="text-danger">*</span></label>
              <input
                type="text"
                v-model="deliveryNote"
                placeholder="Contoh: SJ-SCI-2026-991"
                class="form-control fs-7"
                required
              />
            </div>

            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan Kondisi Barang Saat Tiba</label>
              <textarea
                v-model="grnNotes"
                class="form-control fs-7"
                rows="2"
                placeholder="Kemasan kardus utuh, segel hologram vendor lengkap..."
              ></textarea>
            </div>

            <p class="fs-9 text-secondary mb-0">
              <i class="bi bi-info-circle text-primary me-1"></i>
              Kuantitas fisik akan dicatat dan saldo persediaan Gudang Margomulyo akan diperbarui otomatis pada Buku Besar Persediaan (<em>Stock Ledger</em>).
            </p>
          </div>
          <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="activeGrnPo = null">
              Batal
            </button>
            <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3" @click="submitGoodsReceipt">
              <i class="bi bi-check2-circle me-1"></i> Simpan & Update Stok Masuk
            </button>
          </div>
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
const filterWarehouse = ref('ALL');
const filterVendor = ref('ALL');
const activeGrnPo = ref(null);
const deliveryNote = ref('');
const grnNotes = ref('');
const currentPage = ref(1);
const perPage = ref(10);

const resetFilters = () => {
  searchQuery.value = '';
  filterWarehouse.value = 'ALL';
  filterVendor.value = 'ALL';
  currentPage.value = 1;
};

const poList = ref([]);

onMounted(() => {
  loadPurchaseOrders();
});

const loadPurchaseOrders = async () => {
  const response = await api.get('/procurement/po');
  poList.value = (response.data || []).map((po) => ({
    id: po.id,
    poNumber: po.poNumber,
    refPr: po.notes || '-',
    vendor: po.vendor?.name || '-',
    warehouse: po.warehouse?.name || '-',
    orderDate: formatDate(po.orderDate),
    totalAmount: Number(po.totalAmount || 0),
    status: po.status
  }));
};

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value));
};

const formatCompact = (value) => {
  const number = Number(value || 0);
  if (number >= 1000000000) return `${(number / 1000000000).toFixed(2)}M`;
  if (number >= 1000000) return `${(number / 1000000).toFixed(1)}jt`;
  return number.toLocaleString('id-ID');
};

const inDeliveryCount = computed(() => {
  return poList.value.filter(p => ['ISSUED', 'VENDOR_PROCESS', 'IN_DELIVERY'].includes(p.status)).length;
});

const completedCount = computed(() => {
  return poList.value.filter(p => p.status === 'COMPLETED').length;
});

const totalCommitment = computed(() => poList.value.reduce((sum, po) => sum + po.totalAmount, 0));
const warehouseOptions = computed(() => Array.from(new Set(poList.value.map((po) => po.warehouse))).filter(Boolean).sort());
const vendorOptions = computed(() => Array.from(new Set(poList.value.map((po) => po.vendor))).filter(Boolean).sort());

const filteredPoList = computed(() => {
  return poList.value.filter(po => {
    if (activeTab.value === 'delivery' && !['ISSUED', 'VENDOR_PROCESS', 'IN_DELIVERY'].includes(po.status)) return false;
    if (activeTab.value === 'completed' && po.status !== 'COMPLETED') return false;

    if (filterWarehouse.value !== 'ALL' && po.warehouse !== filterWarehouse.value) return false;
    if (filterVendor.value !== 'ALL' && po.vendor !== filterVendor.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNum = po.poNumber.toLowerCase().includes(q);
      const matchVendor = po.vendor.toLowerCase().includes(q);
      const matchPr = po.refPr ? po.refPr.toLowerCase().includes(q) : false;
      if (!matchNum && !matchVendor && !matchPr) return false;
    }
    return true;
  });
});

const paginatedPoList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPoList.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  switch (status) {
    case 'COMPLETED': return 'text-bg-success';
    case 'IN_DELIVERY': return 'text-bg-info';
    case 'VENDOR_PROCESS':
    case 'ISSUED': return 'text-bg-warning';
    case 'CANCELLED': return 'text-bg-danger';
    default: return 'text-bg-secondary';
  }
};

const openGoodsReceiptModal = (po) => {
  activeGrnPo.value = po;
  deliveryNote.value = '';
  grnNotes.value = '';
};

const submitGoodsReceipt = async () => {
  if (!deliveryNote.value) {
    alert('Harap isi nomor surat jalan vendor.');
    return;
  }
  if (activeGrnPo.value) {
    await api.post(`/procurement/po/${activeGrnPo.value.id}/receive-goods`, null, {
      params: { deliveryNoteNumber: deliveryNote.value }
    });
    await loadPurchaseOrders();
    alert(`Barang masuk dari PO ${activeGrnPo.value.poNumber} berhasil diterima dengan Surat Jalan ${deliveryNote.value}.`);
    activeGrnPo.value = null;
  }
};
</script>

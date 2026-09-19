<template>
  <div class="production-consolidation-page">
    <!-- Header & Breadcrumbs -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Konsolidasi Order Emboss Cabang</h3>
            <span class="fs-8 text-secondary">Konsolidasikan permintaan kartu debit nasabah dari cabang-cabang untuk diterbitkan ke SPK Produksi Pusat</span>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Dashboard</router-link></li>
              <li class="breadcrumb-item"><router-link to="/production" class="text-decoration-none text-danger">Percetakan &amp; Produksi</router-link></li>
              <li class="breadcrumb-item active" aria-current="page">Konsolidasi Order</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Alert / Banner Info -->
    <div class="alert alert-danger bg-danger-subtle text-danger-emphasis border-0 py-2 px-3 mb-3 d-flex align-items-center justify-content-between shadow-xs fs-8">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-info-circle-fill fs-6"></i>
        <span>
          <strong>Pusat Konsolidasi Produksi:</strong> Pilih pesanan cabang berikut untuk digabungkan ke dalam 1 Surat Perintah Kerja (SPK) mesin personalisasi kartu.
        </span>
      </div>
      <router-link to="/production" class="btn btn-sm btn-outline-danger fw-bold fs-9 text-nowrap">
        Daftar SPK Aktif
      </router-link>
    </div>

    <!-- Main Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold text-body fs-7">Antrean Order Emboss Cabang Menunggu Produksi</span>
          <span class="badge bg-danger fs-8">{{ consolidationList.length }} Batch Order</span>
        </div>

        <div class="d-flex align-items-center gap-2 ms-md-auto">
          <button
            type="button"
            class="btn btn-sm btn-danger fw-bold shadow-xs fs-8"
            :disabled="selectedOrderIds.length === 0"
            @click="openGenerateModal"
          >
            Terbitkan SPK ({{ selectedOrderIds.length }} Dipilih - {{ selectedTotalCards }} Kartu)
          </button>
          <button type="button" class="btn btn-sm btn-outline-secondary fs-8" @click="fetchConsolidation" title="Refresh Data">
            <i class="bi bi-arrow-clockwise"></i>
          </button>
        </div>
      </div>

      <!-- Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Cabang Pemesan</option>
                <option v-for="b in uniqueBranches" :key="b" :value="b">{{ b }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-credit-card"></i></span>
              <select v-model="filterCardType" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Tipe Kartu</option>
                <option value="GPN">GPN (Chip)</option>
                <option value="MC">Mastercard</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 fs-8"
                placeholder="Cari Cabang, No. Order, Nama Nasabah..."
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Table Content -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary text-uppercase fs-9">
            <tr>
              <th class="text-center" style="width: 40px;">
                <input
                  type="checkbox"
                  class="form-check-input"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  :disabled="filteredList.length === 0"
                />
              </th>
              <th class="ps-2">Kantor Cabang Pemesan</th>
              <th>Nomor Order Emboss</th>
              <th>Jenis Kartu (BOM Target)</th>
              <th class="text-center">Jumlah Kartu</th>
              <th>Sampel Nama Nasabah Terlampir</th>
              <th>Ref. File Emboss</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in filteredList" :key="row.orderId + '-' + row.cardType">
              <td class="text-center">
                <input
                  type="checkbox"
                  class="form-check-input"
                  :value="row.orderId"
                  v-model="selectedOrderIds"
                />
              </td>
              <td class="ps-2">
                <strong class="text-body">{{ row.branchName }}</strong>
                <span class="text-muted d-block font-monospace fs-9">{{ row.branchCode }}</span>
              </td>
              <td class="font-monospace fw-bold text-danger">
                {{ row.orderNumber }}
              </td>
              <td>
                <span class="badge" :class="row.cardType.includes('MC') ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-success-subtle text-success border border-success-subtle'">
                  {{ row.cardType.includes('MC') ? 'Mastercard (ATM-EMB-MC-001)' : 'GPN Chip (ATM-EMB-GPN-001)' }}
                </span>
              </td>
              <td class="text-center font-monospace fw-bold fs-7">
                {{ row.totalCards?.toLocaleString('id-ID') }} Keping
              </td>
              <td>
                <div class="d-flex flex-wrap gap-1">
                  <span
                    v-for="(name, nIdx) in row.customerNames"
                    :key="nIdx"
                    class="badge bg-body-secondary text-body border fs-9"
                  >
                   {{ name }}
                  </span>
                  <span v-if="row.totalCards > row.customerNames.length" class="text-muted fs-9 align-self-center">
                    +{{ row.totalCards - row.customerNames.length }} lainnya
                  </span>
                </div>
              </td>
              <td class="fs-9 text-secondary font-monospace">
                {{ row.embossFileName || ('File #' + row.embossFileId) }}
              </td>
              <td class="text-center pe-3">
                <router-link :to="'/orders/' + row.orderId" class="btn btn-sm btn-outline-secondary py-0 px-2 fs-9">
                  Lihat Order
                </router-link>
              </td>
            </tr>
            <tr v-if="filteredList.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada order emboss cabang yang perlu dikonsolidasikan saat ini.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Selection Summary Footer -->
      <div v-if="selectedOrderIds.length > 0" class="card-footer bg-body-secondary border-top p-3 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 fs-8">
        <div>
          <i class="bi bi-check2-square text-danger me-1"></i>
          Terpilih <strong>{{ selectedOrderIds.length }} order</strong> dengan total <strong>{{ selectedTotalCards }} keping kartu</strong> siap diproduksi.
        </div>
        <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-4" @click="openGenerateModal">
          Proses Penerbitan SPK Sekarang
        </button>
      </div>
    </div>

    <!-- Modal Generate SPK Produksi -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Konfirmasi Penerbitan SPK Produksi Emboss
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="submitGenerateSpk">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="alert alert-info py-2 px-3 fs-8 border-0 mb-3">
                <i class="bi bi-layers me-1"></i> SPK Produksi ini akan otomatis menghubungkan rekaman data nasabah dari <strong>{{ selectedOrderIds.length }} order cabang</strong> dan menetapkan kuantitas bahan baku kartu blank via BOM.
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Gudang Sumber Bahan Blank <span class="text-danger">*</span></label>
                  <select v-model="formSpk.warehouseId" class="form-select form-select-sm fs-8" required>
                    <option :value="null">-- Pilih Gudang Sentral --</option>
                    <option v-for="wh in warehouseOptions" :key="wh.id" :value="wh.id">
                      {{ wh.name }} ({{ wh.code }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Total Kuantitas Kartu</label>
                  <input type="text" :value="selectedTotalCards + ' Keping Kartu'" class="form-control form-control-sm font-monospace fs-8 bg-body-secondary" readonly />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan Produksi / Keterangan SPK</label>
                  <textarea
                    v-model="formSpk.notes"
                    class="form-control form-control-sm fs-8"
                    rows="3"
                    placeholder="Contoh: Produksi Massal Batch 1 Kartu Debit Nasabah KC Surabaya, KC Sidoarjo..."
                  ></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-4 fs-8" :disabled="isSubmitting">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                Terbitkan SPK
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
import { useRouter } from 'vue-router';
import api from '@/api/client';

const router = useRouter();
const consolidationList = ref([]);
const warehouseOptions = ref([]);
const selectedOrderIds = ref([]);
const isLoading = ref(false);
const isSubmitting = ref(false);
const showModal = ref(false);

const filterBranch = ref('ALL');
const filterCardType = ref('ALL');
const searchQuery = ref('');

const formSpk = ref({
  warehouseId: null,
  notes: ''
});

const fetchConsolidation = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/production-orders/consolidation');
    consolidationList.value = res.data?.data || res.data || [];
  } catch (err) {
    console.error('Failed to load consolidation data:', err);
  } finally {
    isLoading.value = false;
  }
};

const fetchWarehouses = async () => {
  try {
    const res = await api.get('/master/warehouses');
    const whs = res.data?.data || res.data || [];
    warehouseOptions.value = whs;
    if (whs.length > 0) {
      const central = whs.find(w => w.type === 'CENTRAL_LOGISTICS') || whs[0];
      formSpk.value.warehouseId = central.id;
    }
  } catch (err) {
    warehouseOptions.value = [
      { id: 1, name: 'Gudang Sentral Margomulyo Surabaya', code: 'WH-CEN-01' }
    ];
    formSpk.value.warehouseId = 1;
  }
};

const uniqueBranches = computed(() => {
  return [...new Set(consolidationList.value.map(r => r.branchName))].filter(Boolean);
});

const filteredList = computed(() => {
  return consolidationList.value.filter(r => {
    if (filterBranch.value !== 'ALL' && r.branchName !== filterBranch.value) return false;
    if (filterCardType.value !== 'ALL' && !r.cardType.toUpperCase().includes(filterCardType.value)) return false;
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchBranch = r.branchName?.toLowerCase().includes(q) || r.branchCode?.toLowerCase().includes(q);
      const matchOrder = r.orderNumber?.toLowerCase().includes(q);
      const matchNames = r.customerNames?.some(n => n.toLowerCase().includes(q));
      if (!matchBranch && !matchOrder && !matchNames) return false;
    }
    return true;
  });
});

const isAllSelected = computed(() => {
  if (filteredList.value.length === 0) return false;
  return filteredList.value.every(r => selectedOrderIds.value.includes(r.orderId));
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedOrderIds.value = [];
  } else {
    selectedOrderIds.value = [...new Set(filteredList.value.map(r => r.orderId))];
  }
};

const selectedTotalCards = computed(() => {
  return consolidationList.value
    .filter(r => selectedOrderIds.value.includes(r.orderId))
    .reduce((sum, r) => sum + (r.totalCards || 0), 0);
});

const openGenerateModal = () => {
  if (selectedOrderIds.value.length === 0) return;
  formSpk.value.notes = `Konsolidasi ${selectedOrderIds.value.length} order cabang (${selectedTotalCards.value} kartu)`;
  showModal.value = true;
};

const submitGenerateSpk = async () => {
  isSubmitting.value = true;
  try {
    const payload = {
      warehouseId: formSpk.value.warehouseId,
      orderIds: selectedOrderIds.value,
      notes: formSpk.value.notes
    };
    const res = await api.post('/production-orders', payload);
    const created = res.data?.data || res.data;
    showModal.value = false;
    alert(`SPK Produksi ${created.productionNumber || ''} berhasil diterbitkan.`);
    if (created.id) {
      router.push(`/production/${created.id}`);
    } else {
      router.push('/production');
    }
  } catch (err) {
    alert('Gagal menerbitkan SPK: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchConsolidation();
  fetchWarehouses();
});
</script>

<style scoped>
.btn-close-modal {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  color: var(--bs-secondary);
  cursor: pointer;
}
.btn-close-modal:hover {
  color: var(--bs-dark);
}
</style>

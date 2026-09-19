<template>
  <div class="production-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Surat Perintah Kerja (SPK) Produksi &amp; Personalisasi</h3>
            <span class="fs-8 text-secondary">Manajemen pencetakan kartu debit emboss nasabah dan warkat berharga kantor pusat</span>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Dashboard</router-link></li>
              <li class="breadcrumb-item">Percetakan</li>
              <li class="breadcrumb-item active" aria-current="page">SPK Produksi</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Stats Cards -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-sm-6 col-md-3">
        <div class="card shadow-xs border-0 border-start border-danger border-3 p-3">
          <span class="fs-9 text-secondary text-uppercase fw-bold">Total SPK Aktif</span>
          <h4 class="mb-0 fw-bold text-dark font-monospace">{{ productionList.length }}</h4>
          <span class="fs-9 text-muted mt-1">Seluruh batch produksi terdaftar</span>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <div class="card shadow-xs border-0 border-start border-warning border-3 p-3">
          <span class="fs-9 text-secondary text-uppercase fw-bold">Menunggu Otorisasi Bahan</span>
          <h4 class="mb-0 fw-bold text-warning font-monospace">{{ pendingMaterialCount }}</h4>
          <span class="fs-9 text-muted mt-1">Status MATERIAL_REQUESTED</span>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <div class="card shadow-xs border-0 border-start border-primary border-3 p-3">
          <span class="fs-9 text-secondary text-uppercase fw-bold">Sedang Dicetak / QC</span>
          <h4 class="mb-0 fw-bold text-primary font-monospace">{{ inProductionCount }}</h4>
          <span class="fs-9 text-muted mt-1">Status IN_PRODUCTION &amp; QC_REVIEW</span>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-md-3">
        <div class="card shadow-xs border-0 border-start border-success border-3 p-3">
          <span class="fs-9 text-secondary text-uppercase fw-bold">Selesai Produksi</span>
          <h4 class="mb-0 fw-bold text-success font-monospace">{{ completedCount }}</h4>
          <span class="fs-9 text-muted mt-1">Siap dipenuhi ke cabang</span>
        </div>
      </div>
    </div>

    <!-- Main Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row align-items-stretch align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'; currentPage = 1"
            >
              Semua SPK
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'action' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'action'; currentPage = 1"
            >
              Perlu Tindakan
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'completed'; currentPage = 1"
            >
              Selesai QC
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/production/consolidation" class="btn btn-sm btn-outline-danger fw-bold shadow-xs fs-8">
            Konsolidasi Order Cabang
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            SPK Baru
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-funnel"></i>
              </span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8" @change="currentPage = 1">
                <option value="ALL">Semua Status SPK</option>
                <option value="DRAFT">DRAFT</option>
                <option value="MATERIAL_REQUESTED">MATERIAL_REQUESTED</option>
                <option value="MATERIAL_APPROVED">MATERIAL_APPROVED</option>
                <option value="IN_PRODUCTION">IN_PRODUCTION</option>
                <option value="QC_REVIEW">QC_REVIEW</option>
                <option value="COMPLETED">COMPLETED</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterStatus !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              Reset
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
                placeholder="Cari No. SPK, Gudang, Catatan..."
                @input="currentPage = 1"
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button" @click="currentPage = 1">Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. SPK Produksi</th>
              <th>Gudang Sumber Bahan</th>
              <th>Rencana / Target Item Selesai</th>
              <th class="text-center">Kuantitas Direncanakan</th>
              <th class="text-center">Hasil QC (Bagus / Gagal)</th>
              <th>Tanggal SPK</th>
              <th class="text-center">Status Alur</th>
              <th class="text-center pe-3" style="width: 130px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="p in paginatedProductionList" :key="p.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                <router-link :to="'/production/' + p.id" class="text-decoration-none text-danger">
                  {{ p.productionNumber }}
                </router-link>
                <div class="fs-9 text-secondary font-monospace">
                  Dibuat oleh: {{ p.createdByUser?.name || 'Operator' }}
                </div>
              </td>
              <td>
                <span class="fw-semibold text-body">{{ p.warehouse?.name || 'Gudang Sentral' }}</span>
                <span class="text-muted d-block font-monospace fs-9">{{ p.warehouse?.code || 'WH-CEN-01' }}</span>
              </td>
              <td>
                <div v-if="p.items && p.items.length > 0">
                  <span v-for="it in p.items" :key="it.id" class="badge bg-secondary-subtle text-secondary me-1">
                    {{ it.itemName || it.itemSku }} ({{ it.qtyPlanned }} {{ it.uom }})
                  </span>
                </div>
                <span v-else class="text-secondary fst-italic fs-9">
                  {{ p.notes || 'Personalisasi Kartu Emboss' }}
                </span>
              </td>
              <td class="text-end font-monospace fw-bold text-body text-center">
                {{ (p.totalQty || 0).toLocaleString('id-ID') }} Keping
              </td>
              <td class="text-center font-monospace fs-8">
                <span class="text-success fw-bold">{{ (p.totalProduced || 0).toLocaleString('id-ID') }}</span>
                <span class="text-secondary"> / </span>
                <span :class="p.totalDamaged > 0 ? 'text-danger fw-bold' : 'text-muted'">{{ (p.totalDamaged || 0).toLocaleString('id-ID') }}</span>
              </td>
              <td class="fs-8 text-secondary">{{ formatDate(p.productionDate || p.createdAt) }}</td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(p.status)">
                  {{ formatStatusLabel(p.status) }}
                </span>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <router-link :to="'/production/' + p.id" class="btn-action-icon text-secondary" title="Buka Detail SPK">
                    <i class="bi bi-eye"></i>
                  </router-link>
                  <router-link :to="'/production/' + p.id + '/manifest'" target="_blank" class="btn-action-icon text-dark" title="Cetak Dokumen SPK">
                    <i class="bi bi-printer"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="!isLoading && filteredProductionList.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data SPK produksi yang sesuai kriteria pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Footer -->
      <PaginationFooter
        v-if="!isLoading && filteredProductionList.length > 0"
        :total="filteredProductionList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Terbitkan SPK Baru -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Penerbitan Surat Perintah Kerja (SPK) Baru
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="saveProduction">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Gudang Sumber Bahan Blank <span class="text-danger">*</span></label>
                  <select v-model="prodForm.warehouseId" class="form-select form-select-sm fs-8" required>
                    <option v-for="wh in warehouseList" :key="wh.id" :value="wh.id">
                      {{ wh.name }} ({{ wh.code }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Pilih Finished Good (Kartu Emboss) <span class="text-danger">*</span></label>
                  <select v-model="prodForm.itemId" class="form-select form-select-sm fs-8" required>
                    <option v-for="it in finishedItemList" :key="it.id" :value="it.id">
                      {{ it.name }} ({{ it.sku }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kuantitas Rencana Cetak <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="prodForm.qty" class="form-control form-control-sm font-monospace fs-8" min="1" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan Keterangan</label>
                  <input type="text" v-model="prodForm.notes" class="form-control form-control-sm fs-8" placeholder="Contoh: Cetak Batch Stok Buffer Cabang Surabaya" />
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" :disabled="isSubmitting">
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
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

const showModal = ref(false);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterStatus = ref('ALL');
const isLoading = ref(false);
const isSubmitting = ref(false);

const productionList = ref([]);
const warehouseList = ref([]);
const finishedItemList = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterStatus.value = 'ALL';
  currentPage.value = 1;
};

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch {
    return val;
  }
};

const fetchProductionList = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/production-orders');
    productionList.value = res.data?.data || res.data || [];
  } catch (err) {
    console.error('Failed to load production orders:', err);
    productionList.value = [];
  } finally {
    isLoading.value = false;
  }
};

const fetchMasters = async () => {
  try {
    const [whRes, itemRes] = await Promise.all([
      api.get('/master/warehouses'),
      api.get('/master/items')
    ]);
    const whs = whRes.data?.data || whRes.data || [];
    warehouseList.value = whs;
    if (whs.length > 0) {
      const central = whs.find(w => w.type === 'CENTRAL_LOGISTICS') || whs[0];
      prodForm.warehouseId = central.id;
    }

    const items = itemRes.data?.data || itemRes.data?.content || itemRes.data || [];
    finishedItemList.value = items.filter(i => (i.sku && i.sku.includes('EMB')) || (i.name && i.name.toLowerCase().includes('emboss')));
    if (finishedItemList.value.length === 0) {
      finishedItemList.value = items.filter(i => i.name && i.name.toLowerCase().includes('kartu'));
    }
    if (finishedItemList.value.length > 0) {
      prodForm.itemId = finishedItemList.value[0].id;
    }
  } catch (e) {
    console.warn('Failed to load masters for modal:', e);
  }
};

const pendingMaterialCount = computed(() => {
  return productionList.value.filter(p => p.status === 'MATERIAL_REQUESTED').length;
});

const inProductionCount = computed(() => {
  return productionList.value.filter(p => ['IN_PRODUCTION', 'QC_REVIEW', 'MATERIAL_APPROVED'].includes(p.status)).length;
});

const completedCount = computed(() => {
  return productionList.value.filter(p => p.status === 'COMPLETED').length;
});

const prodForm = reactive({
  warehouseId: 1,
  itemId: null,
  qty: 100,
  notes: ''
});

const openCreateModal = () => {
  prodForm.qty = 100;
  prodForm.notes = 'Produksi SPK Kartu Emboss Manual';
  showModal.value = true;
};

const saveProduction = async () => {
  isSubmitting.value = true;
  try {
    const payload = {
      warehouseId: prodForm.warehouseId,
      items: [
        {
          itemId: prodForm.itemId,
          qtyPlanned: prodForm.qty
        }
      ],
      notes: prodForm.notes
    };
    const res = await api.post('/production-orders', payload);
    const created = res.data?.data || res.data;
    showModal.value = false;
    alert(`SPK Produksi ${created.productionNumber || ''} berhasil diterbitkan.`);
    await fetchProductionList();
  } catch (err) {
    alert('Gagal menerbitkan SPK: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const filteredProductionList = computed(() => {
  return productionList.value.filter(p => {
    if (activeTab.value === 'action' && p.status === 'COMPLETED') return false;
    if (activeTab.value === 'completed' && p.status !== 'COMPLETED') return false;

    if (filterStatus.value !== 'ALL' && p.status !== filterStatus.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNo = p.productionNumber?.toLowerCase().includes(q);
      const matchWh = p.warehouse?.name?.toLowerCase().includes(q);
      const matchNotes = p.notes?.toLowerCase().includes(q);
      if (!matchNo && !matchWh && !matchNotes) return false;
    }

    return true;
  });
});

const paginatedProductionList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredProductionList.value.slice(start, start + perPage.value);
});

const formatStatusLabel = (status) => {
  switch (status) {
    case 'DRAFT': return 'DRAFT';
    case 'MATERIAL_REQUESTED': return 'REQ. BAHAN';
    case 'MATERIAL_APPROVED': return 'BAHAN DISETUJUI';
    case 'IN_PRODUCTION': return 'PROSES CETAK';
    case 'QC_REVIEW': return 'QC REVIEW';
    case 'COMPLETED': return 'SELESAI';
    case 'CANCELLED': return 'DIBATALKAN';
    default: return status;
  }
};

const badgeClass = (status) => {
  switch (status) {
    case 'COMPLETED': return 'text-bg-success';
    case 'IN_PRODUCTION': return 'text-bg-primary';
    case 'QC_REVIEW': return 'text-bg-info text-white';
    case 'MATERIAL_REQUESTED': return 'text-bg-warning';
    case 'MATERIAL_APPROVED': return 'text-bg-secondary';
    case 'DRAFT': return 'text-bg-light border text-dark';
    default: return 'text-bg-secondary';
  }
};

onMounted(() => {
  fetchProductionList();
  fetchMasters();
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

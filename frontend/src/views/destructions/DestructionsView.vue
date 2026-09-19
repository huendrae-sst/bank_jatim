<template>
  <div class="destructions-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Pemusnahan Barang &amp; Berita Acara (BA)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Retur & Pemusnahan</li>
              <li class="breadcrumb-item active" aria-current="page">Pemusnahan Aset</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


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
              Semua Berita Acara
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'waiting' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'waiting'"
            >
              Menunggu Approval
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'executed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'executed'"
            >
              Selesai Eksekusi
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto">
          <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            Tambah
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
              <select v-model="filterLocation" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Lokasi Eksekusi</option>
                <option value="Kantor Pusat Surabaya">Kantor Pusat Surabaya</option>
                <option value="Gudang Margomulyo">Gudang Margomulyo</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-gear"></i>
              </span>
              <select v-model="filterMethod" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Metode Pemusnahan</option>
                <option value="Penghancuran Mesin Shredder Industri">Mesin Shredder Industri</option>
                <option value="Pembakaran Bersertifikasi (Incinerator)">Insenerator Bersertifikasi</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterLocation !== 'ALL' || filterMethod !== 'ALL'">
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
                placeholder="Cari No. BA, Lokasi, atau Deskripsi Barang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. Berita Acara</th>
              <th>Lokasi Eksekusi</th>
              <th>Nama Barang / Deskripsi</th>
              <th class="text-end">Jumlah Dimusnahkan</th>
              <th>Metode Pemusnahan</th>
              <th>Saksi Audit SKAI</th>
              <th class="text-center">Status</th>
              <th class="text-center pe-3" style="width: 120px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="d in paginatedRecords" :key="d.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                <a href="#" @click.prevent="openDetailModal(d)" class="text-decoration-none text-danger">
                  {{ d.baNo }}
                </a>
              </td>
              <td class="fw-semibold text-body">{{ d.location }}</td>
              <td>{{ d.item }}</td>
              <td class="text-end font-monospace fw-bold text-danger">{{ d.qty.toLocaleString('id-ID') }}</td>
              <td class="fs-8 text-secondary">{{ d.method }}</td>
              <td class="fs-8 text-body">{{ d.witness }}</td>
              <td class="text-center">
                <span class="badge fs-9" :class="badgeClass(d.status)">{{ d.status }}</span>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <button type="button" class="btn-action-icon text-secondary border-0 bg-transparent" @click="openDetailModal(d)" title="Lihat Detail">
                    <i class="bi bi-eye"></i>
                  </button>
                  <router-link :to="'/destructions/' + d.id + '/berita-acara'" target="_blank" class="btn-action-icon text-dark" title="Cetak Berita Acara">
                    <i class="bi bi-printer"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRecords.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data berita acara pemusnahan yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredRecords.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form Usulkan Pemusnahan -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showCreateModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Form Usulan Pemusnahan Aset Persediaan
            </h6>
            <button type="button" class="btn-close" @click="showCreateModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="saveDestruction">
            <div class="modal-body p-3 fs-8">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Lokasi Eksekusi / Gudang <span class="text-danger">*</span></label>
                  <select v-model="destructionForm.location" class="form-select form-select-sm fs-8" required>
                    <option value="Kantor Pusat Surabaya">Kantor Pusat Surabaya</option>
                    <option value="Gudang Margomulyo">Gudang Margomulyo</option>
                    <option value="Gudang Cabang Malang">Gudang Cabang Malang</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Metode Pemusnahan <span class="text-danger">*</span></label>
                  <select v-model="destructionForm.method" class="form-select form-select-sm fs-8" required>
                    <option value="Penghancuran Mesin Shredder Industri">Penghancuran Mesin Shredder Industri</option>
                    <option value="Pembakaran Bersertifikasi (Incinerator)">Pembakaran Bersertifikasi (Incinerator)</option>
                    <option value="Peleburan Kimiawi">Peleburan Kimiawi</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alasan Pemusnahan <span class="text-danger">*</span></label>
                  <select v-model="destructionForm.reason" class="form-select form-select-sm fs-8" required>
                    <option value="Chip Kartu ATM / KUE Kadaluarsa (Expired)">Chip Kartu ATM / KUE Kadaluarsa (Expired)</option>
                    <option value="Barang Rusak Total Tidak Dapat Dipakai">Barang Rusak Total Tidak Dapat Dipakai</option>
                    <option value="Desain / Format Discontinue (Tidak Berlaku)">Desain / Format Discontinue (Tidak Berlaku)</option>
                    <option value="Gagal Proses Personalisasi / Cacat Emboss">Gagal Proses Personalisasi / Cacat Emboss</option>
                    <option value="Lainnya">Lainnya</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Saksi Audit SKAI &amp; Kepatuhan <span class="text-danger">*</span></label>
                  <input type="text" v-model="destructionForm.witness" class="form-control form-control-sm fs-8" placeholder="Contoh: SKAI & Divisi Kepatuhan" required />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Barang / Deskripsi Persediaan <span class="text-danger">*</span></label>
                  <input type="text" v-model="destructionForm.item" class="form-control form-control-sm fs-8" placeholder="Contoh: Blanko Kartu ATM Magnetik Kadaluarsa" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Unit <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="destructionForm.qty" class="form-control form-control-sm fs-8 font-monospace text-danger fw-bold" min="1" required />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Penjelasan &amp; Kronologi Pemusnahan</label>
                  <textarea v-model="destructionForm.notes" class="form-control form-control-sm fs-8" rows="2" placeholder="Uraikan latar belakang teknis atau dasar persetujuan pemusnahan..."></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showCreateModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                Usulkan Pemusnahan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Detail Pemusnahan -->
    <div v-if="showDetailModal && selectedDestruction" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showDetailModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Detail Berita Acara Pemusnahan - <span class="font-monospace text-danger">{{ selectedDestruction.baNo }}</span>
            </h6>
            <button type="button" class="btn-close" @click="showDetailModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-3 fs-8">
            <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
              <div>
                <span class="fs-9 text-uppercase text-secondary fw-bold d-block">Status Pemusnahan</span>
                <span class="badge fs-8 mt-1" :class="badgeClass(selectedDestruction.status)">{{ selectedDestruction.status }}</span>
              </div>
              <div class="text-end">
                <span class="fs-9 text-uppercase text-secondary fw-bold d-block">Nomor Berita Acara</span>
                <span class="font-monospace fw-bold text-danger fs-7">{{ selectedDestruction.baNo }}</span>
              </div>
            </div>

            <div class="row g-3 mb-3">
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Lokasi Eksekusi</span>
                  <span class="fw-bold text-body fs-7">{{ selectedDestruction.location }}</span>
                </div>
              </div>
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Metode Pemusnahan</span>
                  <span class="fw-bold text-body fs-7">{{ selectedDestruction.method }}</span>
                </div>
              </div>
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Saksi Audit SKAI</span>
                  <span class="fw-bold text-body fs-7">{{ selectedDestruction.witness }}</span>
                </div>
              </div>
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Jumlah Fisik Dimusnahkan</span>
                  <span class="font-monospace fw-bold text-danger fs-7">{{ selectedDestruction.qty.toLocaleString('id-ID') }} unit</span>
                </div>
              </div>
            </div>

            <div class="table-responsive border rounded mb-3">
              <table class="table table-sm table-bordered align-middle mb-0 fs-8">
                <thead class="table-light text-secondary text-uppercase fs-9">
                  <tr>
                    <th class="ps-3">Deskripsi Barang Persediaan</th>
                    <th class="text-end">Jumlah Dimusnahkan</th>
                    <th>Metode Pemusnahan</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td class="ps-3 fw-bold text-body">{{ selectedDestruction.item }}</td>
                    <td class="text-end font-monospace fw-bold text-danger">{{ selectedDestruction.qty.toLocaleString('id-ID') }} unit</td>
                    <td class="text-secondary">{{ selectedDestruction.method }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <router-link :to="'/destructions/' + selectedDestruction.id + '/berita-acara'" target="_blank" class="btn btn-sm btn-outline-danger px-3 fs-8">
              Cetak Berita Acara
            </router-link>
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showDetailModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

const showCreateModal = ref(false);
const showDetailModal = ref(false);
const selectedDestruction = ref(null);
const isLoading = ref(false);

const destructionForm = reactive({
  location: 'Gudang Margomulyo',
  method: 'Penghancuran Mesin Shredder Industri',
  reason: 'Chip Kartu ATM / KUE Kadaluarsa (Expired)',
  witness: 'SKAI & Divisi Kepatuhan',
  itemId: 1,
  warehouseId: 1,
  qty: 1,
  notes: ''
});

const openCreateModal = () => {
  destructionForm.location = 'Gudang Margomulyo';
  destructionForm.method = 'Penghancuran Mesin Shredder Industri';
  destructionForm.reason = 'Chip Kartu ATM / KUE Kadaluarsa (Expired)';
  destructionForm.witness = 'SKAI & Divisi Kepatuhan';
  destructionForm.itemId = 1;
  destructionForm.warehouseId = 1;
  destructionForm.qty = 1;
  destructionForm.notes = '';
  showCreateModal.value = true;
};

const openDetailModal = (d) => {
  selectedDestruction.value = d;
  showDetailModal.value = true;
};

const mapDestruction = (d, idx) => ({
  id: d.id,
  baNo: d.baNumber || d.referenceNumber || d.baNo || `BA-MUSNAH-2026-${String(idx + 1).padStart(3, '0')}`,
  location: d.location || d.warehouse?.name || 'Gudang Margomulyo',
  item: d.itemName || d.item?.name || d.item || 'Barang Persediaan',
  qty: Number(d.qty || d.qtyOut || 0),
  method: d.method || 'Penghancuran Mesin Shredder Industri',
  witness: d.witness || 'SKAI & Divisi Kepatuhan',
  status: d.status || 'SELESAI / EXECUTED'
});

const records = ref([]);

const fetchDestructions = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/ledgers', {
      params: { transactionType: 'DESTRUCTION', size: 100 }
    });
    const items = res.data?.data?.content || res.data?.content || res.data || [];
    if (Array.isArray(items) && items.length > 0) {
      records.value = items.map(mapDestruction);
    }
  } catch (err) {
    records.value = [];
    console.warn('Failed loading destructions from backend:', err);
  } finally {
    isLoading.value = false;
  }
};

const saveDestruction = async () => {
  try {
    await api.post('/inventory/destructions', {
      warehouseId: destructionForm.warehouseId || 1,
      itemId: destructionForm.itemId || 1,
      qty: destructionForm.qty,
      baNo: `BA-MUSNAH-2026-${String(records.value.length + 1).padStart(3, '0')}`,
      notes: `${destructionForm.method} - ${destructionForm.notes || 'Pemusnahan fisik'}`
    });
    showCreateModal.value = false;
    await fetchDestructions();
  } catch (err) {
    console.error('Failed to save destruction', err);
    alert('Gagal menyimpan pemusnahan: ' + (err.response?.data?.message || err.message));
  }
};

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterLocation = ref('ALL');
const filterMethod = ref('ALL');

const resetFilters = () => {
  searchQuery.value = '';
  filterLocation.value = 'ALL';
  filterMethod.value = 'ALL';
  currentPage.value = 1;
};

const waitingApprovalCount = computed(() => {
  return records.value.filter(r => r.status.includes('MENUNGGU')).length;
});

const executedCount = computed(() => {
  return records.value.filter(r => r.status.includes('EXECUTED')).length;
});

const filteredRecords = computed(() => {
  return records.value.filter(d => {
    if (activeTab.value === 'waiting' && !d.status.includes('MENUNGGU')) return false;
    if (activeTab.value === 'executed' && !d.status.includes('EXECUTED')) return false;

    if (filterLocation.value !== 'ALL' && d.location !== filterLocation.value) return false;
    if (filterMethod.value !== 'ALL' && d.method !== filterMethod.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNo = d.baNo.toLowerCase().includes(q);
      const matchLoc = d.location.toLowerCase().includes(q);
      const matchItem = d.item.toLowerCase().includes(q);
      if (!matchNo && !matchLoc && !matchItem) return false;
    }

    return true;
  });
});

const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredRecords.value.slice(start, start + perPage.value);
});

const badgeClass = (status) => {
  if (status.includes('EXECUTED')) return 'text-bg-success';
  if (status.includes('MENUNGGU')) return 'text-bg-warning';
  return 'text-bg-secondary';
};

onMounted(() => {
  fetchDestructions();
});
</script>

<style scoped>
</style>

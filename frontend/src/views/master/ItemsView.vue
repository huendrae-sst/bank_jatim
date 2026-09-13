<template>
  <div class="items-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Barang & Katalog SKU</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/master/items" class="text-decoration-none text-body">Master Data</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Katalog SKU</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-boxes"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total SKU Terdaftar</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ items.length }} SKU</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Katalog Resmi Bank Jatim</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-check-circle-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">SKU Aktif Operasional</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">{{ items.filter(i => i.status === 'AKTIF').length }} SKU</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Dapat Dipesan Unit Cabang</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-shield-exclamation"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Buffer Safety Stock</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">5 Kategori</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 80%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Ambang Batas Minimum Gudang</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-tags-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Kelompok Barang</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">4 Kelompok</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 70%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Warkat, Kartu, ATK, Cetakan</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            <i class="bi bi-card-checklist text-danger me-2"></i>Katalog Master Barang (SKU Terstandarisasi)
          </h3>
          <span class="badge text-bg-danger fs-9">{{ items.length }} SKU</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Tambah SKU Barang
          </button>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tag"></i></span>
              <select v-model="filterCategory" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Kategori</option>
                <option v-for="category in categoryOptions" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-activity"></i></span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Status</option>
                <option value="AKTIF">AKTIF</option>
                <option value="NONAKTIF">NONAKTIF</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterCategory || filterStatus">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Kode SKU, Nama Barang..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                <i class="bi bi-search me-1"></i> Cari
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
              <th class="ps-3 py-2 text-uppercase fs-9">Kode SKU</th>
              <th class="py-2 text-uppercase fs-9">Nama Barang</th>
              <th class="py-2 text-uppercase fs-9">Kategori</th>
              <th class="py-2 text-uppercase fs-9">Satuan (UOM)</th>
              <th class="py-2 text-uppercase fs-9 text-end">Harga Acuan</th>
              <th class="py-2 text-uppercase fs-9 text-end">Safety Stock</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedItems" :key="item.id">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ item.sku }}</td>
              <td class="py-2 fw-semibold text-body">{{ item.name }}</td>
              <td class="py-2"><span class="badge text-bg-light border text-secondary">{{ item.category }}</span></td>
              <td class="py-2 font-monospace fs-8">{{ item.uom }}</td>
              <td class="py-2 text-end font-monospace text-body">Rp {{ item.price.toLocaleString('id-ID') }}</td>
              <td class="py-2 text-end font-monospace text-warning fw-bold">{{ item.safetyStock.toLocaleString('id-ID') }}</td>
              <td class="py-2"><span class="badge text-bg-success">{{ item.status }}</span></td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(item)" title="Edit SKU"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteItem(item)" title="Hapus SKU"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredItems.length === 0">
              <td colspan="8" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data master barang yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredItems.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Modal Form (Tambah / Edit SKU) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              {{ isEditMode ? 'Edit SKU Barang' : 'Tambah SKU Barang Baru' }}
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup"><i class="bi bi-x-lg"></i></button>
          </div>
          <form @submit.prevent="saveItem">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kode SKU <span class="text-danger">*</span></label>
                  <input type="text" v-model="itemForm.sku" class="form-control form-control-sm font-monospace" placeholder="Contoh: SKU-TB-01" required />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fw-bold mb-1">Nama Barang <span class="text-danger">*</span></label>
                  <input type="text" v-model="itemForm.name" class="form-control form-control-sm" placeholder="Contoh: Buku Tabungan Baru" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kategori Barang</label>
                  <select v-model="itemForm.category" class="form-select form-select-sm">
                    <option value="BUKU_TABUNGAN">BUKU_TABUNGAN</option>
                    <option v-for="category in categoryOptions" :key="category" :value="category">{{ category }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Satuan (UOM)</label>
                  <select v-model="itemForm.uom" class="form-select form-select-sm font-monospace">
                    <option value="BUKU">BUKU</option>
                    <option value="PCS">PCS</option>
                    <option value="BOX">BOX</option>
                    <option value="ROL">ROL</option>
                    <option value="RIM">RIM</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Status</label>
                  <select v-model="itemForm.status" class="form-select form-select-sm">
                    <option value="AKTIF">AKTIF</option>
                    <option value="NONAKTIF">NONAKTIF</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Harga Acuan Satuan (Rp) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="itemForm.price" class="form-control form-control-sm font-monospace" min="0" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Min Safety Stock</label>
                  <input type="number" v-model.number="itemForm.safetyStock" class="form-control form-control-sm font-monospace" min="0" />
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                <i class="bi bi-check2-circle me-1"></i> Simpan SKU
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

const showModal = ref(false);
const isEditMode = ref(false);
const editingId = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterCategory = ref('');
const filterStatus = ref('');

const items = ref([]);

onMounted(() => {
  loadItems();
});

const loadItems = async () => {
  const response = await api.get('/master/items');
  items.value = (response.data || []).map((item) => ({
    id: item.id,
    sku: item.sku,
    name: item.name,
    category: item.category?.name || '-',
    uom: item.uom,
    price: Number(item.estimatedUnitPrice || 0),
    safetyStock: Number(item.safetyStock || 0),
    status: item.isActive ? 'AKTIF' : 'NONAKTIF'
  }));
};

const categoryOptions = computed(() => Array.from(new Set(items.value.map((item) => item.category))).filter(Boolean).sort());

const itemForm = reactive({
  sku: '',
  name: '',
  category: 'BUKU_TABUNGAN',
  uom: 'BUKU',
  price: 0,
  safetyStock: 0,
  status: 'AKTIF'
});

const resetFilters = () => {
  searchQuery.value = '';
  filterCategory.value = '';
  filterStatus.value = '';
  currentPage.value = 1;
};

const filteredItems = computed(() => {
  return items.value.filter(item => {
    const matchQuery = !searchQuery.value ||
      item.sku.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchCat = !filterCategory.value || item.category === filterCategory.value;
    const matchStat = !filterStatus.value || item.status === filterStatus.value;
    return matchQuery && matchCat && matchStat;
  });
});

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredItems.value.slice(start, start + perPage.value);
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingId.value = null;
  Object.assign(itemForm, {
    sku: '',
    name: '',
    category: 'BUKU_TABUNGAN',
    uom: 'BUKU',
    price: 0,
    safetyStock: 0,
    status: 'AKTIF'
  });
  showModal.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  editingId.value = item.id;
  Object.assign(itemForm, {
    sku: item.sku,
    name: item.name,
    category: item.category,
    uom: item.uom,
    price: item.price,
    safetyStock: item.safetyStock,
    status: item.status
  });
  showModal.value = true;
};

const saveItem = () => {
  alert('Penyimpanan master barang belum tersedia di backend production.');
  showModal.value = false;
};

const deleteItem = (item) => {
  alert(`Penghapusan SKU "${item.name}" belum tersedia di backend production.`);
};
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

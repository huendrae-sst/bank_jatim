<template>
  <div class="initial-stock-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Saldo Awal Gudang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/inventory/balances" class="text-decoration-none text-danger">Persediaan</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary fw-semibold" aria-current="page">
                Saldo Awal Gudang
              </li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Feedback Notification Banner -->
    <div v-if="alertMessage" :class="`alert alert-${alertType} alert-dismissible fade show fs-8 py-2 px-3 shadow-xs`" role="alert">
      <i :class="alertType === 'success' ? 'bi bi-check-circle-fill me-2' : 'bi bi-exclamation-triangle-fill me-2'"></i>
      <span>{{ alertMessage }}</span>
      <button type="button" class="btn-close py-2" @click="alertMessage = ''" aria-label="Close"></button>
    </div>

    <!-- 2. Warehouse & Cut-off Date Selector Header Bar -->
    <div class="card shadow-xs border-0 rounded-3 p-3 bg-body">
      <div class="d-flex flex-column flex-lg-row lg-align-items-center justify-content-between gap-3">
        <!-- Warehouse Info Display -->
        <div class="d-flex align-items-center gap-3">
          <div class="p-2 rounded-3 bg-danger-subtle text-danger fs-4 d-flex align-items-center justify-content-center" style="width: 44px; height: 44px;">
            <i class="bi bi-box-seam"></i>
          </div>
          <div>
            <div class="text-secondary fw-bold text-uppercase fs-9" style="letter-spacing: 0.5px;">
              Lokasi Gudang Penempatan Saldo Awal:
            </div>
            <div class="text-body fw-bold fs-6 d-flex align-items-center flex-wrap gap-2">
              <span>{{ currentWarehouse?.name || 'Pilih Gudang' }}</span>
              <span
                v-if="currentWarehouse"
                :class="currentWarehouse.type === 'CENTRAL_LOGISTICS' ? 'badge bg-warning-subtle text-warning-emphasis' : 'badge bg-secondary-subtle text-secondary-emphasis'"
                class="fs-9"
              >
                {{ currentWarehouse.type === 'CENTRAL_LOGISTICS' ? 'Gudang Logistik Pusat' : 'Penyimpanan Cabang' }}
              </span>
              <span v-if="currentWarehouse" class="text-secondary fw-normal font-monospace fs-8">
                ({{ currentWarehouse.code }} • {{ currentWarehouse.organization?.city || currentWarehouse.city || 'Surabaya' }})
              </span>
            </div>
          </div>
        </div>

        <!-- Action Controls: Cut-off Date, Warehouse Selector, Download, Import & History Link -->
        <div class="d-flex flex-wrap align-items-center gap-2">
          <!-- Cutoff Date Selector -->
          <div class="d-inline-flex align-items-center gap-2 bg-body-tertiary border rounded-3 px-2 py-1 shadow-2xs">
            <span class="text-secondary fw-bold fs-9 text-nowrap">Tanggal Cut-off:</span>
            <input
              type="date"
              v-model="cutoffDate"
              class="form-control form-control-sm border bg-body fw-bold fs-8"
              style="width: 140px;"
            />
          </div>

          <!-- Searchable Warehouse Dropdown Button -->
          <div class="position-relative" ref="warehouseDropdownRef">
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary fw-bold fs-8 d-inline-flex align-items-center gap-2 shadow-2xs"
              @click="warehouseDropdownOpen = !warehouseDropdownOpen"
            >
              <span>Ganti Lokasi Gudang</span>
            </button>

            <!-- Searchable Dropdown Popup -->
            <div
              v-if="warehouseDropdownOpen"
              class="position-absolute end-0 mt-2 bg-body rounded-3 border shadow-lg overflow-hidden"
              style="width: 320px; max-width: 90vw; z-index: 1060;"
            >
              <!-- Search Input in Dropdown -->
              <div class="p-2 border-bottom bg-body-tertiary">
                <input
                  type="text"
                  v-model="warehouseSearch"
                  placeholder="Cari gudang atau cabang..."
                  class="form-control form-control-sm fs-8"
                />
              </div>

              <!-- Warehouse List Options -->
              <div class="overflow-y-auto" style="max-height: 240px;">
                <div
                  v-for="wh in filteredWarehouses"
                  :key="wh.id"
                  @click="selectWarehouse(wh)"
                  class="p-2 d-flex align-items-center justify-content-between border-bottom cursor-pointer dropdown-wh-item"
                  :class="{ 'bg-danger-subtle text-danger fw-bold': wh.id === selectedWarehouseId }"
                >
                  <div>
                    <div class="fs-8 fw-semibold">{{ wh.name }}</div>
                    <div class="fs-9 text-secondary font-monospace">{{ wh.code }} • {{ wh.organization?.city || wh.city || '-' }}</div>
                  </div>
                  <span
                    class="badge fs-9"
                    :class="wh.type === 'CENTRAL_LOGISTICS' ? 'bg-warning-subtle text-warning-emphasis' : 'bg-secondary-subtle text-secondary-emphasis'"
                  >
                    {{ wh.type === 'CENTRAL_LOGISTICS' ? 'PUSAT' : 'CABANG' }}
                  </span>
                </div>
                <div v-if="filteredWarehouses.length === 0" class="p-3 text-center text-secondary fs-8">
                  Tidak ada gudang ditemukan
                </div>
              </div>
            </div>
          </div>

          <!-- Download Template Button -->
          <button
            type="button"
            @click="downloadTemplate"
            class="btn btn-sm btn-outline-secondary fw-semibold fs-8 d-inline-flex align-items-center gap-1 shadow-2xs"
            title="Download Template Format Excel/CSV"
          >
            <span>Download Template</span>
          </button>

          <!-- Import Button -->
          <button
            type="button"
            @click="importModalOpen = true"
            class="btn btn-sm btn-success text-white fw-semibold fs-8 d-inline-flex align-items-center gap-1 shadow-2xs"
          >
            <span>Import Saldo Awal</span>
          </button>

          <!-- History Button -->
          <router-link
            to="/inventory/ledgers"
            class="btn btn-sm btn-outline-danger fw-semibold fs-8 d-inline-flex align-items-center gap-1 shadow-2xs"
          >
            <span>Riwayat Transaksi</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 3. Live Summary Metrics (AdminLTE 4 Info-Boxes) -->
    <div class="row g-3">
      <!-- 1. Total SKU -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-primary"><i class="bi bi-box-seam"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total SKU Terdaftar</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ totalSKU }}</span>
            <span class="fs-9 text-secondary">Katalog barang aktif di sistem</span>
          </div>
        </div>
      </div>

      <!-- 2. SKU Terisi Saldo -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-check2-circle"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">SKU Terisi Saldo</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">{{ filledSKUCount }} SKU</span>
            <span class="fs-9 text-secondary">{{ totalSKU - filledSKUCount }} SKU belum ada saldo (0)</span>
          </div>
        </div>
      </div>

      <!-- 3. Total Kuantitas Fisik -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info"><i class="bi bi-layers"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total Kuantitas Fisik</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-info-emphasis">
              {{ totalQty.toLocaleString('id-ID') }} Unit
            </span>
            <span class="fs-9 text-secondary">Akumulasi kuantitas baik & rusak</span>
          </div>
        </div>
      </div>

      <!-- 4. Total Valuasi Saldo Awal -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-cash-stack"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total Valuasi Saldo Awal</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-danger">
              Rp {{ totalValuation.toLocaleString('id-ID') }}
            </span>
            <span class="fs-9 text-secondary">Kredit RAK / Ekuitas Awal (31101)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. Items Worksheet Form -->
    <form @submit.prevent="submitInitialStock">
      <div class="card shadow-xs border-0 rounded-3">
        <!-- Filter Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Kategori Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tag"></i></span>
                <select v-model="categoryFilter" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="all">Semua Kategori</option>
                  <option v-for="cat in availableCategories" :key="cat" :value="cat">
                    {{ cat }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Status Stok Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
                <select v-model="statusFilter" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="all">Semua Saldo</option>
                  <option value="filled">Hanya Terisi Saldo (&gt;0)</option>
                  <option value="zero">Saldo Belum Terisi (0)</option>
                </select>
              </div>
            </div>

            <!-- Reset Filter Button -->
            <div class="col-auto" v-if="searchQuery || categoryFilter !== 'all' || statusFilter !== 'all'">
              <button
                type="button"
                @click="resetFilters"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                Reset Filter
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="searchQuery"
                  class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                  placeholder="Cari SKU atau nama item..."
                />
                <button
                  type="button"
                  v-if="searchQuery"
                  @click="searchQuery = ''"
                  class="btn btn-sm btn-outline-secondary border-start-0 border-end-0"
                  title="Clear"
                >
                  <i class="bi bi-x"></i>
                </button>
                <button type="button" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">Cari</button>
              </div>
            </div>

            <!-- Batch Reset Button -->
            <div class="col-auto">
              <button
                type="button"
                @click="resetAllInputs"
                class="btn btn-sm btn-outline-secondary fs-8"
                title="Kosongkan Semua Nilai Kuantitas"
              >
                Reset Input
              </button>
            </div>
          </div>
        </div>

        <!-- Table View -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0 fs-8">
              <thead class="bg-body-secondary text-secondary border-bottom">
                <tr>
                  <th class="ps-3 py-2 text-uppercase fs-9" style="min-width: 220px;">Item & SKU</th>
                  <th class="py-2 text-uppercase fs-9" style="width: 140px;">Kategori</th>
                  <th class="py-2 text-center text-uppercase fs-9" style="width: 80px;">Satuan</th>
                  <th class="py-2 text-center text-uppercase fs-9" style="width: 170px;">Qty Kondisi Baik</th>
                  <th class="py-2 text-center text-uppercase fs-9" style="width: 170px;">Qty Kondisi Rusak</th>
                  <th class="py-2 text-end text-uppercase fs-9" style="width: 160px;">Harga Satuan (Rp)</th>
                  <th class="py-2 pe-3 text-end text-uppercase fs-9" style="width: 180px;">Subtotal Valuasi (Rp)</th>
                </tr>
              </thead>
              <tbody>
                <!-- Table Rows -->
                <tr
                  v-for="row in filteredRows"
                  :key="row.itemId"
                  :class="{ 'table-success': (Number(row.qtyGood) || 0) > 0 || (Number(row.qtyDamaged) || 0) > 0 }"
                >
                  <td class="ps-3 py-2">
                    <div class="fw-bold text-body">{{ row.name }}</div>
                    <div class="text-secondary font-monospace fs-9">{{ row.sku }}</div>
                  </td>
                  <td class="py-2">
                    <span class="badge bg-secondary-subtle text-secondary-emphasis fs-9">{{ row.category || 'Umum' }}</span>
                  </td>
                  <td class="py-2 text-center font-monospace text-secondary fs-8">{{ row.uom || '-' }}</td>

                  <!-- Qty Good Stepper -->
                  <td class="py-2 text-center">
                    <div class="input-group input-group-sm justify-content-center mx-auto" style="max-width: 130px;">
                      <button
                        type="button"
                        @click="decrementQty(row, 'qtyGood')"
                        class="btn btn-outline-secondary px-2"
                      >-</button>
                      <input
                        type="number"
                        v-model.number="row.qtyGood"
                        min="0"
                        class="form-control form-control-sm text-center fw-bold font-monospace"
                      />
                      <button
                        type="button"
                        @click="incrementQty(row, 'qtyGood')"
                        class="btn btn-outline-secondary px-2"
                      >+</button>
                    </div>
                  </td>

                  <!-- Qty Damaged Stepper -->
                  <td class="py-2 text-center">
                    <div class="input-group input-group-sm justify-content-center mx-auto" style="max-width: 130px;">
                      <button
                        type="button"
                        @click="decrementQty(row, 'qtyDamaged')"
                        class="btn btn-outline-secondary px-2"
                      >-</button>
                      <input
                        type="number"
                        v-model.number="row.qtyDamaged"
                        min="0"
                        class="form-control form-control-sm text-center fw-bold font-monospace text-danger"
                      />
                      <button
                        type="button"
                        @click="incrementQty(row, 'qtyDamaged')"
                        class="btn btn-outline-secondary px-2"
                      >+</button>
                    </div>
                  </td>

                  <!-- Unit Cost -->
                  <td class="py-2 text-end">
                    <input
                      type="number"
                      v-model.number="row.unitCost"
                      min="0"
                      step="any"
                      class="form-control form-control-sm text-end font-monospace ms-auto"
                      style="max-width: 140px;"
                    />
                  </td>

                  <!-- Subtotal -->
                  <td
                    class="py-2 pe-3 text-end font-monospace fw-bold fs-8"
                    :class="getRowSubtotal(row) > 0 ? 'text-dark' : 'text-secondary'"
                  >
                    Rp {{ getRowSubtotal(row).toLocaleString('id-ID') }}
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="!isLoading && filteredRows.length === 0">
                  <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                    <span>Tidak ada barang yang sesuai dengan kriteria pencarian atau filter.</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Notes, Accounting Explanation & Submission Footer -->
        <div class="card-footer bg-body-tertiary p-3 border-top">
          <div class="row g-3 align-items-end">
            <div class="col-12 col-lg-8">
              <label class="form-label fw-bold fs-8 text-secondary text-uppercase mb-1">
                Berita Acara / Keterangan Penetapan Saldo Awal <span class="text-danger">*</span>
              </label>
              <textarea
                v-model="notes"
                rows="2"
                required
                class="form-control form-control-sm fs-8"
                placeholder="Contoh: Penetapan Saldo Awal Cut-Off Go-Live Gudang..."
              ></textarea>
              <div class="form-text fs-9 text-secondary mt-1">
                Kuantitas kondisi baik akan masuk sebagai stok siap pakai (on hand), sedangkan kuantitas rusak dialokasikan ke penampungan barang rusak (damaged stock).
              </div>
            </div>

            <div class="col-12 col-lg-4 d-flex flex-column align-items-lg-end gap-2">
              <div class="text-end">
                <span class="fs-8 text-secondary">Total Valuasi: </span>
                <span class="fs-5 fw-bold font-monospace text-danger">
                  Rp {{ totalValuation.toLocaleString('id-ID') }}
                </span>
              </div>
              <button
                type="submit"
                :disabled="isSubmitting || filledSKUCount === 0"
                class="btn btn-danger fw-bold shadow-xs px-4 py-2 w-100 w-lg-auto fs-8"
              >
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
                <span>Simpan & Posting Saldo Awal</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </form>

    <!-- ==================== IMPORT MODAL ==================== -->
    <div
      v-if="importModalOpen"
      class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3"
      @click.self="importModalOpen = false"
    >
      <div class="card shadow-2xl border border-secondary-subtle modal-content-custom overflow-hidden bg-body">
        <!-- Modal Header -->
        <div class="card-header bg-danger text-white py-2 px-3 d-flex justify-content-between align-items-center">
          <h5 class="modal-title fs-6 fw-bold mb-0">Import Saldo Awal Excel / CSV</h5>
          <button type="button" @click="importModalOpen = false" class="btn-close btn-close-white" aria-label="Close"></button>
        </div>

        <!-- Modal Form -->
        <form @submit.prevent="handleImportSubmit">
          <div class="modal-body p-3 fs-8 space-y-3">
            <div class="p-2 bg-body-tertiary rounded-3 border">
              <div class="text-secondary fs-9 fw-bold text-uppercase mb-1">Gudang Tujuan Impor</div>
              <div class="fw-bold text-body fs-7">{{ currentWarehouse?.name }} ({{ currentWarehouse?.code }})</div>
              <div class="text-secondary fs-9">
                {{ currentWarehouse?.organization?.name || 'Bank Jatim' }} - {{ currentWarehouse?.organization?.city || currentWarehouse?.city || '-' }}
              </div>
            </div>

            <div>
              <label class="form-label fw-bold text-secondary fs-8">
                File CSV Format (*.csv) <span class="text-danger">*</span>
              </label>
              <input
                type="file"
                ref="importFileInputRef"
                accept=".csv,text/csv,text/plain"
                required
                class="form-control form-control-sm fs-8"
                @change="onImportFileChange"
              />
              <div class="form-text fs-9 text-secondary mt-1">
                Gunakan format file template CSV Bank Jatim. Kolom mencakup SKU, Nama Barang, Qty Baik, Qty Rusak, dan Harga Satuan.
              </div>
            </div>

            <div class="p-2 bg-body-tertiary rounded-2 border d-flex align-items-center justify-content-between">
              <span class="fs-9 text-secondary">Belum memiliki template impor?</span>
              <button
                type="button"
                @click="downloadTemplate"
                class="btn btn-sm btn-outline-danger fs-9 py-1 px-2"
              >
                Download Template CSV
              </button>
            </div>

            <div class="form-check p-2 bg-body-tertiary rounded border ms-0">
              <input
                class="form-check-input ms-0 me-2"
                type="checkbox"
                v-model="directPost"
                id="directPostCheck"
              />
              <label class="form-check-label fs-8 fw-semibold text-body" for="directPostCheck">
                Langsung Posting ke Buku Besar (Stock Ledger & Balances)
              </label>
              <div class="form-text fs-9 text-secondary ms-0 mt-1">
                Jika tidak dicentang, data file akan dimuat terlebih dahulu ke lembar kerja di layar untuk ditinjau sebelum disimpan.
              </div>
            </div>

            <div>
              <label class="form-label fw-bold text-secondary fs-8">Catatan Impor (Opsional)</label>
              <input
                type="text"
                v-model="importNotes"
                class="form-control form-control-sm fs-8"
                placeholder="Contoh: Impor data migrasi saldo awal dari sistem lama..."
              />
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary p-2 border-top d-flex justify-content-between">
            <button
              type="button"
              @click="importModalOpen = false"
              class="btn btn-sm btn-outline-secondary fs-8 px-3"
            >
              Batal
            </button>
            <button
              type="submit"
              :disabled="isImporting"
              class="btn btn-sm btn-danger fw-bold fs-8 px-4"
            >
              <span v-if="isImporting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              <span>Unggah & Proses File</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';
import { exportToCsv } from '@/utils/exportHelper';

const route = useRoute();

// State
const warehouses = ref([]);
const selectedWarehouseId = ref(null);
const warehouseDropdownOpen = ref(false);
const warehouseDropdownRef = ref(null);
const warehouseSearch = ref('');
const cutoffDate = ref(new Date().toISOString().split('T')[0]);
const rows = ref([]);
const isLoading = ref(false);
const isSubmitting = ref(false);
const isImporting = ref(false);

const searchQuery = ref('');
const categoryFilter = ref('all');
const statusFilter = ref('all');
const notes = ref('');

const alertMessage = ref('');
const alertType = ref('success');

// Import modal state
const importModalOpen = ref(false);
const importFile = ref(null);
const importFileInputRef = ref(null);
const directPost = ref(false);
const importNotes = ref('');

// Computed
const currentWarehouse = computed(() => {
  return warehouses.value.find(w => w.id === selectedWarehouseId.value) || null;
});

const filteredWarehouses = computed(() => {
  if (!warehouseSearch.value.trim()) return warehouses.value;
  const q = warehouseSearch.value.toLowerCase();
  return warehouses.value.filter(w =>
    (w.name && w.name.toLowerCase().includes(q)) ||
    (w.code && w.code.toLowerCase().includes(q)) ||
    (w.organization?.name && w.organization.name.toLowerCase().includes(q)) ||
    (w.organization?.city && w.organization.city.toLowerCase().includes(q)) ||
    (w.city && w.city.toLowerCase().includes(q))
  );
});

const availableCategories = computed(() => {
  const set = new Set();
  rows.value.forEach(r => {
    if (r.category) set.add(r.category);
  });
  return Array.from(set).sort();
});

const filteredRows = computed(() => {
  const q = searchQuery.value.trim().toLowerCase();
  return rows.value.filter(row => {
    const matchSearch = !q ||
      (row.name && row.name.toLowerCase().includes(q)) ||
      (row.sku && row.sku.toLowerCase().includes(q)) ||
      (row.category && row.category.toLowerCase().includes(q));

    const matchCategory = categoryFilter.value === 'all' || row.category === categoryFilter.value;

    const qtyTotal = (Number(row.qtyGood) || 0) + (Number(row.qtyDamaged) || 0);
    const matchStatus = statusFilter.value === 'all' ||
      (statusFilter.value === 'filled' && qtyTotal > 0) ||
      (statusFilter.value === 'zero' && qtyTotal === 0);

    return matchSearch && matchCategory && matchStatus;
  });
});

// Metrics
const totalSKU = computed(() => rows.value.length);
const filledSKUCount = computed(() => {
  return rows.value.filter(r => (Number(r.qtyGood) || 0) > 0 || (Number(r.qtyDamaged) || 0) > 0).length;
});
const totalQty = computed(() => {
  return rows.value.reduce((sum, r) => sum + (Number(r.qtyGood) || 0) + (Number(r.qtyDamaged) || 0), 0);
});
const totalValuation = computed(() => {
  return rows.value.reduce((sum, r) => {
    const qty = (Number(r.qtyGood) || 0) + (Number(r.qtyDamaged) || 0);
    return sum + (qty * (Number(r.unitCost) || 0));
  }, 0);
});

const getRowSubtotal = (row) => {
  const qty = (Number(row.qtyGood) || 0) + (Number(row.qtyDamaged) || 0);
  return qty * (Number(row.unitCost) || 0);
};

// Stepper helpers
const incrementQty = (row, field) => {
  row[field] = (Number(row[field]) || 0) + 1;
};

const decrementQty = (row, field) => {
  if ((Number(row[field]) || 0) > 0) {
    row[field] = (Number(row[field]) || 0) - 1;
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  categoryFilter.value = 'all';
  statusFilter.value = 'all';
};

const resetAllInputs = () => {
  if (confirm('Kosongkan semua isian kuantitas pada lembar kerja?')) {
    rows.value.forEach(r => {
      r.qtyGood = 0;
      r.qtyDamaged = 0;
    });
  }
};

// Data Fetching
const fetchWarehouses = async () => {
  try {
    const res = await api.get('/master/warehouses');
    const list = res.data?.data?.content || res.data?.data || res.data || [];
    warehouses.value = list;

    if (list.length > 0) {
      // Pick warehouse from route query or default to central or first
      const queryWhId = route.query.warehouseId ? Number(route.query.warehouseId) : null;
      const matched = list.find(w => w.id === queryWhId);
      const central = list.find(w => w.type === 'CENTRAL_LOGISTICS');
      const target = matched || central || list[0];
      selectedWarehouseId.value = target.id;
      updateDefaultNotes(target.name);
      await fetchWorksheet();
    }
  } catch (err) {
    console.error('Failed to fetch warehouses', err);
    showAlert('Gagal mengambil daftar gudang', 'danger');
  }
};

const updateDefaultNotes = (whName) => {
  const formattedDate = new Date().toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  notes.value = `Penetapan Saldo Awal Cut-Off Go-Live Gudang ${whName || ''} per ${formattedDate}`;
};

const selectWarehouse = async (wh) => {
  selectedWarehouseId.value = wh.id;
  warehouseDropdownOpen.value = false;
  updateDefaultNotes(wh.name);
  await fetchWorksheet();
};

const fetchWorksheet = async () => {
  if (!selectedWarehouseId.value) return;
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/initial-stock/worksheet', {
      params: { warehouseId: selectedWarehouseId.value }
    });
    const data = res.data?.data || res.data || [];
    rows.value = data.map(item => ({
      itemId: item.itemId,
      sku: item.sku,
      name: item.name,
      uom: item.uom,
      category: item.category || 'Umum',
      unitCost: item.unitCost || 0,
      qtyGood: item.qtyGood || 0,
      qtyDamaged: item.qtyDamaged || 0
    }));
  } catch (err) {
    console.error('Failed to fetch initial stock worksheet', err);
    showAlert('Gagal memuat lembar kerja saldo awal: ' + (err.response?.data?.message || err.message), 'danger');
  } finally {
    isLoading.value = false;
  }
};

// Batch Submission
const submitInitialStock = async () => {
  if (filledSKUCount.value === 0) {
    showAlert('Harap isi kuantitas minimal 1 barang sebelum menyimpan saldo awal.', 'warning');
    return;
  }

  if (!notes.value.trim()) {
    showAlert('Berita acara / keterangan penetapan wajib diisi.', 'warning');
    return;
  }

  isSubmitting.value = true;
  try {
    const payloadItems = rows.value
      .filter(r => (Number(r.qtyGood) || 0) > 0 || (Number(r.qtyDamaged) || 0) > 0)
      .map(r => ({
        itemId: r.itemId,
        qtyGood: Number(r.qtyGood) || 0,
        qtyDamaged: Number(r.qtyDamaged) || 0,
        unitCost: Number(r.unitCost) || 0
      }));

    const payload = {
      warehouseId: selectedWarehouseId.value,
      cutoffDate: cutoffDate.value,
      notes: notes.value,
      items: payloadItems
    };

    const res = await api.post('/inventory/initial-stock/batch', payload);
    const result = res.data?.data;
    const skuCount = result?.skuCount || payloadItems.length;
    const refNo = result?.refNo || 'BERHASIL';

    showAlert(`Saldo awal gudang ${currentWarehouse.value?.name} berhasil diposting (${skuCount} SKU, Ref: ${refNo}).`, 'success');
    await fetchWorksheet();
  } catch (err) {
    console.error('Failed to post initial stock batch', err);
    showAlert('Gagal memposting saldo awal: ' + (err.response?.data?.message || err.message), 'danger');
  } finally {
    isSubmitting.value = false;
  }
};

// CSV Export Template
const downloadTemplate = () => {
  const whCode = currentWarehouse.value ? currentWarehouse.value.code.replace(/[^A-Za-z0-9_-]/g, '_') : 'Gudang';
  const filename = `Template_Saldo_Awal_${whCode}_${cutoffDate.value.replace(/-/g, '')}.csv`;

  const headers = [
    { key: 'sku', label: 'SKU' },
    { key: 'name', label: 'Nama Barang' },
    { key: 'category', label: 'Kategori' },
    { key: 'uom', label: 'Satuan' },
    { key: 'qtyGood', label: 'Qty Saldo Baik' },
    { key: 'qtyDamaged', label: 'Qty Saldo Rusak' },
    { key: 'unitCost', label: 'Harga Satuan (Rp)' },
    { key: 'notes', label: 'Catatan' }
  ];

  const exportRows = rows.value.map(r => ({
    sku: r.sku,
    name: r.name,
    category: r.category,
    uom: r.uom,
    qtyGood: r.qtyGood || 0,
    qtyDamaged: r.qtyDamaged || 0,
    unitCost: r.unitCost || 0,
    notes: 'Saldo awal cut-off migrasi'
  }));

  exportToCsv(filename, headers, exportRows);
};

// CSV Import Handling
const onImportFileChange = (e) => {
  const file = e.target.files?.[0];
  importFile.value = file || null;
};

const parseCsvText = (text) => {
  const lines = text.split(/\r\n|\n/);
  const result = [];
  for (let line of lines) {
    line = line.trim();
    if (!line) continue;
    // Simple CSV row parser considering quotes
    const values = [];
    let insideQuote = false;
    let entry = '';
    for (let i = 0; i < line.length; i++) {
      const char = line[i];
      if (char === '"') {
        if (insideQuote && line[i + 1] === '"') {
          entry += '"';
          i++;
        } else {
          insideQuote = !insideQuote;
        }
      } else if (char === ',' && !insideQuote) {
        values.push(entry.trim());
        entry = '';
      } else {
        entry += char;
      }
    }
    values.push(entry.trim());
    result.push(values);
  }
  return result;
};

const handleImportSubmit = async () => {
  if (!importFile.value) {
    alert('Pilih file CSV terlebih dahulu.');
    return;
  }

  isImporting.value = true;
  try {
    const text = await importFile.value.text();
    const parsed = parseCsvText(text);

    if (parsed.length < 2) {
      alert('File CSV kosong atau tidak memiliki baris data.');
      return;
    }

    // Header mapping
    const header = parsed[0].map(h => h.replace(/^[\uFEFF"]+|["\s]+$/g, '').toLowerCase());
    const skuIdx = header.findIndex(h => h === 'sku' || h.includes('sku') || h.includes('kode'));
    const qtyGoodIdx = header.findIndex(h => h.includes('baik') || h.includes('good') || h === 'qty');
    const qtyDamagedIdx = header.findIndex(h => h.includes('rusak') || h.includes('damaged'));
    const priceIdx = header.findIndex(h => h.includes('harga') || h.includes('cost') || h.includes('satuan'));

    if (skuIdx === -1) {
      alert('Kolom SKU tidak ditemukan pada file CSV. Pastikan header CSV memiliki kolom SKU.');
      return;
    }

    let matchCount = 0;
    const rowMap = new Map();
    rows.value.forEach(r => rowMap.set(r.sku.toLowerCase(), r));

    for (let i = 1; i < parsed.length; i++) {
      const col = parsed[i];
      const sku = (col[skuIdx] || '').trim().toLowerCase();
      if (!sku || !rowMap.has(sku)) continue;

      const target = rowMap.get(sku);
      if (qtyGoodIdx !== -1 && col[qtyGoodIdx] !== undefined) {
        target.qtyGood = parseInt(col[qtyGoodIdx], 10) || 0;
      }
      if (qtyDamagedIdx !== -1 && col[qtyDamagedIdx] !== undefined) {
        target.qtyDamaged = parseInt(col[qtyDamagedIdx], 10) || 0;
      }
      if (priceIdx !== -1 && col[priceIdx] !== undefined) {
        const cost = parseFloat(col[priceIdx]);
        if (!isNaN(cost) && cost >= 0) {
          target.unitCost = cost;
        }
      }
      matchCount++;
    }

    if (directPost.value) {
      // Post immediately to backend
      const customNotes = importNotes.value || `Impor File CSV Saldo Awal (${importFile.value.name})`;
      notes.value = customNotes;
      await submitInitialStock();
      importModalOpen.value = false;
    } else {
      importModalOpen.value = false;
      showAlert(`Berhasil memuat ${matchCount} item dari file CSV ke lembar kerja. Silakan periksa sebelum menyimpan.`, 'success');
    }
  } catch (err) {
    console.error('Import CSV error', err);
    alert('Gagal memproses file CSV: ' + err.message);
  } finally {
    isImporting.value = false;
  }
};

const showAlert = (msg, type = 'success') => {
  alertMessage.value = msg;
  alertType.value = type;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// Close dropdown on outside click
const handleDocumentClick = (e) => {
  if (warehouseDropdownRef.value && !warehouseDropdownRef.value.contains(e.target)) {
    warehouseDropdownOpen.value = false;
  }
};

onMounted(() => {
  fetchWarehouses();
  document.addEventListener('click', handleDocumentClick);
});

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick);
});
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.dropdown-wh-item:hover {
  background-color: var(--bs-tertiary-bg);
}

.rotate-180 {
  transform: rotate(180deg);
}

.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(2px);
  z-index: 1055;
}

.modal-content-custom {
  width: 100%;
  max-width: 540px;
  border-radius: 0.75rem;
}
</style>


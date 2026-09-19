<template>
  <div class="emboss-detail-page space-y-3">
    <!-- 1. Judul Halaman & Breadcrumb (UI seperti orders/emboss) -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <div class="d-flex align-items-center gap-2 flex-wrap">
              <h3 class="mb-0 text-body fw-bold">
                <span v-if="isLoading && (!file || !file.id)" class="placeholder-glow">
                  <span class="placeholder col-6 bg-secondary" style="width: 200px; display: inline-block; height: 1.5rem; border-radius: 4px;"></span>
                </span>
                <span v-else>{{ file.file_name }}</span>
              </h3>
              <template v-if="!isLoading || file.id">
                <span class="badge text-bg-danger fs-9 font-monospace">{{ file.file_id }}</span>
                <span class="badge fs-9 text-uppercase" :class="fileStatusBadgeClass(file.status)">
                  {{ file.status === 'ORDERS_GENERATED' ? 'Order Terbit' : file.status }}
                </span>
              </template>
            </div>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/emboss" class="text-decoration-none text-danger">Personalisasi Kartu</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Detail Berkas</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Card Container (UI seperti orders/emboss) -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Title & Action Buttons -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <h6 class="card-title fw-bold text-body mb-0">
          Data Rekaman Emboss
        </h6>

        <!-- Right Side Header Actions: Hapus Tombol Order Sudah Terbit -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2 flex-wrap">
          <router-link to="/emboss" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs text-white">
            Kembali
          </router-link>
          <router-link v-if="file.reject_records > 0" to="/emboss/reject-queue" class="btn btn-sm btn-outline-danger fw-bold fs-8">
            Antrean Reject ({{ file.reject_records }})
          </router-link>
          <!-- Tombol Generate Order hanya muncul jika order belum terbit -->
          <button
            v-if="file.status !== 'ORDERS_GENERATED' && file.success_records > 0"
            type="button"
            class="btn btn-sm btn-danger fw-bold shadow-xs fs-8"
            @click="showGenerateModal = true"
          >
            Generate Order
          </button>
        </div>
      </div>
        <!-- Filter & Search Toolbar (UI seperti orders/emboss) -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Unit Kerja / Cabang Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
                <select v-model="recordsFilterBranch" class="form-select form-select-sm border-start-0 fs-8" @change="recordsCurrentPage = 1">
                  <option value="ALL">Semua Unit Cabang</option>
                  <option v-for="b in recordBranchOptions" :key="b" :value="b">{{ b }}</option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="recordsFilterStatus" class="form-select form-select-sm border-start-0 fs-8" @change="recordsCurrentPage = 1">
                  <option value="ALL">Semua Status</option>
                  <option value="VALID">VALID (Siap Cetak)</option>
                  <option value="REJECTED">REJECTED (Anomali)</option>
                </select>
              </div>
            </div>

            <!-- Sort By Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-sort-down"></i></span>
                <select v-model="recordsSortBy" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="id">Urutan File</option>
                  <option value="masked_name">Nama Nasabah</option>
                  <option value="account_number">Nomor Rekening</option>
                  <option value="branch_code">Kode Cabang</option>
                  <option value="status">Status</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="isRecordsFiltered">
              <button type="button" @click="resetRecordsFilter" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
                Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="recordsSearchQuery"
                  class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                  placeholder="Cari Nasabah, No Rekening, PAN..."
                  @input="recordsCurrentPage = 1"
                />
                <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button" @click="recordsCurrentPage = 1">
                  Cari
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Content Data Emboss -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0 fs-7">
              <thead class="border-bottom fs-8 text-uppercase fw-semibold text-secondary bg-body-tertiary">
                <tr>
                  <th class="ps-3 ps-md-4 py-3" style="width: 140px;">Reference ID</th>
                  <th class="py-3" style="min-width: 150px;">Cabang</th>
                  <th class="py-3" style="min-width: 170px;">Produk</th>
                  <th class="text-center py-3" style="width: 100px;">Tipe</th>
                  <th class="py-3" style="min-width: 180px;">Nama Nasabah (Masked)</th>
                  <th class="py-3" style="min-width: 170px;">Nomor Rekening / PAN</th>
                  <th class="text-center py-3" style="width: 120px;">Status</th>
                  <th class="text-center pe-3 pe-md-4 py-3" style="width: 100px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <template v-if="!isLoading || paginatedRecords.length > 0">
                  <tr
                    v-for="rec in paginatedRecords"
                    :key="rec.id"
                  >
                    <td class="ps-3 ps-md-4 font-monospace fw-bold text-dark">{{ rec.ref_id }}</td>
                    <td>
                      <span class="fw-semibold text-body">{{ rec.org_name }}</span>
                      <div class="fs-9 font-monospace text-secondary">
                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle fs-9">
                          {{ rec.branch_code }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span class="fw-semibold font-monospace">{{ rec.product_code }}</span>
                      <span class="fs-9 text-secondary d-block">{{ rec.product_name }}</span>
                    </td>
                    <td class="text-center">
                      <span class="badge bg-secondary bg-opacity-10 text-secondary border fs-9">{{ rec.card_type }}</span>
                    </td>
                    <td class="font-monospace fw-semibold text-body">{{ rec.masked_name }}</td>
                    <td class="font-monospace">
                      <div class="text-dark">{{ rec.account_number || '-' }}</div>
                      <div class="text-secondary fs-9">{{ rec.masked_pan }}</div>
                    </td>
                    <td class="text-center">
                      <span
                        class="badge fs-9"
                        :class="rec.status === 'VALID' ? 'bg-success bg-opacity-10 text-success border border-success' : 'bg-danger bg-opacity-10 text-danger border border-danger'"
                      >
                        {{ rec.status }}
                      </span>
                      <div v-if="rec.rejection_reason" class="fs-9 text-danger mt-0.5" :title="rec.rejection_reason">
                        {{ rec.rejection_reason }}
                      </div>
                    </td>
                    <!-- Kolom Aksi View Modal Kartu ATM -->
                    <td class="text-center pe-3 pe-md-4 py-2">
                      <button
                        type="button"
                        class="btn-action-icon text-secondary"
                        @click="openCardModal(rec)"
                        title="Lihat Pratinjau Kartu ATM"
                      >
                        <i class="bi bi-eye"></i>
                      </button>
                    </td>
                  </tr>
                  <tr v-if="!isLoading && paginatedRecords.length === 0">
                    <td colspan="8" class="text-center py-5 text-secondary">
                      <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                      <p class="fw-bold mb-1">Tidak ada data emboss records ditemukan</p>
                      <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau sesuaikan filter status.</p>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Paging Footer Data Emboss -->
        <PaginationFooter
          v-if="!isLoading && filteredRecords.length > 0"
          :total="filteredRecords.length"
          v-model:currentPage="recordsCurrentPage"
          v-model:perPage="recordsPerPage"
        />
      </div>

    <!-- ==================== MODAL GENERATE ORDERS ==================== -->
    <div v-if="showGenerateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fs-6 fw-bold text-body mb-0">Generate Order Persediaan JIMS</h6>
            <button type="button" class="btn-close" @click="showGenerateModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8">
            <p class="text-secondary mb-3">
              Sistem akan membuat <strong>Order Persediaan</strong> otomatis untuk setiap kantor cabang dari <strong>{{ formatNumber(file.success_records) }} kartu valid</strong> pada berkas ini.
            </p>
            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                Pilih Metode Distribusi / Pengiriman <span class="text-danger">*</span>
              </label>
              <div class="d-flex gap-3 mt-1">
                <div class="form-check">
                  <input class="form-check-input" type="radio" v-model="deliveryMethod" id="mCourier" value="COURIER">
                  <label class="form-check-label fw-semibold" for="mCourier">
                    <i class="bi bi-truck me-1"></i> Ekspedisi Kurir Reguler
                  </label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" v-model="deliveryMethod" id="mPickup" value="PICKUP_KP">
                  <label class="form-check-label fw-semibold text-danger" for="mPickup">
                    <i class="bi bi-building me-1"></i> Ambil di Kantor Pusat (KP)
                  </label>
                </div>
              </div>
            </div>

            <div v-if="deliveryMethod === 'PICKUP_KP'" class="border rounded p-3 bg-light space-y-2 mt-2">
              <span class="fs-9 text-uppercase text-danger fw-bold d-block">
                <i class="bi bi-person-badge me-1"></i> Data PIC Pengambil di Kantor Pusat
              </span>
              <div>
                <label class="form-label fs-9 fw-semibold text-secondary mb-0">NIP Pegawai Pengambil</label>
                <input type="text" v-model="pickupNip" class="form-control form-control-sm" placeholder="Contoh: 198501102010121001">
              </div>
              <div>
                <label class="form-label fs-9 fw-semibold text-secondary mb-0">Nama Lengkap PIC</label>
                <input type="text" v-model="pickupName" class="form-control form-control-sm" placeholder="Contoh: Agus Setiawan">
              </div>
            </div>
          </div>
          <div class="modal-footer bg-light d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="showGenerateModal = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger fw-bold" :disabled="isGenerating" @click="confirmGenerateOrders">
              <span v-if="isGenerating" class="spinner-border spinner-border-sm me-1"></span>
              Konfirmasi & Terbitkan Order
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL: PRATINJAU FISIK KARTU ATM ==================== -->
    <div
      v-if="showCardModal && selectedRecord"
      class="modal fade show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.5); z-index: 1060;"
      @click.self="showCardModal = false"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header (UI warna orders/emboss view) -->
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <h6 class="modal-title fw-bold text-body fs-6 mb-0">
                Pratinjau Fisik Kartu ATM / Debit Nasabah
              </h6>
              <span
                class="badge fs-8 text-uppercase"
                :class="selectedRecord.status === 'VALID' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle'"
              >
                {{ selectedRecord.status }}
              </span>
            </div>
            <button type="button" class="btn-close" @click="showCardModal = false" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4">
            <!-- Container Kartu ATM Visual Realistis -->
            <div class="d-flex justify-content-center mb-4">
              <div
                class="atm-card-mockup shadow-lg"
                :class="selectedRecord.card_type === 'MASTERCARD' || selectedRecord.product_code?.includes('MC') ? 'atm-card-mastercard' : 'atm-card-gpn'"
              >
                <!-- Baris Atas: Logo Bank Jatim & Label Tipe Kartu -->
                <div class="d-flex justify-content-between align-items-center">
                  <img
                    src="/images/logo-bankjatim-white.png"
                    alt="Bank Jatim"
                    class="atm-card-logo"
                  />
                  <span class="atm-card-type-label">
                    {{ selectedRecord.card_type === 'MASTERCARD' || selectedRecord.product_code?.includes('MC') ? 'DEBIT PLATINUM' : 'DEBIT GPN' }}
                  </span>
                </div>

                <!-- Bagian Tengah Atas: EMV Chip & Simbol Contactless -->
                <div class="d-flex align-items-center gap-3 my-2">
                  <div class="emv-chip">
                    <div class="emv-chip-line emv-chip-line-horizontal"></div>
                    <div class="emv-chip-line emv-chip-line-vertical"></div>
                    <div class="emv-chip-center"></div>
                  </div>
                  <i class="bi bi-wifi contactless-icon"></i>
                </div>

                <!-- Nomor Kartu / PAN (Embossed Monospace Style) -->
                <div class="atm-card-pan my-2">
                  {{ formatPanBlocks(selectedRecord.masked_pan) }}
                </div>

                <!-- Baris Bawah: Nama Nasabah, Valid Thru, & Logo Jaringan -->
                <div class="d-flex justify-content-between align-items-end mt-2">
                  <div class="pe-2 overflow-hidden" style="max-width: 65%;">
                    <div class="atm-card-label">CARDHOLDER NAME</div>
                    <div class="atm-card-name text-truncate">{{ selectedRecord.masked_name || 'NAMA NASABAH' }}</div>
                  </div>

                  <div class="text-center px-2">
                    <div class="atm-card-label">VALID THRU</div>
                    <div class="atm-card-validthru">12/31</div>
                  </div>

                  <div class="ps-2">
                    <!-- Logo Mastercard / Logo GPN -->
                    <div v-if="selectedRecord.card_type === 'MASTERCARD' || selectedRecord.product_code?.includes('MC')" class="mastercard-logo" title="Mastercard Network">
                      <span class="mc-circle mc-circle-red"></span>
                      <span class="mc-circle mc-circle-yellow"></span>
                    </div>
                    <div v-else class="gpn-logo-badge" title="Gerbang Pembayaran Nasional">
                      <span class="gpn-badge-text">GPN</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Panel Rincian Spesifikasi & Metadata (UI warna orders/emboss view) -->
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="d-flex align-items-center gap-2 mb-2 pb-2 border-bottom border-secondary-subtle">
                <i class="bi bi-info-circle text-danger"></i>
                <span class="fw-bold fs-7 text-body">Spesifikasi Data Personalisasi Berkas</span>
              </div>
              <div class="row g-2 fs-8">
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Reference ID:</span>
                  <span class="font-monospace fw-bold text-dark">{{ selectedRecord.ref_id }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Nomor Rekening:</span>
                  <span class="font-monospace fw-bold text-dark">{{ selectedRecord.account_number || '-' }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Kantor Cabang:</span>
                  <span class="fw-semibold text-body">{{ selectedRecord.org_name }}</span>
                  <span class="badge bg-secondary-subtle text-secondary font-monospace fs-9 ms-1">{{ selectedRecord.branch_code }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Produk:</span>
                  <span class="fw-semibold text-body">{{ selectedRecord.product_name }}</span>
                  <div class="fs-9 font-monospace text-secondary">{{ selectedRecord.product_code }}</div>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Tipe Jaringan:</span>
                  <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle fs-9">{{ selectedRecord.card_type }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block fs-9 text-uppercase fw-semibold">Status Validasi:</span>
                  <span
                    class="badge fs-9"
                    :class="selectedRecord.status === 'VALID' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle'"
                  >
                    {{ selectedRecord.status }}
                  </span>
                </div>
                <div v-if="selectedRecord.rejection_reason" class="col-12 text-danger fs-8 border-top border-secondary-subtle pt-2 mt-2">
                  <i class="bi bi-exclamation-octagon me-1"></i>
                  <strong>Alasan Rejection:</strong> {{ selectedRecord.rejection_reason }}
                </div>
                <div v-if="selectedRecord.order_number && selectedRecord.order_number !== '-'" class="col-12 fs-8 border-top border-secondary-subtle pt-2 mt-2">
                  <span class="text-secondary fw-semibold">Order Persediaan: </span>
                  <span class="font-monospace text-danger fw-bold">{{ selectedRecord.order_number }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer (UI warna orders/emboss view) -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showCardModal = false">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';
import { toast } from '@/utils/toast';

const route = useRoute();
const isLoading = ref(true);

// Records State (Data Emboss)
const recordsFilterBranch = ref('ALL');
const recordsFilterStatus = ref('ALL');
const recordsSortBy = ref('id');
const recordsSearchQuery = ref('');
const recordsCurrentPage = ref(1);
const recordsPerPage = ref(10);

// Modal & Process State
const showGenerateModal = ref(false);
const deliveryMethod = ref('COURIER');
const pickupNip = ref('');
const pickupName = ref('');
const isGenerating = ref(false);

// ATM Card View Modal State
const showCardModal = ref(false);
const selectedRecord = ref(null);

const openCardModal = (rec) => {
  selectedRecord.value = rec;
  showCardModal.value = true;
};

const formatPanBlocks = (pan) => {
  if (!pan) return '•••• •••• •••• ••••';
  const clean = String(pan).replace(/\s+/g, '');
  return clean.match(/.{1,4}/g)?.join(' ') || clean;
};

const file = ref({
  id: null,
  file_id: '-',
  file_name: '-',
  source: 'CORE_BANKING_ALTO',
  duration_seconds: 2.8,
  uploader_name: '-',
  created_at: '-',
  success_rate: 0,
  total_records: 0,
  success_records: 0,
  reject_records: 0,
  duplicate_records: 0,
  status: ''
});

const records = ref([]);

const mapRecord = (rec) => ({
  id: rec.id,
  ref_id: `REF-${rec.branchCode || 'SBY'}-${String(rec.id).padStart(3, '0')}`,
  branch_code: rec.branchCode || 'KC-SBY',
  org_name: `Kantor Cabang ${rec.branchCode || ''}`.trim(),
  product_code: rec.item?.sku || (rec.cardType?.includes('MC') ? 'SKU-BLANK-MC' : 'SKU-BLANK-GPN'),
  product_name: rec.item?.name || (rec.cardType?.includes('MC') ? 'Kartu Mastercard Chip' : 'Kartu ATM Chip GPN Reguler'),
  card_type: rec.cardType || 'GPN',
  masked_name: rec.customerName || '-',
  account_number: rec.accountNumber || '-',
  masked_pan: rec.cardNumberMasked || '-',
  unit_price: 25000,
  status: rec.status || 'VALID',
  rejection_reason: rec.rejectionReason || null,
  order_id: rec.order?.id || null,
  order_number: rec.order?.orderNumber || '-'
});

// Branch options
const recordBranchOptions = computed(() => {
  const set = new Set();
  for (const r of records.value) {
    if (r.branch_code) set.add(r.branch_code);
  }
  return Array.from(set).sort();
});

// Records filtering & sorting
const isRecordsFiltered = computed(() => {
  return recordsFilterBranch.value !== 'ALL' || recordsFilterStatus.value !== 'ALL' || !!recordsSearchQuery.value;
});

const resetRecordsFilter = () => {
  recordsFilterBranch.value = 'ALL';
  recordsFilterStatus.value = 'ALL';
  recordsSearchQuery.value = '';
  recordsCurrentPage.value = 1;
};

const filteredRecords = computed(() => {
  let list = records.value;
  if (recordsFilterBranch.value !== 'ALL') {
    list = list.filter(r => r.branch_code === recordsFilterBranch.value);
  }
  if (recordsFilterStatus.value !== 'ALL') {
    list = list.filter(r => r.status === recordsFilterStatus.value);
  }
  if (recordsSearchQuery.value) {
    const q = recordsSearchQuery.value.toLowerCase().trim();
    list = list.filter(r =>
      (r.masked_name && r.masked_name.toLowerCase().includes(q)) ||
      (r.account_number && r.account_number.toLowerCase().includes(q)) ||
      (r.masked_pan && r.masked_pan.toLowerCase().includes(q)) ||
      (r.branch_code && r.branch_code.toLowerCase().includes(q)) ||
      (r.ref_id && r.ref_id.toLowerCase().includes(q))
    );
  }

  // Sort
  const sortKey = recordsSortBy.value;
  list = [...list].sort((a, b) => {
    let valA = a[sortKey] || '';
    let valB = b[sortKey] || '';
    if (typeof valA === 'string') valA = valA.toLowerCase();
    if (typeof valB === 'string') valB = valB.toLowerCase();
    return valA > valB ? 1 : valA < valB ? -1 : 0;
  });

  return list;
});

const paginatedRecords = computed(() => {
  const start = (recordsCurrentPage.value - 1) * recordsPerPage.value;
  return filteredRecords.value.slice(start, start + recordsPerPage.value);
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);

const fileStatusBadgeClass = (status) => {
  switch (status) {
    case 'ORDERS_GENERATED': return 'bg-success text-white';
    case 'VALIDATED': return 'bg-primary text-white';
    case 'PROCESSING': return 'bg-warning text-dark';
    default: return 'bg-secondary text-white';
  }
};

const loadFile = async () => {
  try {
    const res = await api.get(`/emboss/${route.params.id}`);
    const data = res.data;
    if (data) {
      const total = Number(data.totalRecords || 0);
      const valid = Number(data.validRecords || 0);
      const reject = Number(data.rejectedRecords || 0);
      file.value = {
        id: data.id,
        file_id: data.fileId || `#EB-202609-00${data.id}`,
        file_name: data.filename || '-',
        source: 'CORE_BANKING_ALTO',
        duration_seconds: 2.8,
        uploader_name: data.uploadedByUser?.name || 'Administrator',
        created_at: data.createdAt ? new Date(data.createdAt).toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' }) : '-',
        total_records: total,
        success_records: valid,
        reject_records: reject,
        duplicate_records: 0,
        success_rate: total > 0 ? Math.round((valid / total) * 100) : 0,
        status: data.status || 'PARSED'
      };
    }
  } catch (err) {
    toast.error(err?.message || err?.error || 'Gagal memuat detail berkas emboss.');
  }
};

const loadRecords = async () => {
  try {
    const statusParam = recordsFilterStatus.value === 'ALL' ? '' : `?status=${recordsFilterStatus.value}`;
    const res = await api.get(`/emboss/${route.params.id}/records${statusParam}`);
    records.value = (res.data || []).map(mapRecord);
  } catch (err) {
    toast.error(err?.message || err?.error || 'Gagal memuat daftar rekaman berkas emboss.');
  }
};

const loadAllData = async () => {
  isLoading.value = true;
  try {
    await Promise.allSettled([
      loadFile(),
      loadRecords()
    ]);
  } catch (err) {
    console.error('Failed to load emboss detail data:', err);
  } finally {
    isLoading.value = false;
  }
};

const confirmGenerateOrders = async () => {
  isGenerating.value = true;
  try {
    await api.post(`/emboss/${route.params.id}/generate-orders`);
    showGenerateModal.value = false;
    toast.success('Order persediaan cabang berhasil dibuat dari berkas emboss.');
    await loadAllData();
  } catch (err) {
    toast.error('Gagal membuat order: ' + (err?.message || err?.error || 'Terjadi kesalahan sistem'));
  } finally {
    isGenerating.value = false;
  }
};

onMounted(loadAllData);
</script>

<style scoped>
.btn-xs {
  padding: 0.15rem 0.4rem;
  font-size: 0.75rem;
  border-radius: 0.2rem;
}
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ==================== REALISTIC ATM CARD MOCKUP ==================== */
.atm-card-mockup {
  width: 100%;
  max-width: 440px;
  height: 260px;
  border-radius: 18px;
  padding: 22px 26px;
  color: #ffffff;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  user-select: none;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.45), 0 4px 12px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.atm-card-mockup:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 42px rgba(0, 0, 0, 0.5), 0 6px 16px rgba(0, 0, 0, 0.3);
}

/* Bank Jatim Signature Ruby Red Metallic Gradient */
.atm-card-gpn {
  background: linear-gradient(135deg, #c7141b 0%, #8f080e 45%, #420204 100%);
}
.atm-card-gpn::before {
  content: '';
  position: absolute;
  top: -40%;
  right: -25%;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0) 65%);
  border-radius: 50%;
  pointer-events: none;
}
.atm-card-gpn::after {
  content: '';
  position: absolute;
  bottom: -30px;
  left: -20px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.1) 0%, rgba(255, 215, 0, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

/* Mastercard Titanium/Platinum Dark Theme */
.atm-card-mastercard {
  background: linear-gradient(135deg, #2c3038 0%, #17191e 50%, #0d0e12 100%);
}
.atm-card-mastercard::before {
  content: '';
  position: absolute;
  top: -40%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(217, 37, 42, 0.22) 0%, rgba(217, 37, 42, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

/* Header Elements */
.atm-card-logo {
  height: 34px;
  width: auto;
  max-width: 140px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.45));
}

.atm-card-type-label {
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 1.6px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
}

/* EMV Chip */
.emv-chip {
  width: 48px;
  height: 36px;
  background: linear-gradient(135deg, #e6c86e 0%, #fef3a4 40%, #c49a2a 75%, #8c6a12 100%);
  border-radius: 7px;
  position: relative;
  box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.6), 0 2px 5px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  border: 1px solid #75550c;
  flex-shrink: 0;
}
.emv-chip-line {
  position: absolute;
  background: rgba(0, 0, 0, 0.35);
}
.emv-chip-line-horizontal {
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  transform: translateY(-50%);
}
.emv-chip-line-vertical {
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  transform: translateX(-50%);
}
.emv-chip-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 16px;
  border: 1px solid rgba(0, 0, 0, 0.35);
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.06);
}

.contactless-icon {
  font-size: 1.35rem;
  transform: rotate(90deg);
  color: rgba(255, 255, 255, 0.8);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.4));
}

/* Card Number / PAN Embossed */
.atm-card-pan {
  font-family: 'Courier New', Courier, monospace;
  font-size: 1.32rem;
  font-weight: 700;
  letter-spacing: 3px;
  color: #ffffff;
  text-shadow: 0 1px 2px #000, 0 -1px 1px rgba(255, 255, 255, 0.4);
  word-spacing: 4px;
}

/* Bottom Labels */
.atm-card-label {
  font-size: 0.56rem;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 2px;
}

.atm-card-name {
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #ffffff;
  text-shadow: 0 1px 2px #000, 0 -1px 1px rgba(255, 255, 255, 0.35);
  line-height: 1.1;
}

.atm-card-validthru {
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.88rem;
  font-weight: 700;
  letter-spacing: 1px;
  color: #ffffff;
  text-shadow: 0 1px 2px #000, 0 -1px 1px rgba(255, 255, 255, 0.35);
  line-height: 1.1;
}

/* GPN Badge */
.gpn-logo-badge {
  background: #ffffff;
  color: #c8102e;
  padding: 3px 9px;
  border-radius: 5px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}
.gpn-badge-text {
  font-weight: 900;
  font-size: 0.92rem;
  letter-spacing: 1.2px;
  line-height: 1;
}

/* Mastercard Network Logo */
.mastercard-logo {
  display: flex;
  align-items: center;
  position: relative;
  width: 48px;
  height: 30px;
}
.mc-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  position: absolute;
}
.mc-circle-red {
  background-color: #eb001b;
  left: 0;
}
.mc-circle-yellow {
  background-color: #f79e1b;
  right: 0;
  opacity: 0.92;
  mix-blend-mode: hard-light;
}
</style>

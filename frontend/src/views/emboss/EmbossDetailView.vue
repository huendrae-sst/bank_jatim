<template>
  <div class="emboss-detail-page space-y-3">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="mb-0 text-body fw-bold">{{ file.file_name }}</h3>
          <span class="badge text-bg-danger fs-9">{{ file.file_id }}</span>
        </div>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 fs-8">
            <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
            <li class="breadcrumb-item"><router-link to="/emboss" class="text-decoration-none text-danger">Personalisasi Kartu</router-link></li>
            <li class="breadcrumb-item active text-secondary" aria-current="page">Detail Berkas & Explorer</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <router-link to="/emboss" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i> Kembali
        </router-link>
        <router-link v-if="file.reject_records > 0" to="/emboss/reject-queue" class="btn btn-sm btn-outline-danger fw-semibold">
          <i class="bi bi-exclamation-octagon me-1"></i> Reject Queue ({{ file.reject_records }})
        </router-link>
        <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs" @click="showGenerateModal = true" :disabled="file.success_records === 0 || file.status === 'ORDERS_GENERATED'">
          <i class="bi bi-cart-plus me-1"></i> {{ file.status === 'ORDERS_GENERATED' ? 'Order Terbit' : 'Generate Order' }}
        </button>
      </div>
    </div>

    <!-- Error Banner -->
    <div v-if="errorMessage" class="alert alert-danger fs-8 py-2 px-3 mb-3">{{ errorMessage }}</div>

    <!-- Batch Overview Card -->
    <div class="card card-outline card-danger shadow-xs">

      <div class="card-body p-4">
        <div class="row g-3">
          <div class="col-6 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Sistem Sumber</span>
            <span class="badge bg-light text-dark border fs-8 mt-1">{{ file.source.replace('_', ' ') }}</span>
          </div>
          <div class="col-6 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Durasi Pemrosesan</span>
            <span class="fs-8 fw-bold text-dark mt-1 d-block font-monospace">
              <i class="bi bi-stopwatch me-1"></i>{{ file.duration_seconds }} detik
            </span>
          </div>
          <div class="col-6 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Diunggah Oleh</span>
            <span class="fs-8 fw-semibold text-dark mt-1 d-block">{{ file.uploader_name }}</span>
          </div>
          <div class="col-6 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Waktu Unggah</span>
            <span class="fs-8 fw-semibold text-dark mt-1 d-block font-monospace">{{ file.created_at }} WIB</span>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="mt-4 pt-3 border-top">
          <div class="d-flex justify-content-between align-items-center mb-1 fs-8">
            <span class="fw-semibold text-secondary">
              Tingkat Keberhasilan: <strong class="text-dark">{{ file.success_rate }}%</strong>
            </span>
            <span class="font-monospace text-muted">
              Total: {{ formatNumber(file.total_records) }} | Valid: {{ formatNumber(file.success_records) }} | Reject: {{ formatNumber(file.reject_records) }} | Duplikat: {{ formatNumber(file.duplicate_records) }}
            </span>
          </div>
          <div class="progress" style="height: 10px;">
            <div class="progress-bar bg-success" role="progressbar" :style="'width: ' + ((file.success_records / file.total_records) * 100) + '%'"></div>
            <div class="progress-bar bg-warning" role="progressbar" :style="'width: ' + ((file.duplicate_records / file.total_records) * 100) + '%'"></div>
            <div class="progress-bar bg-danger" role="progressbar" :style="'width: ' + ((file.reject_records / file.total_records) * 100) + '%'"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <ul class="nav nav-pills fs-7 pb-1 mb-2 gap-1" role="tablist">
      <li class="nav-item">
        <button
          type="button"
          class="nav-link py-1 px-3 text-nowrap"
          :class="activeTab === 'records' ? 'active bg-danger fw-bold text-white' : 'text-body'"
          @click="activeTab = 'records'"
        >
          <i class="bi bi-table me-1"></i> Data Mapping & Record Explorer
        </button>
      </li>
      <li class="nav-item">
        <button
          type="button"
          class="nav-link py-1 px-3 text-nowrap"
          :class="activeTab === 'grouping' ? 'active bg-danger fw-bold text-white' : 'text-body'"
          @click="activeTab = 'grouping'"
        >
          <i class="bi bi-collection me-1"></i> Ringkasan Grouping Order Persediaan
        </button>
      </li>
    </ul>

    <!-- Tab 1: Records Explorer -->
    <div v-if="activeTab === 'records'" class="card card-outline card-danger shadow-xs">
      <div class="card-header bg-light py-2 px-3 d-flex justify-content-between align-items-center">
        <span class="fs-8 fw-bold text-secondary text-uppercase">Tabel Record Data Personalisasi</span>
        <select v-model="filterStatus" class="form-select form-select-sm fs-9 w-auto">
          <option value="ALL">-- Semua Status Record --</option>
          <option value="VALID">VALID</option>
          <option value="PROCESSED_TO_ORDER">PROCESSED TO ORDER</option>
          <option value="INVALID">INVALID (Reject)</option>
          <option value="DUPLICATE">DUPLICATE</option>
        </select>
      </div>
      <div class="table-responsive">
        <table class="table table-hover table-striped align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-9 text-uppercase">
            <tr>
              <th class="ps-3 py-2">Reference ID</th>
              <th class="py-2">Cabang</th>
              <th class="py-2">Produk / SKU</th>
              <th class="py-2">Tipe</th>
              <th class="py-2">Nama Nasabah (Masked)</th>
              <th class="py-2">Nomor Kartu / PAN</th>
              <th class="text-end py-2">Harga</th>
              <th class="text-center py-2">Status</th>
              <th class="text-center py-2">Order Terkait</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rec in filteredRecords" :key="rec.id">
              <td class="ps-3 py-2 font-monospace fw-bold text-dark">{{ rec.ref_id }}</td>
              <td class="py-2">
                <span class="badge bg-light text-dark border font-monospace">{{ rec.branch_code }}</span>
                <span class="fs-9 text-muted d-block">{{ rec.org_name }}</span>
              </td>
              <td class="py-2">
                <span class="fw-semibold font-monospace">{{ rec.product_code }}</span>
                <span class="fs-9 text-muted d-block">{{ rec.product_name }}</span>
              </td>
              <td class="py-2"><span class="badge bg-secondary bg-opacity-10 text-secondary border fs-9">{{ rec.card_type }}</span></td>
              <td class="py-2 font-monospace">{{ rec.masked_name }}</td>
              <td class="py-2 font-monospace fw-bold"><i class="bi bi-shield-lock text-success me-1"></i>{{ rec.masked_pan }}</td>
              <td class="text-end py-2 font-monospace fw-semibold">{{ formatRupiah(rec.unit_price) }}</td>
              <td class="text-center py-2">
                <span class="badge" :class="rec.status === 'VALID' ? 'bg-success bg-opacity-10 text-success border border-success' : 'bg-primary'">
                  {{ rec.status }}
                </span>
              </td>
              <td class="text-center py-2">
                <router-link v-if="rec.order_id" :to="`/orders/${rec.order_id}`" class="badge bg-info text-dark text-decoration-none font-monospace">
                  {{ rec.order_number }}
                </router-link>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab 2: Grouping Order Summary -->
    <div v-if="activeTab === 'grouping'" class="card card-outline card-danger shadow-xs">
      <div class="card-header bg-light py-2 px-3 d-flex justify-content-between align-items-center">
        <span class="fs-8 fw-bold text-secondary text-uppercase">Pengelompokan Otomatis Order Persediaan</span>
        <span class="fs-9 text-muted">Dikelompokkan berdasarkan Unit Kerja Cabang & Jenis Kartu</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-9 text-uppercase">
            <tr>
              <th class="ps-3 py-2">Cabang Tujuan</th>
              <th class="py-2">Tipe Kartu</th>
              <th class="py-2">SKU Barang Master</th>
              <th class="text-center py-2">Jumlah Kartu (Qty)</th>
              <th class="text-end py-2">Estimasi Nilai Beban</th>
              <th class="text-center py-2">Status Konversi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(g, idx) in groupingSummary" :key="idx">
              <td class="ps-3 py-2">
                <span class="fw-bold text-dark font-monospace">{{ g.branch_code }}</span>
                <span class="fs-9 text-muted d-block">{{ g.org_name }}</span>
              </td>
              <td class="py-2"><span class="badge bg-secondary bg-opacity-10 text-secondary border">{{ g.card_type }}</span></td>
              <td class="py-2">
                <span class="font-monospace fw-semibold">{{ g.product_code }}</span>
                <span class="fs-9 text-muted d-block">{{ g.product_name }}</span>
              </td>
              <td class="text-center py-2 font-monospace fw-bold fs-7">{{ formatNumber(g.card_count) }} PCS</td>
              <td class="text-end py-2 font-monospace fw-bold text-dark">{{ formatRupiah(g.estimated_val) }}</td>
              <td class="text-center py-2"><span class="badge bg-success bg-opacity-10 text-success border border-success">SIAP GENERATE</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Generate Orders -->
    <div v-if="showGenerateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h5 class="modal-title fs-6 fw-bold">Generate Order Persediaan JIMS</h5>
            <button type="button" class="btn-close btn-close-white" @click="showGenerateModal = false"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8">
            <p class="text-secondary mb-3">
              Sistem akan membuat <strong>Order Persediaan</strong> otomatis untuk setiap cabang dari <strong>{{ formatNumber(file.success_records) }} kartu valid</strong> di berkas ini.
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
            <button type="button" class="btn btn-sm btn-danger fw-bold" @click="confirmGenerateOrders">
              <i class="bi bi-check2-circle me-1"></i> Konfirmasi & Buat Order
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();
const activeTab = ref('records');
const filterStatus = ref('ALL');
const showGenerateModal = ref(false);
const deliveryMethod = ref('COURIER');
const pickupNip = ref('');
const pickupName = ref('');
const errorMessage = ref('');
const isGenerating = ref(false);

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
  masked_pan: rec.cardNumberMasked || '-',
  unit_price: 25000,
  status: rec.status || 'VALID',
  order_id: rec.order?.id || null,
  order_number: rec.order?.orderNumber || '-'
});

const groupingSummary = computed(() => {
  const map = new Map();
  for (const r of records.value) {
    if (r.status === 'REJECTED') continue;
    const key = `${r.branch_code}__${r.card_type}__${r.product_code}`;
    if (!map.has(key)) {
      map.set(key, {
        branch_code: r.branch_code,
        org_name: r.org_name,
        card_type: r.card_type,
        product_code: r.product_code,
        product_name: r.product_name,
        card_count: 0,
        estimated_val: 0
      });
    }
    const g = map.get(key);
    g.card_count += 1;
    g.estimated_val += r.unit_price;
  }
  return Array.from(map.values());
});

const filteredRecords = computed(() => {
  if (filterStatus.value === 'ALL') return records.value;
  return records.value.filter(r => r.status === filterStatus.value);
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);
const formatRupiah = (val) => 'Rp ' + formatNumber(val);

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
        file_id: `#EB-202609-00${data.id}`,
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
    errorMessage.value = err?.message || err?.error || 'Gagal memuat detail berkas emboss.';
  }
};

const loadRecords = async () => {
  try {
    const statusParam = filterStatus.value === 'ALL' ? '' : `?status=${filterStatus.value}`;
    const res = await api.get(`/emboss/${route.params.id}/records${statusParam}`);
    records.value = (res.data || []).map(mapRecord);
  } catch (err) {
    errorMessage.value = err?.message || err?.error || 'Gagal memuat daftar rekaman berkas emboss.';
  }
};

const confirmGenerateOrders = async () => {
  isGenerating.value = true;
  try {
    await api.post(`/emboss/${route.params.id}/generate-orders`);
    showGenerateModal.value = false;
    alert('Order persediaan cabang berhasil dibuat dari berkas emboss.');
    await loadFile();
    await loadRecords();
  } catch (err) {
    alert('Gagal membuat order: ' + (err?.message || err?.error || 'Terjadi kesalahan sistem'));
  } finally {
    isGenerating.value = false;
  }
};

watch(filterStatus, () => {
  loadRecords();
});

onMounted(async () => {
  await loadFile();
  await loadRecords();
});
</script>

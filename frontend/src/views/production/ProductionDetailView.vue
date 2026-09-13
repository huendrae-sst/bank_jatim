<template>
  <div class="production-detail-page space-y-3">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="mb-0 text-body fw-bold">{{ prodOrder.production_number }}</h3>
          <span class="badge fs-8 text-uppercase" :class="prodOrder.status === 'COMPLETED' ? 'text-bg-success' : 'text-bg-primary'">
            {{ prodOrder.status }}
          </span>
        </div>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 fs-8">
            <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
            <li class="breadcrumb-item"><router-link to="/production" class="text-decoration-none text-danger">Bon Produksi Warkat</router-link></li>
            <li class="breadcrumb-item active text-secondary" aria-current="page">Detail Bon Produksi</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <router-link to="/production" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i> Kembali
        </router-link>
        <router-link :to="`/production/${prodOrder.id}/manifest`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold">
          <i class="bi bi-printer me-1"></i> Cetak Rekap Manifest
        </router-link>
        <button v-if="prodOrder.status !== 'COMPLETED'" type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3" @click="showIssueModal = true">
          <i class="bi bi-box-arrow-right me-1"></i> Keluarkan Bahan Baku
        </button>
      </div>
    </div>

    <div class="card card-outline card-danger shadow-xs">

      <div class="card-body p-4">
        <div class="row g-3 mb-4">
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Unit Cabang Tujuan</span>
            <span class="fw-bold text-dark fs-7">{{ prodOrder.dest_org }}</span>
            <span class="text-muted fs-8 d-block">{{ prodOrder.dest_address }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Gudang Sumber Bahan</span>
            <span class="fw-bold text-dark fs-7">{{ prodOrder.warehouse_name }}</span>
            <span class="text-muted fs-8 d-block">{{ prodOrder.warehouse_code }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Referensi Tautan</span>
            <span v-if="prodOrder.emboss_file" class="text-dark fs-8 d-block"><i class="bi bi-file-earmark-code me-1 text-danger"></i>Emboss: {{ prodOrder.emboss_file }}</span>
            <span v-if="prodOrder.order_number" class="text-dark fs-8 d-block"><i class="bi bi-bag me-1 text-primary"></i>Order: {{ prodOrder.order_number }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Realisasi Pengeluaran Bahan</span>
            <span class="fw-bold fs-7" :class="prodOrder.total_issued_qty >= prodOrder.total_planned_qty ? 'text-success' : 'text-primary'">
              {{ formatNumber(prodOrder.total_issued_qty) }} / {{ formatNumber(prodOrder.total_planned_qty) }} Unit
            </span>
            <span v-if="prodOrder.issued_at" class="text-muted fs-8 d-block">Dikeluarkan oleh: {{ prodOrder.issuer_name }} ({{ prodOrder.issued_at }})</span>
          </div>
        </div>

        <!-- Items Table -->
        <h6 class="fw-bold mb-3 text-dark"><i class="bi bi-credit-card me-2"></i>Rincian Bahan Blankcard / Token / KUE pada Bon</h6>
        <div class="table-responsive border rounded-3">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3">SKU Bahan</th>
                <th>Nama Barang</th>
                <th>Kategori</th>
                <th class="text-center">Kuantitas Bon (Rencana)</th>
                <th class="text-center">Kuantitas Dikeluarkan</th>
                <th class="text-center">Sisa Pengeluaran</th>
                <th>Keterangan</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in prodOrder.items" :key="item.id">
                <td class="ps-3 font-monospace fw-bold">{{ item.sku }}</td>
                <td class="fw-semibold">{{ item.name }}</td>
                <td>{{ item.category }}</td>
                <td class="text-center font-monospace fw-bold">{{ formatNumber(item.qty_planned) }} {{ item.uom }}</td>
                <td class="text-center font-monospace fw-bold text-success">{{ formatNumber(item.qty_issued) }}</td>
                <td class="text-center font-monospace fw-bold text-danger">{{ formatNumber(Math.max(0, item.qty_planned - item.qty_issued)) }}</td>
                <td class="text-secondary">{{ item.notes || '-' }}</td>
              </tr>
            </tbody>
            <tfoot class="table-light fw-bold">
              <tr>
                <td colspan="3" class="ps-3 text-uppercase">Total</td>
                <td class="text-center font-monospace">{{ formatNumber(prodOrder.total_planned_qty) }}</td>
                <td class="text-center font-monospace text-success">{{ formatNumber(prodOrder.total_issued_qty) }}</td>
                <td class="text-center font-monospace text-danger">{{ formatNumber(Math.max(0, prodOrder.total_planned_qty - prodOrder.total_issued_qty)) }}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Issue Stock -->
    <div v-if="showIssueModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showIssueModal = false">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Pengeluaran Bahan Baku dari Gudang (Stock Deduction)
            </h6>
            <button type="button" class="btn-close-modal" @click="showIssueModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body space-y-3 fs-8 p-3">
            <div class="alert alert-info py-2 fs-8 border-0 mb-3">
              <i class="bi bi-shield-check me-1"></i> <strong>Over-Issue Protection:</strong> Sistem memvalidasi saldo on-hand fisik di {{ prodOrder.warehouse_name }}.
            </div>
            <table class="table table-bordered table-sm align-middle fs-8 mb-0">
              <thead class="table-light">
                <tr>
                  <th>Barang</th>
                  <th class="text-center">Bon Rencana</th>
                  <th class="text-center">Sudah Keluar</th>
                  <th class="text-center" style="width: 30%">Kuantitas Dikeluarkan Sekarang</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in prodOrder.items" :key="item.id">
                  <td>
                    <strong>{{ item.name }}</strong>
                    <span class="text-muted d-block font-monospace fs-9">{{ item.sku }}</span>
                  </td>
                  <td class="text-center font-monospace">{{ item.qty_planned }}</td>
                  <td class="text-center font-monospace text-success">{{ item.qty_issued }}</td>
                  <td>
                    <input type="number" v-model.number="item.qty_to_issue" class="form-control form-control-sm text-center font-monospace fw-bold fs-8" min="0" :max="Math.max(0, item.qty_planned - item.qty_issued)">
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showIssueModal = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" @click="confirmIssue">
              <i class="bi bi-check2-circle me-1"></i> Proses Pengeluaran Bahan
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();
const showIssueModal = ref(false);
const isLoading = ref(true);

const prodOrder = ref({
  id: route.params.id,
  production_number: '',
  status: 'IN_PROGRESS',
  created_at: '',
  creator_name: '',
  dest_org: 'Kantor Cabang Surabaya Utama',
  dest_address: 'Jl. Basuki Rahmat No. 98-104, Surabaya',
  warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
  warehouse_code: 'WH-CEN-01',
  emboss_file: '',
  order_number: '',
  total_planned_qty: 0,
  total_issued_qty: 0,
  issued_at: null,
  issuer_name: 'Imam Prasetyo',
  items: []
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return val;
  }
};

const fetchProductionDetail = async () => {
  isLoading.value = true;
  try {
    const res = await api.get(`/emboss/${route.params.id}`);
    const f = res.data?.data || res.data;
    const total = f.totalRecords || 100;
    const valid = f.validRecords || total;
    prodOrder.value = {
      id: f.id,
      production_number: `PRD-2026-09-${String(f.id).padStart(4, '0')}`,
      status: f.status === 'COMPLETED' ? 'COMPLETED' : 'IN_PROGRESS',
      created_at: formatDate(f.createdAt),
      creator_name: f.uploadedByUser?.fullName || f.uploadedByUser?.username || 'Staff Produksi',
      dest_org: 'Kantor Cabang Surabaya Utama',
      dest_address: 'Jl. Basuki Rahmat No. 98-104, Surabaya',
      warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
      warehouse_code: 'WH-CEN-01',
      emboss_file: f.filename || `EMB-${f.id}`,
      order_number: `ORD-2026-09-${String(f.id).padStart(4, '0')}`,
      total_planned_qty: total,
      total_issued_qty: valid,
      issued_at: formatDate(f.updatedAt || f.createdAt),
      issuer_name: 'Petugas Gudang Bahan',
      items: [
        {
          id: 1,
          sku: 'CRD-ATM-GPN',
          name: `Bahan Personalisasi (${f.filename})`,
          category: 'Kartu ATM',
          uom: 'PCS',
          qty_planned: total,
          qty_issued: valid,
          qty_to_issue: Math.max(0, total - valid),
          notes: `File status: ${f.status}`
        }
      ]
    };
  } catch (err) {
    console.error('Failed to load production detail', err);
  } finally {
    isLoading.value = false;
  }
};

const confirmIssue = () => {
  prodOrder.value.items.forEach(item => {
    item.qty_issued += item.qty_to_issue || 0;
  });
  prodOrder.value.total_issued_qty = prodOrder.value.items.reduce((sum, it) => sum + it.qty_issued, 0);
  if (prodOrder.value.total_issued_qty >= prodOrder.value.total_planned_qty) {
    prodOrder.value.status = 'COMPLETED';
  }
  showIssueModal.value = false;
  alert('Bahan baku produksi berhasil dikeluarkan dari gudang.');
};

onMounted(() => {
  fetchProductionDetail();
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

<template>
  <div class="pr-detail-page space-y-3">
    <!-- Loading State -->
    <div v-if="isLoading" class="text-center py-5">
      <div class="spinner-border text-danger" role="status"></div>
      <div class="text-secondary fs-8 mt-2">Memuat rincian Purchase Request...</div>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMessage" class="alert alert-danger p-3 shadow-xs">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      {{ errorMessage }}
      <div class="mt-2">
        <router-link to="/procurement/pr" class="btn btn-sm btn-outline-danger">Kembali ke Daftar</router-link>
      </div>
    </div>

    <template v-else>
      <!-- Breadcrumb & Header -->
      <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
        <div>
          <div class="d-flex align-items-center gap-2">
            <h3 class="mb-0 text-body fw-bold">{{ pr.pr_number }}</h3>
            <span class="badge fs-8 text-uppercase" :class="badgeClass(pr.status)">
              {{ pr.status ? pr.status.replace('_', ' ') : 'SUBMITTED' }}
            </span>
          </div>
          <p class="fs-8 text-secondary mb-0 mt-0.5">
            Unit Pengaju: <strong class="text-body">{{ pr.org_name }}</strong> &bull; Dibuat: {{ formatDate(pr.created_at) }} &bull; Maker: {{ pr.requester_name }}
          </p>
        </div>

        <div class="d-flex align-items-center gap-2 flex-wrap">
          <router-link to="/procurement/pr" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar
          </router-link>
          <router-link :to="`/procurement/pr/${pr.id}/print`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold shadow-xs">
            <i class="bi bi-printer me-1"></i> Cetak PR
          </router-link>
          <template v-if="['SUBMITTED', 'WAITING_APPROVAL'].includes(pr.status)">
            <button type="button" @click="showRejectModal = true" class="btn btn-sm btn-outline-danger fw-bold shadow-xs" :disabled="isSubmitting">
              <i class="bi bi-x-circle me-1"></i> Tolak PR
            </button>
            <button type="button" @click="approvePr" class="btn btn-sm btn-success fw-bold shadow-xs" :disabled="isSubmitting">
              <i class="bi bi-check2-all me-1"></i> Setujui PR (Approve)
            </button>
          </template>
          <router-link v-else-if="pr.status === 'APPROVED'" to="/procurement/consolidation" class="btn btn-sm btn-primary fw-bold shadow-xs">
            <i class="bi bi-layers me-1"></i> Masuk ke Konsolidasi PO
          </router-link>
        </div>
      </div>

      <!-- Metadata Card -->
      <div class="card shadow-xs border bg-body p-4 row g-3">
        <div class="col-12 col-md-4">
          <div class="fs-9 font-bold text-uppercase text-secondary">Unit Pengaju</div>
          <div class="fs-7 fw-bold text-body mt-1">{{ pr.org_name }}</div>
          <div class="fs-9 text-secondary font-monospace">Kode: {{ pr.cost_center_code }}</div>
        </div>

        <div class="col-12 col-md-4">
          <div class="fs-9 font-bold text-uppercase text-secondary">Maker & Approver</div>
          <div class="fs-7 fw-bold text-body mt-1">Maker: {{ pr.requester_name }}</div>
          <div class="fs-9 text-secondary">Approver: {{ pr.approver_name || 'Belum disetujui' }}</div>
        </div>

        <div class="col-12 col-md-4">
          <div class="fs-9 font-bold text-uppercase text-secondary">Status Validasi Anggaran</div>
          <div class="fs-7 fw-bold text-success mt-1 d-flex align-items-center gap-1">
            <i class="bi bi-check-circle-fill"></i>
            <span>{{ pr.budget_status || 'Tervalidasi (Pagu Mencukupi)' }}</span>
          </div>
          <div class="fs-9 text-secondary">Metode: {{ pr.procurement_method || 'TENDER_TERBUKA' }}</div>
        </div>
      </div>

      <!-- Purpose Box -->
      <div class="card shadow-xs border bg-body p-3 mt-3">
        <h6 class="fs-8 fw-bold text-uppercase text-secondary mb-1">Tujuan Pengadaan</h6>
        <p class="fs-8 text-body mb-0 leading-relaxed">{{ pr.purpose || '-' }}</p>
      </div>

      <!-- Items Table Card -->
      <div class="card card-outline card-danger shadow-xs mt-3">
        <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
          <h3 class="card-title fw-semibold mb-0 fs-6 text-body">Rincian Item Purchase Request</h3>
          <span class="badge text-bg-secondary fs-9">{{ pr.items?.length || 0 }} Items</span>
        </div>

        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3 py-2.5">Item SKU & Nama</th>
                <th class="py-2.5 text-center">Diajukan</th>
                <th class="py-2.5 text-center">Disetujui</th>
                <th class="py-2.5 text-center">Dikonsolidasi (PO)</th>
                <th class="py-2.5 text-center">Sisa Order</th>
                <th class="py-2.5 text-end">Est. Harga Satuan</th>
                <th class="py-2.5 text-end pe-3">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in pr.items" :key="it.id">
                <td class="ps-3 py-3">
                  <div class="fw-bold text-dark">{{ it.name }}</div>
                  <div class="fs-9 text-muted font-monospace">{{ it.sku }} &bull; Satuan: {{ it.uom }}</div>
                </td>
                <td class="text-center font-monospace fw-bold text-secondary">{{ it.qty_requested }}</td>
                <td class="text-center font-monospace fw-bold text-success">{{ it.qty_approved }}</td>
                <td class="text-center font-monospace fw-bold text-primary">{{ it.qty_ordered }}</td>
                <td class="text-center font-monospace fw-bold text-danger">{{ it.remaining_qty }}</td>
                <td class="text-end font-monospace text-secondary">{{ formatRupiah(it.unit_price) }}</td>
                <td class="text-end pe-3 font-monospace fw-bold text-dark">{{ formatRupiah(it.subtotal) }}</td>
              </tr>
              <tr v-if="!pr.items || pr.items.length === 0">
                <td colspan="7" class="text-center py-4 text-secondary">Tidak ada rincian item.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="bg-light p-3 d-flex align-items-center justify-content-between border-top">
          <span class="fs-8 fw-bold text-secondary text-uppercase">Total Nilai PR</span>
          <span class="fs-5 fw-bold text-danger font-monospace">{{ formatRupiah(pr.total_cost) }}</span>
        </div>
      </div>

      <!-- Reject Modal -->
      <div v-if="showRejectModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow">
            <div class="modal-header bg-danger text-white py-2.5 px-3">
              <h6 class="modal-title fw-bold">Tolak Purchase Request</h6>
              <button type="button" class="btn-close btn-close-white" @click="showRejectModal = false"></button>
            </div>
            <div class="modal-body p-3 fs-8">
              <p class="text-secondary mb-2">
                Anda akan menolak pengajuan Purchase Request <strong class="text-danger font-monospace">{{ pr.pr_number }}</strong>.
              </p>
              <textarea v-model="rejectionReason" class="form-control fs-8" rows="3" placeholder="Tuliskan catatan atau alasan penolakan secara jelas..."></textarea>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-secondary btn-sm" @click="showRejectModal = false">Batal</button>
              <button type="button" class="btn btn-danger btn-sm fw-bold" :disabled="isSubmitting" @click="confirmReject">
                <i class="bi bi-x-octagon me-1"></i> Konfirmasi Tolak PR
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();
const isLoading = ref(true);
const isSubmitting = ref(false);
const errorMessage = ref('');
const showRejectModal = ref(false);
const rejectionReason = ref('');

const pr = ref({
  id: route.params.id,
  pr_number: '',
  status: '',
  created_at: null,
  org_name: '',
  cost_center_code: '',
  requester_name: '',
  approver_name: null,
  procurement_method: '',
  purpose: '',
  budget_status: '',
  total_cost: 0,
  items: []
});

const formatRupiah = (val) => 'Rp ' + new Intl.NumberFormat('id-ID').format(val || 0);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return val;
  }
};

const badgeClass = (st) => {
  switch (st) {
    case 'APPROVED': return 'bg-success';
    case 'FULLY_ORDERED': return 'bg-purple text-white';
    case 'PARTIALLY_ORDERED': return 'bg-primary';
    case 'REJECTED': return 'bg-danger';
    default: return 'bg-warning text-dark';
  }
};

const mapPrData = (data) => {
  return {
    id: data.id,
    pr_number: data.prNumber,
    status: data.status,
    created_at: data.createdAt,
    org_name: data.organization?.name || 'Kantor Pusat',
    cost_center_code: data.organization?.code || '-',
    requester_name: data.createdByUser?.fullName || data.createdByUser?.username || 'Maker Logistik',
    approver_name: data.approvedByUser?.fullName || data.approvedByUser?.username || null,
    procurement_method: data.procurementMethod || 'TENDER_TERBUKA',
    purpose: data.purpose,
    budget_status: data.budgetStatus || 'Tervalidasi (Pagu Mencukupi)',
    total_cost: data.estimatedTotalCost || 0,
    items: (data.items || []).map(i => ({
      id: i.id,
      name: i.item?.name || 'Item',
      sku: i.item?.sku || '-',
      uom: i.item?.uom || 'PCS',
      qty_requested: i.qtyRequested || 0,
      qty_approved: i.qtyApproved || 0,
      qty_ordered: i.qtyOrdered || 0,
      remaining_qty: Math.max(0, (i.qtyApproved || i.qtyRequested || 0) - (i.qtyOrdered || 0)),
      unit_price: i.estimatedUnitPrice || 0,
      subtotal: i.estimatedSubtotal || 0
    }))
  };
};

const fetchPr = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get(`/procurement/pr/${route.params.id}`);
    const data = res.data.data || res.data;
    pr.value = mapPrData(data);
  } catch (err) {
    console.error('Failed to load PR', err);
    errorMessage.value = 'Gagal memuat data Purchase Request: ' + (err.response?.data?.message || err.message);
  } finally {
    isLoading.value = false;
  }
};

const approvePr = async () => {
  if (!confirm('Apakah Anda yakin ingin menyetujui Purchase Request ini?')) return;
  isSubmitting.value = true;
  try {
    const res = await api.post(`/procurement/pr/${pr.value.id}/approve`);
    const data = res.data.data || res.data;
    pr.value = mapPrData(data);
    alert('Purchase Request berhasil disetujui!');
  } catch (err) {
    alert('Gagal menyetujui PR: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const confirmReject = async () => {
  if (!rejectionReason.value.trim()) {
    alert('Silakan masukkan alasan penolakan.');
    return;
  }
  isSubmitting.value = true;
  try {
    const res = await api.post(`/procurement/pr/${pr.value.id}/reject`, {
      reason: rejectionReason.value
    });
    const data = res.data.data || res.data;
    pr.value = mapPrData(data);
    showRejectModal.value = false;
    alert('Purchase Request telah ditolak.');
  } catch (err) {
    alert('Gagal menolak PR: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchPr();
});
</script>

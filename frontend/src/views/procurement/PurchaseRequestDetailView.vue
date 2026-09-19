<template>
  <div class="pr-detail-page space-y-3">
    <!-- Error State -->
    <div v-if="errorMessage" class="alert alert-danger p-3 shadow-xs">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      {{ errorMessage }}
      <div class="mt-2">
        <router-link to="/procurement/pr" class="btn btn-sm btn-outline-danger">Kembali ke Daftar</router-link>
      </div>
    </div>

    <template v-else-if="!isLoading">
      <!-- Breadcrumb & Header -->
      <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
        <div>
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <h3 class="mb-0 text-body fw-bold">{{ pr.pr_number }}</h3>
            <span class="badge fs-8 text-uppercase" :class="badgeClass(pr.status)">
              {{ pr.status ? pr.status.replace('_', ' ') : 'SUBMITTED' }}
            </span>
            <span v-if="pr.fulfillment_status" class="badge fs-8 text-uppercase" :class="fulfillmentBadgeClass(pr.fulfillment_status)">
              Pemenuhan: {{ fulfillmentLabel(pr.fulfillment_status) }}
            </span>
          </div>
          <p class="fs-8 text-secondary mb-0 mt-0.5">
            Unit Pengaju: <strong class="text-body">{{ pr.org_name }}</strong> &bull; Dibuat: {{ formatDate(pr.created_at) }} &bull; Maker: {{ pr.requester_name }}
          </p>
        </div>

        <div class="d-flex align-items-center gap-2 flex-wrap">
          <router-link to="/procurement/pr" class="btn btn-sm btn-outline-secondary">
            Kembali ke Daftar
          </router-link>
          <router-link :to="`/procurement/pr/${pr.id}/print`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold shadow-xs">
            Cetak PR
          </router-link>
          <template v-if="['SUBMITTED', 'WAITING_APPROVAL'].includes(pr.status)">
            <button type="button" @click="showRejectModal = true" class="btn btn-sm btn-outline-danger fw-bold shadow-xs" :disabled="isSubmitting">
              Tolak PR
            </button>
            <button type="button" @click="approvePr" class="btn btn-sm btn-success fw-bold shadow-xs" :disabled="isSubmitting">
              Setujui PR (Approve)
            </button>
          </template>
          <template v-else-if="['APPROVED', 'FULLY_ORDERED'].includes(pr.status)">
            <router-link to="/procurement/consolidation" class="btn btn-sm btn-outline-primary fw-bold shadow-xs">
              Konsolidasi PO
            </router-link>
            <button
              v-if="pr.fulfillment_status !== 'FULFILLED'"
              type="button"
              class="btn btn-sm btn-danger fw-bold shadow-xs"
              :disabled="isSubmitting"
              @click="dispatchToBranch"
              title="Teruskan barang pesanan PR ini ke Antrean Gudang Distribusi Cabang"
            >
              Teruskan ke Distribusi Cabang
            </button>
          </template>
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
        <div class="card-header border-bottom p-3">
          <h3 class="card-title fw-semibold mb-0 fs-6 text-body">Rincian Item Purchase Request</h3>
        </div>

        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3 py-2.5">Item SKU & Nama</th>
                <th class="py-2.5 text-center">Diajukan</th>
                <th class="py-2.5 text-center">Disetujui</th>
                <th class="py-2.5 text-center">PO Vendor</th>
                <th class="py-2.5 text-center">Dipenuhi (Kirim)</th>
                <th class="py-2.5 text-center">Diterima Cabang</th>
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
                <td class="text-center font-monospace fw-bold text-info">{{ it.qty_fulfilled }}</td>
                <td class="text-center font-monospace fw-bold text-success">{{ it.qty_received }}</td>
                <td class="text-end font-monospace text-secondary">{{ formatRupiah(it.unit_price) }}</td>
                <td class="text-end pe-3 font-monospace fw-bold text-dark">{{ formatRupiah(it.subtotal) }}</td>
              </tr>
              <tr v-if="!pr.items || pr.items.length === 0">
                <td colspan="8" class="text-center py-4 text-secondary">Tidak ada rincian item.</td>
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
                Konfirmasi Tolak PR
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
  fulfillment_status: '',
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

const fulfillmentLabel = (status) => {
  switch (status) {
    case 'FULFILLED': return 'Lengkap Terpenuhi';
    case 'PARTIALLY_FULFILLED': return 'Sebagian';
    case 'UNFULFILLED': return 'Belum Dipenuhi';
    default: return status || '-';
  }
};

const fulfillmentBadgeClass = (status) => {
  switch (status) {
    case 'FULFILLED': return 'bg-success text-white';
    case 'PARTIALLY_FULFILLED': return 'bg-warning text-dark';
    case 'UNFULFILLED': return 'bg-secondary text-white';
    default: return 'bg-light text-dark';
  }
};

const mapPrData = (data) => {
  return {
    id: data.id,
    pr_number: data.prNumber,
    status: data.status,
    fulfillment_status: data.fulfillmentStatus || 'UNFULFILLED',
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
      qty_fulfilled: i.qtyFulfilled || 0,
      qty_received: i.qtyReceived || 0,
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

const dispatchToBranch = async () => {
  if (!confirm('Apakah Anda yakin ingin meneruskan barang hasil pengadaan PR ini ke antrean Gudang Distribusi Cabang (Picking, Packing & Pengiriman)?')) return;
  isSubmitting.value = true;
  try {
    const res = await api.post(`/procurement/pr/${pr.value.id}/dispatch-to-branch`);
    alert(res.data?.message || 'Pemenuhan PR berhasil diteruskan ke antrean Gudang Distribusi Cabang!');
    await fetchPr();
  } catch (err) {
    alert('Gagal meneruskan PR ke distribusi: ' + (err.response?.data?.message || err.message));
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

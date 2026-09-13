<template>
  <div class="return-detail-page space-y-3">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="mb-0 text-body fw-bold">{{ returnData.return_number }}</h3>
          <span class="badge fs-8 text-uppercase" :class="badgeClass(returnData.status)">
            {{ returnData.status }}
          </span>
        </div>
        <p class="fs-8 text-secondary mb-0 mt-0.5">
          Diajukan pada {{ returnData.created_at }} oleh <strong class="text-body">{{ returnData.requester_name }}</strong>
        </p>
      </div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <router-link to="/returns" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i> Kembali
        </router-link>
        <template v-if="returnData.status === 'REQUESTED'">
          <button type="button" class="btn btn-sm btn-outline-danger" @click="showRejectModal = true">
            <i class="bi bi-x-circle me-1"></i> Tolak
          </button>
          <button type="button" class="btn btn-sm btn-success fw-bold px-3 shadow-xs" @click="showApproveModal = true">
            <i class="bi bi-check-circle me-1"></i> Setujui Retur
          </button>
        </template>
        <template v-else-if="returnData.status === 'APPROVED'">
          <button type="button" class="btn btn-sm btn-danger fw-bold px-3 shadow-xs" @click="showShipModal = true">
            <i class="bi bi-truck me-1"></i> Kirim Barang Retur
          </button>
        </template>
        <template v-else-if="returnData.status === 'SHIPPED'">
          <button type="button" class="btn btn-sm btn-success fw-bold px-3 shadow-xs" @click="showReceiveModal = true">
            <i class="bi bi-box-seam me-1"></i> Konfirmasi Terima di Pusat
          </button>
        </template>
      </div>
    </div>

    <div class="card card-outline card-danger shadow-xs">
      <div class="card-body p-4">
        <div class="row g-3 mb-4">
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Gudang Asal (Cabang)</span>
            <span class="fw-bold text-dark fs-7">{{ returnData.origin_warehouse_name }}</span>
            <span class="text-muted fs-8 d-block">{{ returnData.origin_org_name }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Gudang Tujuan (Pusat)</span>
            <span class="fw-bold text-dark fs-7">{{ returnData.dest_warehouse_name }}</span>
            <span class="text-muted fs-8 d-block">CENTRAL_LOGISTICS</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Alasan Retur</span>
            <span class="badge bg-secondary bg-opacity-10 text-dark border fs-8 mt-1">
              {{ returnData.reason.replace('_', ' ') }}
            </span>
            <p v-if="returnData.reason_details" class="text-secondary fs-8 mt-1 mb-0">{{ returnData.reason_details }}</p>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Informasi Pengiriman</span>
            <div v-if="returnData.tracking_number">
              <span class="fw-semibold text-primary fs-8 d-block"><i class="bi bi-upc-scan me-1"></i>Resi: {{ returnData.tracking_number }}</span>
              <span class="text-muted fs-8">Kurir: {{ returnData.courier_name }} ({{ returnData.shipped_at }})</span>
            </div>
            <div v-else>
              <span class="text-muted fs-8 fst-italic">Belum dikirim</span>
            </div>
          </div>
        </div>

        <!-- Items Table -->
        <h6 class="fw-bold mb-3 text-dark"><i class="bi bi-box me-2"></i>Rincian Barang yang Diretur</h6>
        <div class="table-responsive border rounded-3">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3">SKU Barang</th>
                <th>Nama Barang</th>
                <th>Kategori</th>
                <th>Kondisi Fisik</th>
                <th class="text-center">Qty Retur</th>
                <th class="text-center">Qty Diterima (Bagus)</th>
                <th class="text-center">Qty Diterima (Rusak)</th>
                <th class="text-end pe-3">Estimasi Nilai</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in returnData.items" :key="item.id">
                <td class="ps-3 font-monospace fw-bold">{{ item.sku }}</td>
                <td class="fw-semibold">{{ item.name }}</td>
                <td>{{ item.category }}</td>
                <td>
                  <span class="badge bg-opacity-10 border" :class="item.condition === 'GOOD' ? 'bg-success text-success border-success' : 'bg-danger text-danger border-danger'">
                    {{ item.condition }}
                  </span>
                </td>
                <td class="text-center font-monospace fw-bold">{{ formatNumber(item.qty_returned) }} {{ item.uom }}</td>
                <td class="text-center font-monospace text-success fw-bold">{{ formatNumber(item.qty_received_good) }}</td>
                <td class="text-center font-monospace text-danger fw-bold">{{ formatNumber(item.qty_received_damaged) }}</td>
                <td class="text-end pe-3 font-monospace">{{ formatRupiah(item.subtotal) }}</td>
              </tr>
            </tbody>
            <tfoot class="table-light fw-bold">
              <tr>
                <td colspan="4" class="ps-3 text-uppercase">Total</td>
                <td class="text-center font-monospace">{{ formatNumber(returnData.total_qty) }}</td>
                <td colspan="2"></td>
                <td class="text-end pe-3 font-monospace text-danger">{{ formatRupiah(returnData.total_value) }}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Approve -->
    <div v-if="showApproveModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h6 class="modal-title fw-bold">Otorisasi Persetujuan Retur</h6>
            <button type="button" class="btn-close" @click="showApproveModal = false"></button>
          </div>
          <div class="modal-body fs-8 text-secondary">
            Apakah Anda yakin menyetujui permohonan retur barang dari <strong>{{ returnData.origin_warehouse_name }}</strong>? Cabang akan diinstruksikan untuk mengirimkan fisik barang ke gudang pusat.
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showApproveModal = false">Batal</button>
            <button type="button" class="btn btn-success btn-sm" @click="approveReturn">Setujui Permohonan</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Reject -->
    <div v-if="showRejectModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h6 class="modal-title fw-bold">Tolak Permohonan Retur Cabang</h6>
            <button type="button" class="btn-close btn-close-white" @click="showRejectModal = false"></button>
          </div>
          <div class="modal-body fs-8 space-y-3">
            <p class="text-secondary mb-2">Mohon berikan alasan penolakan permohonan retur ini agar cabang dapat melakukan revisi atau penyesuaian:</p>
            <div>
              <label class="form-label fw-bold mb-1">Alasan Penolakan <span class="text-danger">*</span></label>
              <textarea v-model="rejectReason" class="form-control form-control-sm" rows="3" placeholder="Contoh: Format berkas tidak lengkap / barang bukan persediaan warkat..." required></textarea>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showRejectModal = false">Batal</button>
            <button type="button" class="btn btn-danger btn-sm" @click="rejectReturn">Tolak Permohonan</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Ship -->
    <div v-if="showShipModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-primary text-white py-2 px-3">
            <h6 class="modal-title fw-bold">Kirim Fisik Barang Retur</h6>
            <button type="button" class="btn-close btn-close-white" @click="showShipModal = false"></button>
          </div>
          <div class="modal-body space-y-3 fs-8">
            <div class="mb-2">
              <label class="form-label fs-8 fw-semibold">Nama Ekspedisi / Kurir <span class="text-danger">*</span></label>
              <input type="text" v-model="shipForm.courier_name" class="form-control form-control-sm" placeholder="Contoh: JNE Logistik / Internal Driver" required>
            </div>
            <div class="mb-2">
              <label class="form-label fs-8 fw-semibold">Nomor Resi / AWB / No. Polisi <span class="text-danger">*</span></label>
              <input type="text" v-model="shipForm.tracking_number" class="form-control form-control-sm font-monospace" placeholder="Contoh: RET-AWB-987654" required>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showShipModal = false">Batal</button>
            <button type="button" class="btn btn-primary btn-sm" @click="confirmShip">Konfirmasi Kirim Retur</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Receive -->
    <div v-if="showReceiveModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-success text-white py-2 px-3">
            <h6 class="modal-title fw-bold">Konfirmasi Penerimaan Retur di Gudang Pusat</h6>
            <button type="button" class="btn-close btn-close-white" @click="showReceiveModal = false"></button>
          </div>
          <div class="modal-body space-y-3 fs-8">
            <div class="alert alert-success py-2 fs-8 border-0 mb-3">
              <i class="bi bi-check-circle me-1"></i> Verifikasi fisik barang yang diterima di Gudang Pusat. Stok akan otomatis bertambah (<strong>RETURN_IN</strong>).
            </div>
            <table class="table table-bordered table-sm align-middle fs-8 mb-0">
              <thead class="table-light">
                <tr>
                  <th>Barang</th>
                  <th class="text-center">Qty Dikirim</th>
                  <th class="text-center" style="width: 25%">Qty Diterima Bagus</th>
                  <th class="text-center" style="width: 25%">Qty Diterima Rusak</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in returnData.items" :key="item.id">
                  <td>
                    <strong>{{ item.name }}</strong>
                    <span class="text-muted d-block font-monospace fs-9">{{ item.sku }}</span>
                  </td>
                  <td class="text-center font-monospace fw-bold">{{ item.qty_returned }}</td>
                  <td>
                    <input type="number" v-model.number="item.qty_received_good" class="form-control form-control-sm text-center font-monospace" min="0" :max="item.qty_returned">
                  </td>
                  <td>
                    <input type="number" v-model.number="item.qty_received_damaged" class="form-control form-control-sm text-center font-monospace" min="0" :max="item.qty_returned">
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showReceiveModal = false">Batal</button>
            <button type="button" class="btn btn-success btn-sm" @click="confirmReceive">Simpan Penerimaan Retur</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();
const isLoading = ref(true);

const showApproveModal = ref(false);
const showRejectModal = ref(false);
const showShipModal = ref(false);
const showReceiveModal = ref(false);
const rejectReason = ref('');

const shipForm = reactive({
  courier_name: 'JNE Logistik',
  tracking_number: 'RET-AWB-2026-0045'
});

const returnData = ref({
  id: route.params.id || 1,
  return_number: 'RET-2026-09-0001',
  status: 'REQUESTED',
  created_at: '',
  requester_name: 'Petugas Cabang',
  origin_warehouse_name: 'Gudang Cabang Pemohon',
  origin_org_name: 'Kantor Cabang',
  dest_warehouse_name: 'WH-CEN-01 - Gudang Sentral Margomulyo',
  reason: 'DAMAGED_ON_ARRIVAL',
  reason_details: '',
  tracking_number: null,
  courier_name: null,
  shipped_at: null,
  total_qty: 0,
  total_value: 0,
  items: []
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);
const formatRupiah = (val) => 'Rp ' + formatNumber(val);

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
    case 'RECEIVED': return 'bg-success';
    case 'SHIPPED': return 'bg-info text-dark';
    case 'APPROVED': return 'bg-primary';
    case 'REJECTED': return 'bg-danger';
    default: return 'bg-warning text-dark';
  }
};

const fetchReturnDetail = async () => {
  isLoading.value = true;
  try {
    const res = await api.get(`/returns/${route.params.id}`);
    const r = res.data?.data || res.data;
    if (r) {
      returnData.value = {
        id: r.id,
        return_number: r.returnNumber,
        status: r.status,
        created_at: formatDate(r.createdAt),
        requester_name: r.requesterName || 'Petugas Cabang',
        origin_warehouse_name: r.organizationName || 'Kantor Cabang',
        origin_org_name: r.organizationName || 'Kantor Cabang',
        dest_warehouse_name: r.destinationWarehouseName || 'Gudang Sentral Margomulyo',
        reason: r.reason || 'DAMAGED_ON_ARRIVAL',
        reason_details: r.reason,
        tracking_number: null,
        courier_name: null,
        shipped_at: formatDate(r.shippedAt),
        total_qty: r.totalQty || 0,
        total_value: r.totalValue || 0,
        items: (r.items || []).map(i => ({
          id: i.id,
          sku: i.sku,
          name: i.name,
          category: i.category,
          condition: i.condition,
          uom: i.uom,
          qty_returned: i.qtyReturned,
          qty_received_good: i.qtyReceived || 0,
          qty_received_damaged: (i.qtyReturned || 0) - (i.qtyReceived || 0),
          subtotal: i.subtotal
        }))
      };
    }
  } catch (err) {
    console.error('Failed to load return detail', err);
  } finally {
    isLoading.value = false;
  }
};

const approveReturn = async () => {
  try {
    await api.post(`/returns/${returnData.value.id}/approve`);
    returnData.value.status = 'APPROVED';
    showApproveModal.value = false;
    alert('Permohonan retur telah disetujui!');
  } catch (err) {
    console.error('Failed to approve return', err);
    alert('Gagal menyetujui retur: ' + (err.response?.data?.message || err.message));
  }
};

const rejectReturn = async () => {
  try {
    await api.post(`/returns/${returnData.value.id}/reject`, {
      reason: rejectReason.value || 'Permohonan ditolak oleh Kantor Pusat.'
    });
    returnData.value.status = 'REJECTED';
    showRejectModal.value = false;
    alert('Permohonan retur telah ditolak.');
  } catch (err) {
    console.error('Failed to reject return', err);
    alert('Gagal menolak retur: ' + (err.response?.data?.message || err.message));
  }
};

const confirmShip = async () => {
  try {
    await api.post(`/returns/${returnData.value.id}/ship`, {
      courierName: shipForm.courier_name,
      trackingNumber: shipForm.tracking_number
    });
    returnData.value.status = 'SHIPPED';
    returnData.value.courier_name = shipForm.courier_name;
    returnData.value.tracking_number = shipForm.tracking_number;
    returnData.value.shipped_at = new Date().toLocaleString('id-ID');
    showShipModal.value = false;
    alert('Pengiriman barang retur berhasil dikonfirmasi!');
  } catch (err) {
    console.error('Failed to ship return', err);
    alert('Gagal mengkonfirmasi kirim: ' + (err.response?.data?.message || err.message));
  }
};

const confirmReceive = async () => {
  try {
    const items = returnData.value.items.map(it => ({
      itemId: it.id,
      qtyGood: it.qty_received_good,
      qtyDamaged: it.qty_received_damaged
    }));
    await api.post(`/returns/${returnData.value.id}/receive`, { items });
    returnData.value.status = 'RECEIVED';
    showReceiveModal.value = false;
    alert('Penerimaan retur di Gudang Pusat berhasil diverifikasi dan stok diperbarui!');
  } catch (err) {
    console.error('Failed to receive return', err);
    alert('Gagal menyimpan penerimaan: ' + (err.response?.data?.message || err.message));
  }
};

onMounted(() => {
  fetchReturnDetail();
});
</script>

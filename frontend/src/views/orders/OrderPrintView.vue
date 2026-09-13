<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 text-slate-900">
    <div class="print-container max-w-4xl mx-auto bg-white p-4 p-md-5 border border-slate-300 shadow-md rounded-sm">
      <!-- Top Toolbar (Hidden when printing) -->
      <div class="no-print mb-4 d-flex justify-content-between align-items-center bg-slate-50 p-3 rounded border border-slate-200">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/orders" class="text-xs text-slate-600 font-semibold text-decoration-none d-inline-flex align-items-center">
            <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Order
          </router-link>
          <span class="text-slate-300">|</span>
          <span class="text-xs text-slate-500 font-monospace">Surat Permintaan Barang Resmi Bank Jatim</span>
        </div>
        <button @click="triggerPrint" class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1">
          <i class="bi bi-printer"></i>
          <span>Cetak Dokumen Order (Print)</span>
        </button>
      </div>

      <div v-if="errorMessage" class="no-print alert alert-danger fs-8">{{ errorMessage }}</div>

      <!-- Official Bank Jatim Letterhead -->
      <div class="border-b-2 border-danger pb-3 mb-4 d-flex justify-content-between align-items-start">
        <div class="d-flex align-items-center gap-3">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" style="height: 48px; width: auto; object-fit: contain;">
          <div>
            <h1 class="fs-6 fw-black text-danger mb-0">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR TBK</h1>
            <p class="fs-8 fw-semibold text-secondary mb-0">DIVISI LOGISTIK & UMUM - PENGELOLAAN PERSEDIAAN BARANG</p>
            <p class="fs-9 text-muted mb-0">Kantor Pusat: Jl. Basuki Rahmat No. 98-104, Surabaya | Telp: (031) 5310090</p>
          </div>
        </div>
        <div class="text-end">
          <h2 class="fs-6 fw-black text-dark mb-0">FORM PERMINTAAN BARANG</h2>
          <p class="fs-7 font-monospace fw-bold text-danger mb-0">{{ order.order_number }}</p>
          <div class="fs-9 text-muted">Status: <span class="fw-semibold text-dark">{{ order.status }}</span></div>
        </div>
      </div>

      <!-- Meta Information Grid -->
      <div class="row g-3 fs-8 mb-4">
        <!-- Unit Pemohon Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Unit Kerja Pemohon:
            </div>
            <div class="fw-bold fs-7 text-dark">{{ order.org_name }}</div>
            <div class="text-secondary font-monospace fs-9">Kode Unit: {{ order.org_code }}</div>
            <div class="text-secondary">Kota / Wilayah: {{ order.city }}</div>
            <div class="text-secondary">Pemohon: <span class="fw-medium text-dark">{{ order.requester_name }}</span></div>
          </div>
        </div>

        <!-- Detail Order & Logistik Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Informasi Kebutuhan & Logistik:
            </div>
            <div class="row g-1 fs-9">
              <div class="col-6 text-secondary">Tanggal Pengajuan:</div>
              <div class="col-6 fw-semibold text-dark font-monospace">{{ order.created_at }} WIB</div>

              <div class="col-6 text-secondary">Target Dibutuhkan:</div>
              <div class="col-6 fw-semibold text-dark font-monospace">{{ order.required_date }}</div>

              <div class="col-6 text-secondary">Tingkat Prioritas:</div>
              <div class="col-6">
                <span class="badge" :class="order.priority === 'URGENT' ? 'bg-danger' : 'bg-secondary'">{{ order.priority }}</span>
              </div>

              <div class="col-6 text-secondary">Gudang Pemenuhan:</div>
              <div class="col-6 fw-semibold text-dark">{{ order.warehouse_name }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Items Table -->
      <div class="mb-4">
        <h3 class="fs-8 fw-bold text-uppercase text-secondary mb-2">
          Daftar Barang yang Diminta:
        </h3>
        <table class="table table-bordered table-sm align-middle fs-8 mb-0">
          <thead class="table-light text-secondary">
            <tr>
              <th class="text-center" style="width: 40px;">No</th>
              <th>Kode & Nama Barang</th>
              <th>Kategori</th>
              <th class="text-center" style="width: 70px;">Satuan</th>
              <th class="text-center" style="width: 80px;">Kuantitas</th>
              <th class="text-end" style="width: 140px;">Harga Satuan Ref (Rp)</th>
              <th class="text-end" style="width: 150px;">Subtotal (Rp)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(it, idx) in order.items" :key="idx">
              <td class="text-center font-monospace text-secondary">{{ idx + 1 }}</td>
              <td>
                <div class="fw-bold text-dark">{{ it.name }}</div>
                <div class="fs-9 text-muted font-monospace">SKU: {{ it.sku }}</div>
              </td>
              <td class="text-secondary">{{ it.category }}</td>
              <td class="text-center font-monospace">{{ it.uom }}</td>
              <td class="text-center font-monospace fw-bold text-dark">{{ it.qty }}</td>
              <td class="text-end font-monospace text-secondary">{{ formatRupiah(it.unit_price) }}</td>
              <td class="text-end font-monospace fw-bold text-dark">{{ formatRupiah(it.subtotal) }}</td>
            </tr>
          </tbody>
          <tfoot class="table-light fw-bold">
            <tr>
              <td colspan="6" class="text-end text-uppercase fs-9">Total Estimasi Nilai Permintaan:</td>
              <td class="text-end font-monospace text-danger fs-7">
                {{ formatRupiah(order.total_amount) }}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- Signatures / Legal Approval Section -->
      <div class="row text-center fs-8 mt-5 pt-4 border-top">
        <div class="col-4">
          <p class="text-secondary mb-1">Pemohon / Pengaju:</p>
          <div style="height: 60px;"></div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">{{ order.requester_name }}</p>
          <p class="fs-9 text-secondary">{{ order.org_name }}</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-1">Menyetujui (Pimpinan Unit):</p>
          <div style="height: 60px;">
            <div class="border border-success bg-success bg-opacity-10 text-success p-1 rounded fs-9 fw-bold d-inline-block">
              DISETUJUI SECARA SISTEM<br>
              <span class="fw-normal">{{ order.approved_at }}</span>
            </div>
          </div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">{{ order.approved_by }}</p>
          <p class="fs-9 text-secondary">Pemimpin Cabang / Bagian</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-1">Penerima di Logistik Pusat:</p>
          <div style="height: 60px;"></div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">Petugas Logistik & Gudang</p>
          <p class="fs-9 text-secondary">Divisi Logistik & Umum</p>
        </div>
      </div>

      <!-- Document Footer -->
      <div class="mt-4 pt-3 border-top d-flex justify-content-between fs-9 text-muted">
        <span>Dokumen dicetak secara elektronik melalui Sistem Informasi Logistik & Persediaan Bank Jatim.</span>
        <span class="font-monospace">Tanggal Cetak: {{ order.printed_at }} WIB</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';

const route = useRoute();
const errorMessage = ref('');

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('id-ID', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(new Date(value));
};

const emptyOrder = () => ({
  order_number: '-',
  status: '-',
  org_name: '-',
  org_code: '-',
  city: '-',
  requester_name: '-',
  created_at: '-',
  required_date: '-',
  priority: '-',
  warehouse_name: '-',
  approved_by: '-',
  approved_at: '-',
  printed_at: formatDate(new Date()),
  total_amount: 0,
  items: []
});

const order = ref(emptyOrder());

const mapOrder = (data) => ({
  order_number: data.orderNumber || '-',
  status: data.status || '-',
  org_name: data.requestingOrganization?.name || '-',
  org_code: data.requestingOrganization?.code || '-',
  city: '-',
  requester_name: data.createdByUser
    ? `${data.createdByUser.name || '-'}${data.createdByUser.nip ? ` (NIP: ${data.createdByUser.nip})` : ''}`
    : '-',
  created_at: formatDate(data.createdAt || data.submittedAt),
  required_date: data.requiredDate || '-',
  priority: data.priority || '-',
  warehouse_name: '-',
  approved_by: data.approvedByUser?.name || '-',
  approved_at: formatDate(data.approvedAt),
  printed_at: formatDate(new Date()),
  total_amount: Number(data.totalEstimatedValue || 0),
  items: (data.items || []).map((item) => ({
    name: item.item?.name || '-',
    sku: item.item?.sku || '-',
    category: '-',
    uom: item.item?.uom || '-',
    qty: Number(item.qtyRequested || 0),
    unit_price: Number(item.unitPriceRef || 0),
    subtotal: Number(item.subtotalRef || 0)
  }))
});

const loadOrder = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get(`/orders/${route.params.id}`);
    order.value = mapOrder(response.data);
  } catch (error) {
    order.value = emptyOrder();
    errorMessage.value = error?.message || error?.error || 'Gagal memuat dokumen order.';
  }
};

const formatRupiah = (val) => {
  return 'Rp ' + new Intl.NumberFormat('id-ID').format(Math.round(val || 0));
};

const triggerPrint = () => {
  window.print();
};

onMounted(loadOrder);
</script>

<style scoped>
@media print {
  .no-print {
    display: none !important;
  }
  .print-page {
    background: white !important;
    padding: 0 !important;
  }
  .print-container {
    border: none !important;
    box-shadow: none !important;
    max-width: 100% !important;
    padding: 0 !important;
  }
}
</style>

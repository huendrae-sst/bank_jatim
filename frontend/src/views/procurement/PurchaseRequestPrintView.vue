<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 text-slate-900">
    <div class="print-container max-w-4xl mx-auto bg-white p-4 p-md-5 border border-slate-300 shadow-md rounded-sm">
      <!-- Top Toolbar (Hidden when printing) -->
      <div class="no-print mb-4 d-flex justify-content-between align-items-center bg-slate-50 p-3 rounded border border-slate-200">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/procurement/pr" class="text-xs text-slate-600 font-semibold text-decoration-none d-inline-flex align-items-center">
            Kembali ke Daftar PR
          </router-link>
          <span class="text-slate-300">|</span>
          <span class="text-xs text-slate-500 font-monospace">Form Pengajuan Pengadaan Barang Resmi Bank Jatim</span>
        </div>
        <button @click="triggerPrint" class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1">
          <span>Cetak Purchase Request (Print)</span>
        </button>
      </div>

      <!-- Official Bank Jatim Letterhead -->
      <div class="border-b-2 border-danger pb-3 mb-4 d-flex justify-content-between align-items-start">
        <div class="d-flex align-items-center gap-3">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" style="height: 48px; width: auto; object-fit: contain;">
          <div>
            <h1 class="fs-6 fw-black text-danger mb-0">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR TBK</h1>
            <p class="fs-8 fw-semibold text-secondary mb-0">DIVISI LOGISTIK & UMUM - PENGADAAN BARANG & JASA</p>
            <p class="fs-9 text-muted mb-0">Kantor Pusat: Jl. Basuki Rahmat No. 98-104, Surabaya | Telp: (031) 5310090</p>
          </div>
        </div>
        <div class="text-end">
          <h2 class="fs-6 fw-black text-dark mb-0">PURCHASE REQUEST</h2>
          <p class="fs-7 font-monospace fw-bold text-danger mb-0">{{ pr.pr_number }}</p>
          <div class="fs-9 text-muted">Status: <span class="fw-semibold text-dark">{{ pr.status }}</span></div>
        </div>
      </div>

      <!-- Meta Information Grid -->
      <div class="row g-3 fs-8 mb-4">
        <!-- Requester Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Unit Pengaju & Pemohon:
            </div>
            <div class="fw-bold fs-7 text-dark">{{ pr.org_name }}</div>
            <div class="text-secondary font-monospace fs-9">Kode Unit: {{ pr.org_code }}</div>
            <div class="text-secondary">Kota: Surabaya</div>
            <div class="text-secondary">Diajukan Oleh: <span class="fw-medium text-dark">{{ pr.requester_name }}</span></div>
          </div>
        </div>

        <!-- Procurement & Budget Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Parameter Pengadaan & Anggaran:
            </div>
            <div class="row g-1 fs-9">
              <div class="col-6 text-secondary">Tanggal Pengajuan:</div>
              <div class="col-6 fw-semibold text-dark font-monospace">{{ pr.created_at }} WIB</div>

              <div class="col-6 text-secondary">Metode Pengadaan:</div>
              <div class="col-6 fw-semibold text-dark">{{ pr.procurement_method.replace('_', ' ') }}</div>

              <div class="col-6 text-secondary">Ketersediaan Anggaran:</div>
              <div class="col-6">
                <span class="badge bg-success">Tersedia (VALIDATED)</span>
              </div>

              <div class="col-6 text-secondary">Pejabat Penyetuju:</div>
              <div class="col-6 fw-semibold text-dark">{{ pr.approver_name || 'Menunggu Persetujuan' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Purpose / Justification -->
      <div class="mb-4 p-3 bg-light rounded border border-slate-200 fs-8">
        <div class="fw-bold text-secondary text-uppercase fs-9 mb-1">Tujuan & Justifikasi Pengadaan:</div>
        <p class="text-dark font-monospace mb-0 leading-relaxed">{{ pr.purpose }}</p>
      </div>

      <!-- Items Table -->
      <div class="mb-4">
        <h3 class="fs-8 fw-bold text-uppercase text-secondary mb-2">
          Rincian Barang yang Diajukan:
        </h3>
        <table class="table table-bordered table-sm align-middle fs-8 mb-0">
          <thead class="table-light text-secondary">
            <tr>
              <th class="text-center" style="width: 40px;">No</th>
              <th>Kode & Nama Barang</th>
              <th>Kategori</th>
              <th class="text-center" style="width: 70px;">Satuan</th>
              <th class="text-center" style="width: 90px;">Qty Diajukan</th>
              <th class="text-center" style="width: 90px;">Qty Disetujui</th>
              <th class="text-end" style="width: 140px;">Harga Satuan (Rp)</th>
              <th class="text-end" style="width: 150px;">Subtotal (Rp)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(it, idx) in pr.items" :key="idx">
              <td class="text-center font-monospace text-secondary">{{ idx + 1 }}</td>
              <td>
                <div class="fw-bold text-dark">{{ it.name }}</div>
                <div class="fs-9 text-muted font-monospace">SKU: {{ it.sku }}</div>
              </td>
              <td class="text-secondary">{{ it.category }}</td>
              <td class="text-center font-monospace">{{ it.uom }}</td>
              <td class="text-center font-monospace fw-bold text-dark">{{ formatNumber(it.qty_requested) }}</td>
              <td class="text-center font-monospace fw-bold text-success">{{ formatNumber(it.qty_approved) }}</td>
              <td class="text-end font-monospace text-secondary">{{ formatRupiah(it.unit_price) }}</td>
              <td class="text-end font-monospace fw-bold text-dark">{{ formatRupiah(it.subtotal) }}</td>
            </tr>
          </tbody>
          <tfoot class="table-light fw-bold">
            <tr>
              <td colspan="7" class="text-end text-uppercase fs-9">Total Estimasi Nilai Pengadaan:</td>
              <td class="text-end font-monospace text-danger fs-7">
                {{ formatRupiah(pr.total_cost) }}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- Three Signatures Section -->
      <div class="row text-center fs-8 mt-5 pt-4 border-top">
        <div class="col-4">
          <p class="text-secondary mb-1">Diajukan Oleh (Pemohon):</p>
          <div style="height: 60px;"></div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">{{ pr.requester_name }}</p>
          <p class="fs-9 text-secondary">{{ pr.org_name }}</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-1">Verifikasi Anggaran:</p>
          <div style="height: 60px;">
            <div class="border border-success bg-success bg-opacity-10 text-success p-1 rounded fs-9 fw-bold d-inline-block">
              ANGGARAN TERSEDIA<br>
              <span class="fw-normal">Sistem Otomasi Anggaran</span>
            </div>
          </div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">Bagian Anggaran & Keuangan</p>
          <p class="fs-9 text-secondary">Divisi Keuangan & Akuntansi</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-1">Disetujui Oleh (Approver):</p>
          <div style="height: 60px;">
            <div class="border border-success bg-success bg-opacity-10 text-success p-1 rounded fs-9 fw-bold d-inline-block">
              DISETUJUI SECARA SISTEM<br>
              <span class="fw-normal">11/09/2026 14:20 WIB</span>
            </div>
          </div>
          <p class="fw-bold text-dark text-decoration-underline mb-0">{{ pr.approver_name || 'Procurement Approver' }}</p>
          <p class="fs-9 text-secondary">Pejabat Pengadaan Barang & Jasa</p>
        </div>
      </div>

      <!-- Document Footer -->
      <div class="mt-4 pt-3 border-top d-flex justify-content-between fs-9 text-muted">
        <span>Form Purchase Request ini merupakan dokumen sah pengajuan kebutuhan persediaan internal Bank Jatim.</span>
        <span class="font-monospace">Tanggal Cetak: {{ printDate }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';
import { toast } from '@/utils/toast';

const route = useRoute();
const isLoading = ref(true);

const pr = ref({
  pr_number: '',
  status: '',
  created_at: '',
  org_name: '',
  org_code: '',
  requester_name: '',
  approver_name: null,
  procurement_method: '',
  purpose: '',
  total_cost: 0,
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

const printDate = new Date().toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' }) + ' WIB';

const triggerPrint = () => {
  window.print();
};

const fetchPr = async () => {
  isLoading.value = true;
  try {
    const res = await api.get(`/procurement/pr/${route.params.id}`);
    const data = res.data.data || res.data;
    pr.value = {
      pr_number: data.prNumber,
      status: data.status,
      created_at: formatDate(data.createdAt),
      org_name: data.organization?.name || 'Kantor Pusat',
      org_code: data.organization?.code || '-',
      requester_name: data.createdByUser?.fullName || data.createdByUser?.username || 'Maker Logistik',
      approver_name: data.approvedByUser?.fullName || data.approvedByUser?.username || null,
      procurement_method: data.procurementMethod || 'TENDER_TERBUKA',
      purpose: data.purpose,
      total_cost: data.estimatedTotalCost || 0,
      items: (data.items || []).map(i => ({
        name: i.item?.name || 'Item',
        sku: i.item?.sku || '-',
        category: i.item?.categoryName || 'Umum',
        uom: i.item?.uom || 'PCS',
        qty_requested: i.qtyRequested || 0,
        qty_approved: i.qtyApproved || 0,
        unit_price: i.estimatedUnitPrice || 0,
        subtotal: i.estimatedSubtotal || 0
      }))
    };
  } catch (err) {
    console.error('Failed to fetch PR print data', err);
    toast.error('Gagal memuat data Purchase Request untuk dicetak: ' + (err.response?.data?.message || err.message));
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchPr();
});
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

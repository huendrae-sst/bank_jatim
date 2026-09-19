<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 text-slate-900">
    <div class="print-container max-w-4xl mx-auto bg-white p-4 p-md-5 border border-slate-300 shadow-md rounded-sm">
      <!-- Top Toolbar (Hidden when printing) -->
      <div class="no-print mb-4 d-flex justify-content-between align-items-center bg-slate-50 p-3 rounded border border-slate-200">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/receiving/discrepancies" class="text-xs text-slate-600 font-semibold text-decoration-none d-inline-flex align-items-center">
            Kembali ke Daftar Discrepancy
          </router-link>
          <span class="text-slate-300">|</span>
          <span class="text-xs text-slate-500 font-monospace">Berita Acara Selisih & Klaim Resmi Bank Jatim</span>
        </div>
        <button @click="triggerPrint" class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1">
          <span>Cetak Berita Acara (Print)</span>
        </button>
      </div>

      <!-- Official Bank Jatim Letterhead -->
      <div class="border-b-2 border-danger pb-3 mb-4 d-flex justify-content-between align-items-start">
        <div class="d-flex align-items-center gap-3">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" style="height: 48px; width: auto; object-fit: contain;">
          <div>
            <h1 class="fs-6 fw-black text-danger mb-0">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR TBK</h1>
            <p class="fs-8 fw-semibold text-secondary mb-0">DIVISI LOGISTIK & UMUM - PENGAWASAN & PENERIMAAN BARANG</p>
            <p class="fs-9 text-muted mb-0">Kantor Pusat: Jl. Basuki Rahmat No. 98-104, Surabaya | Telp: (031) 5310090</p>
          </div>
        </div>
        <div class="text-end">
          <h2 class="fs-6 fw-black text-dark mb-0">BERITA ACARA DISCREPANCY</h2>
          <p class="fs-7 font-monospace fw-bold text-danger mb-0">{{ discrepancy.ba_number }}</p>
          <div class="fs-9 text-muted">Status Klaim: <span class="fw-bold text-danger">{{ discrepancy.resolution_status }}</span></div>
        </div>
      </div>

      <!-- Meta Information Grid -->
      <div class="row g-3 fs-8 mb-4">
        <!-- Receiving & Delivery Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Informasi Penerimaan & Ekspedisi:
            </div>
            <div class="row g-1 fs-9">
              <div class="col-6 text-secondary">No. Penerimaan:</div>
              <div class="col-6 font-monospace fw-bold text-dark">{{ discrepancy.receiving_number }}</div>

              <div class="col-6 text-secondary">Tanggal Diterima:</div>
              <div class="col-6 fw-semibold text-dark font-monospace">{{ discrepancy.receipt_date }}</div>

              <div class="col-6 text-secondary">No. Order Acuan:</div>
              <div class="col-6 font-monospace text-dark">{{ discrepancy.order_number }}</div>

              <div class="col-6 text-secondary">No. Manifest Kirim:</div>
              <div class="col-6 font-monospace text-dark">{{ discrepancy.manifest_number }}</div>

              <div class="col-6 text-secondary">Jasa Ekspedisi / Kurir:</div>
              <div class="col-6 fw-semibold text-dark">{{ discrepancy.courier_name }}</div>
            </div>
          </div>
        </div>

        <!-- Locations & Personnel Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Lokasi & Petugas Penerima:
            </div>
            <div class="row g-1 fs-9">
              <div class="col-6 text-secondary">Cabang Penerima:</div>
              <div class="col-6 fw-bold text-dark">{{ discrepancy.branch_name }}</div>

              <div class="col-6 text-secondary">Kota / Wilayah:</div>
              <div class="col-6 text-dark">{{ discrepancy.branch_city }}</div>

              <div class="col-6 text-secondary">Gudang Pengirim:</div>
              <div class="col-6 fw-semibold text-dark">{{ discrepancy.warehouse_origin }}</div>

              <div class="col-6 text-secondary">Petugas Penerima:</div>
              <div class="col-6 fw-semibold text-dark">{{ discrepancy.receiver_name }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Discrepancy Details Box -->
      <div class="mb-4 p-3 rounded border border-danger-subtle bg-danger bg-opacity-10">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h3 class="fs-8 fw-bold text-uppercase text-danger mb-0">
            Rincian Barang Berselisih (Discrepancy Breakdown)
          </h3>
          <span class="badge bg-danger text-white fs-9">
            Kategori: {{ discrepancy.discrepancy_type }}
          </span>
        </div>

        <div class="bg-white p-2.5 rounded border mb-3 fs-8">
          <div class="fw-bold fs-7 text-dark">{{ discrepancy.item_name }}</div>
          <div class="text-secondary font-monospace fs-9 mt-0.5">
            Kode SKU: <strong class="text-dark">{{ discrepancy.item_sku }}</strong> | 
            Satuan: {{ discrepancy.item_uom }}
          </div>
        </div>

        <!-- Comparison Table -->
        <table class="table table-bordered table-sm text-center fs-8 bg-white mb-0">
          <thead class="table-light text-secondary font-bold">
            <tr>
              <th>Kuantitas Dikirim (Manifest)</th>
              <th class="text-success">Kuantitas Diterima Baik</th>
              <th class="text-danger">Kuantitas Rusak / Kurang</th>
              <th>Satuan</th>
            </tr>
          </thead>
          <tbody>
            <tr class="font-monospace fw-bold fs-7">
              <td class="text-dark">{{ formatNumber(discrepancy.qty_expected) }}</td>
              <td class="text-success bg-success bg-opacity-10">{{ formatNumber(discrepancy.qty_actual) }}</td>
              <td class="text-danger bg-danger bg-opacity-10">{{ formatNumber(discrepancy.qty_damaged) }}</td>
              <td class="text-secondary font-normal fs-8">{{ discrepancy.item_uom }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Notes / Remarks -->
      <div class="mb-4 p-3 bg-light rounded border border-slate-200 fs-8">
        <div class="fw-bold text-secondary text-uppercase fs-9 mb-1">
          Keterangan / Kronologi Temuan Selisih:
        </div>
        <p class="text-dark font-monospace mb-0 leading-relaxed">
          {{ discrepancy.resolution_notes }}
        </p>
      </div>

      <!-- Four Legal Signatures Section -->
      <div class="row text-center fs-8 mt-5 pt-4 border-top">
        <div class="col-3">
          <p class="text-secondary mb-4">Penerima Cabang:</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">{{ discrepancy.receiver_name }}</p>
          <p class="fs-9 text-muted">{{ discrepancy.branch_name }}</p>
        </div>
        <div class="col-3">
          <p class="text-secondary mb-4">Kurir / Ekspedisi:</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">Driver Ekspedisi</p>
          <p class="fs-9 text-muted">{{ discrepancy.courier_name }}</p>
        </div>
        <div class="col-3">
          <p class="text-secondary mb-4">Pimpinan Cabang:</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">Pimpinan Unit</p>
          <p class="fs-9 text-muted">Pemimpin Bagian</p>
        </div>
        <div class="col-3">
          <p class="text-secondary mb-4">Logistik Pusat:</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">Petugas Logistik</p>
          <p class="fs-9 text-muted">Divisi Logistik & Umum</p>
        </div>
      </div>

      <!-- Document Footer -->
      <div class="mt-4 pt-3 border-top d-flex justify-content-between fs-9 text-muted">
        <span>Berita Acara ini merupakan bukti sah pemeriksaan fisik penerimaan barang dan dasar klaim/pergantian logistik Bank Jatim.</span>
        <span class="font-monospace">Tanggal Cetak: 12/09/2026 13:48 WIB</span>
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

const discrepancy = ref({
  ba_number: '',
  receiving_number: '',
  receipt_date: '',
  order_number: '',
  manifest_number: '',
  courier_name: '',
  branch_name: '',
  branch_city: '',
  warehouse_origin: '',
  receiver_name: '',
  discrepancy_type: '',
  resolution_status: '',
  item_name: '',
  item_sku: '',
  item_uom: '',
  qty_expected: 0,
  qty_actual: 0,
  qty_damaged: 0,
  resolution_notes: ''
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);

const printDate = new Date().toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' }) + ' WIB';

const triggerPrint = () => {
  window.print();
};

const fetchDiscrepancy = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get(`/receiving/discrepancies/${route.params.id}`);
    const data = res.data.data || res.data;
    discrepancy.value = {
      ba_number: data.beritaAcaraNumber || `BA-DISC/2026/${data.id}`,
      receiving_number: data.receivingNumber || `RCV-${data.receivingId || data.id}`,
      receipt_date: new Date().toLocaleDateString('id-ID'),
      order_number: data.orderNumber || '-',
      manifest_number: `MNF-2026-${data.id}`,
      courier_name: 'PT JNE Express Logistik',
      branch_name: 'Kantor Cabang Bank Jatim',
      branch_city: 'Jawa Timur',
      warehouse_origin: 'Gudang Sentral Margomulyo Surabaya',
      receiver_name: 'Petugas Penerima Unit',
      discrepancy_type: data.discrepancyType || 'DAMAGED_ITEMS',
      resolution_status: data.resolutionStatus || 'INVESTIGATING',
      item_name: data.item?.name || 'Barang Persediaan',
      item_sku: data.item?.sku || '-',
      item_uom: data.item?.uom || 'PCS',
      qty_expected: data.qtyExpected || 0,
      qty_actual: data.qtyActual || 0,
      qty_damaged: data.qtyDamaged || 0,
      resolution_notes: data.resolutionNotes || 'Pemeriksaan fisik saat serah terima barang di cabang.'
    };
  } catch (err) {
    console.error('Failed to load discrepancy print data', err);
    toast.error('Gagal memuat berita acara selisih: ' + (err.response?.data?.message || err.message));
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchDiscrepancy();
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

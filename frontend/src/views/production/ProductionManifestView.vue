<template>
  <div class="manifest-print-page bg-light min-vh-100 p-3 p-md-4">
    <div class="no-print text-center py-3">
      <button @click="triggerPrint" class="btn btn-primary btn-sm px-4 shadow-sm">
        <i class="bi bi-printer me-1"></i> Cetak / Simpan PDF
      </button>
      <router-link :to="`/production/${prodOrder.id}`" class="btn btn-secondary btn-sm ms-2">
        <i class="bi bi-arrow-left me-1"></i> Kembali
      </router-link>
    </div>

    <div class="document-box shadow-sm bg-white mx-auto p-4 p-md-5 border" style="max-width: 850px; font-family: 'Times New Roman', Times, serif; color: #111; font-size: 13px;">
      <div class="text-center header-title border-bottom border-dark pb-3 mb-4">
        <h4 class="mb-1 fw-bold">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR, Tbk.</h4>
        <h5 class="mb-1 text-uppercase text-decoration-underline">REKAP MANIFEST PRODUKSI & PERSONALISASI KARTU</h5>
        <span class="fs-6 font-monospace">Nomor Manifest: {{ manifestData.manifest_number }}</span>
      </div>

      <div class="row g-3 mb-3 border p-3 rounded bg-light">
        <div class="col-6">
          <span class="fs-8 text-uppercase fw-bold text-secondary d-block">Unit Kerja / Cabang Tujuan:</span>
          <strong class="fs-6">{{ manifestData.dest_name }} ({{ manifestData.dest_code }})</strong>
          <p class="mb-0 text-dark fs-8">
            Alamat: {{ manifestData.dest_address }}<br>
            Kota: {{ manifestData.dest_city }} | Telp: {{ manifestData.dest_phone }}<br>
            Cost Center: <span class="font-monospace fw-semibold">{{ manifestData.cost_center }}</span>
          </p>
        </div>
        <div class="col-6 text-end">
          <span class="fs-8 text-uppercase fw-bold text-secondary d-block">Data Bon Produksi:</span>
          <strong class="font-monospace fs-6 text-danger">{{ prodOrder.production_number }}</strong>
          <p class="mb-0 text-secondary fs-8">
            Tanggal: {{ manifestData.generated_at }}<br>
            Gudang Asal: {{ manifestData.warehouse_name }}<br>
            Ref. File Emboss: <span class="font-monospace fw-semibold">{{ prodOrder.emboss_file }}</span><br>
            Ref. Order: <span class="font-monospace fw-semibold">{{ prodOrder.order_number }}</span>
          </p>
        </div>
      </div>

      <h6 class="fw-bold mb-2">RINCIAN BAHAN KARTU / TOKEN / KUE YANG DIKELUARKAN:</h6>
      <table class="table table-bordered table-sm w-100 mb-3 text-dark" style="border: 1px solid #333; font-size: 12px;">
        <thead>
          <tr class="table-secondary text-center">
            <th style="width: 5%">No.</th>
            <th>Kode / SKU</th>
            <th>Nama Produk</th>
            <th>Kategori</th>
            <th>Satuan</th>
            <th class="text-center">Qty Rencana</th>
            <th class="text-center">Qty Dikeluarkan</th>
            <th class="text-end">Estimasi Nilai</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in manifestData.items" :key="idx">
            <td class="text-center">{{ idx + 1 }}</td>
            <td class="font-monospace text-center">{{ item.sku }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.category }}</td>
            <td class="text-center">{{ item.uom }}</td>
            <td class="text-center font-monospace">{{ formatNumber(item.qty_planned) }}</td>
            <td class="text-center font-monospace fw-bold">{{ formatNumber(item.qty_issued) }}</td>
            <td class="text-end font-monospace">{{ formatRupiah(item.subtotal) }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="fw-bold table-light">
            <td colspan="5" class="text-center text-uppercase">Total Rekap</td>
            <td class="text-center font-monospace">{{ formatNumber(prodOrder.total_planned_qty) }}</td>
            <td class="text-center font-monospace">{{ formatNumber(prodOrder.total_issued_qty) }}</td>
            <td class="text-end font-monospace text-danger">{{ formatRupiah(manifestData.total_subtotal) }}</td>
          </tr>
        </tfoot>
      </table>

      <p class="text-justify mb-4 fs-8 text-secondary">
        Catatan: Dokumen ini merupakan bukti sah pengeluaran bahan baku personalisasi kartu ATM / instrumen perbankan dari Gudang Logistik Pusat untuk didistribusikan ke unit kerja peminta.
      </p>

      <div class="row text-center signature-box mt-5">
        <div class="col-4">
          <p class="mb-0 fs-8">Direncanakan Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Staff Personalisasi Kartu</p>
          <div style="height: 50px;"></div>
          <strong><u>{{ prodOrder.creator_name }}</u></strong>
        </div>
        <div class="col-4">
          <p class="mb-0 fs-8">Dikeluarkan Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Petugas Gudang Bahan</p>
          <div style="height: 50px;"></div>
          <strong><u>{{ prodOrder.issuer_name }}</u></strong>
        </div>
        <div class="col-4">
          <p class="mb-0 fs-8">Disetujui Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Supervisor Logistik & Distribusi</p>
          <div style="height: 50px;"></div>
          <strong><u>Hendra Sudrajat</u></strong>
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
const isLoading = ref(true);

const prodOrder = ref({
  id: route.params.id,
  production_number: '',
  emboss_file: '',
  order_number: '',
  total_planned_qty: 0,
  total_issued_qty: 0,
  creator_name: '',
  issuer_name: ''
});

const manifestData = ref({
  manifest_number: '',
  generated_at: '',
  warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
  dest_name: 'Kantor Cabang Surabaya Utama',
  dest_code: 'CBR-SBY-UT',
  dest_address: 'Jl. Basuki Rahmat No. 98-104, Surabaya',
  dest_city: 'Kota Surabaya',
  dest_phone: '(031) 5310090',
  cost_center: 'CC-SBY-01',
  total_subtotal: 0,
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

const triggerPrint = () => {
  window.print();
};

const fetchManifest = async () => {
  isLoading.value = true;
  try {
    const res = await api.get(`/emboss/${route.params.id}`);
    const f = res.data?.data || res.data;
    const total = f.totalRecords || 100;
    const valid = f.validRecords || total;
    const unitPrice = 15500;
    const subtotal = valid * unitPrice;

    prodOrder.value = {
      id: f.id,
      production_number: `PRD-2026-09-${String(f.id).padStart(4, '0')}`,
      emboss_file: f.filename || `EMB-${f.id}`,
      order_number: `ORD-2026-09-${String(f.id).padStart(4, '0')}`,
      total_planned_qty: total,
      total_issued_qty: valid,
      creator_name: f.uploadedByUser?.fullName || f.uploadedByUser?.username || 'Staff Produksi',
      issuer_name: 'Petugas Gudang Bahan'
    };

    manifestData.value = {
      manifest_number: `MNF-PRD-202609-${String(f.id).padStart(4, '0')}`,
      generated_at: formatDate(f.createdAt),
      warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
      dest_name: 'Kantor Cabang Surabaya Utama',
      dest_code: 'CBR-SBY-UT',
      dest_address: 'Jl. Basuki Rahmat No. 98-104, Surabaya',
      dest_city: 'Kota Surabaya',
      dest_phone: '(031) 5310090',
      cost_center: 'CC-SBY-01',
      total_subtotal: subtotal,
      items: [
        {
          sku: 'CRD-ATM-GPN',
          name: `Bahan Kartu ATM (${f.filename})`,
          category: 'Kartu ATM',
          uom: 'PCS',
          qty_planned: total,
          qty_issued: valid,
          subtotal: subtotal
        }
      ]
    };
  } catch (err) {
    console.error('Failed to load production manifest', err);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchManifest();
});
</script>

<style scoped>
@media print {
  .no-print {
    display: none !important;
  }
  .manifest-print-page {
    background: white !important;
    padding: 0 !important;
  }
  .document-box {
    border: none !important;
    padding: 0 !important;
    margin: 0 !important;
  }
}
</style>

<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 text-slate-900">
    <div class="print-container max-w-4xl mx-auto bg-white p-4 p-md-5 border border-slate-300 shadow-md rounded-sm">
      <!-- Top Toolbar (Hidden when printing) -->
      <div class="no-print mb-4 d-flex justify-content-between align-items-center bg-slate-50 p-3 rounded border border-slate-200">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/procurement/po" class="text-xs text-slate-600 font-semibold text-decoration-none d-inline-flex align-items-center">
            <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar PO
          </router-link>
          <span class="text-slate-300">|</span>
          <span class="text-xs text-slate-500 font-monospace">Surat Pesanan Resmi Bank Jatim</span>
        </div>
        <button @click="triggerPrint" class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1">
          <i class="bi bi-printer"></i>
          <span>Cetak Purchase Order (Print)</span>
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
          <h2 class="fs-6 fw-black text-dark mb-0">PURCHASE ORDER</h2>
          <p class="fs-7 font-monospace fw-bold text-danger mb-0">{{ po.po_number }}</p>
          <div class="fs-9 text-muted">Status: <span class="fw-semibold text-dark">{{ po.status }}</span></div>
        </div>
      </div>

      <!-- Meta Information Grid -->
      <div class="row g-3 fs-8 mb-4">
        <!-- Vendor Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Kepada Rekanan Vendor:
            </div>
            <div class="fw-bold fs-7 text-dark">{{ po.vendor_name }}</div>
            <div class="text-secondary font-monospace fs-9">Kode Vendor: {{ po.vendor_code }}</div>
            <div class="text-secondary">{{ po.vendor_address }}</div>
            <div class="text-secondary">Kontak / Telp: {{ po.vendor_phone }} (PIC: {{ po.vendor_pic }})</div>
          </div>
        </div>

        <!-- Delivery & Warehouse Information -->
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 border-bottom pb-1 mb-1">
              Tujuan Pengiriman & Gudang:
            </div>
            <div class="fw-bold fs-7 text-dark">{{ po.warehouse_name }}</div>
            <div class="text-secondary font-monospace fs-9">Kode Gudang: {{ po.warehouse_code }}</div>
            <div class="text-secondary">Kawasan Industri Margomulyo Blok C-12, Surabaya</div>
            <div class="row g-1 pt-1 border-top fs-9">
              <div class="col-6"><strong>Tgl. Order:</strong> {{ po.order_date }}</div>
              <div class="col-6"><strong>Target Kirim:</strong> {{ po.delivery_date }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Items Table -->
      <div class="mb-4">
        <table class="table table-bordered table-sm align-middle fs-8 mb-0">
          <thead class="table-light text-secondary font-bold uppercase">
            <tr>
              <th class="text-center" style="width: 40px;">No</th>
              <th>Deskripsi & Nama Barang</th>
              <th class="text-center" style="width: 80px;">Satuan</th>
              <th class="text-center" style="width: 90px;">Qty Order</th>
              <th class="text-end" style="width: 140px;">Harga Satuan</th>
              <th class="text-end" style="width: 150px;">Subtotal (Rp)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(it, idx) in po.items" :key="idx">
              <td class="text-center font-monospace text-secondary">{{ idx + 1 }}</td>
              <td>
                <strong class="text-dark">{{ it.name }}</strong>
                <div class="fs-9 text-muted font-monospace">{{ it.sku }} &bull; {{ it.category }}</div>
              </td>
              <td class="text-center font-monospace">{{ it.uom }}</td>
              <td class="text-center font-monospace fw-bold text-dark">{{ formatNumber(it.qty) }}</td>
              <td class="text-end font-monospace text-secondary">{{ formatRupiah(it.unit_price) }}</td>
              <td class="text-end font-monospace fw-bold text-dark">{{ formatRupiah(it.subtotal) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Summary & Notes Section -->
      <div class="row g-3 fs-8 mb-4">
        <div class="col-12 col-md-6">
          <div class="p-3 bg-light rounded border border-slate-200 space-y-1">
            <div class="fw-bold text-secondary text-uppercase fs-9 mb-1">Syarat & Ketentuan Pengadaan:</div>
            <ul class="ps-3 fs-9 text-secondary mb-0">
              <li>Barang harus sesuai dengan spesifikasi yang telah disepakati bersama Bank Jatim.</li>
              <li>Surat Jalan asli dan salinan PO ini wajib dilampirkan saat serah terima barang ke gudang tujuan.</li>
              <li>Faktur tagihan / invoice resmi diproses setelah Berita Acara Penerimaan Barang (GRN) diterbitkan.</li>
            </ul>
          </div>
        </div>

        <div class="col-12 col-md-6 space-y-2">
          <div class="d-flex justify-content-between px-3 py-1.5 bg-light rounded border fs-8">
            <span class="text-secondary">Subtotal Barang:</span>
            <span class="font-monospace fw-semibold text-dark">{{ formatRupiah(po.subtotal) }}</span>
          </div>
          <div class="d-flex justify-content-between px-3 py-1.5 bg-light rounded border fs-8">
            <span class="text-secondary">PPN (11%):</span>
            <span class="font-monospace fw-semibold text-dark">{{ formatRupiah(po.tax_amount) }}</span>
          </div>
          <div class="d-flex justify-content-between px-3 py-2 bg-danger bg-opacity-10 rounded border border-danger-subtle fs-7">
            <span class="text-danger fw-bold">TOTAL NILAI PO:</span>
            <span class="font-monospace fw-bold text-danger">{{ formatRupiah(po.total_amount) }}</span>
          </div>
        </div>
      </div>

      <!-- Signature Boxes (3 Columns) -->
      <div class="row text-center fs-8 pt-4 border-top">
        <div class="col-4">
          <p class="text-secondary mb-4">Dibuat Oleh (Procurement Officer)</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">{{ po.creator_name }}</p>
          <p class="fs-9 text-muted">Divisi Logistik & Umum</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-4">Disetujui Oleh (Approver)</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">{{ po.approver_name }}</p>
          <p class="fs-9 text-muted">Bank Jatim Kantor Pusat</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-4">Konfirmasi Rekanan Vendor</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">__________________________</p>
          <p class="fs-9 text-muted">Tanda Tangan & Cap Perusahaan</p>
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
const errorMessage = ref('');

const po = ref({
  po_number: '',
  status: '',
  order_date: '',
  delivery_date: '',
  vendor_name: '',
  vendor_code: '',
  vendor_address: '',
  vendor_phone: '',
  vendor_pic: '',
  warehouse_name: '',
  warehouse_code: '',
  creator_name: '',
  approver_name: '',
  subtotal: 0,
  tax_amount: 0,
  total_amount: 0,
  items: []
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);
const formatRupiah = (val) => 'Rp ' + formatNumber(val);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return val;
  }
};

const triggerPrint = () => {
  window.print();
};

const fetchPo = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get(`/procurement/po/${route.params.id}`);
    const data = res.data.data || res.data;
    po.value = {
      po_number: data.poNumber,
      status: data.status,
      order_date: formatDate(data.orderDate),
      delivery_date: formatDate(data.expectedDeliveryDate),
      vendor_name: data.vendor?.name || 'Rekanan Vendor Bank Jatim',
      vendor_code: data.vendor?.code || '-',
      vendor_address: 'Alamat rekanan terdaftar di pengadaan Bank Jatim',
      vendor_phone: '(031) Hub Logistik',
      vendor_pic: 'PIC Rekanan',
      warehouse_name: data.warehouse?.name || 'Gudang Pusat Bank Jatim',
      warehouse_code: data.warehouse?.code || '-',
      creator_name: data.createdByUser?.fullName || data.createdByUser?.username || 'Procurement Officer',
      approver_name: data.approvedByUser?.fullName || data.approvedByUser?.username || 'Pejabat Pemutus Pengadaan',
      subtotal: data.subtotal || 0,
      tax_amount: data.taxAmount || 0,
      total_amount: data.totalAmount || 0,
      items: (data.items || []).map(i => ({
        name: i.item?.name || 'Item',
        sku: i.item?.sku || '-',
        category: i.item?.categoryName || 'Umum',
        uom: i.item?.uom || 'PCS',
        qty: i.qtyOrdered || 0,
        unit_price: i.unitPrice || 0,
        subtotal: i.subtotal || 0
      }))
    };
  } catch (err) {
    console.error('Failed to fetch PO print data', err);
    errorMessage.value = 'Gagal memuat data Purchase Order untuk dicetak: ' + (err.response?.data?.message || err.message);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchPo();
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

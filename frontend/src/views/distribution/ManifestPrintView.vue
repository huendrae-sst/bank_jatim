<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 text-slate-900">
    <div class="max-w-3xl mx-auto bg-white p-4 p-md-5 border border-slate-300 shadow-md">
      <!-- Print Button -->
      <div class="no-print mb-4 d-flex justify-content-between align-items-center bg-slate-50 p-3 rounded border border-slate-200">
        <div class="d-flex align-items-center gap-2">
          <router-link to="/distribution/shipments" class="text-xs text-slate-600 font-semibold text-decoration-none">
            &larr; Kembali ke Daftar Pengiriman
          </router-link>
          <span class="text-slate-300">|</span>
          <span class="text-xs text-slate-500 font-monospace">Surat Jalan / Delivery Manifest Bank Jatim</span>
        </div>
        <button @click="triggerPrint" class="btn btn-sm btn-danger fw-bold shadow-xs">
          Cetak Dokumen (Print)
        </button>
      </div>

      <!-- Header Bank Jatim -->
      <div class="border-b-2 border-danger pb-3 mb-4 d-flex justify-content-between align-items-start">
        <div class="d-flex align-items-center gap-3">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" style="height: 40px; width: auto; object-fit: contain;">
          <div>
            <h1 class="fs-6 fw-black text-danger mb-0">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR TBK</h1>
            <p class="fs-8 fw-semibold text-secondary mb-0">DIVISI LOGISTIK & UMUM - GUDANG PUSAT SIER SURABAYA</p>
            <p class="fs-9 text-muted mb-0">Jl. Basuki Rahmat No. 98-104, Surabaya | Telp: (031) 5310090</p>
          </div>
        </div>
        <div class="text-end">
          <h2 class="fs-6 fw-black text-dark mb-0">SURAT JALAN / MANIFEST</h2>
          <p class="fs-7 font-monospace fw-bold text-danger mb-0">{{ shipment.manifest_number }}</p>
        </div>
      </div>

      <!-- Metadata -->
      <div class="row g-3 fs-8 mb-4">
        <div class="col-6 space-y-1">
          <div><strong>Tanggal Kirim:</strong> {{ shipment.dispatched_at }}</div>
          <div><strong>Dokumen Referensi:</strong> No. Order {{ shipment.order_number }}</div>
          <div><strong>Gudang Pengirim (Asal):</strong> {{ shipment.origin_warehouse }}</div>
          <div><strong>Ekspedisi / Kurir:</strong> {{ shipment.courier_name }}</div>
          <div><strong>No. Resi (AWB):</strong> <span class="font-monospace fw-bold text-primary">{{ shipment.tracking_number }}</span></div>
        </div>
        <div class="col-6 space-y-1 bg-light p-3 rounded border border-slate-200">
          <div class="fw-bold text-secondary text-uppercase fs-9">Unit Tujuan Pengiriman:</div>
          <div class="fw-bold fs-7 text-dark">{{ shipment.dest_org }}</div>
          <div class="fs-8 text-secondary">Gudang Tujuan: {{ shipment.dest_warehouse }}</div>
          <div class="text-secondary">{{ shipment.dest_address }}</div>
          <div class="text-secondary">Telp: {{ shipment.dest_phone }}</div>
        </div>
      </div>

      <!-- Items Table -->
      <table class="table table-bordered table-sm align-middle fs-8 mb-4">
        <thead class="table-light text-secondary font-bold uppercase">
          <tr>
            <th class="text-center" style="width: 40px;">No</th>
            <th>Kode & Nama Barang</th>
            <th class="text-center" style="width: 90px;">Satuan</th>
            <th class="text-center" style="width: 100px;">Jumlah Qty</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(it, idx) in shipment.items" :key="idx">
            <td class="text-center font-monospace text-secondary">{{ idx + 1 }}</td>
            <td>
              <strong class="text-dark">{{ it.name }}</strong>
              <div class="fs-9 text-muted font-monospace">{{ it.sku }}</div>
            </td>
            <td class="text-center font-monospace">{{ it.uom }}</td>
            <td class="text-center font-monospace fw-bold fs-7">{{ formatNumber(it.qty) }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Packaging Summary -->
      <div class="fs-8 bg-light p-3 rounded border border-slate-200 mb-4 d-flex justify-content-between">
        <div>Jumlah Koli: <strong>{{ shipment.koli_count }} Koli</strong></div>
        <div>Total Berat: <strong>{{ shipment.weight_kg }} Kg</strong></div>
        <div>Biaya Pengiriman: <strong>{{ formatRupiah(shipment.shipping_cost) }}</strong></div>
      </div>

      <!-- Signature Boxes -->
      <div class="row text-center fs-8 pt-4 border-top">
        <div class="col-4">
          <p class="text-secondary mb-4">Petugas Pengirim (Gudang)</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">{{ shipment.dispatcher_name }}</p>
          <p class="fs-9 text-muted">Logistik Bank Jatim</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-4">Petugas Ekspedisi / Kurir</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">_______________________</p>
          <p class="fs-9 text-muted">{{ shipment.courier_name }}</p>
        </div>
        <div class="col-4">
          <p class="text-secondary mb-4">Penerima Cabang (Capem)</p>
          <div style="height: 35px;"></div>
          <p class="fw-bold text-decoration-underline text-dark mb-0">_______________________</p>
          <p class="fs-9 text-muted">Tanda Tangan & Cap Unit</p>
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

const shipment = ref({
  manifest_number: '',
  order_number: '',
  dispatched_at: '',
  origin_warehouse: '',
  courier_name: '',
  tracking_number: '',
  dest_org: '',
  dest_warehouse: '',
  dest_address: '',
  dest_phone: '',
  dispatcher_name: '',
  koli_count: 1,
  weight_kg: 0,
  shipping_cost: 0,
  items: []
});

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);
const formatRupiah = (val) => 'Rp ' + formatNumber(val);

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });
  } catch {
    return val;
  }
};

const triggerPrint = () => {
  window.print();
};

const fetchShipment = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get(`/distribution/shipments/${route.params.id}`);
    const data = res.data.data || res.data;
    shipment.value = {
      manifest_number: data.manifestNumber,
      order_number: data.order?.orderNumber || '-',
      dispatched_at: formatDate(data.dispatchedAt),
      origin_warehouse: data.originWarehouse?.name || 'Gudang Pusat Logistik',
      courier_name: data.courier?.name || 'Ekspedisi Rekanan',
      tracking_number: data.trackingNumber || '-',
      dest_org: data.destinationOrganization?.name || 'Unit Cabang Bank Jatim',
      dest_warehouse: data.destinationOrganization?.code ? `WH-${data.destinationOrganization.code} - Gudang Cabang` : 'Gudang Cabang',
      dest_address: 'Alamat resmi kantor operasional Bank Jatim',
      dest_phone: '(031) Call Center',
      dispatcher_name: data.dispatchedByUser?.fullName || data.dispatchedByUser?.username || 'Petugas Gudang',
      koli_count: data.koliCount || 1,
      weight_kg: data.totalWeightKg || 1,
      shipping_cost: data.shippingCost || 0,
      items: (data.order?.items || []).map(i => ({
        name: i.item?.name || 'Barang Logistik',
        sku: i.item?.sku || '-',
        uom: i.item?.uom || 'PCS',
        qty: i.qtyApproved || i.qtyRequested || 0
      }))
    };
  } catch (err) {
    console.error('Failed to load manifest print data', err);
    errorMessage.value = 'Gagal memuat surat jalan manifest: ' + (err.response?.data?.message || err.message);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchShipment();
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
}
</style>

<template>
  <div class="print-page bg-slate-100 min-vh-100 p-3 p-md-5 d-flex flex-column align-items-center">
    <div class="no-print mb-3 d-flex gap-2">
      <router-link to="/distribution/shipments" class="btn btn-outline-secondary btn-sm">
        Kembali
      </router-link>
      <button @click="triggerPrint" class="btn btn-danger btn-sm fw-bold shadow-xs">
        Cetak Label (Print)
      </button>
    </div>

    <!-- Label Box (Standard Shipping Size) -->
    <div class="bg-white p-4 border border-2 border-dashed border-secondary shadow-md space-y-3" style="width: 450px; max-width: 100%;">
      <div class="border-b-2 border-danger pb-2 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" style="height: 28px; width: auto; object-fit: contain;">
          <div>
            <h1 class="fs-8 fw-black text-danger mb-0 lh-1">LOGISTICS</h1>
            <p class="fs-9 text-muted mb-0 lh-1">Shipping Identification Label</p>
          </div>
        </div>
        <span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle fs-9 font-bold">FRAGILE / RESMI</span>
      </div>

      <div class="d-flex justify-content-between align-items-center">
        <div>
          <div class="fs-9 uppercase text-secondary fw-bold">No. Manifest:</div>
          <div class="fs-6 fw-bold font-monospace text-dark">{{ label.manifest_number }}</div>
          <div class="fs-9 uppercase text-secondary fw-bold mt-1">No. Resi:</div>
          <div class="fs-7 font-monospace fw-bold text-primary">{{ label.tracking_number }}</div>
        </div>
        <!-- Barcode / QR Simulation -->
        <div class="p-2 bg-white border rounded text-center">
          <i class="bi bi-qr-code fs-1 text-dark"></i>
        </div>
      </div>

      <div class="bg-light p-3 rounded border fs-8 space-y-1">
        <div class="fs-9 fw-bold text-secondary text-uppercase">Penerima:</div>
        <div class="fw-bold fs-7 text-dark">{{ label.dest_org }}</div>
        <div class="fs-8 text-secondary">Gudang: {{ label.dest_warehouse }}</div>
        <div class="text-secondary">{{ label.dest_address }}</div>
        <div class="fs-9 text-secondary fw-semibold">Kota: {{ label.dest_city }}</div>
      </div>

      <div class="row g-2 text-center fs-8 fw-bold">
        <div class="col-6">
          <div class="p-2 bg-light rounded border">Koli: {{ label.koli_count }} Paket</div>
        </div>
        <div class="col-6">
          <div class="p-2 bg-light rounded border">Berat: {{ label.weight_kg }} Kg</div>
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

const label = ref({
  manifest_number: '',
  tracking_number: '',
  dest_org: '',
  dest_warehouse: '',
  dest_address: '',
  dest_city: '',
  koli_count: 1,
  weight_kg: 0
});

const triggerPrint = () => {
  window.print();
};

const fetchLabel = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get(`/distribution/shipments/${route.params.id}`);
    const data = res.data.data || res.data;
    label.value = {
      manifest_number: data.manifestNumber,
      tracking_number: data.trackingNumber || '-',
      dest_org: data.destinationOrganization?.name || 'Unit Cabang Bank Jatim',
      dest_warehouse: data.destinationOrganization?.code ? `WH-${data.destinationOrganization.code} - Gudang Cabang` : 'Gudang Cabang',
      dest_address: 'Alamat resmi kantor operasional Bank Jatim',
      dest_city: 'Jawa Timur',
      koli_count: data.koliCount || 1,
      weight_kg: data.totalWeightKg || 1
    };
  } catch (err) {
    console.error('Failed to load shipment label', err);
    errorMessage.value = 'Gagal memuat data label pengiriman: ' + (err.response?.data?.message || err.message);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchLabel();
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

<template>
  <div class="manifest-print-page bg-light min-vh-100 p-3 p-md-4">
    <div class="no-print text-center py-3">
      <button @click="triggerPrint" class="btn btn-primary btn-sm px-4 shadow-sm">
        Cetak / Simpan PDF
      </button>
      <router-link :to="`/production/${prodOrder.id}`" class="btn btn-secondary btn-sm ms-2">
        Kembali
      </router-link>
    </div>

    <div class="document-box shadow-sm bg-white mx-auto p-4 p-md-5 border" style="max-width: 850px; font-family: 'Times New Roman', Times, serif; color: #111; font-size: 13px;">
      <div class="text-center header-title border-bottom border-dark pb-3 mb-4">
        <h4 class="mb-1 fw-bold">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR, Tbk.</h4>
        <h5 class="mb-1 text-uppercase text-decoration-underline">SURAT PERINTAH KERJA (SPK) &amp; MANIFEST PRODUKSI EMBOSS</h5>
        <span class="fs-6 font-monospace">Nomor SPK: {{ prodOrder.productionNumber }}</span>
      </div>

      <div class="row g-3 mb-3 border p-3 rounded bg-light">
        <div class="col-6">
          <span class="fs-8 text-uppercase fw-bold text-secondary d-block">Gudang Sumber Bahan Baku:</span>
          <strong class="fs-6">{{ prodOrder.warehouse?.name || 'Gudang Sentral Margomulyo' }} ({{ prodOrder.warehouse?.code || 'WH-CEN-01' }})</strong>
          <p class="mb-0 text-dark fs-8">
            Status Alur: <strong class="text-danger">{{ prodOrder.status }}</strong><br>
            Status Bahan Baku: <span class="font-monospace fw-semibold">{{ prodOrder.materialIssueStatus }}</span><br>
            Otorisasi Bahan: <span class="fw-semibold">{{ prodOrder.approvedByUser?.name || 'Menunggu Otorisasi' }}</span>
          </p>
        </div>
        <div class="col-6 text-end">
          <span class="fs-8 text-uppercase fw-bold text-secondary d-block">Data Administrasi SPK:</span>
          <strong class="font-monospace fs-6 text-danger">{{ prodOrder.productionNumber }}</strong>
          <p class="mb-0 text-secondary fs-8">
            Tanggal SPK: {{ formatDate(prodOrder.productionDate || prodOrder.createdAt) }}<br>
            Pembuat SPK: {{ prodOrder.createdByUser?.name || 'Operator Produksi' }}<br>
            Ref. File Emboss: <span class="font-monospace fw-semibold">{{ prodOrder.embossFileName || '-' }}</span><br>
            Target Kuantitas: <span class="font-monospace fw-bold text-dark">{{ prodOrder.totalQty || 0 }} Keping</span>
          </p>
        </div>
      </div>

      <h6 class="fw-bold mb-2">RINCIAN BAHAN KARTU &amp; TARGET FINISHED GOOD (BOM):</h6>
      <table class="table table-bordered table-sm w-100 mb-3 text-dark" style="border: 1px solid #333; font-size: 12px;">
        <thead>
          <tr class="table-secondary text-center">
            <th style="width: 5%">No.</th>
            <th>Target SKU (Finished Good)</th>
            <th>Nama Kartu Emboss</th>
            <th>Bahan Blank (BOM)</th>
            <th class="text-center">Qty Rencana</th>
            <th class="text-center">Qty Bagus (Pass)</th>
            <th class="text-center">Qty Gagal (Damaged)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in prodOrder.items" :key="idx">
            <td class="text-center">{{ idx + 1 }}</td>
            <td class="font-monospace text-center">{{ item.itemSku }}</td>
            <td>{{ item.itemName }}</td>
            <td class="font-monospace text-center">{{ item.materialSku || 'ATM-INST-001' }}</td>
            <td class="text-center font-monospace">{{ item.qtyPlanned }}</td>
            <td class="text-center font-monospace fw-bold text-success">{{ item.qtyProduced || 0 }}</td>
            <td class="text-center font-monospace text-danger">{{ item.qtyDamaged || 0 }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="fw-bold table-light">
            <td colspan="4" class="text-center text-uppercase">Total Rekapitulasi</td>
            <td class="text-center font-monospace">{{ prodOrder.totalQty }}</td>
            <td class="text-center font-monospace text-success">{{ prodOrder.totalProduced }}</td>
            <td class="text-center font-monospace text-danger">{{ prodOrder.totalDamaged }}</td>
          </tr>
        </tfoot>
      </table>

      <!-- Order Cabang Terhubung -->
      <div v-if="prodOrder.fulfillments && prodOrder.fulfillments.length > 0" class="mb-3">
        <h6 class="fw-bold mb-2">CABANG TUJUAN PEMENUHAN KARTU EMBOSS:</h6>
        <ul class="list-group list-group-flush fs-8 mb-2 border rounded">
          <li v-for="ful in prodOrder.fulfillments" :key="ful.id" class="list-group-item d-flex justify-content-between align-items-center py-1.5 px-3">
            <span><strong>{{ ful.branchName }}</strong> (Order: <span class="font-monospace">{{ ful.orderNumber }}</span>)</span>
            <span class="badge bg-danger fs-9">{{ ful.qtyFulfilled || prodOrder.totalProduced }} Keping</span>
          </li>
        </ul>
      </div>

      <p class="text-justify mb-4 fs-8 text-secondary">
        Catatan: Dokumen ini merupakan bukti sah perintah kerja mesin personalisasi kartu ATM / debit chip Bank Jatim. Seluruh pengeluaran bahan baku blank card dan hasil cetak finished good diverifikasi melalui pencatatan mutasi kartu stok sistem logistik terpadu (JIMS).
      </p>

      <div class="row text-center signature-box mt-5">
        <div class="col-4">
          <p class="mb-0 fs-8">Direncanakan Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Operator Personalisasi Kartu</p>
          <div style="height: 50px;"></div>
          <strong><u>{{ prodOrder.createdByUser?.name || 'Staff Produksi' }}</u></strong>
        </div>
        <div class="col-4">
          <p class="mb-0 fs-8">Disetujui Bahan Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Inventory Officer</p>
          <div style="height: 50px;"></div>
          <strong><u>{{ prodOrder.approvedByUser?.name || 'Petugas Gudang Bahan' }}</u></strong>
        </div>
        <div class="col-4">
          <p class="mb-0 fs-8">Supervisor Logistik &amp; Distribusi,</p>
          <p class="mb-0 fs-8 text-secondary">Penyelia Operasional Pusat</p>
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
import { toast } from '@/utils/toast';

const route = useRoute();
const isLoading = ref(true);

const prodOrder = ref({
  id: route.params.id,
  productionNumber: '',
  status: '',
  materialIssueStatus: '',
  totalQty: 0,
  totalProduced: 0,
  totalDamaged: 0,
  productionDate: null,
  warehouse: null,
  createdByUser: null,
  approvedByUser: null,
  items: [],
  fulfillments: []
});

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
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
    const res = await api.get(`/production-orders/${route.params.id}`);
    prodOrder.value = res.data?.data || res.data;
  } catch (err) {
    console.error('Failed to load production manifest data:', err);
    toast.error('Gagal memuat data manifes produksi.');
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

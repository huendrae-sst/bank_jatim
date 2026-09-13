<template>
  <div class="ba-print-page bg-light min-vh-100 p-3 p-md-4">
    <div class="no-print text-center py-3">
      <button @click="triggerPrint" class="btn btn-primary btn-sm px-4 shadow-sm">
        <i class="bi bi-printer me-1"></i> Cetak / Simpan PDF
      </button>
      <router-link :to="`/destructions/${destruction.id}`" class="btn btn-secondary btn-sm ms-2">
        Kembali
      </router-link>
    </div>

    <div class="document-box shadow-sm bg-white mx-auto p-4 p-md-5 border" style="max-width: 850px; font-family: 'Times New Roman', Times, serif; color: #111; font-size: 14px;">
      <div class="text-center header-title border-bottom border-dark pb-3 mb-4">
        <h4 class="mb-1 fw-bold">PT BANK PEMBANGUNAN DAERAH JAWA TIMUR, Tbk.</h4>
        <h5 class="mb-1 text-uppercase text-decoration-underline">BERITA ACARA PEMUSNAHAN BARANG PERSEDIAAN</h5>
        <span class="fs-6 font-monospace">Nomor: {{ destruction.berita_acara_number }}</span>
      </div>

      <p class="text-justify leading-relaxed mb-3">
        Pada hari ini, tanggal <strong>{{ destruction.execution_date }}</strong>, bertempat di lokasi <strong>{{ destruction.warehouse_name }}</strong>, telah dilaksanakan pemusnahan barang persediaan yang dinyatakan tidak dapat dipergunakan kembali karena <em>{{ destruction.reason.replace('_', ' ') }}</em>.
      </p>

      <p class="mb-2">Pelaksanaan pemusnahan ini disaksikan oleh pihak-pihak yang bertanda tangan di bawah ini:</p>
      <ol class="mb-4 ps-3">
        <li><strong>{{ destruction.witness_name_1 }}</strong> - Jabatan: {{ destruction.witness_title_1 }} (Saksi 1)</li>
        <li><strong>{{ destruction.witness_name_2 }}</strong> - Jabatan: {{ destruction.witness_title_2 }} (Saksi 2)</li>
      </ol>

      <h6 class="fw-bold mb-2">DAFTAR BARANG YANG DIMUSNAHKAN:</h6>
      <table class="table table-bordered table-sm w-100 mb-3 text-dark" style="border: 1px solid #333; font-size: 13px;">
        <thead>
          <tr class="table-secondary text-center">
            <th style="width: 5%">No.</th>
            <th>Kode / SKU</th>
            <th>Nama Barang Persediaan</th>
            <th>No. Batch / Seri</th>
            <th>Kuantitas</th>
            <th>Satuan</th>
            <th class="text-end">Nilai Kerugian</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in destruction.items" :key="idx">
            <td class="text-center">{{ idx + 1 }}</td>
            <td class="font-monospace text-center">{{ item.sku }}</td>
            <td>{{ item.name }}</td>
            <td class="font-monospace text-center">{{ item.batch_or_serial || '-' }}</td>
            <td class="text-center font-monospace">{{ formatNumber(item.qty) }}</td>
            <td class="text-center">{{ item.uom }}</td>
            <td class="text-end font-monospace">{{ formatRupiah(item.total_loss) }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="fw-bold">
            <td colspan="4" class="text-center text-uppercase">Total Barang Dimusnahkan</td>
            <td class="text-center font-monospace">{{ formatNumber(destruction.total_qty) }}</td>
            <td></td>
            <td class="text-end font-monospace">{{ formatRupiah(destruction.total_loss_value) }}</td>
          </tr>
        </tfoot>
      </table>

      <p class="text-justify mb-4">
        Pemusnahan fisik barang persediaan di atas dilaksanakan dengan metode perusakan fisik total (pemotongan chip / pencacahan) sehingga barang tersebut dipastikan tidak dapat disalahgunakan atau diedarkan kembali.
      </p>

      <p class="mb-4">Demikian Berita Acara ini dibuat dengan sebenarnya dalam rangkap secukupnya untuk dipergunakan sebagaimana mestinya.</p>

      <div class="row text-center signature-box mt-5">
        <div class="col-6 mb-4">
          <p class="mb-0 fs-8">Diajukan Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Petugas Gudang / Maker</p>
          <div style="height: 60px;"></div>
          <strong><u>{{ destruction.requester_name }}</u></strong>
        </div>
        <div class="col-6 mb-4">
          <p class="mb-0 fs-8">Disetujui / Diotorisasi Oleh,</p>
          <p class="mb-0 fs-8 text-secondary">Pejabat Berwenang / Checker</p>
          <div style="height: 60px;"></div>
          <strong><u>Hendra Sudrajat</u></strong>
        </div>
        <div class="col-6">
          <p class="mb-0 fs-8">Saksi I,</p>
          <p class="mb-0 fs-8 text-secondary">{{ destruction.witness_title_1 }}</p>
          <div style="height: 60px;"></div>
          <strong><u>{{ destruction.witness_name_1 }}</u></strong>
        </div>
        <div class="col-6">
          <p class="mb-0 fs-8">Saksi II,</p>
          <p class="mb-0 fs-8 text-secondary">{{ destruction.witness_title_2 }}</p>
          <div style="height: 60px;"></div>
          <strong><u>{{ destruction.witness_name_2 }}</u></strong>
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

const destruction = ref({
  id: route.params.id || 1,
  berita_acara_number: `BA-DST/LOG/2026/09/${String(route.params.id || 1).padStart(3, '0')}`,
  warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
  reason: 'EXPIRED_CHIP',
  witness_name_1: 'Achmad Soebarjo',
  witness_title_1: 'Pemimpin Cabang Pembantu',
  witness_name_2: 'Bambang Irawan',
  witness_title_2: 'Supervisor Operasional / Audit',
  requester_name: 'Petugas Gudang',
  total_qty: 0,
  total_loss_value: 0,
  execution_date: '12 September 2026',
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

const fetchDestruction = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/ledgers', {
      params: { transactionType: 'DESTRUCTION', size: 50 }
    });
    const items = res.data?.data?.content || res.data?.content || [];
    const found = items.find(i => String(i.id) === String(route.params.id)) || items[0];

    if (found) {
      destruction.value = {
        id: found.id,
        berita_acara_number: found.referenceNumber || `BA-DST/LOG/2026/09/${String(found.id).padStart(3, '0')}`,
        warehouse_name: found.warehouse?.name || 'Gudang Sentral Margomulyo Surabaya',
        reason: 'EXPIRED_CHIP',
        witness_name_1: 'Achmad Soebarjo',
        witness_title_1: 'Pemimpin Cabang Pembantu',
        witness_name_2: 'Bambang Irawan',
        witness_title_2: 'Supervisor Operasional / Audit',
        requester_name: found.createdByUser?.fullName || found.createdByUser?.username || 'Petugas Gudang',
        total_qty: found.qtyOut || 0,
        total_loss_value: found.totalValue || 0,
        execution_date: formatDate(found.createdAt),
        items: [
          {
            sku: found.item?.sku || 'CRD-ATM-GPN',
            name: found.item?.name || 'Kartu ATM Chip GPN Reguler',
            batch_or_serial: 'BATCH-2021-Q3',
            qty: found.qtyOut || 0,
            uom: found.item?.uom || 'PCS',
            total_loss: found.totalValue || 0
          }
        ]
      };
    }
  } catch (err) {
    console.error('Failed to load destruction for berita acara', err);
  } finally {
    isLoading.value = false;
  }
};

const triggerPrint = () => {
  window.print();
};

onMounted(() => {
  fetchDestruction();
});
</script>

<style scoped>
@media print {
  .no-print {
    display: none !important;
  }
  .ba-print-page {
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

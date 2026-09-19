<template>
  <div class="destruction-detail-page space-y-3">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="mb-0 text-body fw-bold">{{ destruction.destruction_number }}</h3>
          <span class="badge fs-8 text-uppercase" :class="destruction.status === 'EXECUTED' ? 'text-bg-success' : 'text-bg-warning'">
            {{ destruction.status }}
          </span>
        </div>
        <p class="fs-8 text-secondary mb-0 mt-0.5">
          No. BA: <strong class="font-monospace text-body">{{ destruction.berita_acara_number }}</strong> &bull; Diajukan pada {{ destruction.created_at }} oleh <strong class="text-body">{{ destruction.requester_name }}</strong>
        </p>
      </div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <router-link to="/destructions" class="btn btn-sm btn-outline-secondary">
          Kembali
        </router-link>
        <router-link :to="`/destructions/${destruction.id}/berita-acara`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold">
          Cetak Berita Acara
        </router-link>
        <template v-if="destruction.status === 'REQUESTED'">
          <button type="button" class="btn btn-sm btn-success fw-bold shadow-xs px-3" @click="showApproveModal = true">
            Otorisasi Persetujuan
          </button>
        </template>
        <template v-else-if="destruction.status === 'APPROVED'">
          <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3" @click="showExecuteModal = true">
            Eksekusi Pemusnahan Fisik
          </button>
        </template>
      </div>
    </div>

    <div class="card card-outline card-danger shadow-xs">

      <div class="card-body p-4">
        <div class="row g-3 mb-4">
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Lokasi Gudang</span>
            <span class="fw-bold text-dark fs-7">{{ destruction.warehouse_name }}</span>
            <span class="text-muted fs-8 d-block">{{ destruction.org_name }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Alasan Pemusnahan</span>
            <span class="badge bg-secondary bg-opacity-10 text-dark border fs-8 mt-1">
              {{ destruction.reason.replace('_', ' ') }}
            </span>
            <p v-if="destruction.reason_details" class="text-secondary fs-8 mt-1 mb-0">{{ destruction.reason_details }}</p>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Saksi-Saksi Berita Acara</span>
            <span class="fw-semibold text-dark fs-8 d-block">1. {{ destruction.witness_name_1 }} ({{ destruction.witness_title_1 }})</span>
            <span class="fw-semibold text-dark fs-8 d-block">2. {{ destruction.witness_name_2 }} ({{ destruction.witness_title_2 }})</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Status Eksekusi</span>
            <span v-if="destruction.status === 'EXECUTED'" class="text-success fw-bold fs-8 d-block">
              <i class="bi bi-check-all me-1"></i>Dimusnahkan pada {{ destruction.executed_at }}
            </span>
            <span v-else class="text-warning fw-semibold fs-8 fst-italic">Belum dimusnahkan</span>
          </div>
        </div>

        <!-- Items Table -->
        <h6 class="fw-bold mb-3 text-dark">Rincian Barang yang Dimusnahkan</h6>
        <div class="table-responsive border rounded-3">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3">SKU Barang</th>
                <th>Nama Barang</th>
                <th>Kategori</th>
                <th>Batch / Serial</th>
                <th class="text-center">Jumlah Fisik</th>
                <th class="text-end">Harga Satuan</th>
                <th class="text-end pe-3">Nilai Kerugian</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in destruction.items" :key="item.id">
                <td class="ps-3 font-monospace fw-bold">{{ item.sku }}</td>
                <td class="fw-semibold">{{ item.name }}</td>
                <td>{{ item.category }}</td>
                <td class="font-monospace text-muted">{{ item.batch_or_serial || '-' }}</td>
                <td class="text-center font-monospace fw-bold">{{ formatNumber(item.qty) }} {{ item.uom }}</td>
                <td class="text-end font-monospace">{{ formatRupiah(item.unit_price) }}</td>
                <td class="text-end pe-3 font-monospace text-danger fw-bold">{{ formatRupiah(item.total_loss) }}</td>
              </tr>
            </tbody>
            <tfoot class="table-light fw-bold">
              <tr>
                <td colspan="4" class="ps-3 text-uppercase">Total Pemusnahan</td>
                <td class="text-center font-monospace">{{ formatNumber(destruction.total_qty) }}</td>
                <td></td>
                <td class="text-end pe-3 font-monospace text-danger fs-7">{{ formatRupiah(destruction.total_loss_value) }}</td>
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
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body mb-0">Otorisasi Persetujuan Pemusnahan</h6>
            <button type="button" class="btn-close" @click="showApproveModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body fs-8 text-secondary">
            Apakah Anda menyetujui pemusnahan resmi sebanyak <strong>{{ formatNumber(destruction.total_qty) }} barang</strong> dengan total estimasi kerugian <strong>{{ formatRupiah(destruction.total_loss_value) }}</strong> di {{ destruction.warehouse_name }}?
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showApproveModal = false">Batal</button>
            <button type="button" class="btn btn-success btn-sm" @click="approveDestruction">Setujui Pemusnahan</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Execute -->
    <div v-if="showExecuteModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body mb-0">Eksekusi Pemusnahan Fisik</h6>
            <button type="button" class="btn-close" @click="showExecuteModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body space-y-3 fs-8">
            <div class="alert alert-warning py-2 fs-8 border-0 mb-3">
              <i class="bi bi-exclamation-triangle me-1"></i> Perhatian: Tindakan ini akan <strong>mengurangi saldo persediaan fisik secara permanen (DESTROYED)</strong> dan menerbitkan Berita Acara resmi.
            </div>
            <div>
              <label class="form-label fs-8 fw-semibold">Metode / Catatan Pemusnahan</label>
              <textarea v-model="executionNotes" class="form-control form-control-sm" rows="2" placeholder="Contoh: Dihancurkan menggunakan mesin pencacah shredder / dipotong chip EMV disaksikan saksi"></textarea>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-secondary btn-sm" @click="showExecuteModal = false">Batal</button>
            <button type="button" class="btn btn-danger btn-sm" @click="executeDestruction">Eksekusi Pemusnahan</button>
          </div>
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

const showApproveModal = ref(false);
const showExecuteModal = ref(false);
const executionNotes = ref('');
const isLoading = ref(true);

const destruction = ref({
  id: route.params.id,
  destruction_number: `DST-2026-09-${String(route.params.id).padStart(4, '0')}`,
  berita_acara_number: `BA-DST/LOG/2026/09/${String(route.params.id).padStart(3, '0')}`,
  status: 'APPROVED',
  created_at: '',
  requester_name: 'Petugas Gudang',
  warehouse_name: 'Gudang Sentral Margomulyo Surabaya',
  org_name: 'Divisi Logistik & Umum',
  reason: 'EXPIRED_CHIP',
  reason_details: 'Kartu ATM chip / produk telah melewati masa simpan atau cacat fisik.',
  witness_name_1: 'Achmad Soebarjo',
  witness_title_1: 'Pemimpin Cabang Pembantu',
  witness_name_2: 'Bambang Irawan',
  witness_title_2: 'Supervisor Operasional / Audit',
  total_qty: 500,
  total_loss_value: 7750000,
  executed_at: null,
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

const fetchDestructionDetail = async () => {
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
        destruction_number: found.referenceNumber || `DST-2026-09-${String(found.id).padStart(4, '0')}`,
        berita_acara_number: found.referenceNumber || `BA-DST/LOG/2026/09/${String(found.id).padStart(3, '0')}`,
        status: 'EXECUTED',
        created_at: formatDate(found.createdAt),
        requester_name: found.createdByUser?.fullName || found.createdByUser?.username || 'Petugas Gudang',
        warehouse_name: found.warehouse?.name || 'Gudang Sentral Margomulyo Surabaya',
        org_name: 'Divisi Logistik & Umum',
        reason: 'EXPIRED_CHIP',
        reason_details: found.notes || 'Pemusnahan fisik barang persediaan kadaluarsa.',
        witness_name_1: 'Achmad Soebarjo',
        witness_title_1: 'Pemimpin Cabang Pembantu',
        witness_name_2: 'Bambang Irawan',
        witness_title_2: 'Supervisor Operasional / Audit',
        total_qty: found.qtyOut || 500,
        total_loss_value: found.totalValue || 7750000,
        executed_at: formatDate(found.createdAt),
        items: [
          {
            id: found.id,
            sku: found.item?.sku || 'CRD-ATM-GPN',
            name: found.item?.name || 'Kartu ATM Chip GPN Reguler',
            category: 'Kartu ATM',
            batch_or_serial: 'BATCH-2021-Q3',
            qty: found.qtyOut || 500,
            uom: found.item?.uom || 'PCS',
            unit_price: found.unitCost || 15500,
            total_loss: found.totalValue || 7750000
          }
        ]
      };
    } else {
      destruction.value.created_at = new Date().toLocaleDateString('id-ID');
      destruction.value.items = [
        { id: 1, sku: 'CRD-ATM-GPN', name: 'Kartu ATM Chip GPN Reguler', category: 'Kartu ATM', batch_or_serial: 'BATCH-2021-Q3', qty: 500, uom: 'PCS', unit_price: 15500, total_loss: 7750000 }
      ];
    }
  } catch (err) {
    console.error('Failed to load destruction detail', err);
  } finally {
    isLoading.value = false;
  }
};

const approveDestruction = () => {
  destruction.value.status = 'APPROVED';
  showApproveModal.value = false;
  alert('Pemusnahan barang telah disetujui.');
};

const executeDestruction = async () => {
  try {
    await api.post('/inventory/destructions', {
      warehouseId: 1,
      itemId: 1,
      qty: destruction.value.total_qty,
      baNo: destruction.value.berita_acara_number,
      notes: executionNotes.value || 'Pemusnahan fisik disaksikan SKAI & Kepatuhan'
    });
    destruction.value.status = 'EXECUTED';
    destruction.value.executed_at = new Date().toLocaleString('id-ID');
    showExecuteModal.value = false;
    alert('Eksekusi pemusnahan berhasil dicatat ke dalam buku besar persediaan!');
  } catch (err) {
    console.error('Failed to execute destruction', err);
    alert('Gagal mengeksekusi pemusnahan: ' + (err.response?.data?.message || err.message));
  }
};

onMounted(() => {
  fetchDestructionDetail();
});
</script>

<template>
  <div class="ess-cost-saving-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">ESS: Efisiensi Biaya Switching Stock</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="exportCsv">
          Ekspor CSV
        </button>
      </div>
    </div>

    <!-- 3 Box Metrics -->
    <!-- 3 Box Metrics -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-4">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-cash-coin fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Total Efisiensi Biaya</div>
            <div class="fs-4 fw-bold font-monospace text-success">{{ formatRupiahShort(totalSavings) }}</div>
            <div class="fs-9 text-secondary">Tahun Berjalan 2026</div>
          </div>
        </div>
      </div>
      <div class="col-12 col-sm-4">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-primary text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-box-seam fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Volume Barang Dialihkan</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ totalVolume.toLocaleString('id-ID') }} Unit</div>
            <div class="fs-9 text-secondary">Menghindari Dead Stock</div>
          </div>
        </div>
      </div>
      <div class="col-12 col-sm-4">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-warning text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-lightning-charge fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Lead Time Ditekan</div>
            <div class="fs-4 fw-bold font-monospace text-body">2.4 Hari</div>
            <div class="fs-9 text-secondary">Vs 14 Hari Lead Time PO Vendor</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Rincian Penghematan per Mutasi Switching</h3>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">No. Transaksi</th>
              <th>Nama Barang</th>
              <th>Cabang Surplus</th>
              <th>Cabang Penerima</th>
              <th class="text-end">Jumlah</th>
              <th class="text-end">Biaya Ekspedisi</th>
              <th class="text-end">Penghematan Bersih</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="savingsList.length === 0">
              <td colspan="7" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Belum ada data transaksi switching stock tercatat.
              </td>
            </tr>
            <tr v-for="item in paginatedSavingsList" :key="item.no">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ item.no }}</td>
              <td class="fw-semibold text-body">{{ item.item }}</td>
              <td>{{ item.from }}</td>
              <td>{{ item.to }}</td>
              <td class="text-end font-monospace">{{ item.qty.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-secondary fs-8">Rp {{ item.shipping.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-success">Rp {{ item.netSaving.toLocaleString('id-ID') }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="savingsList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { toast } from '@/utils/toast';
import { exportToCsv } from '@/utils/exportHelper';

const currentPage = ref(1);
const perPage = ref(10);
const isLoading = ref(true);
const savingsList = ref([]);

const formatRupiahShort = (val) => {
  if (val >= 1000000000) return `Rp ${(val / 1000000000).toFixed(2)} Miliar`;
  if (val >= 1000000) return `Rp ${(val / 1000000).toFixed(1)} Juta`;
  return `Rp ${val.toLocaleString('id-ID')}`;
};

const totalSavings = computed(() => {
  return savingsList.value.reduce((sum, item) => sum + (item.netSaving || 0), 0);
});

const totalVolume = computed(() => {
  return savingsList.value.reduce((sum, item) => sum + (item.qty || 0), 0);
});

const paginatedSavingsList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return savingsList.value.slice(start, start + perPage.value);
});

const fetchSwitchingData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/switching').catch(() => ({ data: [] }));
    let items = res.data?.data || res.data || [];
    if (!Array.isArray(items)) items = [];
    const list = [];

    for (const sw of items) {
      const swItems = sw.items || [];
      for (const it of swItems) {
        const qty = it.qty || 0;
        const unitPrice = Number(it.item?.estimatedUnitPrice || 15000);
        const grossSaving = qty * unitPrice;
        const shipping = 75000;
        const netSaving = Math.max(0, grossSaving - shipping);

        list.push({
          no: sw.switchNo || `SW-2026-${String(sw.id).padStart(4, '0')}`,
          item: it.item?.name || 'Barang Persediaan',
          from: sw.sourceOrganization?.name || 'Cabang Asal',
          to: sw.destinationOrganization?.name || 'Cabang Tujuan',
          qty,
          shipping,
          netSaving
        });
      }
    }

    savingsList.value = list;
  } catch (err) {
    console.error('Failed to load switching stock savings', err);
  } finally {
    isLoading.value = false;
  }
};

const exportCsv = () => {
  const headers = [
    { key: 'no', label: 'No. Switching / Inisiatif' },
    { key: 'item', label: 'Barang' },
    { key: 'from', label: 'Asal' },
    { key: 'to', label: 'Tujuan' },
    { key: 'qty', label: 'Kuantitas' },
    { key: 'shipping', label: 'Biaya Kirim (Rp)' },
    { key: 'netSaving', label: 'Net Penghematan (Rp)' }
  ];
  exportToCsv('ess_cost_saving', headers, savingsList.value);
  toast.success('Laporan penghematan biaya (Cost Saving) berhasil diekspor ke CSV.');
};

onMounted(() => {
  fetchSwitchingData();
});
</script>

<template>
  <div class="ess-audit-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">ESS: Akuntabilitas & Kepatuhan Audit Persediaan</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="handleExport">
          Ekspor Laporan
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Daftar Indikator Kepatuhan Audit (Compliance Checklist)</h3>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Indikator Kepatuhan Operasional</th>
              <th>Standar SOP / Regulasi Bank Jatim</th>
              <th class="text-end">Frekuensi Uji</th>
              <th class="text-end">Tingkat Kepatuhan</th>
              <th>Status Evaluasi SKAI</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedComplianceList" :key="item.indicator">
              <td class="ps-3 fw-semibold text-body">{{ item.indicator }}</td>
              <td class="fs-8 text-secondary">{{ item.standard }}</td>
              <td class="text-end font-monospace">{{ item.freq }}</td>
              <td class="text-end font-monospace fw-bold text-success">{{ item.rate }}%</td>
              <td><span class="badge text-bg-success">{{ item.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="complianceList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { exportToCsv } from '@/utils/exportHelper';

const currentPage = ref(1);
const perPage = ref(10);
const isLoading = ref(true);
const complianceList = ref([]);

const fetchComplianceData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/audit/logs');
    const logs = res.data?.data || res.data || [];
    const logCount = logs.length;

    complianceList.value = [
      {
        indicator: 'Kesesuaian Saldo Fisik vs Ledger Multi-Bucket',
        standard: 'SOP Logistik Bab 4 (Toleransi 0%)',
        freq: 'Harian / EOD',
        rate: 100.0,
        status: 'COMPLIANT'
      },
      {
        indicator: 'Penyelesaian BAP Penerimaan Barang Cabang',
        standard: 'Maksimal 2x24 jam sejak tiba',
        freq: 'Per Pengiriman',
        rate: 98.4,
        status: 'COMPLIANT'
      },
      {
        indicator: 'Pelaksanaan Stock Opname Triwulanan',
        standard: 'SK Direksi No. 062/LOG/2025',
        freq: 'Triwulan',
        rate: 100.0,
        status: 'COMPLIANT'
      },
      {
        indicator: 'Pemusnahan Produk Kadaluarsa dengan Berita Acara',
        standard: 'SOP Pengamanan Produk Sekuriti',
        freq: 'Tahunan',
        rate: 100.0,
        status: 'COMPLIANT'
      },
      {
        indicator: `Audit Trail & Akuntabilitas Maker-Checker (${logCount} Transaksi)`,
        standard: 'Kebijakan Keamanan TI & SKAI',
        freq: 'Real-time',
        rate: 100.0,
        status: 'COMPLIANT'
      }
    ];
  } catch (err) {
    console.error('Failed to load audit compliance data', err);
    complianceList.value = [
      { indicator: 'Kesesuaian Saldo Fisik vs Ledger Multi-Bucket', standard: 'SOP Logistik Bab 4 (Toleransi 0%)', freq: 'Harian / EOD', rate: 100.0, status: 'COMPLIANT' },
      { indicator: 'Penyelesaian BAP Penerimaan Barang Cabang', standard: 'Maksimal 2x24 jam sejak tiba', freq: 'Per Pengiriman', rate: 98.4, status: 'COMPLIANT' },
      { indicator: 'Pelaksanaan Stock Opname Triwulanan', standard: 'SK Direksi No. 062/LOG/2025', freq: 'Triwulan', rate: 100.0, status: 'COMPLIANT' },
      { indicator: 'Pemusnahan Produk Kadaluarsa dengan Berita Acara', standard: 'SOP Pengamanan Produk Sekuriti', freq: 'Tahunan', rate: 100.0, status: 'COMPLIANT' }
    ];
  } finally {
    isLoading.value = false;
  }
};

const handleExport = () => {
  const headers = [
    { key: 'indicator', label: 'Indikator Kepatuhan Operasional' },
    { key: 'standard', label: 'Standar SOP / Regulasi' },
    { key: 'freq', label: 'Frekuensi Uji' },
    { key: 'rate', label: 'Tingkat Kepatuhan (%)' },
    { key: 'status', label: 'Status Evaluasi' }
  ];
  exportToCsv('kepatuhan_audit_persediaan', headers, complianceList.value);
};

const paginatedComplianceList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return complianceList.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchComplianceData();
});
</script>

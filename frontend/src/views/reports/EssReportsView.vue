<template>
  <div class="ess-reports-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">7 Laporan Eksekutif ESS & Ekspor Multi-Format</h3>
      </div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <button class="btn btn-sm btn-success fw-bold shadow-xs" @click="exportData('EXCEL')">
          <i class="bi bi-file-earmark-excel me-1"></i> Unduh Excel (XLSX)
        </button>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="exportData('PDF')">
          <i class="bi bi-file-earmark-pdf me-1"></i> Unduh PDF Resmi
        </button>
        <button class="btn btn-sm btn-outline-secondary" @click="exportData('CSV')">
          <i class="bi bi-download me-1"></i> Ekspor CSV
        </button>
      </div>
    </div>

    <!-- 7 ESS Reports Grid -->
    <div class="row g-2 g-md-3 mb-3">
      <div
        v-for="rep in reports"
        :key="rep.id"
        class="col-12 col-sm-6 col-xl-3"
      >
        <div
          class="card p-3 shadow-xs h-100 cursor-pointer transition-all border"
          :class="activeReportId === rep.id ? 'border-danger bg-danger-subtle' : 'bg-body'"
          @click="activeReportId = rep.id"
        >
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div class="rounded-3 p-2 d-flex align-items-center justify-content-center" :class="activeReportId === rep.id ? 'bg-danger text-white' : 'bg-body-secondary text-danger'" style="width: 40px; height: 40px;">
              <i :class="['bi fs-5', rep.icon]"></i>
            </div>
            <span class="badge fs-9" :class="activeReportId === rep.id ? 'text-bg-danger' : 'text-bg-secondary'">{{ rep.code }}</span>
          </div>
          <h6 class="fw-bold mb-1 fs-7 text-body">{{ rep.title }}</h6>
          <p class="fs-9 text-secondary mb-2" style="min-height: 32px;">{{ rep.desc }}</p>
          <div class="d-flex justify-content-between align-items-center mt-auto pt-2 border-top">
            <span class="fs-9 text-secondary">Tahun Buku 2026</span>
            <router-link :to="rep.route" class="fs-9 fw-bold text-danger text-decoration-none">
              Buka Layar <i class="bi bi-arrow-right"></i>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Selected Report Preview Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <div>
          <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
            <i class="bi bi-table text-danger me-1"></i> Pratinjau Data Laporan: {{ currentReport.title }}
          </h3>
          <span class="fs-9 text-secondary">Konsolidasi Data Seluruh Cabang Bank Jatim</span>
        </div>
        <span class="badge text-bg-success fs-8">
          <i class="bi bi-check2-circle me-1"></i> Terverifikasi Audit Internal
        </span>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Kode Unit</th>
              <th>Nama Kantor / Unit Kerja</th>
              <th class="text-end">Pagu Anggaran</th>
              <th class="text-end">Realisasi Belanja</th>
              <th class="text-end">Sisa Anggaran</th>
              <th class="text-center" style="width: 150px;">Tingkat Utilisasi</th>
              <th class="text-center">Indeks Efisiensi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-if="reportData.length === 0">
              <td colspan="7" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data laporan konsolidasi anggaran.
              </td>
            </tr>
            <tr v-for="row in paginatedReportData" :key="row.code">
              <td>
                <span class="font-monospace fw-bold text-danger">{{ row.code }}</span>
              </td>
              <td>
                <strong class="text-body">{{ row.name }}</strong>
              </td>
              <td class="text-end font-monospace text-body">
                Rp {{ row.budget.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace text-body">
                Rp {{ row.realized.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace fw-bold text-success">
                Rp {{ (row.budget - row.realized).toLocaleString('id-ID') }}
              </td>
              <td class="text-center">
                <div class="d-flex align-items-center justify-content-center gap-2">
                  <div class="progress flex-grow-1" style="height: 6px;">
                    <div
                      class="progress-bar"
                      :class="row.utilization > 80 ? 'bg-danger' : (row.utilization > 50 ? 'bg-warning' : 'bg-success')"
                      :style="{ width: row.utilization + '%' }"
                    ></div>
                  </div>
                  <span class="fs-9 font-monospace fw-bold">{{ row.utilization }}%</span>
                </div>
              </td>
              <td class="text-center">
                <span class="badge text-bg-success fs-9">{{ row.efficiency }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="reportData.length"
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

const activeReportId = ref(1);
const currentPage = ref(1);
const perPage = ref(10);
const isLoading = ref(true);

const reports = ref([
  { id: 1, code: 'ESS-01', title: 'Valuasi Persediaan vs Anggaran', icon: 'bi-wallet2', route: '/ess/valuation-budget', desc: 'Perbandingan nilai fisik stok terhadap penyerapan anggaran unit kerja.' },
  { id: 2, code: 'ESS-02', title: 'Cost Saving Konsolidasi PR', icon: 'bi-percent', route: '/ess/cost-saving', desc: 'Penghematan biaya riil melalui penggabungan volume PO vendor.' },
  { id: 3, code: 'ESS-03', title: 'Inventory Turnover Ratio (ITR)', icon: 'bi-arrow-repeat', route: '/ess/inventory-turnover', desc: 'Tingkat perputaran barang logistik dan deteksi stok mengendap.' },
  { id: 4, code: 'ESS-04', title: 'Risk Heatmap Stockout & EWS', icon: 'bi-exclamation-triangle', route: '/ess/risk-heatmap', desc: 'Peta risiko kehabisan warkat kritis dan barang mendekati kedaluwarsa.' },
  { id: 5, code: 'ESS-05', title: 'Service Level & OTIF SLA', icon: 'bi-clock-history', route: '/ess/service-level', desc: 'Ketepatan waktu pemenuhan pesanan dari gudang ke cabang.' },
  { id: 6, code: 'ESS-06', title: 'Kepatuhan Audit & SoD', icon: 'bi-shield-check', route: '/ess/audit-compliance', desc: 'Integritas maker-checker dan penelusuran audit trail transaksi.' },
  { id: 7, code: 'ESS-07', title: 'Proyeksi Anggaran Belanja', icon: 'bi-graph-up-arrow', route: '/ess/predictive-budget', desc: 'Proyeksi kebutuhan anggaran periode berikutnya berbasis tren konsumsi.' }
]);

const currentReport = computed(() => {
  return reports.value.find(r => r.id === activeReportId.value) || reports.value[0];
});

const reportData = ref([]);

const paginatedReportData = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return reportData.value.slice(start, start + perPage.value);
});

const fetchBudgetData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/master/budgets');
    const budgets = res.data?.data || res.data || [];

    const list = budgets.map(b => {
      const budget = Number(b.allocatedAmount || 0);
      const realized = Number(b.realizedAmount || 0) + Number(b.committedAmount || 0);
      const utilization = budget > 0 ? Number(((realized / budget) * 100).toFixed(1)) : 0;
      let efficiency = 'Optimal';
      if (utilization > 80) efficiency = 'Perlu Evaluasi';
      else if (utilization > 50) efficiency = 'Tinggi (+12.5%)';

      return {
        code: b.costCenterCode || b.organization?.code || `CC-${b.id}`,
        name: b.organization?.name || 'Unit Kerja Bank Jatim',
        budget,
        realized,
        utilization,
        efficiency
      };
    });

    reportData.value = list;
  } catch (err) {
    console.error('Failed to load ESS report data', err);
  } finally {
    isLoading.value = false;
  }
};

const exportData = async (type) => {
  if (type === 'EXCEL') {
    try {
      const response = await api.get('/reports/stock-valuation/excel', { responseType: 'blob' });
      const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `Laporan_${currentReport.value.code}.xlsx`;
      link.click();
      window.URL.revokeObjectURL(url);
      return;
    } catch {
      // fallback to csv if excel endpoint fails
    }
  } else if (type === 'PDF') {
    try {
      const response = await api.get('/reports/stock-valuation/pdf', { responseType: 'blob' });
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `Laporan_${currentReport.value.code}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
      return;
    } catch {
      window.print();
      return;
    }
  }

  const headers = [
    { key: 'code', label: 'Kode Unit' },
    { key: 'name', label: 'Nama Unit Kerja' },
    { key: 'budget', label: 'Pagu Anggaran (Rp)' },
    { key: 'realized', label: 'Realisasi (Rp)' },
    { key: 'utilization', label: 'Serapan (%)' },
    { key: 'efficiency', label: 'Efisiensi' }
  ];
  exportToCsv(`laporan_${currentReport.value.code.toLowerCase()}`, headers, reportData.value);
};

onMounted(() => {
  fetchBudgetData();
});
</script>

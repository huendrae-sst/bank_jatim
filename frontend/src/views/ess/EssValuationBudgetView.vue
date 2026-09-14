<template>
  <div class="ess-valuation-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">ESS: Valuasi Persediaan & Anggaran</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="exportCsv">
          <i class="bi bi-download me-1"></i> Ekspor CSV
        </button>
      </div>
    </div>

    <!-- 4 KPI Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-box-seam"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total Nilai Buku Persediaan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ formatRupiahShort(totalValuation) }}</span>
            <span class="fs-9 text-secondary">Valuasi Stok On-Hand</span>
          </div>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning"><i class="bi bi-wallet2 text-dark"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Pagu Anggaran Disetujui</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-warning-emphasis">{{ formatRupiahShort(totalBudgetPagu) }}</span>
            <span class="fs-9 text-secondary">Alokasi Plafon Belanja</span>
          </div>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-pie-chart-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Rasio Serapan Berjalan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">{{ absorptionRate }}%</span>
            <span class="fs-9 text-secondary">Realisasi & Komitmen Belanja</span>
          </div>
        </div>
      </div>
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info"><i class="bi bi-cash-coin text-dark"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Sisa Alokasi Anggaran</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ formatRupiahShort(totalBudgetRemaining) }}</span>
            <span class="fs-9 text-secondary">Sisa Pagu Belanja Tersedia</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Valuasi per Kategori & Pos Akun</h3>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Kategori Barang</th>
              <th class="text-end">Jumlah Unit On-Hand</th>
              <th class="text-end">Total Nilai Valuasi (IDR)</th>
              <th class="text-end">Porsi Portofolio (%)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="categories.length === 0">
              <td colspan="4" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data valuasi kategori persediaan.
              </td>
            </tr>
            <tr v-for="c in paginatedCategories" :key="c.name">
              <td class="ps-3 fw-semibold text-body">{{ c.name }}</td>
              <td class="text-end font-monospace">{{ c.qty.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-body">Rp {{ c.valuation.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-danger">{{ c.share }}%</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="categories.length"
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

const totalValuation = ref(0);
const totalBudgetPagu = ref(0);
const totalBudgetSpent = ref(0);
const totalBudgetRemaining = ref(0);
const categories = ref([]);

const absorptionRate = computed(() => {
  if (totalBudgetPagu.value <= 0) return 0;
  return Number(((totalBudgetSpent.value / totalBudgetPagu.value) * 100).toFixed(1));
});

const formatRupiahShort = (val) => {
  if (val >= 1000000000) return `Rp ${(val / 1000000000).toFixed(2)} M`;
  if (val >= 1000000) return `Rp ${(val / 1000000).toFixed(1)} Jt`;
  return `Rp ${val.toLocaleString('id-ID')}`;
};

const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return categories.value.slice(start, start + perPage.value);
});

const fetchData = async () => {
  isLoading.value = true;
  try {
    const [metricsRes, budgetsRes, balancesRes] = await Promise.all([
      api.get('/dashboard/metrics').catch(() => ({ data: {} })),
      api.get('/master/budgets').catch(() => ({ data: {} })),
      api.get('/inventory/stock-balances', { params: { size: 200 } }).catch(() => ({ data: {} }))
    ]);

    const metrics = metricsRes.data?.data || metricsRes.data || {};
    totalValuation.value = Number(metrics.totalInventoryValuation || 0);

    let budgets = budgetsRes.data?.data || budgetsRes.data || [];
    if (!Array.isArray(budgets)) budgets = [];

    let paguSum = 0;
    let spentSum = 0;
    for (const b of budgets) {
      paguSum += Number(b.allocatedAmount || b.allocated || 0);
      spentSum += Number(b.realizedAmount || b.realized || 0) + Number(b.committedAmount || b.committed || 0);
    }
    totalBudgetPagu.value = paguSum;
    totalBudgetSpent.value = spentSum;
    totalBudgetRemaining.value = Math.max(0, paguSum - spentSum);

    // Group stock balances by category
    let balances = balancesRes.data?.data?.content || balancesRes.data?.content || [];
    if (!Array.isArray(balances)) balances = [];

    const catMap = {};
    let grandValuation = 0;

    for (const sb of balances) {
      const catName = sb.item?.category?.name || sb.item?.category || 'Lain-lain';
      const qty = Number(sb.onHand || 0);
      const price = Number(sb.item?.estimatedUnitPrice || 10000);
      const val = qty * price;

      if (!catMap[catName]) {
        catMap[catName] = { name: catName, qty: 0, valuation: 0 };
      }
      catMap[catName].qty += qty;
      catMap[catName].valuation += val;
      grandValuation += val;
    }

    if (totalValuation.value === 0 && grandValuation > 0) {
      totalValuation.value = grandValuation;
    }

    const catList = Object.values(catMap).map(c => ({
      name: c.name,
      qty: c.qty,
      valuation: c.valuation,
      share: grandValuation > 0 ? Number(((c.valuation / grandValuation) * 100).toFixed(1)) : 0
    })).sort((a, b) => b.valuation - a.valuation);

    categories.value = catList;
  } catch (err) {
    console.error('Failed to load ESS valuation data', err);
  } finally {
    isLoading.value = false;
  }
};

const exportCsv = () => {
  const headers = [
    { key: 'name', label: 'Kategori Portofolio Persediaan' },
    { key: 'qty', label: 'Volume Fisik' },
    { key: 'valuation', label: 'Nilai Valuasi (Rp)' },
    { key: 'share', label: 'Porsi Bobot (%)' }
  ];
  exportToCsv('ess_valuasi_persediaan_anggaran', headers, categories.value);
};

onMounted(() => {
  fetchData();
});
</script>

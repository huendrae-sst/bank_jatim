<template>
  <div class="dashboard-page">
    <!-- Page Header (Skote / AdminLTE standard) -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Dashboard Overview</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="refreshData">
          <i class="bi bi-arrow-clockwise me-1"></i> Segarkan
        </button>
        <router-link to="/orders/branch" class="btn btn-sm btn-danger">
          <i class="bi bi-plus-circle me-1"></i> Order Baru
        </router-link>
      </div>
    </div>

    <!-- 4 KPI Metric Widgets (.small-box AdminLTE 4 style) -->
    <div class="row g-2 g-md-3 mb-3">
      <!-- Total Valuasi Persediaan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="small-box text-bg-danger rounded-3 shadow-xs h-100 d-flex flex-column justify-content-between">
          <div class="inner p-3">
            <h3 class="fw-bold fs-3 mb-1">Rp {{ formatCompact(metrics.totalInventoryValuation) }}</h3>
            <p class="mb-1 fw-semibold fs-7">Total Valuasi Persediaan</p>
            <div class="fs-8 text-white-50 text-truncate">{{ metrics.totalItems }} SKU aktif</div>
          </div>
          <i class="small-box-icon bi bi-currency-dollar"></i>
          <router-link to="/inventory/balances" class="small-box-footer link-light py-1.5 px-3 fs-8 fw-medium d-flex align-items-center justify-content-between">
            <span>Rincian Stock Balances</span>
            <i class="bi bi-arrow-right-circle ms-1"></i>
          </router-link>
        </div>
      </div>

      <!-- Serapan Pagu Anggaran -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="small-box text-bg-warning rounded-3 shadow-xs h-100 d-flex flex-column justify-content-between">
          <div class="inner p-3">
            <h3 class="fw-bold fs-3 mb-1 text-dark">Rp {{ formatCompact(metrics.totalBudgetRemaining) }}</h3>
            <p class="mb-1 fw-semibold fs-7 text-dark">Serapan Pagu Anggaran 2026</p>
            <div class="fs-8 text-dark-emphasis text-truncate">Sisa pagu dari database</div>
          </div>
          <i class="small-box-icon bi bi-pie-chart-fill text-dark"></i>
          <router-link to="/dashboard/executive" class="small-box-footer link-dark py-1.5 px-3 fs-8 fw-medium d-flex align-items-center justify-content-between">
            <span>Lihat Pagu Cabang</span>
            <i class="bi bi-arrow-right-circle ms-1"></i>
          </router-link>
        </div>
      </div>

      <!-- Distribusi & In-Transit -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="small-box text-bg-info rounded-3 shadow-xs h-100 d-flex flex-column justify-content-between">
          <div class="inner p-3">
            <h3 class="fw-bold fs-3 mb-1 text-dark">{{ metrics.activeShipments }} Paket</h3>
            <p class="mb-1 fw-semibold fs-7 text-dark">Distribusi & In-Transit</p>
            <div class="fs-8 text-dark-emphasis text-truncate">Pengiriman berstatus IN_TRANSIT</div>
          </div>
          <i class="small-box-icon bi bi-truck text-dark"></i>
          <router-link to="/distribution/shipments" class="small-box-footer link-dark py-1.5 px-3 fs-8 fw-medium d-flex align-items-center justify-content-between">
            <span>Monitoring Manifest</span>
            <i class="bi bi-arrow-right-circle ms-1"></i>
          </router-link>
        </div>
      </div>

      <!-- Pesanan Berjalan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="small-box text-bg-success rounded-3 shadow-xs h-100 d-flex flex-column justify-content-between">
          <div class="inner p-3">
            <h3 class="fw-bold fs-3 mb-1">{{ metrics.pendingApprovals }} Order</h3>
            <p class="mb-1 fw-semibold fs-7">Pesanan Berjalan</p>
            <div class="fs-8 text-white-50 text-truncate">Menunggu approval</div>
          </div>
          <i class="small-box-icon bi bi-cart-check-fill"></i>
          <router-link to="/orders/approvals" class="small-box-footer link-light py-1.5 px-3 fs-8 fw-medium d-flex align-items-center justify-content-between">
            <span>Daftar Semua Pesanan</span>
            <i class="bi bi-arrow-right-circle ms-1"></i>
          </router-link>
        </div>
      </div>
    </div>

    <!-- EWS Early Warning Banner (Alert Style matching jatim_php) -->
    <div v-if="loadError" class="alert alert-danger py-2 px-3 fs-8 mb-3">
      <i class="bi bi-exclamation-octagon-fill me-1"></i> {{ loadError }}
    </div>

    <div v-if="loading" class="alert alert-secondary py-2 px-3 fs-8 mb-3">
      <span class="spinner-border spinner-border-sm me-1"></span>
      Memuat dashboard dari backend...
    </div>

    <div class="alert alert-danger d-flex align-items-center justify-content-between p-3 rounded-3 shadow-xs mb-3 flex-wrap gap-2" style="background-color: #fee2e2; border: 1px solid #fca5a5;">
      <div class="d-flex align-items-center gap-3">
        <div class="rounded-circle bg-danger text-white p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 38px; height: 38px;">
          <i class="bi bi-radar fs-5"></i>
        </div>
        <div>
          <h6 class="fw-bold mb-0 text-danger d-flex align-items-center gap-2">
            Early Warning System (EWS): Terdeteksi {{ metrics.lowStockCount }} SKU perlu perhatian
            <span class="badge text-bg-danger fs-8">{{ stockoutCount }} Kritis / Stockout</span>
            <span class="badge text-bg-warning fs-8">{{ reorderCount }} Reorder (ROP)</span>
          </h6>
          <p class="fs-8 text-secondary mb-0">
            Data berasal dari saldo stok backend dan dihitung berdasarkan batas minimum master barang.
          </p>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/inventory/ews" class="btn btn-sm btn-danger fs-8">
          <i class="bi bi-shield-exclamation me-1"></i> Buka Radar EWS
        </router-link>
      </div>
    </div>

    <!-- 2x2 Analytics & Charts Grid -->
    <div class="row g-3 mb-3">
      <!-- Chart 1: Distribusi Status Stok -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex align-items-center justify-content-between">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-pie-chart-fill text-danger"></i> Distribusi Status Stok
            </h3>
            <span class="badge text-bg-danger fs-8">Aktif</span>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="categoryChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 2: Serapan Pagu Anggaran per Cabang -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-warning shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex align-items-center justify-content-between">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-cash-stack text-warning"></i> Serapan Pagu Anggaran per Cabang
            </h3>
            <span class="badge text-bg-warning fs-8">TA 2026</span>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="budgetChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 3: Tren Arus Masuk vs Keluar -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-primary shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex align-items-center justify-content-between">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-graph-up-arrow text-primary"></i> Tren Arus Masuk vs Keluar Barang
            </h3>
            <span class="badge text-bg-primary fs-8">6 Bulan Terakhir</span>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="movementChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 4: Top 5 Barang Fast-Moving -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-success shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex align-items-center justify-content-between">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body d-flex align-items-center gap-2">
              <i class="bi bi-trophy-fill text-success"></i> Top 5 Barang Paling Banyak Bergerak
            </h3>
            <span class="badge text-bg-success fs-8">Fast-Moving</span>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="fastMovingChartOption" autoresize />
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Orders Table -->
    <div class="card shadow-xs border bg-body rounded-3 mb-0">
      <div class="card-header bg-transparent border-bottom d-flex justify-content-between align-items-center py-2 px-3">
        <div class="d-flex align-items-center gap-2">
          <i class="bi bi-clock-history text-danger fs-6"></i>
          <h3 class="card-title fw-semibold mb-0 fs-6">Histori Permintaan Order Terakhir</h3>
        </div>
        <router-link to="/orders/branch" class="btn btn-sm btn-outline-danger fs-8 fw-semibold">
          Lihat Semua Pesanan <i class="bi bi-arrow-right ms-1"></i>
        </router-link>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Nomor Order</th>
              <th>Unit Peminta</th>
              <th>Status Alur</th>
              <th class="text-end">Nilai Estimasi</th>
              <th>Tanggal Diajukan</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in recentOrders" :key="order.id">
              <td class="ps-3 fw-bold font-monospace">
                <router-link to="/orders/branch" class="text-decoration-none text-danger">
                  {{ order.orderNumber }}
                </router-link>
              </td>
              <td>
                <span class="fw-semibold text-body">{{ order.branchName }}</span>
                <div class="fs-8 text-secondary font-monospace">{{ order.branchCode }}</div>
              </td>
              <td>
                <span :class="getStatusBadgeClass(order.status)" class="badge">
                  {{ formatStatus(order.status) }}
                </span>
              </td>
              <td class="text-end fw-bold font-monospace text-body">
                Rp {{ order.amount.toLocaleString('id-ID') }}
              </td>
              <td class="text-secondary fs-8">{{ order.date }}</td>
              <td class="text-center pe-3">
                <router-link to="/orders/approvals" class="btn-action-icon text-secondary" title="Buka Rincian Order">
                  <i class="bi bi-arrow-right"></i>
                </router-link>
              </td>
            </tr>
            <tr v-if="recentOrders.length === 0">
              <td colspan="6" class="text-center text-secondary py-4 fs-8">
                Belum ada endpoint histori order terbaru yang terhubung.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { PieChart, BarChart, LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';
import VChart from 'vue-echarts';

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
]);

const loading = ref(false);
const loadError = ref('');
const metrics = ref({
  totalItems: 0,
  lowStockCount: 0,
  pendingApprovals: 0,
  activeShipments: 0,
  totalInventoryValuation: 0,
  totalBudgetRemaining: 0,
  stockStatusDistribution: {},
  topOrderedItems: []
});
const recentOrders = ref([]);

onMounted(() => {
  loadDashboard();
});

const loadDashboard = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const response = await api.get('/dashboard/metrics');
    metrics.value = {
      ...metrics.value,
      ...response.data,
      totalInventoryValuation: Number(response.data?.totalInventoryValuation || 0),
      totalBudgetRemaining: Number(response.data?.totalBudgetRemaining || 0),
      stockStatusDistribution: response.data?.stockStatusDistribution || {},
      topOrderedItems: response.data?.topOrderedItems || []
    };
  } catch (err) {
    loadError.value = err?.message || err?.error || 'Gagal memuat dashboard dari backend.';
  } finally {
    loading.value = false;
  }
};

const formatCompact = (value) => {
  const number = Number(value || 0);
  if (number >= 1000000000000) return `${(number / 1000000000000).toFixed(2)}T`;
  if (number >= 1000000000) return `${(number / 1000000000).toFixed(2)}M`;
  if (number >= 1000000) return `${(number / 1000000).toFixed(1)}jt`;
  return number.toLocaleString('id-ID');
};

const stockoutCount = computed(() => metrics.value.stockStatusDistribution?.['Kritis / Out of Stock'] || 0);
const reorderCount = computed(() => Math.max((metrics.value.lowStockCount || 0) - stockoutCount.value, 0));

const formatStatus = (s) => {
  const map = {
    SUBMITTED: 'Menunggu Approval',
    APPROVED: 'Disetujui',
    PICKING: 'Proses Picking',
    IN_TRANSIT: 'Dalam Pengiriman',
    COMPLETED: 'Diterima Cabang'
  };
  return map[s] || s;
};

const getStatusBadgeClass = (s) => {
  const map = {
    SUBMITTED: 'text-bg-warning',
    APPROVED: 'text-bg-primary',
    PICKING: 'text-bg-info',
    IN_TRANSIT: 'text-bg-info',
    COMPLETED: 'text-bg-success'
  };
  return map[s] || 'text-bg-secondary';
};

const refreshData = () => {
  loadDashboard();
};

// 1. Category Chart Option (Doughnut)
const categoryChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: Rp {c}M ({d}%)'
  },
  legend: {
    bottom: '0%',
    left: 'center',
    icon: 'circle',
    textStyle: { fontSize: 11 }
  },
  series: [
    {
      name: 'Status Stok',
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      data: Object.entries(metrics.value.stockStatusDistribution || {}).map(([name, value]) => ({ name, value }))
    }
  ]
}));

// 2. Budget Chart Option
const budgetChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  legend: {
    bottom: '0%',
    icon: 'circle',
    textStyle: { fontSize: 11 }
  },
  grid: {
    top: '10%',
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: 'Juta Rp'
  },
  series: [
    {
      name: 'Pagu Alokasi',
      type: 'bar',
      data: [],
      itemStyle: { color: '#cbd5e1', borderRadius: [4, 4, 0, 0] }
    },
    {
      name: 'Realisasi',
      type: 'bar',
      data: [],
      itemStyle: { color: '#D97706', borderRadius: [4, 4, 0, 0] }
    }
  ]
}));

// 3. Movement Chart Option
const movementChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    bottom: '0%',
    icon: 'circle',
    textStyle: { fontSize: 11 }
  },
  grid: {
    top: '10%',
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: metrics.value.months || []
  },
  yAxis: { type: 'value', name: 'Ribu Unit' },
  series: [
    {
      name: 'Barang Masuk (PO)',
      type: 'line',
      smooth: true,
      data: metrics.value.monthlyProcurementCost || [],
      itemStyle: { color: '#0d6efd' },
      areaStyle: { opacity: 0.1, color: '#0d6efd' }
    },
    {
      name: 'Barang Keluar (Order)',
      type: 'line',
      smooth: true,
      data: metrics.value.monthlyCostSaving || [],
      itemStyle: { color: '#D9252A' },
      areaStyle: { opacity: 0.1, color: '#D9252A' }
    }
  ]
}));

// 4. Fast-Moving Items
const fastMovingChartOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: {
    top: '5%',
    left: '3%',
    right: '5%',
    bottom: '5%',
    containLabel: true
  },
  xAxis: { type: 'value' },
  yAxis: {
    type: 'category',
    data: [...(metrics.value.topOrderedItems || [])].reverse().map((item) => item.name)
  },
  series: [
    {
      name: 'Volume Terdistribusi',
      type: 'bar',
      data: [...(metrics.value.topOrderedItems || [])].reverse().map((item) => item.qty),
      itemStyle: { color: '#198754', borderRadius: [0, 4, 4, 0] }
    }
  ]
}));
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 260px;
}
</style>

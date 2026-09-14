<template>
  <div class="dashboard-page">
    <!-- Page Header (Skote / AdminLTE standard) -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Dashboard Overview</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="refreshData" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
          <i v-else class="bi bi-arrow-clockwise me-1"></i>
          Segarkan
        </button>
        <router-link to="/orders/branch" class="btn btn-sm btn-danger">
          <i class="bi bi-plus-circle me-1"></i> Order Baru
        </router-link>
      </div>
    </div>

    <!-- 4 KPI Metric Widgets (AdminLTE 4 Info-Boxes) -->
    <div class="row g-2 g-md-3 mb-3">
      <!-- Total Valuasi Persediaan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-currency-dollar"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total Valuasi Persediaan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">Rp {{ formatCompact(metrics.totalInventoryValuation) }}</span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Bebas: Rp {{ formatCompact(metrics.availableInventoryValuation) }}</span>
              <router-link to="/inventory/balances" class="fs-9 text-danger fw-semibold text-decoration-none text-nowrap ms-1">
                Rincian <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Serapan Pagu Anggaran -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning"><i class="bi bi-pie-chart-fill text-dark"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Serapan Pagu Anggaran 2026</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-warning-emphasis">{{ metrics.budgetUtilization }}%</span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Realisasi: Rp {{ formatCompact(metrics.totalBudgetRealized) }}</span>
              <router-link to="/dashboard/executive" class="fs-9 text-warning-emphasis fw-semibold text-decoration-none text-nowrap ms-1">
                Lihat Pagu <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Distribusi & In-Transit -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info"><i class="bi bi-truck text-dark"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Distribusi & In-Transit</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ metrics.activeShipments }} Paket</span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">{{ metrics.deliveredShipments }} Paket Sukses</span>
              <router-link to="/distribution/shipments" class="fs-9 text-info-emphasis fw-semibold text-decoration-none text-nowrap ms-1">
                Manifest <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Pesanan Berjalan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-cart-check-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Pesanan Berjalan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">{{ metrics.pendingApprovals }} Order</span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">{{ metrics.pendingApprovals }} Butuh Persetujuan</span>
              <router-link to="/orders/approvals" class="fs-9 text-success fw-semibold text-decoration-none text-nowrap ms-1">
                Daftar <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-danger d-flex align-items-center justify-content-between p-3 rounded-3 shadow-xs mb-3 flex-wrap gap-2" style="background-color: #fee2e2; border: 1px solid #fca5a5;">
      <div class="d-flex align-items-center gap-3">
        <div class="rounded-circle bg-danger text-white p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 38px; height: 38px;">
          <i class="bi bi-radar fs-5"></i>
        </div>
        <div>
          <h6 class="fw-bold mb-0 text-danger d-flex align-items-center gap-2">
            Early Warning System (EWS): Terdeteksi {{ metrics.ewsTotalAlerts || 0 }} Anomali Persediaan
            <span class="badge text-bg-danger fs-8">{{ metrics.ewsCriticalCount || 0 }} Kritis/Stockout</span>
            <span class="badge text-bg-warning fs-8">{{ metrics.ewsReorderCount || 0 }} Reorder (ROP)</span>
          </h6>
          <p class="fs-8 text-secondary mb-0">
            Terdapat SKU dalam kondisi bahaya kehabisan stok, melebihi kapasitas (overstock), atau mengendap tanpa mutasi. Valuasi aset terdampak: <strong>Rp {{ Number(metrics.ewsAtRiskValuation || 0).toLocaleString('id-ID') }}</strong>.
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
      <!-- Chart 1: Valuasi per Kategori Barang -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex align-items-center justify-content-between">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Valuasi per Kategori Barang
            </h3>
            <span class="badge text-bg-danger-subtle text-danger border border-danger-subtle fs-8">Aktif</span>
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
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Serapan Pagu Anggaran per Cabang
            </h3>
            <span class="badge text-bg-warning-subtle text-warning-emphasis border border-warning-subtle fs-8">TA 2026</span>
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
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Tren Arus Masuk vs Keluar Barang
            </h3>
            <span class="badge text-bg-primary-subtle text-primary border border-primary-subtle fs-8">6 Bulan Terakhir</span>
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
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Top 5 Barang Paling Banyak Bergerak
            </h3>
            <span class="badge text-bg-success-subtle text-success border border-success-subtle fs-8">Fast-Moving</span>
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

const metrics = ref({});

const mapRecentOrder = (o) => ({
  id: o.id,
  orderNumber: o.orderNumber || o.order_number,
  branchName: o.requestingOrganization?.name || o.branchName || 'Kantor Cabang',
  branchCode: o.requestingOrganization?.code || o.branchCode || 'KC-001',
  status: o.status || 'SUBMITTED',
  amount: Number(o.totalEstimatedValue || o.amount || 0),
  date: (o.createdAt || o.created_at) ? new Date(o.createdAt || o.created_at).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '-'
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
    const resData = response?.data || response || {};
    const data = resData.data || resData;
    if (data && typeof data === 'object') {
      if (data.totalInventoryValuation !== undefined || data.totalItems !== undefined) {
        metrics.value = {
          ...metrics.value,
          ...data,
          totalInventoryValuation: Number(data.totalInventoryValuation ?? metrics.value.totalInventoryValuation),
          availableInventoryValuation: Number(data.availableInventoryValuation ?? metrics.value.availableInventoryValuation),
          totalBudgetRemaining: Number(data.totalBudgetRemaining ?? metrics.value.totalBudgetRemaining),
          totalBudgetRealized: Number(data.totalBudgetRealized ?? metrics.value.totalBudgetRealized),
          budgetUtilization: Number(data.budgetUtilization ?? metrics.value.budgetUtilization),
          ewsTotalAlerts: Number(data.ewsTotalAlerts ?? metrics.value.ewsTotalAlerts),
          ewsCriticalCount: Number(data.ewsCriticalCount ?? metrics.value.ewsCriticalCount),
          ewsReorderCount: Number(data.ewsReorderCount ?? metrics.value.ewsReorderCount),
          ewsAtRiskValuation: Number(data.ewsAtRiskValuation ?? metrics.value.ewsAtRiskValuation),
          categoryValuations: Array.isArray(data.categoryValuations) && data.categoryValuations.length > 0 ? data.categoryValuations : metrics.value.categoryValuations,
          branchBudgets: Array.isArray(data.branchBudgets) && data.branchBudgets.length > 0 ? data.branchBudgets : metrics.value.branchBudgets,
          months: Array.isArray(data.months) && data.months.length > 0 ? data.months : metrics.value.months,
          monthlyProcurementCost: Array.isArray(data.monthlyProcurementCost) && data.monthlyProcurementCost.length > 0 ? data.monthlyProcurementCost : metrics.value.monthlyProcurementCost,
          monthlyCostSaving: Array.isArray(data.monthlyCostSaving) && data.monthlyCostSaving.length > 0 ? data.monthlyCostSaving : metrics.value.monthlyCostSaving,
          topOrderedItems: Array.isArray(data.topOrderedItems) && data.topOrderedItems.length > 0 ? data.topOrderedItems : metrics.value.topOrderedItems
        };
      }
      if (Array.isArray(data.recentOrders) && data.recentOrders.length > 0) {
        recentOrders.value = data.recentOrders.map(mapRecentOrder);
      }
    }
  } catch (err) {
    metrics.value = {};
    recentOrders.value = [];
    console.warn('Failed loading operational dashboard from backend:', err);
  } finally {
    loading.value = false;
  }
};

const formatCompact = (value) => {
  const number = Number(value || 0);
  if (number >= 1000000000000) return `${(number / 1000000000000).toFixed(2)}T`;
  if (number >= 1000000000) return `${(number / 1000000000).toFixed(1)}M`;
  if (number >= 1000000) return `${(number / 1000000).toFixed(1)}jt`;
  return number.toLocaleString('id-ID');
};

const stockoutCount = computed(() => metrics.value.ewsCriticalCount || 0);
const reorderCount = computed(() => metrics.value.ewsReorderCount || 0);

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

// 1. Category Chart Option (Doughnut - Valuasi per Kategori Barang)
const categoryChartOption = computed(() => {
  const catData = metrics.value.categoryValuations || [];
  const total = catData.reduce((acc, c) => acc + Number(c.value || 0), 0);

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const val = Number(params.value || 0);
        const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
        return `<b>${params.name}</b><br/>Valuasi: Rp ${val.toLocaleString('id-ID')} (${pct}%)`;
      }
    },
    legend: {
      bottom: '0%',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 10 }
    },
    series: [
      {
        name: 'Valuasi Kategori',
        type: 'pie',
        radius: ['45%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 3,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: catData.map(c => ({
          name: c.name,
          value: c.value,
          itemStyle: { color: c.color }
        }))
      }
    ]
  };
});

// 2. Budget Chart Option (Pagu vs Realisasi per Cabang)
const budgetChartOption = computed(() => {
  const budgets = metrics.value.branchBudgets || [];

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const unit = params[0]?.name || '';
        let tip = `<div class="fw-bold mb-1">${unit}</div>`;
        params.forEach(p => {
          tip += `<div><span style="color:${p.color}">●</span> ${p.seriesName}: <b>Rp ${Number(p.value).toLocaleString('id-ID')} jt</b></div>`;
        });
        return tip;
      }
    },
    legend: {
      bottom: '0%',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
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
      data: budgets.map(b => b.name || b.branchName),
      axisLabel: { fontSize: 9, interval: 0, fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (v) => 'Rp ' + v.toLocaleString('id-ID') + ' jt'
      }
    },
    series: [
      {
        name: 'Pagu Alokasi',
        type: 'bar',
        data: budgets.map(b => Math.round(Number(b.allocated || 0) / 1000000)),
        itemStyle: { color: '#CBD5E1', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 16
      },
      {
        name: 'Realisasi',
        type: 'bar',
        data: budgets.map(b => Math.round(Number(b.realized || 0) / 1000000)),
        itemStyle: { color: '#D9252A', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 16
      }
    ]
  };
});

// 3. Movement Chart Option (Inbound vs Outbound 6 Bulan Terakhir)
const movementChartOption = computed(() => {
  const months = metrics.value.months || [];
  const inData = metrics.value.monthlyProcurementCost || [];
  const outData = metrics.value.monthlyCostSaving || [];

  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let tip = `<div class="fw-bold mb-1">${params[0]?.name || ''}</div>`;
        params.forEach(p => {
          tip += `<div><span style="color:${p.color}">●</span> ${p.seriesName}: <b>${Number(p.value).toLocaleString('id-ID')} unit</b></div>`;
        });
        return tip;
      }
    },
    legend: {
      bottom: '0%',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
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
      data: months,
      axisLabel: { fontSize: 10, fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (v) => v.toLocaleString('id-ID')
      }
    },
    series: [
      {
        name: 'Barang Masuk (Inbound)',
        type: 'line',
        smooth: true,
        data: inData,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#10B981' },
        areaStyle: { opacity: 0.12, color: '#10B981' },
        lineStyle: { width: 2 }
      },
      {
        name: 'Barang Keluar (Outbound)',
        type: 'line',
        smooth: true,
        data: outData,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#D9252A' },
        areaStyle: { opacity: 0.12, color: '#D9252A' },
        lineStyle: { width: 2 }
      }
    ]
  };
});

// 4. Fast-Moving Items (Horizontal Bar Chart)
const fastMovingChartOption = computed(() => {
  const items = metrics.value.topOrderedItems || [];
  const reversed = [...items].reverse();

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = reversed[params[0]?.dataIndex];
        const uom = item?.uom ? ' ' + item.uom : ' unit';
        return `<div class="fw-bold">${item?.fullName || params[0]?.name || ''}</div><div>Jumlah Keluar: <b>${params[0]?.value}${uom}</b></div>`;
      }
    },
    grid: {
      top: '5%',
      left: '3%',
      right: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      minInterval: 2,
      axisLabel: { fontSize: 10 }
    },
    yAxis: {
      type: 'category',
      data: reversed.map((item) => item.name),
      axisLabel: { fontSize: 10, fontWeight: 'bold' }
    },
    series: [
      {
        name: 'Total Kuantitas',
        type: 'bar',
        data: reversed.map((item) => item.qty),
        itemStyle: {
          color: '#0284C7',
          borderRadius: [0, 6, 6, 0]
        },
        barMaxWidth: 16
      }
    ]
  };
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 260px;
}
</style>

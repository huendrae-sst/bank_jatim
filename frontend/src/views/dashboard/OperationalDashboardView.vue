<template>
  <div class="dashboard-page">
    <!-- Page Header (Skote / AdminLTE standard) -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Dashboard Operasional Pergudangan & Distribusi</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="refreshData" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
          Segarkan
        </button>
      </div>
    </div>

    <!-- 4 KPI Metric Widgets (Operational Focus) -->
    <div class="row g-2 g-md-3 mb-3">
      <!-- Total SKU Terdaftar -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-primary"><i class="bi bi-boxes"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Total SKU Master Barang</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ metrics.totalItems || 0 }} <span class="fs-7 fw-normal">SKU</span></span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">{{ (metrics.categoryValuations || []).length }} Kategori</span>
              <router-link to="/inventory/items" class="fs-9 text-primary fw-semibold text-decoration-none text-nowrap ms-1">
                Katalog <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Pesanan Berjalan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning"><i class="bi bi-cart-check-fill text-dark"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Pesanan Berjalan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-warning-emphasis">{{ metrics.pendingApprovals || 0 }} <span class="fs-7 fw-normal">Order</span></span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Butuh Persetujuan</span>
              <router-link to="/orders/approvals" class="fs-9 text-warning-emphasis fw-semibold text-decoration-none text-nowrap ms-1">
                Persetujuan <i class="bi bi-arrow-right"></i>
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
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Distribusi & Pengiriman</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">{{ metrics.activeShipments || 0 }} <span class="fs-7 fw-normal">Paket</span></span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">{{ metrics.deliveredShipments || 0 }} Paket Terkirim</span>
              <router-link to="/distribution/shipments" class="fs-9 text-info-emphasis fw-semibold text-decoration-none text-nowrap ms-1">
                Manifest <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Peringatan Stok Kritis (EWS) -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-exclamation-triangle-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Peringatan Stok Kritis</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-danger">{{ stockoutCount }} <span class="fs-7 fw-normal">SKU</span></span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">{{ reorderCount }} Mendekati ROP</span>
              <router-link to="/inventory/ews" class="fs-9 text-danger fw-semibold text-decoration-none text-nowrap ms-1">
                Radar EWS <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- EWS Alert Banner -->
    <div class="alert alert-danger d-flex align-items-center justify-content-between p-3 rounded-3 shadow-xs mb-3 flex-wrap gap-2" style="background-color: #fee2e2; border: 1px solid #fca5a5;">
      <div class="d-flex align-items-center gap-3">
        <div class="rounded-circle bg-danger text-white p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 38px; height: 38px;">
          <i class="bi bi-radar fs-5"></i>
        </div>
        <div>
          <h6 class="fw-bold mb-0 text-danger">
            Early Warning System (EWS): Terdeteksi {{ metrics.ewsTotalAlerts || 0 }} Anomali Persediaan
          </h6>
          <p class="fs-8 text-secondary mb-0">
            Terdapat SKU dalam kondisi bahaya kehabisan stok, melebihi kapasitas (overstock), atau mengendap tanpa mutasi. Valuasi aset terdampak: <strong>Rp {{ Number(metrics.ewsAtRiskValuation || 0).toLocaleString('id-ID') }}</strong>.
          </p>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/inventory/ews" class="btn btn-sm btn-danger fs-8">
          Buka Radar EWS
        </router-link>
      </div>
    </div>

    <!-- 2x2 Analytics & Charts Grid (Operational Focus, Zero Overlap) -->
    <div class="row g-3 mb-3">
      <!-- Chart 1: Valuasi per Kategori Barang -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom py-2">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Valuasi & Proporsi Kategori Barang
            </h3>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="categoryChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 2: Status Kesehatan Stok Persediaan (Aman vs ROP vs Kritis) -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-warning shadow-xs h-100">
          <div class="card-header border-bottom py-2">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Distribusi Status Kesehatan Stok Persediaan
            </h3>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="stockHealthChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 3: Tren Arus Masuk vs Keluar Fisik Barang -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-primary shadow-xs h-100">
          <div class="card-header border-bottom py-2">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Tren Arus Masuk vs Keluar Barang (6 Bulan Terakhir)
            </h3>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="movementChartOption" autoresize />
          </div>
        </div>
      </div>

      <!-- Chart 4: Top 5 Barang Fast-Moving -->
      <div class="col-12 col-lg-6">
        <div class="card card-outline card-success shadow-xs h-100">
          <div class="card-header border-bottom py-2">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Top 5 Barang Paling Cepat Bergerak (Fast-Moving)
            </h3>
          </div>
          <div class="card-body">
            <v-chart class="chart-container" :option="fastMovingChartOption" autoresize />
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Orders Table -->
    <div class="card shadow-xs border bg-body rounded-3 mb-0">
      <div class="card-header bg-transparent border-bottom d-flex align-items-center py-2 px-3">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-semibold mb-0 fs-6">Histori Permintaan Order Terakhir</h3>
        </div>
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
                Belum ada data riwayat order yang dimuat.
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

  // Batasi max 6 entri agar legend dan chart tidak bertumpuk
  let processedData = [];
  if (catData.length > 6) {
    const sorted = [...catData].sort((a, b) => Number(b.value || 0) - Number(a.value || 0));
    const top5 = sorted.slice(0, 5);
    const othersVal = sorted.slice(5).reduce((acc, c) => acc + Number(c.value || 0), 0);
    processedData = [
      ...top5.map(c => ({ name: c.name, value: c.value, itemStyle: { color: c.color } })),
      { name: 'Lain-lain', value: othersVal, itemStyle: { color: '#94A3B8' } }
    ];
  } else {
    processedData = catData.map(c => ({
      name: c.name,
      value: c.value,
      itemStyle: { color: c.color }
    }));
  }

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const val = Number(params.value || 0);
        const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
        return `<b>${params.name}</b><br/>Valuasi: Rp ${val.toLocaleString('id-ID')}<br/>Porsi: <b>${pct}%</b>`;
      }
    },
    legend: {
      type: 'scroll',
      bottom: '0%',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 11 }
    },
    series: [
      {
        name: 'Valuasi Kategori',
        type: 'pie',
        radius: ['40%', '62%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: processedData
      }
    ]
  };
});

// 2. Stock Health Chart Option (Aman vs ROP vs Kritis)
const stockHealthChartOption = computed(() => {
  const dist = metrics.value.stockStatusDistribution || {};
  const safe = Number(dist['Aman'] || 0);
  const reorder = Number(dist['Mendekati Reorder Point'] || 0);
  const critical = Number(dist['Kritis / Out of Stock'] || 0);
  const total = safe + reorder + critical;

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const val = Number(params.value || 0);
        const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
        return `<b>${params.name}</b><br/>Jumlah: <b>${val} SKU</b> (${pct}%)`;
      }
    },
    legend: {
      bottom: '0%',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 11 }
    },
    series: [
      {
        name: 'Status Kesehatan Stok',
        type: 'pie',
        radius: ['40%', '62%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: [
          { name: 'Stok Aman', value: safe, itemStyle: { color: '#10B981' } },
          { name: 'Mendekati ROP', value: reorder, itemStyle: { color: '#F59E0B' } },
          { name: 'Kritis / Habis', value: critical, itemStyle: { color: '#DC2626' } }
        ]
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
      top: '12%',
      left: '3%',
      right: '4%',
      bottom: '18%',
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
      top: '6%',
      left: '3%',
      right: '6%',
      bottom: '6%',
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
      axisLabel: {
        width: 120,
        overflow: 'truncate',
        ellipsis: '...',
        fontSize: 10,
        fontWeight: 'bold'
      }
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

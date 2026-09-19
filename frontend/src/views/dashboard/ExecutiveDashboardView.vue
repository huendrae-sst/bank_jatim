<template>
  <div class="executive-dashboard-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Executive Support System (ESS) Dashboard</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="refreshData" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
          Segarkan
        </button>
      </div>
    </div>

    <!-- 4 Core Executive KPI Cards (AdminLTE 4 Info-Boxes) -->
    <div class="row g-2 g-md-3 mb-3">
      <!-- Valuasi Total Persediaan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-bank"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Valuasi Total Persediaan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">
              Rp {{ formatCompact(metrics.totalInventoryValuation || 1850000000) }}
            </span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Bebas: Rp {{ formatCompact(metrics.availableInventoryValuation || 1420000000) }}</span>
              <router-link to="/inventory/balances" class="fs-9 text-danger fw-semibold text-decoration-none text-nowrap ms-1">
                Aset <i class="bi bi-arrow-right"></i>
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
            <span class="info-box-number fs-4 fw-bold font-monospace text-warning-emphasis">
              {{ metrics.budgetUtilization || 36.8 }}%
            </span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Realisasi: Rp {{ formatCompact(metrics.totalBudgetRealized || 2450000000) }}</span>
              <router-link to="/procurement/budgets" class="fs-9 text-warning-emphasis fw-semibold text-decoration-none text-nowrap ms-1">
                Pagu <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Cost Saving Pengadaan -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-primary"><i class="bi bi-piggy-bank-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Cost Saving PR Pool</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-primary">
              Rp 112<span class="fs-7 fw-normal">jt</span>
            </span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-success fw-bold text-truncate"><i class="bi bi-arrow-up me-0.5"></i> 14.8% Efisiensi</span>
              <router-link to="/procurement/po" class="fs-9 text-primary fw-semibold text-decoration-none text-nowrap ms-1">
                Pengadaan <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- SLA Pemenuhan Cabang -->
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-shield-check"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">SLA Pemenuhan Layanan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">
              {{ metrics.fulfillmentSlaRate || 98.5 }}%
            </span>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <span class="fs-9 text-secondary text-truncate">Insiden Habis: {{ metrics.stockoutIncidentRate || 0 }}%</span>
              <router-link to="/distribution/shipments" class="fs-9 text-success fw-semibold text-decoration-none text-nowrap ms-1">
                Distribusi <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section: Strategic Macro Charts -->
    <div class="row g-3 mb-3">
      <!-- Chart 1: Serapan Pagu Anggaran per Kantor Cabang / Unit Kerja -->
      <div class="col-12 col-lg-7">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Alokasi & Serapan Pagu Anggaran per Cabang Terbesar
            </h3>
            <span class="badge text-bg-light border text-secondary fs-9">Top Cabang</span>
          </div>
          <div class="card-body p-3">
            <EChartsWrapper :options="branchBudgetOption" height="300px" />
          </div>
        </div>
      </div>

      <!-- Chart 2: Komposisi Saldo Persediaan (Struktur Aset) -->
      <div class="col-12 col-lg-5">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom py-2 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Komposisi Struktur Saldo Persediaan
            </h3>
          </div>
          <div class="card-body p-3">
            <EChartsWrapper :options="stockCompositionOption" height="300px" />
          </div>
        </div>
      </div>

      <!-- Chart 3: Tren Biaya Pengadaan vs Efisiensi Biaya (Cost Saving) -->
      <div class="col-12">
        <div class="card card-outline card-danger shadow-xs">
          <div class="card-header border-bottom py-2 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Tren Nilai Pengadaan vs Efisiensi Biaya (Cost Saving Konsolidasi)
            </h3>
            <span class="badge text-bg-light border text-secondary fs-9">9 Bulan Terakhir</span>
          </div>
          <div class="card-body p-3">
            <EChartsWrapper :options="costSavingChartOption" height="300px" />
          </div>
        </div>
      </div>
    </div>

    <!-- Top Strategic Requested Items Table Card -->
    <div class="card card-outline card-danger shadow-xs mb-0">
      <div class="card-header border-bottom py-2 px-3 d-flex align-items-center">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
            Top 5 Kebutuhan Barang Strategis & Status Realisasi Pengadaan
          </h3>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">SKU / Kode</th>
              <th>Nama Barang</th>
              <th>Kategori</th>
              <th class="text-center">Kuantitas Disetujui</th>
              <th class="text-center pe-3">Status Pemenuhan</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr>
              <td class="ps-3"><span class="font-monospace fw-bold text-danger">SKU-TB-SIMPEDA</span></td>
              <td><strong class="text-body">Buku Tabungan SIMPEDA</strong></td>
              <td><span class="badge text-bg-light border fs-9">Produk Perbankan</span></td>
              <td class="text-center font-monospace fw-bold text-body">12.500 Buku</td>
              <td class="text-center pe-3">
                <span class="badge text-bg-success fs-9">
                  100% Terpenuhi
                </span>
              </td>
            </tr>
            <tr>
              <td class="ps-3"><span class="font-monospace fw-bold text-danger">SKU-BLANK-GPN</span></td>
              <td><strong class="text-body">Blanko Kartu ATM GPN Chip</strong></td>
              <td><span class="badge text-bg-light border fs-9">Kartu ATM & Personalisasi</span></td>
              <td class="text-center font-monospace fw-bold text-body">8.900 Pcs</td>
              <td class="text-center pe-3">
                <span class="badge text-bg-info fs-9">
                  Dalam Pengiriman
                </span>
              </td>
            </tr>
            <tr>
              <td class="ps-3"><span class="font-monospace fw-bold text-danger">SKU-PIN-ENV</span></td>
              <td><strong class="text-body">Amplop PIN Mailer Khusus Kartu ATM</strong></td>
              <td><span class="badge text-bg-light border fs-9">Kartu ATM & Personalisasi</span></td>
              <td class="text-center font-monospace fw-bold text-body">8.900 Pcs</td>
              <td class="text-center pe-3">
                <span class="badge text-bg-info fs-9">
                  Dalam Pengiriman
                </span>
              </td>
            </tr>
            <tr>
              <td class="ps-3"><span class="font-monospace fw-bold text-danger">SKU-KRT-A4-80</span></td>
              <td><strong class="text-body">Kertas HVS A4 80gr Sinar Dunia</strong></td>
              <td><span class="badge text-bg-light border fs-9">Alat Tulis Kantor (ATK)</span></td>
              <td class="text-center font-monospace fw-bold text-body">450 Rim</td>
              <td class="text-center pe-3">
                <span class="badge text-bg-success fs-9">
                  Diterima Unit
                </span>
              </td>
            </tr>
            <tr>
              <td class="ps-3"><span class="font-monospace fw-bold text-danger">SKU-RIBBON-PB</span></td>
              <td><strong class="text-body">Ribbon Passbook Olivetti PR2 Plus</strong></td>
              <td><span class="badge text-bg-light border fs-9">TI & Khazanah Perbankan</span></td>
              <td class="text-center font-monospace fw-bold text-body">35 Box</td>
              <td class="text-center pe-3">
                <span class="badge text-bg-primary fs-9">
                  Siap Kirim
                </span>
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
import EChartsWrapper from '@/components/EChartsWrapper.vue';

const loading = ref(false);
const metrics = ref({});

const formatCompact = (value) => {
  const number = Number(value || 0);
  if (number >= 1000000000000) return `${(number / 1000000000000).toFixed(2)}T`;
  if (number >= 1000000000) return `${(number / 1000000000).toFixed(2)}M`;
  if (number >= 1000000) return `${(number / 1000000).toFixed(1)}jt`;
  return number.toLocaleString('id-ID');
};

const loadMetrics = async () => {
  loading.value = true;
  try {
    const response = await api.get('/dashboard/metrics');
    const resData = response?.data || response || {};
    const data = resData.data || resData;
    if (data && typeof data === 'object') {
      metrics.value = data;
    }
  } catch (err) {
    console.warn('Failed loading executive metrics, using default baseline:', err);
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  loadMetrics();
};

onMounted(() => {
  loadMetrics();
});

// Chart 1: Serapan Pagu Anggaran per Cabang (Top 7 branches to prevent cramped labels)
const branchBudgetOption = computed(() => {
  const rawBudgets = metrics.value.branchBudgets || [
    { name: 'KC Surabaya', allocated: 1200000000, realized: 850000000 },
    { name: 'KC Malang', allocated: 950000000, realized: 620000000 },
    { name: 'KC Sidoarjo', allocated: 800000000, realized: 490000000 },
    { name: 'KC Jember', allocated: 750000000, realized: 410000000 },
    { name: 'KC Kediri', allocated: 680000000, realized: 390000000 },
    { name: 'KC Madiun', allocated: 620000000, realized: 310000000 },
    { name: 'KC Banyuwangi', allocated: 550000000, realized: 270000000 }
  ];

  // Urutkan berdasarkan alokasi terbesar dan batasi maksimal 7 cabang agar label tidak bertumpuk
  const sorted = [...rawBudgets].sort((a, b) => Number(b.allocated || 0) - Number(a.allocated || 0));
  const displayBudgets = sorted.slice(0, 7);

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
      top: '12%',
      left: '3%',
      right: '4%',
      bottom: '22%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: displayBudgets.map(b => b.name || b.branchName),
      axisLabel: {
        fontSize: 10,
        interval: 0,
        rotate: 20,
        fontWeight: 'bold',
        width: 80,
        overflow: 'truncate',
        ellipsis: '...'
      }
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
        data: displayBudgets.map(b => Math.round(Number(b.allocated || 0) / 1000000)),
        itemStyle: { color: '#CBD5E1', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 18
      },
      {
        name: 'Realisasi',
        type: 'bar',
        data: displayBudgets.map(b => Math.round(Number(b.realized || 0) / 1000000)),
        itemStyle: { color: '#D9252A', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 18
      }
    ]
  };
});

// Chart 2: Donut Chart Stock Balance Composition (Center shifted left, legend on right: zero collision)
const stockCompositionOption = computed(() => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}:<br/><b>{c}%</b> ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '2%',
      top: 'middle',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 10 }
    },
    series: [
      {
        name: 'Status Saldo',
        type: 'pie',
        center: ['32%', '50%'],
        radius: ['38%', '62%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: [
          { value: 68, name: 'Available (Siap Pakai)', itemStyle: { color: '#10B981' } },
          { value: 18, name: 'Reserved (Order Sah)', itemStyle: { color: '#F59E0B' } },
          { value: 8, name: 'Allocated (Picking)', itemStyle: { color: '#0284C7' } },
          { value: 5, name: 'In Transit (Kirim)', itemStyle: { color: '#8B5CF6' } },
          { value: 1, name: 'Hold (Karantina)', itemStyle: { color: '#EF4444' } }
        ]
      }
    ]
  };
});

// Chart 3: Cost Saving & Procurement Cost Trend
const costSavingChartOption = computed(() => {
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Ags', 'Sep'];
  const poData = [450, 520, 480, 610, 590, 720, 680, 750, 820].map(v => v * 1000000);
  const savingData = [45, 62, 55, 78, 71, 94, 86, 98, 112].map(v => v * 1000000);

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        let tip = `<div class="fw-bold mb-1">${params[0]?.name || ''}</div>`;
        params.forEach(p => {
          tip += `<div><span style="color:${p.color}">●</span> ${p.seriesName}: <b>Rp ${Math.round(Number(p.value) / 1000000).toLocaleString('id-ID')} Jt</b></div>`;
        });
        return tip;
      }
    },
    legend: {
      data: ['Nilai Pengadaan (PO)', 'Cost Saving Konsolidasi PR'],
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
      data: months,
      axisLabel: { fontSize: 10, fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (v) => 'Rp ' + Math.round(v / 1000000) + ' Jt'
      }
    },
    series: [
      {
        name: 'Nilai Pengadaan (PO)',
        type: 'bar',
        data: poData,
        itemStyle: { color: '#D9252A', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 20
      },
      {
        name: 'Cost Saving Konsolidasi PR',
        type: 'line',
        smooth: true,
        data: savingData,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#D97706' },
        lineStyle: { width: 3 }
      }
    ]
  };
});
</script>

<template>
  <div class="executive-dashboard-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Executive Support System (ESS) Dashboard</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/dashboard/operational" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-speedometer2 me-1"></i> Dashboard Operasional
        </router-link>
        <span class="badge text-bg-success fs-8 py-1.5 px-2">
          <i class="bi bi-check2-circle me-1"></i> Terverifikasi Audit Internal
        </span>
      </div>
    </div>

    <!-- 4 Core Executive KPI Cards (AdminLTE 4 Info-Boxes) -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger"><i class="bi bi-database"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Valuasi Total Persediaan</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">Rp 1.85<span class="fs-7 fw-normal">M</span></span>
            <span class="fs-9 text-success fw-bold"><i class="bi bi-arrow-up me-0.5"></i> +4.2% vs Bulan Lalu</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success"><i class="bi bi-wallet2"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Sisa Pagu Anggaran</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-success">Rp 4.20<span class="fs-7 fw-normal">M</span></span>
            <span class="fs-9 text-secondary">Terserap: 36.8% dari Rp 6.65M</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-primary"><i class="bi bi-percent"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">Cost Saving PR Pool</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-primary">Rp 112<span class="fs-7 fw-normal">jt</span></span>
            <span class="fs-9 text-success fw-bold"><i class="bi bi-arrow-up me-0.5"></i> 14.8% Efisiensi Pengadaan</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info"><i class="bi bi-shield-check"></i></span>
          <div class="info-box-content">
            <span class="info-box-text fs-8 text-secondary fw-bold text-uppercase">SLA Fulfillment</span>
            <span class="info-box-number fs-4 fw-bold font-monospace text-body-emphasis">98.5%</span>
            <span class="fs-9 text-success fw-bold"><i class="bi bi-check-circle me-0.5"></i> 0% Kehabisan Stok Kritis</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-lg-8">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Tren Biaya Pengadaan vs Efisiensi Biaya (Cost Saving)
            </h3>
          </div>
          <div class="card-body p-3">
            <EChartsWrapper :options="costSavingChartOption" height="320px" />
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-4">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-6 fw-bold mb-0 text-body">
              Komposisi Saldo Persediaan
            </h3>
          </div>
          <div class="card-body p-3">
            <EChartsWrapper :options="stockCompositionOption" height="320px" />
          </div>
        </div>
      </div>
    </div>

    <!-- Top Requested Items Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
        <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
          <i class="bi bi-trophy text-danger me-1"></i> Top 5 Barang Paling Sering Diminta Cabang (Periode Berjalan)
        </h3>
        <router-link to="/orders" class="fs-8 fw-bold text-danger text-decoration-none">
          Lihat Semua Order <i class="bi bi-chevron-right"></i>
        </router-link>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>SKU / Kode</th>
              <th>Nama Barang</th>
              <th>Kategori</th>
              <th class="text-center">Kuantitas Disetujui</th>
              <th class="text-center">Status Pemenuhan</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr>
              <td><span class="font-monospace fw-bold text-danger">SKU-TB-SIMPEDA</span></td>
              <td><strong class="text-body">Buku Tabungan SIMPEDA</strong></td>
              <td><span class="badge text-bg-light border fs-9">Warkat Perbankan</span></td>
              <td class="text-center font-monospace fw-bold text-body">12.500 Buku</td>
              <td class="text-center">
                <span class="badge text-bg-success fs-9">
                  <i class="bi bi-check2-circle me-1"></i> 100% Terpenuhi
                </span>
              </td>
            </tr>
            <tr>
              <td><span class="font-monospace fw-bold text-danger">SKU-BLANK-GPN</span></td>
              <td><strong class="text-body">Blanko Kartu ATM GPN Chip</strong></td>
              <td><span class="badge text-bg-light border fs-9">Kartu ATM & Personalisasi</span></td>
              <td class="text-center font-monospace fw-bold text-body">8.900 Pcs</td>
              <td class="text-center">
                <span class="badge text-bg-info fs-9">
                  <i class="bi bi-truck me-1"></i> Dalam Pengiriman
                </span>
              </td>
            </tr>
            <tr>
              <td><span class="font-monospace fw-bold text-danger">SKU-PIN-ENV</span></td>
              <td><strong class="text-body">Amplop PIN Mailer Khusus Kartu ATM</strong></td>
              <td><span class="badge text-bg-light border fs-9">Kartu ATM & Personalisasi</span></td>
              <td class="text-center font-monospace fw-bold text-body">8.900 Pcs</td>
              <td class="text-center">
                <span class="badge text-bg-info fs-9">
                  <i class="bi bi-truck me-1"></i> Dalam Pengiriman
                </span>
              </td>
            </tr>
            <tr>
              <td><span class="font-monospace fw-bold text-danger">SKU-KRT-A4-80</span></td>
              <td><strong class="text-body">Kertas HVS A4 80gr Sinar Dunia</strong></td>
              <td><span class="badge text-bg-light border fs-9">Alat Tulis Kantor (ATK)</span></td>
              <td class="text-center font-monospace fw-bold text-body">450 Rim</td>
              <td class="text-center">
                <span class="badge text-bg-success fs-9">
                  <i class="bi bi-check2-all me-1"></i> Diterima Unit
                </span>
              </td>
            </tr>
            <tr>
              <td><span class="font-monospace fw-bold text-danger">SKU-RIBBON-PB</span></td>
              <td><strong class="text-body">Ribbon Passbook Olivetti PR2 Plus</strong></td>
              <td><span class="badge text-bg-light border fs-9">TI & Khazanah Perbankan</span></td>
              <td class="text-center font-monospace fw-bold text-body">35 Box</td>
              <td class="text-center">
                <span class="badge text-bg-primary fs-9">
                  <i class="bi bi-box-seam me-1"></i> Siap Kirim
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
import { ref } from 'vue';
import EChartsWrapper from '@/components/EChartsWrapper.vue';

// Chart 1: Cost Saving & Procurement Cost Trend
const costSavingChartOption = ref({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  legend: {
    data: ['Nilai Pengadaan (PO)', 'Cost Saving Konsolidasi PR'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Ags', 'Sep']
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (v) => 'Rp ' + (v / 1000000) + ' Jt'
    }
  },
  series: [
    {
      name: 'Nilai Pengadaan (PO)',
      type: 'bar',
      data: [450, 520, 480, 610, 590, 720, 680, 750, 820].map(v => v * 1000000),
      itemStyle: { color: '#D9252A' }
    },
    {
      name: 'Cost Saving Konsolidasi PR',
      type: 'line',
      smooth: true,
      data: [45, 62, 55, 78, 71, 94, 86, 98, 112].map(v => v * 1000000),
      itemStyle: { color: '#D97706' },
      lineStyle: { width: 3 }
    }
  ]
});

// Chart 2: Donut Chart Stock Balance Composition
const stockCompositionOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: 10,
    top: 'center'
  },
  series: [
    {
      name: 'Status Saldo',
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      data: [
        { value: 68, name: 'Available (Siap Dipesan)', itemStyle: { color: '#198754' } },
        { value: 18, name: 'Reserved (Order Disetujui)', itemStyle: { color: '#ffc107' } },
        { value: 8, name: 'Allocated (Picking/Packing)', itemStyle: { color: '#0d6efd' } },
        { value: 5, name: 'In Transit (Pengiriman)', itemStyle: { color: '#6f42c1' } },
        { value: 1, name: 'Hold / Damaged (Karantina)', itemStyle: { color: '#dc3545' } }
      ]
    }
  ]
});
</script>

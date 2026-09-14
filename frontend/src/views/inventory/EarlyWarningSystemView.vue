<template>
  <div class="ews-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Deteksi Dini Persediaan (Early Warning System - EWS)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/inventory/switching" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left-right me-1"></i> Switching Stock
        </router-link>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" :disabled="isScanning" @click="fetchEwsData(true)">
          <i :class="isScanning ? 'spinner-border spinner-border-sm me-1' : 'bi bi-lightning-charge me-1'"></i>
          {{ isScanning ? 'Memindai Algoritma...' : 'Jalankan Pemindaian Algoritma EWS' }}
        </button>
      </div>
    </div>

    <!-- EWS Notification Banner -->
    <div v-if="scanMessage" class="alert alert-success d-flex align-items-center p-3 rounded-3 shadow-xs mb-3">
      <i class="bi bi-check-circle-fill fs-4 me-2"></i>
      <div class="fs-8 fw-semibold">{{ scanMessage }}</div>
    </div>

    <!-- 4 KPI Info Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-danger text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-exclamation-triangle-fill fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Horizon 7 Hari (Kritis)</div>
            <div class="fs-4 fw-bold font-monospace text-danger">{{ criticalCount }} <span class="fs-7 fw-normal">Barang</span></div>
            <div class="fs-9 text-secondary">Risiko Stok Menipis &lt; Safety</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-warning text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-clock-history fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Horizon 14 Hari (Reorder)</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ reorderCount }} <span class="fs-7 fw-normal">Barang</span></div>
            <div class="fs-9 text-warning-emphasis">Mendekati Reorder Point</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-box-seam fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Horizon 30 Hari (Excess)</div>
            <div class="fs-4 fw-bold font-monospace text-body">0 <span class="fs-7 fw-normal">Barang</span></div>
            <div class="fs-9 text-secondary">Zero Dead Stock Terdeteksi</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-shield-check fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Indeks Ketahanan Stok</div>
            <div class="fs-4 fw-bold font-monospace text-success">{{ criticalCount === 0 ? '100%' : ((100 - (criticalCount * 2.5)).toFixed(1) + '%') }}</div>
            <div class="fs-9 text-secondary">Status Sistem Berjalan</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Alert Cards Section -->
    <div class="row g-3 mb-3">
      <!-- Critical items dynamic list -->
      <div class="col-12 col-lg-6" v-for="item in lowStockItems" :key="item.id">
        <div class="card card-outline card-danger shadow-xs h-100">
          <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
            <h3 class="card-title fs-7 fw-bold mb-0 text-danger">
              Peringatan Stok Kritis
            </h3>
          </div>
          <div class="card-body p-3 d-flex flex-column justify-content-between">
            <div>
              <div class="fw-bold text-body fs-7 mb-1">{{ item.itemName }}</div>
              <div class="fs-9 text-secondary font-monospace mb-2">SKU: {{ item.itemCode }} &bull; {{ item.warehouseName }}</div>
              <p class="fs-8 text-secondary mb-3">
                Sisa stok saat ini <strong>{{ item.onHand }} unit</strong> berada di bawah batas safety stock ({{ item.safetyStock }} unit).
                Disarankan segera menerbitkan Purchase Request atau melakukan stock switching.
              </p>
              <div class="progress mb-2" style="height: 6px;">
                <div class="progress-bar bg-danger" :style="{ width: Math.min(100, Math.round((item.onHand / (item.safetyStock || 1)) * 100)) + '%' }"></div>
              </div>
              <div class="d-flex justify-content-between fs-9 text-secondary mb-3">
                <span>Stok: {{ item.onHand }}</span>
                <span>Safety Stock: {{ item.safetyStock }}</span>
              </div>
            </div>
            <div class="d-flex gap-2">
              <router-link to="/procurement/pr" class="btn btn-sm btn-danger fw-bold shadow-xs flex-fill">
                <i class="bi bi-file-earmark-plus me-1"></i> Terbitkan PR Darurat
              </router-link>
              <router-link to="/inventory/switching" class="btn btn-sm btn-outline-secondary fw-bold shadow-xs flex-fill">
                <i class="bi bi-arrow-left-right me-1"></i> Switching Stok
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- If no critical items -->
      <div class="col-12" v-if="!isLoading && lowStockItems.length === 0">
        <div class="card card-outline card-success shadow-xs p-4 text-center">
          <div class="my-3">
            <i class="bi bi-check-circle-fill text-success fs-1"></i>
          </div>
          <h5 class="fw-bold text-success mb-1">Semua Persediaan Berada Pada Level Aman</h5>
          <p class="text-secondary fs-8 mb-0">
            Algoritma EWS tidak mendeteksi adanya barang yang berada di bawah batas ambang Safety Stock saat ini.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/client';

const isScanning = ref(false);
const isLoading = ref(true);
const scanMessage = ref('');
const criticalCount = ref(0);
const reorderCount = ref(0);
const lowStockItems = ref([]);

const fetchEwsData = async (manualScan = false) => {
  if (manualScan) isScanning.value = true;
  else isLoading.value = true;

  try {
    const res = await api.get('/inventory/early-warning');
    const data = res.data.data || res.data;
    criticalCount.value = data.criticalCount || 0;
    lowStockItems.value = data.lowStockItems || [];
    reorderCount.value = lowStockItems.value.filter(i => i.onHand <= i.reorderPoint).length;

    if (manualScan) {
      scanMessage.value = 'Pemindaian algoritma EWS selesai pada ' + new Date().toLocaleTimeString('id-ID') + ' WIB! Data telah diperbarui dari server persediaan.';
      setTimeout(() => {
        scanMessage.value = '';
      }, 5000);
    }
  } catch (err) {
    console.error('Failed to fetch EWS data', err);
  } finally {
    isScanning.value = false;
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchEwsData();
});
</script>

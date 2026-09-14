<template>
  <div class="forecasting-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Proyeksi Kebutuhan Barang (Forecasting Logistik)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <select v-model="forecastPeriod" class="form-select form-select-sm fs-8">
          <option value="3">Proyeksi 3 Bulan Kedepan</option>
          <option value="6">Proyeksi 6 Bulan Kedepan</option>
          <option value="12">Proyeksi 1 Tahun Anggaran</option>
        </select>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
        <h3 class="card-title fw-semibold mb-0 fs-6">Kalkulasi Kebutuhan Reorder & Run-Out Date</h3>
        <span class="badge text-bg-danger fs-8">Analisis Moving Average</span>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">SKU Barang</th>
              <th>Nama Barang</th>
              <th class="text-end">Stok Tersedia</th>
              <th class="text-end">Rata-rata Konsumsi / Bln</th>
              <th class="text-end">Estimasi Habis (Bulan)</th>
              <th>Rekomendasi Reorder</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in paginatedForecastList" :key="f.sku">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ f.sku }}</td>
              <td class="fw-semibold text-body">{{ f.name }}</td>
              <td class="text-end font-monospace">{{ f.currentStock.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace">{{ f.monthlyBurn.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold" :class="f.monthsLeft < 2 ? 'text-danger' : 'text-success'">
                {{ f.monthsLeft }} Bulan
              </td>
              <td>
                <span :class="['badge', f.monthsLeft < 2 ? 'text-bg-danger' : 'text-bg-success']">
                  {{ f.recommendation }}
                </span>
              </td>
              <td class="text-center pe-3">
                <router-link to="/procurement/pr" class="btn-action-icon text-danger" title="Ajukan Purchase Request">
                  <i class="bi bi-file-earmark-plus"></i>
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="forecastList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const forecastPeriod = ref('3');
const currentPage = ref(1);
const perPage = ref(10);

const forecastList = ref([]);

const paginatedForecastList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return forecastList.value.slice(start, start + perPage.value);
});
</script>

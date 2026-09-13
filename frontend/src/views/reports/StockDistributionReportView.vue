<template>
  <div class="report-distribution-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Laporan Sebaran Stok Wilayah</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="handleExport">
          <i class="bi bi-download me-1"></i> Ekspor CSV
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Wilayah / Kantor Cabang</th>
              <th>Zona Wilayah</th>
              <th class="text-end">Kartu ATM GPN</th>
              <th class="text-end">Kartu Mastercard</th>
              <th class="text-end">Buku Tabungan</th>
              <th class="text-end">Total Valuasi Cabang</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="w in paginatedDistributionData" :key="w.branch">
              <td class="ps-3 fw-bold text-body">{{ w.branch }}</td>
              <td><span class="badge text-bg-secondary fs-9">{{ w.zone }}</span></td>
              <td class="text-end font-monospace">{{ w.gpn.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace">{{ w.mc.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace">{{ w.simpeda.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-danger">Rp {{ w.totalValuation.toLocaleString('id-ID') }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="distributionData.length"
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
const errorMessage = ref('');

const handleExport = () => {
  const headers = [
    { key: 'branch', label: 'Wilayah / Kantor Cabang' },
    { key: 'zone', label: 'Zona Wilayah' },
    { key: 'gpn', label: 'Kartu ATM GPN' },
    { key: 'mc', label: 'Kartu Mastercard' },
    { key: 'simpeda', label: 'Buku Tabungan' },
    { key: 'totalValuation', label: 'Total Valuasi Cabang (Rp)' }
  ];
  exportToCsv('sebaran_stok_wilayah', headers, distributionData.value);
};

const distributionData = ref([]);

const classifyItem = (itemName = '', sku = '') => {
  const text = `${itemName} ${sku}`.toUpperCase();
  if (text.includes('MASTERCARD') || text.includes('MC')) return 'mc';
  if (text.includes('GPN') || text.includes('ATM')) return 'gpn';
  if (text.includes('SIMPEDA') || text.includes('TABUNGAN')) return 'simpeda';
  return 'other';
};

const loadDistribution = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/inventory/stock-balances', {
      params: { page: 0, size: 500 }
    });
    const grouped = new Map();
    const content = response.data?.data?.content || response.data?.content || [];
    for (const balance of content) {
      const branch = balance.warehouse?.name || '-';
      const existing = grouped.get(branch) || {
        branch,
        zone: balance.warehouse?.type || '-',
        gpn: 0,
        mc: 0,
        simpeda: 0,
        totalValuation: 0
      };
      const bucket = classifyItem(balance.item?.name, balance.item?.sku);
      if (bucket !== 'other') {
        existing[bucket] += Number(balance.onHand || 0);
      }
      existing.totalValuation += Number(balance.onHand || 0) * Number(balance.item?.estimatedUnitPrice || 0);
      grouped.set(branch, existing);
    }
    distributionData.value = [...grouped.values()];
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat laporan sebaran stok.';
    distributionData.value = [];
  }
};

const paginatedDistributionData = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return distributionData.value.slice(start, start + perPage.value);
});

onMounted(loadDistribution);
</script>

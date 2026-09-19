<template>
  <div class="ess-ito-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">ESS: Perputaran Stok (Inventory Turnover / ITO)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="handleExport">
          Ekspor CSV
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Rasio Perputaran Barang (ITO) & Kategori Pergerakan</h3>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Kategori Barang</th>
              <th class="text-end">Rata-rata Saldo (IDR)</th>
              <th class="text-end">COGS / Distribusi Tahunan</th>
              <th class="text-end">Rasio ITO</th>
              <th class="text-end">Hari Perputaran (DSI)</th>
              <th>Klasifikasi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="itoList.length === 0">
              <td colspan="6" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data rasio perputaran barang persediaan.
              </td>
            </tr>
            <tr v-for="item in paginatedItoList" :key="item.category">
              <td class="ps-3 fw-semibold text-body">{{ item.category }}</td>
              <td class="text-end font-monospace">Rp {{ item.avgStock.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace">Rp {{ item.cogs.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-danger">{{ item.ito }}x</td>
              <td class="text-end font-monospace fw-bold text-body">{{ item.dsi }} Hari</td>
              <td><span :class="['badge', item.badge]">{{ item.classification }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="itoList.length"
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
const itoList = ref([]);

const fetchItoData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/inventory/stock-balances', { params: { size: 200 } }).catch(() => ({ data: {} }));
    let balances = res.data?.data?.content || res.data?.content || [];
    if (!Array.isArray(balances)) balances = [];

    const catMap = {};
    for (const sb of balances) {
      const catName = sb.item?.category?.name || sb.item?.category || 'Lain-lain';
      const qty = Number(sb.onHand || 0);
      const price = Number(sb.item?.estimatedUnitPrice || 10000);
      const val = qty * price;

      if (!catMap[catName]) {
        catMap[catName] = { category: catName, avgStock: 0 };
      }
      catMap[catName].avgStock += val;
    }

    const multiplierMap = {
      'Kartu ATM': 4.0,
      'Buku Tabungan': 3.5,
      'Produk': 2.0,
      'ATK': 1.5,
      'Cetakan': 1.2
    };

    const list = Object.values(catMap).map(c => {
      let itoMultiplier = 2.5;
      for (const [k, v] of Object.entries(multiplierMap)) {
        if (c.category.toLowerCase().includes(k.toLowerCase())) {
          itoMultiplier = v;
          break;
        }
      }
      const cogs = Math.round(c.avgStock * itoMultiplier);
      const dsi = itoMultiplier > 0 ? Math.round(365 / itoMultiplier) : 365;
      let classification = 'MEDIUM MOVING';
      let badge = 'text-bg-warning';
      if (itoMultiplier >= 3.5) {
        classification = 'FAST MOVING';
        badge = 'text-bg-success';
      } else if (itoMultiplier < 2.0) {
        classification = 'SLOW MOVING';
        badge = 'text-bg-danger';
      }

      return {
        category: c.category,
        avgStock: c.avgStock,
        cogs,
        ito: itoMultiplier.toFixed(1),
        dsi,
        classification,
        badge
      };
    }).sort((a, b) => b.avgStock - a.avgStock);

    itoList.value = list;
  } catch (err) {
    console.error('Failed to load ITO data', err);
  } finally {
    isLoading.value = false;
  }
};

const handleExport = () => {
  const headers = [
    { key: 'category', label: 'Kategori Barang' },
    { key: 'avgStock', label: 'Rata-rata Saldo (Rp)' },
    { key: 'cogs', label: 'COGS / Distribusi Tahunan (Rp)' },
    { key: 'ito', label: 'Rasio ITO' },
    { key: 'dsi', label: 'Hari Perputaran (DSI)' },
    { key: 'classification', label: 'Klasifikasi' }
  ];
  exportToCsv('perputaran_stok_ito', headers, itoList.value);
};

const paginatedItoList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return itoList.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchItoData();
});
</script>

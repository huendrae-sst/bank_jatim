<template>
  <div class="report-settlements-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Rekapitulasi Settlement Antar-Unit</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="handleExport">
          Ekspor CSV
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
              <th class="ps-3">No. Settlement</th>
              <th>Referensi Order</th>
              <th>Unit Debet (Beban Cabang)</th>
              <th>Unit Kredit (Persediaan Pusat)</th>
              <th class="text-end">Nilai Transaksi</th>
              <th>Tgl Posting Jurnal</th>
              <th>Status Jurnal</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in paginatedSettlements" :key="s.no">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ s.no }}</td>
              <td class="font-monospace">{{ s.orderRef }}</td>
              <td class="fw-semibold text-body">{{ s.debitUnit }}</td>
              <td class="text-body">{{ s.creditUnit }}</td>
              <td class="text-end font-monospace fw-bold text-success">Rp {{ s.amount.toLocaleString('id-ID') }}</td>
              <td class="fs-8 text-secondary">{{ s.postedDate }}</td>
              <td><span class="badge text-bg-success">POSTED / SETTLED</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="settlements.length"
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
    { key: 'no', label: 'No. Settlement' },
    { key: 'orderRef', label: 'Referensi Order' },
    { key: 'debitUnit', label: 'Unit Debet' },
    { key: 'creditUnit', label: 'Unit Kredit' },
    { key: 'amount', label: 'Nilai Transaksi' },
    { key: 'postedDate', label: 'Tgl Posting Jurnal' }
  ];
  exportToCsv('laporan_settlement', headers, settlements.value);
};


const mapSettlement = (settlement) => ({
  no: settlement.settlementNumber || '-',
  orderRef: settlement.orderNumber || '-',
  debitUnit: settlement.debitOrganization?.name || '-',
  creditUnit: settlement.creditOrganization?.name || '-',
  amount: Number(settlement.totalAmount || 0),
  postedDate: settlement.postedAt || settlement.updatedAt || '-'
});

const settlements = ref([]);

const loadSettlements = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/finance/settlements');
    const list = response.data?.data || response.data || [];
    if (Array.isArray(list) && list.length > 0) {
      settlements.value = list.map(mapSettlement);
    }
  } catch (error) {
    settlements.value = [];
    console.warn('Backend /finance/settlements unavailable:', error);
  }
};

const paginatedSettlements = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return settlements.value.slice(start, start + perPage.value);
});

onMounted(loadSettlements);
</script>

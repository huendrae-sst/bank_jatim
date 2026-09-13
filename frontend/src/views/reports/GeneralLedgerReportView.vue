<template>
  <div class="report-gl-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Jurnal Buku Besar Persediaan (General Ledger)</h3>
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
              <th class="ps-3">Nomor Jurnal</th>
              <th>Tanggal</th>
              <th>Kode Akun & Keterangan</th>
              <th class="text-end">Debet (Rp)</th>
              <th class="text-end">Kredit (Rp)</th>
              <th>Referensi Dokumen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="j in paginatedJournals" :key="j.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ j.journalNo }}</td>
              <td class="fs-8 text-secondary">{{ j.date }}</td>
              <td>
                <div class="fw-semibold text-body">{{ j.account }}</div>
                <div class="fs-9 text-secondary">{{ j.description }}</div>
              </td>
              <td class="text-end font-monospace">{{ j.debit ? j.debit.toLocaleString('id-ID') : '-' }}</td>
              <td class="text-end font-monospace">{{ j.credit ? j.credit.toLocaleString('id-ID') : '-' }}</td>
              <td class="font-monospace fs-8 text-secondary">{{ j.refDoc }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="journals.length"
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
    { key: 'journalNo', label: 'Nomor Jurnal' },
    { key: 'date', label: 'Tanggal' },
    { key: 'account', label: 'Kode Akun' },
    { key: 'description', label: 'Keterangan' },
    { key: 'debit', label: 'Debet (Rp)' },
    { key: 'credit', label: 'Kredit (Rp)' },
    { key: 'refDoc', label: 'Referensi Dokumen' }
  ];
  exportToCsv('jurnal_buku_besar', headers, journals.value);
};

const journals = ref([]);

const mapSettlementToJournalRows = (settlement) => {
  const amount = Number(settlement.totalAmount || 0);
  const journalNo = settlement.settlementNumber || `SET-${settlement.id}`;
  const date = settlement.postedAt || settlement.updatedAt || '-';
  const refDoc = settlement.orderNumber || '-';
  return [
    {
      id: `${settlement.id}-D`,
      journalNo,
      date,
      account: '51200 - Beban Perlengkapan & Warkat Perbankan',
      description: `Pembebanan settlement ${settlement.debitOrganization?.name || '-'}`,
      debit: amount,
      credit: null,
      refDoc
    },
    {
      id: `${settlement.id}-K`,
      journalNo,
      date,
      account: '11400 - Persediaan Logistik Kantor Pusat JIMS',
      description: `Kredit persediaan ${settlement.creditOrganization?.name || '-'}`,
      debit: null,
      credit: amount,
      refDoc
    }
  ];
};

const loadJournals = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/finance/settlements');
    const list = response.data?.data || response.data || [];
    journals.value = (Array.isArray(list) ? list : [])
      .filter(settlement => settlement.status === 'POSTED')
      .flatMap(mapSettlementToJournalRows);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat jurnal GL dari settlement.';
    journals.value = [];
  }
};

const paginatedJournals = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return journals.value.slice(start, start + perPage.value);
});

onMounted(loadJournals);
</script>

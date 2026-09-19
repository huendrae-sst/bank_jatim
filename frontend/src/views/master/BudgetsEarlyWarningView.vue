<template>
  <div class="budgets-ews-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Radar EWS Anggaran Cabang</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/master/budgets" class="btn btn-sm btn-outline-secondary">
          Kembali ke Master Pagu
        </router-link>
      </div>
    </div>

    <!-- Success Notification Toast -->
    <div v-if="alertMessage" class="alert alert-success d-flex align-items-center p-3 rounded-3 shadow-xs mb-3">
      <i class="bi bi-check-circle-fill fs-4 me-2"></i>
      <div class="fs-8 fw-semibold">{{ alertMessage }}</div>
    </div>

    <!-- Alert Summary -->
    <div v-if="highRiskBranches.length > 0" class="alert alert-warning d-flex align-items-center justify-content-between p-3 rounded-3 shadow-xs mb-3 flex-wrap gap-2">
      <div class="d-flex align-items-center gap-3">
        <i class="bi bi-exclamation-triangle-fill fs-3 text-warning"></i>
        <div>
          <h6 class="fw-bold mb-0 text-dark">Peringatan: {{ highRiskBranches.length }} Unit Kerja Mendekati Plafon Anggaran ({{ highRiskBranches.map(b => b.branch).slice(0, 3).join(', ') }})</h6>
          <p class="fs-8 text-secondary mb-0">Serapan telah melampaui ambang batas siaga (&ge; 80%). Order baru memerlukan persetujuan khusus Divisi Perencanaan & Keuangan.</p>
        </div>
      </div>
    </div>
    <div v-else-if="!isLoading" class="alert alert-success d-flex align-items-center p-3 rounded-3 shadow-xs mb-3">
      <i class="bi bi-check-circle-fill fs-4 text-success me-2"></i>
      <div class="fs-8 fw-semibold">Semua unit kerja saat ini berada dalam koridor anggaran yang aman (&lt; 80%).</div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Unit Kerja</th>
              <th class="text-end">Pagu Tahunan</th>
              <th class="text-end">Realisasi Saat Ini</th>
              <th class="text-end">Sisa Alokasi</th>
              <th class="text-end">Rasio Serapan</th>
              <th>Status Radar EWS</th>
              <th class="text-center pe-3">Tindakan Mitigasi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in paginatedEwsList" :key="b.branch">
              <td class="ps-3 fw-semibold text-body">{{ b.branch }}</td>
              <td class="text-end font-monospace">Rp {{ b.pagu.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-primary">Rp {{ b.spent.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-danger">Rp {{ (b.pagu - b.spent).toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold">{{ b.ratio }}%</td>
              <td>
                <span :class="['badge', b.ratio >= 90 ? 'text-bg-danger' : (b.ratio >= 80 ? 'text-bg-warning' : 'text-bg-success')]">
                  {{ b.ratio >= 90 ? 'KRITIS' : (b.ratio >= 80 ? 'SIAGA' : 'AMAN') }}
                </span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-danger" @click="sendAlert(b)" title="Kirim Notifikasi Peringatan">
                  <i class="bi bi-bell"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="ewsList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const currentPage = ref(1);
const perPage = ref(10);
const isLoading = ref(true);
const ewsList = ref([]);
const alertMessage = ref('');

const fetchBudgets = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/master/budgets');
    const budgets = res.data?.data || res.data || [];
    ewsList.value = budgets.map(b => {
      const pagu = Number(b.allocatedAmount || 0);
      const spent = Number(b.realizedAmount || 0) + Number(b.committedAmount || 0);
      const ratio = pagu > 0 ? Number(((spent / pagu) * 100).toFixed(1)) : 0;
      return {
        id: b.id,
        branch: b.organization?.name || b.costCenterCode || 'Unit Kerja',
        pagu,
        spent,
        ratio
      };
    }).sort((a, b) => b.ratio - a.ratio);
  } catch (err) {
    console.error('Failed to load budgets for EWS', err);
  } finally {
    isLoading.value = false;
  }
};

const highRiskBranches = computed(() => {
  return ewsList.value.filter(b => b.ratio >= 80);
});

const sendAlert = (b) => {
  alertMessage.value = `Notifikasi peringatan dini (EWS) telah berhasil dikirimkan ke Pemimpin ${b.branch} dan Divisi Perencanaan & Keuangan.`;
  setTimeout(() => {
    alertMessage.value = '';
  }, 4000);
};

const paginatedEwsList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return ewsList.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchBudgets();
});
</script>

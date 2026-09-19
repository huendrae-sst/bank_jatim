<template>
  <div class="budgets-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Pagu Anggaran Operasional Unit Kerja</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/master/budgets/early-warning" class="btn btn-sm btn-outline-danger">
          Radar EWS Anggaran
        </router-link>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateModal">
          Tambah
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Pagu Anggaran Tahun Anggaran 2026</h3>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Unit Kerja</th>
              <th>Pos Beban / COA</th>
              <th class="text-end">Pagu Ditetapkan</th>
              <th class="text-end">Realisasi Belanja</th>
              <th class="text-end">Sisa Pagu Tersedia</th>
              <th class="text-end">Serapan (%)</th>
              <th>Status EWS</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in paginatedBudgets" :key="b.id">
              <td class="ps-3 fw-semibold text-body">{{ b.branchName }}</td>
              <td class="fs-8 text-secondary font-monospace">{{ b.coa }}</td>
              <td class="text-end font-monospace fw-bold text-body">Rp {{ b.allocated.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-primary">Rp {{ b.realized.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold" :class="b.remaining < 50000000 ? 'text-danger' : 'text-success'">
                Rp {{ b.remaining.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace fw-bold">
                {{ b.absorptionRate.toFixed(1) }}%
              </td>
              <td>
                <span :class="['badge', b.absorptionRate >= 80 ? 'text-bg-warning' : 'text-bg-success']">
                  {{ b.absorptionRate >= 80 ? 'SIAGA' : 'NORMAL' }}
                </span>
              </td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-secondary" @click="openDetailModal(b)" title="Detail"><i class="bi bi-eye"></i></button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="budgetList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Alokasi Pagu Baru) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">
              Alokasi Pagu Anggaran Baru
            </h6>
            <button type="button" class="btn-close" @click="showModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="saveBudget">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="mb-2">
                <label class="form-label fw-bold mb-1">Kantor Cabang / Unit Kerja <span class="text-danger">*</span></label>
                <input type="text" v-model="budgetForm.branchName" class="form-control form-control-sm" placeholder="Contoh: KC Kediri" required />
              </div>
              <div class="mb-2">
                <label class="form-label fw-bold mb-1">Pos Beban / COA <span class="text-danger">*</span></label>
                <input type="text" v-model="budgetForm.coa" class="form-control form-control-sm" placeholder="Contoh: 5.2.01 (Beban Produk & Cetakan)" required />
              </div>
              <div class="mb-2">
                <label class="form-label fw-bold mb-1">Nominal Pagu Ditetapkan (Rp) <span class="text-danger">*</span></label>
                <input type="number" v-model.number="budgetForm.allocated" class="form-control form-control-sm font-monospace" min="1000000" step="500000" required />
              </div>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-secondary" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Detail Anggaran -->
    <div v-if="showDetailModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">
              Detail Pagu & Realisasi
            </h6>
            <button type="button" class="btn-close" @click="showDetailModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-3 fs-8" v-if="selectedBudget">
            <div class="mb-2">
              <div class="text-secondary fs-9">Unit Kerja:</div>
              <div class="fw-bold fs-7">{{ selectedBudget.branchName }}</div>
            </div>
            <div class="mb-2">
              <div class="text-secondary fs-9">Pos Beban / COA:</div>
              <div class="font-monospace fw-semibold">{{ selectedBudget.coa }}</div>
            </div>
            <div class="row g-2 mt-2 pt-2 border-top">
              <div class="col-6">
                <div class="text-secondary fs-9">Pagu Ditetapkan:</div>
                <div class="font-monospace fw-bold text-dark fs-7">Rp {{ selectedBudget.allocated.toLocaleString('id-ID') }}</div>
              </div>
              <div class="col-6">
                <div class="text-secondary fs-9">Realisasi Belanja:</div>
                <div class="font-monospace fw-bold text-primary fs-7">Rp {{ selectedBudget.realized.toLocaleString('id-ID') }}</div>
              </div>
              <div class="col-6 mt-2">
                <div class="text-secondary fs-9">Sisa Anggaran:</div>
                <div class="font-monospace fw-bold text-success fs-7">Rp {{ selectedBudget.remaining.toLocaleString('id-ID') }}</div>
              </div>
              <div class="col-6 mt-2">
                <div class="text-secondary fs-9">Persentase Serapan:</div>
                <div class="font-monospace fw-bold text-danger fs-7">{{ selectedBudget.absorptionRate.toFixed(1) }}%</div>
              </div>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-secondary" @click="showDetailModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';

const showModal = ref(false);
const showDetailModal = ref(false);
const selectedBudget = ref(null);
const currentPage = ref(1);
const perPage = ref(10);

const mapDefaultBudget = (b) => {
  const allocated = Number(b.allocated || 0);
  const committed = Number(b.committed || 0);
  const realized = Number(b.realized || 0);
  const remaining = allocated - committed - realized;
  return {
    id: b.id,
    branchName: b.org || '-',
    coa: b.costCenter || '5.2.01 (Beban Produk & Cetakan)',
    allocated,
    committed,
    realized,
    remaining,
    absorptionRate: allocated === 0 ? 0 : (realized / allocated) * 100
  };
};

const budgetList = ref([]);

onMounted(() => {
  loadBudgets();
});

const loadBudgets = async () => {
  try {
    const response = await api.get('/master/budgets');
    const items = response.data?.data || response.data || [];
    if (Array.isArray(items)) {
      budgetList.value = items.map((budget) => {
        const allocated = Number(budget.allocatedAmount || budget.allocated || 0);
        const committed = Number(budget.committedAmount || budget.committed || 0);
        const realized = Number(budget.realizedAmount || budget.realized || 0);
        const remaining = allocated - committed - realized;
        return {
          id: budget.id,
          branchName: budget.organization?.name || budget.org || '-',
          coa: budget.costCenterCode || budget.costCenter || '-',
          allocated,
          committed,
          realized,
          remaining,
          absorptionRate: allocated === 0 ? 0 : (realized / allocated) * 100
        };
      });
    }
  } catch (err) {
    console.warn('Backend /master/budgets unavailable:', err);
    budgetList.value = [];
  }
};

const budgetForm = reactive({
  branchName: '',
  coa: '5.2.01 (Beban Produk & Cetakan)',
  allocated: 300000000
});

const openCreateModal = () => {
  Object.assign(budgetForm, {
    branchName: '',
    coa: '5.2.01 (Beban Produk & Cetakan)',
    allocated: 300000000
  });
  showModal.value = true;
};

const openDetailModal = (b) => {
  selectedBudget.value = b;
  showDetailModal.value = true;
};

const saveBudget = () => {
  alert('Penyimpanan pagu anggaran belum tersedia di backend production.');
  showModal.value = false;
};

const paginatedBudgets = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return budgetList.value.slice(start, start + perPage.value);
});
</script>

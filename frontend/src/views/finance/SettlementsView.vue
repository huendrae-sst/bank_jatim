<template>
  <div class="settlements-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Settlement Finansial Antar-Unit & Jurnal GL</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/reports/general-ledger" class="text-decoration-none text-body">Keuangan & GL</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Settlement Antar-Unit</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'draft' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'draft'"
            >
              Draft
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'posted' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'posted'"
            >
              Telah Diposting
            </button>
          </li>
        </ul>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/reports/general-ledger" class="btn btn-sm btn-outline-secondary fs-8">
            Buku Besar Umum
          </router-link>
          <router-link to="/reports/settlements" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            Rekapitulasi
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-auto" v-if="searchQuery">
            <button
              type="button"
              @click="searchQuery = ''"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              Reset
            </button>
          </div>
          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Settlement, Cabang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">Nomor Settlement</th>
              <th class="py-2 text-uppercase fs-9">Unit Debet (Beban Cabang)</th>
              <th class="py-2 text-uppercase fs-9">Unit Kredit (Persediaan)</th>
              <th class="py-2 text-uppercase fs-9 text-end">Nilai Barang</th>
              <th class="py-2 text-uppercase fs-9 text-end">Ongkos Kirim</th>
              <th class="py-2 text-uppercase fs-9 text-end">Total Settlement</th>
              <th class="py-2 text-uppercase fs-9 text-center">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9" style="width: 190px;">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="settle in paginatedSettlements" :key="settle.id">
              <td class="ps-3 py-2">
                <span class="font-monospace fw-bold text-danger">{{ settle.number }}</span>
              </td>
              <td class="py-2">
                <div class="fw-semibold text-body">{{ settle.debitOrg }}</div>
                <div class="fs-9 text-secondary font-monospace">{{ settle.debitCostCenter }}</div>
              </td>
              <td class="py-2">
                <div class="text-body">{{ settle.creditOrg }}</div>
                <div class="fs-9 text-secondary font-monospace">CC-LOG-01</div>
              </td>
              <td class="py-2 text-end font-monospace text-secondary">
                Rp {{ settle.itemAmount.toLocaleString('id-ID') }}
              </td>
              <td class="py-2 text-end font-monospace text-secondary">
                Rp {{ settle.shippingAmount.toLocaleString('id-ID') }}
              </td>
              <td class="py-2 text-end font-monospace fw-bold text-body">
                Rp {{ settle.totalAmount.toLocaleString('id-ID') }}
              </td>
              <td class="py-2 text-center">
                <span class="badge fs-9 text-uppercase" :class="settle.status === 'POSTED' ? 'text-bg-success' : 'text-bg-warning'">
                  {{ settle.status }}
                </span>
              </td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex align-items-center gap-1">
                  <button
                    v-if="settle.status === 'DRAFT'"
                    class="btn-action-icon text-success"
                    @click="approveAndPost(settle)"
                    title="Otorisasi &amp; Posting ke General Ledger"
                  >
                    <i class="bi bi-check2-circle"></i>
                  </button>
                  <button
                    class="btn-action-icon text-secondary"
                    @click="viewJournal(settle)"
                    title="Pratinjau Jurnal GL"
                  >
                    <i class="bi bi-journal-text"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredSettlements.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada data settlement yang sesuai dengan filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredSettlements.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Journal Preview Card -->
    <div v-if="selectedJournal" class="card card-outline card-danger shadow-xs bg-body mt-3">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center bg-body">
        <div class="d-flex align-items-center gap-2">
          <div>
            <h5 class="card-title fw-bold mb-0 text-body fs-6">
              Pratinjau Jurnal Buku Besar (General Ledger): {{ selectedJournal.number }}
            </h5>
            <small class="text-secondary fs-9">Entri Akuntansi Otomatis Multi-Entitas JIMS</small>
          </div>
        </div>
        <button type="button" class="btn-close" @click="selectedJournal = null" aria-label="Close"></button>
      </div>
      <div class="card-body p-3">
        <div class="table-responsive">
          <table class="table table-bordered align-middle mb-0 fs-8">
            <thead class="bg-body-secondary text-secondary fs-9 text-uppercase">
              <tr>
                <th>Kode Akun CoA</th>
                <th>Nama Akun</th>
                <th>Cost Center</th>
                <th class="text-end">Debet (Rp)</th>
                <th class="text-end">Kredit (Rp)</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="font-monospace fw-bold text-body">51200</span></td>
                <td>Beban Perlengkapan & Produk Perbankan</td>
                <td><span class="font-monospace text-secondary">{{ selectedJournal.debitCostCenter }}</span></td>
                <td class="text-end font-monospace fw-bold text-success">
                  Rp {{ selectedJournal.totalAmount.toLocaleString('id-ID') }}
                </td>
                <td class="text-end text-secondary">-</td>
              </tr>
              <tr>
                <td><span class="font-monospace fw-bold text-body">11400</span></td>
                <td>Persediaan Logistik Kantor Pusat JIMS</td>
                <td><span class="font-monospace text-secondary">CC-LOG-01</span></td>
                <td class="text-end text-secondary">-</td>
                <td class="text-end font-monospace fw-bold text-danger">
                  Rp {{ selectedJournal.totalAmount.toLocaleString('id-ID') }}
                </td>
              </tr>
            </tbody>
            <tfoot class="bg-body-secondary fw-bold fs-8">
              <tr>
                <td colspan="3" class="text-end text-secondary">Total Entri Berimbang (Balanced):</td>
                <td class="text-end font-monospace text-success">Rp {{ selectedJournal.totalAmount.toLocaleString('id-ID') }}</td>
                <td class="text-end font-monospace text-danger">Rp {{ selectedJournal.totalAmount.toLocaleString('id-ID') }}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { toast } from '@/utils/toast';
import PaginationFooter from '@/components/PaginationFooter.vue';

const searchQuery = ref('');
const activeTab = ref('all');
const selectedJournal = ref(null);
const currentPage = ref(1);
const perPage = ref(10);

const mapSettlement = (settlement) => ({
  id: settlement.id,
  number: settlement.settlementNumber || settlement.number,
  debitOrg: settlement.debitOrganization?.name || settlement.debitOrg || '-',
  debitCostCenter: settlement.debitCostCenter || '-',
  creditOrg: settlement.creditOrganization?.name || settlement.creditOrg || '-',
  itemAmount: Number(settlement.itemAmount || 0),
  shippingAmount: Number(settlement.shippingAmount || 0),
  totalAmount: Number(settlement.totalAmount || 0),
  status: settlement.status || '-'
});

const settlements = ref([]);

const postedCount = computed(() => {
  return settlements.value.filter(s => s.status === 'POSTED').length;
});

const draftCount = computed(() => {
  return settlements.value.filter(s => s.status === 'DRAFT').length;
});

const filteredSettlements = computed(() => {
  return settlements.value.filter(s => {
    if (activeTab.value === 'draft' && s.status !== 'DRAFT') return false;
    if (activeTab.value === 'posted' && s.status !== 'POSTED') return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNum = s.number.toLowerCase().includes(q);
      const matchDebit = s.debitOrg.toLowerCase().includes(q);
      if (!matchNum && !matchDebit) return false;
    }
    return true;
  });
});

const paginatedSettlements = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredSettlements.value.slice(start, start + perPage.value);
});

const loadSettlements = async () => {
  try {
    const response = await api.get('/finance/settlements');
    const data = response.data?.content || response.data || [];
    if (Array.isArray(data) && data.length > 0) {
      settlements.value = data.map(mapSettlement);
    }
  } catch (error) {
    settlements.value = [];
    console.warn('Failed loading settlements from backend:', error);
  }
};

const approveAndPost = async (settle) => {
  try {
    const response = await api.post(`/finance/settlements/${settle.id}/approve-post`);
    selectedJournal.value = mapSettlement(response.data);
    await loadSettlements();
    toast.success('Settlement ' + settle.number + ' berhasil disetujui! Entri jurnal debit/kredit otomatis diposting ke General Ledger Bank Jatim.');
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal mem-posting settlement.');
  }
};

const viewJournal = (settle) => {
  selectedJournal.value = settle;
};

onMounted(loadSettlements);
</script>

<style scoped>
</style>

<template>
  <div class="stock-opname-history-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Riwayat Stock Opname</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/inventory/balances" class="text-decoration-none text-body">Gudang & Inventori</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Riwayat Opname</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <h3 class="card-title fw-bold mb-0 fs-6 text-body">
          <i class="bi bi-list-columns-reverse text-danger me-2"></i>Daftar Arsip Berita Acara Stock Opname
        </h3>
        <div class="card-tools d-flex align-items-center gap-2">
          <router-link to="/inventory/stock-opname" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8">
            <i class="bi bi-plus-lg me-1"></i> Stock Opname Baru
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterWarehouse" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Lokasi Gudang</option>
                <option value="Gudang Logistik Margomulyo">Gudang Margomulyo</option>
                <option value="Gudang Cabang Surabaya">Gudang Cabang Surabaya</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterWarehouse">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No. Dokumen, Petugas..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                <i class="bi bi-search me-1"></i> Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">No. Dokumen Opname</th>
              <th class="py-2 text-uppercase fs-9">Lokasi Gudang</th>
              <th class="py-2 text-uppercase fs-9">Tanggal Pelaksanaan</th>
              <th class="py-2 text-uppercase fs-9">Petugas Pelaksana</th>
              <th class="py-2 text-uppercase fs-9 text-end">Jumlah Item</th>
              <th class="py-2 text-uppercase fs-9 text-end">Akurasi Fisik</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="h in paginatedHistory" :key="h.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ h.docNo }}</td>
              <td class="fw-semibold text-body">{{ h.warehouse }}</td>
              <td class="fs-8 text-body">{{ h.date }}</td>
              <td class="fs-8 text-secondary">{{ h.officer }}</td>
              <td class="text-end font-monospace">{{ h.itemCount }} SKU</td>
              <td class="text-end font-monospace text-success fw-bold">{{ h.accuracy }}</td>
              <td><span class="badge text-bg-success">{{ h.status }}</span></td>
              <td class="text-center pe-3">
                <router-link :to="'/inventory/stock-opname/history/' + h.id" class="btn-action-icon text-secondary" title="Lihat Berita Acara">
                  <i class="bi bi-eye"></i>
                </router-link>
              </td>
            </tr>
            <tr v-if="filteredHistory.length === 0">
              <td colspan="8" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada dokumen riwayat stock opname yang sesuai dengan filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredHistory.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterWarehouse = ref('');
const isLoading = ref(true);

const history = ref([]);

const resetFilters = () => {
  searchQuery.value = '';
  filterWarehouse.value = '';
  currentPage.value = 1;
};

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch {
    return val;
  }
};

const fetchOpnameHistory = async () => {
  isLoading.value = true;
  try {
    const [resOpname, resAdj] = await Promise.all([
      api.get('/inventory/ledgers', { params: { transactionType: 'STOCK_OPNAME', size: 100 } }),
      api.get('/inventory/ledgers', { params: { transactionType: 'OPNAME_ADJUSTMENT', size: 100 } })
    ]);

    const items1 = resOpname.data?.data?.content || resOpname.data?.content || [];
    const items2 = resAdj.data?.data?.content || resAdj.data?.content || [];
    const all = [...items1, ...items2];

    const grouped = {};
    all.forEach(item => {
      const refNo = item.referenceNumber || `SO-DOC-${item.id}`;
      if (!grouped[refNo]) {
        grouped[refNo] = {
          id: item.id,
          docNo: refNo,
          warehouse: item.warehouse?.name || 'Gudang Pusat Margomulyo',
          date: formatDate(item.createdAt),
          officer: item.createdByUser?.fullName || item.createdByUser?.name || 'Petugas Gudang & Auditor',
          items: [],
          matchedCount: 0
        };
      }
      grouped[refNo].items.push(item);
      if (item.transactionType === 'STOCK_OPNAME' || (item.qtyIn === 0 && item.qtyOut === 0)) {
        grouped[refNo].matchedCount++;
      }
    });

    history.value = Object.values(grouped).map(g => ({
      id: g.id,
      docNo: g.docNo,
      warehouse: g.warehouse,
      date: g.date,
      officer: g.officer,
      itemCount: g.items.length,
      accuracy: g.items.length > 0 ? ((g.matchedCount / g.items.length) * 100).toFixed(1) + '%' : '100%',
      status: 'APPROVED'
    }));
  } catch (err) {
    console.error('Failed to fetch opname history', err);
  } finally {
    isLoading.value = false;
  }
};

const filteredHistory = computed(() => {
  return history.value.filter(item => {
    const matchQuery = !searchQuery.value ||
      item.docNo.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.officer.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchWh = !filterWarehouse.value || item.warehouse.includes(filterWarehouse.value);
    return matchQuery && matchWh;
  });
});

const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredHistory.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchOpnameHistory();
});
</script>

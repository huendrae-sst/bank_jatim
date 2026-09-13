<template>
  <div class="master-data-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Master Data & Konfigurasi Sistem</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/master/items" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-box me-1"></i> Master SKU
        </router-link>
        <router-link to="/master/organizations" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-building me-1"></i> Unit Kerja
        </router-link>
        <router-link to="/master/budgets" class="btn btn-sm btn-danger fw-bold shadow-xs">
          <i class="bi bi-wallet2 me-1"></i> Pagu Anggaran
        </router-link>
      </div>
    </div>

    <!-- 4 KPI Info Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-danger text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-box-seam fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Katalog SKU</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ items.length }} SKU</div>
            <div class="fs-9 text-secondary">Warkat & Logistik</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-primary text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-buildings fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Jaringan Unit Kerja</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ orgs.length }} Lokasi</div>
            <div class="fs-9 text-secondary">KP, KC, KCP & Gudang</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-cash-stack fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Pagu Anggaran</div>
            <div class="fs-4 fw-bold font-monospace text-success">Rp {{ formatCompact(totalBudgetAllocated) }}</div>
            <div class="fs-9 text-secondary">Tahun Anggaran 2026</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-truck fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Vendor & Ekspedisi</div>
            <div class="fs-4 fw-bold font-monospace text-body">{{ partnerCount }} Rekanan</div>
            <div class="fs-9 text-secondary">Penyedia Terakreditasi</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Card with Navigation Tabs -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'items' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'items'"
            >
              <i class="bi bi-box me-1"></i> Katalog Barang
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'orgs' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'orgs'"
            >
              <i class="bi bi-building me-1"></i> Unit Kerja & Cabang
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'budgets' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'budgets'"
            >
              <i class="bi bi-wallet2 me-1"></i> Pagu Anggaran
            </button>
          </li>
        </ul>

      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-auto" v-if="searchQuery">
            <button type="button" @click="searchQuery = ''" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
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
                placeholder="Cari Master Data..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loadError" class="alert alert-danger m-3 mb-0 fs-8">
        <i class="bi bi-exclamation-octagon-fill me-1"></i> {{ loadError }}
      </div>

      <div v-if="loading" class="p-4 text-center text-secondary fs-8">
        <span class="spinner-border spinner-border-sm me-1"></span>
        Memuat master data dari backend...
      </div>

      <!-- Tab 1: Master Barang -->
      <div v-if="!loading && activeTab === 'items'" class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>SKU</th>
              <th>Barcode</th>
              <th>Nama Barang</th>
              <th>Kategori</th>
              <th class="text-center">Satuan</th>
              <th class="text-end">Min Stock</th>
              <th class="text-end">Safety Stock</th>
              <th class="text-end">Harga Estimasi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="item in paginatedItems" :key="item.id">
              <td>
                <span class="font-monospace fw-bold text-danger">{{ item.sku }}</span>
              </td>
              <td>
                <span class="font-monospace text-secondary">{{ item.barcode || '-' }}</span>
              </td>
              <td>
                <strong class="text-body">{{ item.name }}</strong>
              </td>
              <td>
                <span class="badge text-bg-light border fs-9">{{ item.category }}</span>
              </td>
              <td class="text-center font-monospace">{{ item.uom }}</td>
              <td class="text-end font-monospace text-secondary">{{ item.min }}</td>
              <td class="text-end font-monospace text-secondary">{{ item.safety }}</td>
              <td class="text-end font-monospace fw-bold text-body">
                Rp {{ item.price.toLocaleString('id-ID') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Tab 2: Unit Kerja & Cabang -->
      <div v-if="!loading && activeTab === 'orgs'" class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Kode Unit</th>
              <th>Nama Kantor / Unit Kerja</th>
              <th>Tipe Organisasi</th>
              <th>Kota / Wilayah</th>
              <th>Cost Center</th>
              <th class="text-center">Status</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="org in paginatedOrgs" :key="org.id">
              <td>
                <span class="font-monospace fw-bold text-danger">{{ org.code }}</span>
              </td>
              <td>
                <strong class="text-body">{{ org.name }}</strong>
              </td>
              <td>
                <span class="badge text-bg-light border fs-9">{{ org.type }}</span>
              </td>
              <td class="text-body">{{ org.city }}</td>
              <td>
                <span class="font-monospace text-secondary">{{ org.costCenter }}</span>
              </td>
              <td class="text-center">
                <span class="badge text-bg-success fs-9">Aktif</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Tab 3: Budgets -->
      <div v-if="!loading && activeTab === 'budgets'" class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Unit Kerja</th>
              <th>Cost Center</th>
              <th class="text-center">Tahun Fiskal</th>
              <th class="text-end">Pagu Anggaran</th>
              <th class="text-end">Komitmen</th>
              <th class="text-end">Realisasi</th>
              <th class="text-end">Sisa Kuota</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="b in paginatedBudgets" :key="b.id">
              <td>
                <strong class="text-body">{{ b.org }}</strong>
              </td>
              <td>
                <span class="font-monospace text-secondary">{{ b.costCenter }}</span>
              </td>
              <td class="text-center font-monospace">{{ b.fiscalYear }}</td>
              <td class="text-end font-monospace text-body">Rp {{ b.allocated.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-secondary">Rp {{ b.committed.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace text-secondary">Rp {{ b.realized.toLocaleString('id-ID') }}</td>
              <td class="text-end font-monospace fw-bold text-success">
                Rp {{ (b.allocated - b.committed - b.realized).toLocaleString('id-ID') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="currentTotal"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const activeTab = ref('items');
const searchQuery = ref('');
const loading = ref(false);
const loadError = ref('');

const items = ref([]);
const orgs = ref([]);
const budgets = ref([]);
const vendors = ref([]);
const couriers = ref([]);

onMounted(() => {
  loadMasterData();
});

const loadMasterData = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const [itemsRes, orgsRes, budgetsRes, vendorsRes, couriersRes] = await Promise.all([
      api.get('/master/items'),
      api.get('/master/organizations'),
      api.get('/master/budgets'),
      api.get('/master/vendors'),
      api.get('/master/couriers')
    ]);

    items.value = (itemsRes.data || []).map((item) => ({
      id: item.id,
      sku: item.sku,
      barcode: item.barcode,
      name: item.name,
      category: item.category?.name || '-',
      uom: item.uom,
      min: Number(item.minStock || 0),
      safety: Number(item.safetyStock || 0),
      price: Number(item.estimatedUnitPrice || 0)
    }));

    orgs.value = (orgsRes.data || []).map((org) => ({
      id: org.id,
      code: org.code,
      name: org.name,
      type: org.type,
      city: org.city || '-',
      costCenter: org.costCenterCode || '-'
    }));

    budgets.value = (budgetsRes.data || []).map((budget) => ({
      id: budget.id,
      org: budget.organization?.name || '-',
      costCenter: budget.costCenterCode,
      fiscalYear: budget.fiscalYear,
      allocated: Number(budget.allocatedAmount || 0),
      committed: Number(budget.committedAmount || 0),
      realized: Number(budget.realizedAmount || 0)
    }));

    vendors.value = vendorsRes.data || [];
    couriers.value = couriersRes.data || [];
  } catch (err) {
    loadError.value = err?.message || err?.error || 'Gagal memuat master data dari backend.';
  } finally {
    loading.value = false;
  }
};

const totalBudgetAllocated = computed(() => budgets.value.reduce((sum, budget) => sum + budget.allocated, 0));
const partnerCount = computed(() => vendors.value.length + couriers.value.length);

const formatCompact = (value) => {
  if (value >= 1000000000000) return `${(value / 1000000000000).toFixed(2)}T`;
  if (value >= 1000000000) return `${(value / 1000000000).toFixed(2)}M`;
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}jt`;
  return value.toLocaleString('id-ID');
};

const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value;
  const q = searchQuery.value.toLowerCase();
  return items.value.filter(i => i.sku.toLowerCase().includes(q) || i.name.toLowerCase().includes(q) || i.category.toLowerCase().includes(q));
});

const filteredOrgs = computed(() => {
  if (!searchQuery.value) return orgs.value;
  const q = searchQuery.value.toLowerCase();
  return orgs.value.filter(o => o.code.toLowerCase().includes(q) || o.name.toLowerCase().includes(q) || o.city.toLowerCase().includes(q));
});

const filteredBudgets = computed(() => {
  if (!searchQuery.value) return budgets.value;
  const q = searchQuery.value.toLowerCase();
  return budgets.value.filter(b => b.org.toLowerCase().includes(q) || b.costCenter.toLowerCase().includes(q));
});

const currentPage = ref(1);
const perPage = ref(10);
const currentTotal = computed(() => {
  if (activeTab.value === 'items') return filteredItems.value.length;
  if (activeTab.value === 'orgs') return filteredOrgs.value.length;
  return filteredBudgets.value.length;
});
const paginatedItems = computed(() => filteredItems.value.slice((currentPage.value - 1) * perPage.value, currentPage.value * perPage.value));
const paginatedOrgs = computed(() => filteredOrgs.value.slice((currentPage.value - 1) * perPage.value, currentPage.value * perPage.value));
const paginatedBudgets = computed(() => filteredBudgets.value.slice((currentPage.value - 1) * perPage.value, currentPage.value * perPage.value));
</script>

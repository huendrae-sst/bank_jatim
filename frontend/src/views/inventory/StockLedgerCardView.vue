<template>
  <div class="stock-ledger-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Kartu Stok & Buku Besar Persediaan (Stock Ledger)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/inventory/balances" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-boxes me-1"></i> Stock Balances
        </router-link>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="exportExcel">
          <i class="bi bi-file-earmark-excel me-1"></i> Ekspor Kartu Stok
        </button>
      </div>
    </div>

    <!-- Filter & Item Selector Card -->
    <div class="card p-3 shadow-xs mb-3 border bg-body">
      <div class="row g-3 align-items-center">
        <div class="col-12 col-md-5">
          <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Pilih Komoditas Barang (SKU):</label>
          <select v-model="selectedItem" class="form-select form-select-sm fs-7">
            <option v-for="item in itemOptions" :key="item.id" :value="String(item.id)">
              {{ item.name }} ({{ item.sku }})
            </option>
          </select>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Gudang Penyimpanan:</label>
          <select v-model="selectedWarehouse" class="form-select form-select-sm fs-7">
            <option value="ALL">Semua Gudang</option>
            <option v-for="warehouse in warehouseOptions" :key="warehouse.id" :value="warehouse.code">
              {{ warehouse.name }}
            </option>
          </select>
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Rentang Tanggal:</label>
          <input type="month" v-model="selectedMonth" class="form-control form-control-sm fs-7" />
        </div>
      </div>
    </div>

    <!-- 4 Metric Cards -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-success text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-stack fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Saldo Akhir On-Hand</div>
            <div class="fs-4 fw-bold font-monospace text-success">{{ endingBalance.toLocaleString('id-ID') }} <span class="fs-7 fw-normal">{{ selectedItemUom }}</span></div>
            <div class="fs-9 text-secondary">Harga: Rp {{ selectedItemPrice.toLocaleString('id-ID') }} / {{ selectedItemUom }}</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-primary text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-arrow-down-left-circle fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Total Masuk (Inbound)</div>
            <div class="fs-4 fw-bold font-monospace text-body">+{{ totalQtyIn.toLocaleString('id-ID') }} <span class="fs-7 fw-normal">{{ selectedItemUom }}</span></div>
            <div class="fs-9 text-secondary">PO Vendor & Saldo Awal</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-danger text-white d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-arrow-up-right-circle fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Total Keluar (Outbound)</div>
            <div class="fs-4 fw-bold font-monospace text-danger">-{{ totalQtyOut.toLocaleString('id-ID') }} <span class="fs-7 fw-normal">{{ selectedItemUom }}</span></div>
            <div class="fs-9 text-secondary">Distribusi Cabang</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card p-3 shadow-xs h-100 d-flex flex-row align-items-center gap-3">
          <div class="rounded-3 p-3 bg-info text-dark d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
            <i class="bi bi-wallet2 fs-4"></i>
          </div>
          <div>
            <div class="fs-8 text-secondary fw-bold text-uppercase">Total Valuasi Fisik</div>
            <div class="fs-4 fw-bold font-monospace text-body">Rp {{ (totalValuation / 1000000).toFixed(1) }}<span class="fs-7 fw-normal">jt</span></div>
            <div class="fs-9 text-secondary">Rp {{ totalValuation.toLocaleString('id-ID') }} (Ledger)</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Immutable Mutation Records Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
          <i class="bi bi-clock-history text-danger me-1"></i> Riwayat Mutasi Buku Besar (Double-Entry Ledger)
        </h3>
        <div class="d-flex gap-2">
          <div class="input-group input-group-sm" style="min-width: 280px;">
            <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
            <input
              type="text"
              v-model="searchQuery"
              class="form-control form-control-sm border-start-0 border-end-0 fs-8"
              placeholder="Cari Dokumen Ref / Keterangan..."
            />
            <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button">
              Cari
            </button>
            <button v-if="searchQuery" type="button" class="btn btn-sm btn-outline-danger fs-8" @click="searchQuery = ''" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Waktu Mutasi</th>
              <th>Tipe Transaksi</th>
              <th>Nomor Referensi</th>
              <th class="text-end">Masuk (+ In)</th>
              <th class="text-end">Keluar (- Out)</th>
              <th class="text-end">Saldo Akhir</th>
              <th class="text-end">Total Valuasi</th>
              <th>Keterangan / Audit Trail</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="log in paginatedLogs" :key="log.id">
              <td class="font-monospace fs-8 text-secondary">{{ log.time }}</td>
              <td>
                <span class="badge text-bg-light border fs-9">{{ log.type }}</span>
              </td>
              <td>
                <span class="font-monospace fw-bold text-danger">{{ log.ref }}</span>
              </td>
              <td class="text-end font-monospace">
                <span v-if="log.qtyIn > 0" class="text-success fw-bold">+{{ log.qtyIn.toLocaleString('id-ID') }}</span>
                <span v-else class="text-secondary">-</span>
              </td>
              <td class="text-end font-monospace">
                <span v-if="log.qtyOut > 0" class="text-danger fw-bold">-{{ log.qtyOut.toLocaleString('id-ID') }}</span>
                <span v-else class="text-secondary">-</span>
              </td>
              <td class="text-end font-monospace fw-bold text-body">
                {{ log.balanceAfter.toLocaleString('id-ID') }}
              </td>
              <td class="text-end font-monospace text-body">
                Rp {{ log.totalValue.toLocaleString('id-ID') }}
              </td>
              <td>
                <div class="text-truncate text-secondary" style="max-width: 280px;" :title="log.notes">
                  {{ log.notes }}
                </div>
              </td>
            </tr>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="8" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada mutasi yang tercatat untuk filter ini.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredLogs.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import api from '@/api/client';
import { exportToCsv } from '@/utils/exportHelper';

const selectedItem = ref('1');
const selectedWarehouse = ref('WH-CEN-01');
const selectedMonth = ref('2026-09');
const searchQuery = ref('');
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');
const itemOptions = ref([]);
const warehouseOptions = ref([]);

const ledgerLogs = ref([]);

const mapLedger = (ledger) => ({
  id: ledger.id,
  time: ledger.createdAt || '-',
  type: ledger.transactionType || '-',
  ref: ledger.referenceNumber || '-',
  warehouseCode: ledger.warehouse?.code || 'ALL',
  qtyIn: Number(ledger.qtyIn || 0),
  qtyOut: Number(ledger.qtyOut || 0),
  balanceAfter: Number(ledger.balanceAfter || 0),
  totalValue: Number(ledger.totalValue || 0),
  notes: ledger.notes || '-'
});

const loadOptions = async () => {
  const [itemResponse, warehouseResponse] = await Promise.all([
    api.get('/master/items'),
    api.get('/master/warehouses')
  ]);
  itemOptions.value = itemResponse.data || [];
  warehouseOptions.value = warehouseResponse.data || [];
  selectedItem.value = String(itemOptions.value[0]?.id || selectedItem.value);
};

const loadLedger = async () => {
  if (!selectedItem.value) return;
  errorMessage.value = '';
  try {
    const response = await api.get(`/inventory/stock-card/${selectedItem.value}`, {
      params: { page: 0, size: 200, sort: 'createdAt,desc' }
    });
    ledgerLogs.value = (response.data?.content || []).map(mapLedger);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat kartu stok dari server.';
    ledgerLogs.value = [];
  }
};

const filteredLogs = computed(() => {
  const byWarehouse = selectedWarehouse.value === 'ALL'
    ? ledgerLogs.value
    : ledgerLogs.value.filter(l => l.warehouseCode === selectedWarehouse.value);
  if (!searchQuery.value) return byWarehouse;
  const q = searchQuery.value.toLowerCase();
  return byWarehouse.filter(l => l.ref.toLowerCase().includes(q) || l.notes.toLowerCase().includes(q));
});

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredLogs.value.slice(start, start + perPage.value);
});

const selectedItemDetail = computed(() => itemOptions.value.find(item => String(item.id) === String(selectedItem.value)) || {});
const selectedItemUom = computed(() => selectedItemDetail.value.uom || 'Unit');
const selectedItemPrice = computed(() => Number(selectedItemDetail.value.estimatedUnitPrice || 0));
const endingBalance = computed(() => filteredLogs.value[0]?.balanceAfter || 0);
const totalQtyIn = computed(() => filteredLogs.value.reduce((sum, log) => sum + log.qtyIn, 0));
const totalQtyOut = computed(() => filteredLogs.value.reduce((sum, log) => sum + log.qtyOut, 0));
const totalValuation = computed(() => endingBalance.value * selectedItemPrice.value);

const exportExcel = () => {
  const headers = [
    { key: 'time', label: 'Waktu Transaksi' },
    { key: 'type', label: 'Tipe Mutasi' },
    { key: 'ref', label: 'No. Dokumen / Referensi' },
    { key: 'qtyIn', label: 'Masuk (In)' },
    { key: 'qtyOut', label: 'Keluar (Out)' },
    { key: 'balanceAfter', label: 'Saldo Akhir' },
    { key: 'totalValue', label: 'Valuasi (Rp)' },
    { key: 'notes', label: 'Keterangan Mutasi' }
  ];
  exportToCsv('kartu_stok_persediaan', headers, filteredLogs.value);
};

onMounted(async () => {
  try {
    await loadOptions();
    await loadLedger();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat opsi kartu stok.';
  }
});

watch(selectedItem, () => {
  currentPage.value = 1;
  loadLedger();
});
</script>

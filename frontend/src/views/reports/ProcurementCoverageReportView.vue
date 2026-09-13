<template>
  <div class="procurement-coverage-page space-y-3">
    <!-- Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Matriks Keterlacakan Pengadaan (Traceability)</h3>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 fs-8">
            <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
            <li class="breadcrumb-item"><router-link to="/reports/stock-valuation" class="text-decoration-none text-danger">Laporan</router-link></li>
            <li class="breadcrumb-item active text-secondary" aria-current="page">Keterlacakan Pengadaan</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-sm btn-outline-success fw-bold shadow-xs" @click="handleExport">
          <i class="bi bi-file-earmark-excel me-1"></i> Ekspor Excel
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <h5 class="card-title fw-bold mb-0 fs-6">Matriks Keterlacakan Alur PR &rarr; PO &rarr; GRN Penerimaan</h5>
        <span class="badge bg-light text-secondary border fs-8">Traceability Matrix</span>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light border-bottom text-secondary font-bold uppercase fs-9">
              <tr>
                <th class="ps-3 py-3">No. PO</th>
                <th class="py-3">Vendor</th>
                <th class="py-3">Referensi PR Sumber</th>
                <th class="py-3">Item & SKU</th>
                <th class="py-3 text-center">Dipesan (PO)</th>
                <th class="py-3 text-center">Diterima (GRN)</th>
                <th class="py-3 text-center">Outstanding</th>
                <th class="py-3 pe-3 text-center">Status PO</th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr v-if="matrixData.length === 0">
                <td colspan="8" class="text-center py-4 text-muted">
                  <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                  Tidak ada data matriks keterlacakan pengadaan.
                </td>
              </tr>
              <tr v-for="row in paginatedMatrixData" :key="row.id" class="hover:bg-slate-50">
                <td class="ps-3 py-3 font-bold font-monospace text-dark">{{ row.po_number }}</td>
                <td class="py-3">{{ row.vendor_name }}</td>
                <td class="py-3 font-mono font-bold text-primary">
                  {{ row.pr_number }}
                  <div class="fs-9 text-muted font-normal">{{ row.pr_org }}</div>
                </td>
                <td class="py-3">
                  <div class="fw-bold text-dark">{{ row.item_name }}</div>
                  <div class="fs-9 text-muted font-monospace">{{ row.item_sku }}</div>
                </td>
                <td class="py-3 text-center font-bold text-dark font-monospace">{{ formatNumber(row.qty_ordered) }}</td>
                <td class="py-3 text-center font-bold text-success font-monospace">{{ formatNumber(row.qty_received) }}</td>
                <td class="py-3 text-center font-bold text-danger font-monospace">{{ formatNumber(row.outstanding_qty) }}</td>
                <td class="py-3 pe-3 text-center">
                  <span class="badge" :class="row.status === 'COMPLETED' ? 'bg-success' : 'bg-primary'">{{ row.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Standardized Pagination Footer -->
        <PaginationFooter
          :total="matrixData.length"
          v-model:currentPage="currentPage"
          v-model:perPage="perPage"
        />
      </div>
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
const matrixData = ref([]);

const formatNumber = (val) => new Intl.NumberFormat('id-ID').format(val || 0);

const fetchCoverageData = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/procurement/po');
    const orders = res.data?.data || res.data || [];
    const rows = [];

    for (const po of orders) {
      const items = po.items || [];
      for (const it of items) {
        const qtyOrdered = it.qtyOrdered || 0;
        const qtyReceived = it.qtyReceived || 0;
        const outstanding = Math.max(0, qtyOrdered - qtyReceived);

        rows.push({
          id: `${po.id}-${it.id || rows.length}`,
          po_number: po.poNumber,
          vendor_name: po.vendor?.name || 'Vendor Rekanan',
          pr_number: po.prNumber || `PR-${po.poNumber}`,
          pr_org: 'Divisi Logistik & Umum',
          item_name: it.item?.name || 'Barang Persediaan',
          item_sku: it.item?.sku || '-',
          qty_ordered: qtyOrdered,
          qty_received: qtyReceived,
          outstanding_qty: outstanding,
          status: po.status
        });
      }
    }

    matrixData.value = rows;
  } catch (err) {
    console.error('Failed to load procurement coverage report', err);
  } finally {
    isLoading.value = false;
  }
};

const handleExport = () => {
  const headers = [
    { key: 'po_number', label: 'No. PO' },
    { key: 'vendor_name', label: 'Vendor' },
    { key: 'pr_number', label: 'Referensi PR Sumber' },
    { key: 'pr_org', label: 'Unit PR' },
    { key: 'item_name', label: 'Nama Item' },
    { key: 'item_sku', label: 'SKU Item' },
    { key: 'qty_ordered', label: 'Dipesan (PO)' },
    { key: 'qty_received', label: 'Diterima (GRN)' },
    { key: 'outstanding_qty', label: 'Outstanding' },
    { key: 'status', label: 'Status PO' }
  ];
  exportToCsv('matriks_keterlacakan_pengadaan', headers, matrixData.value);
};

const paginatedMatrixData = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return matrixData.value.slice(start, start + perPage.value);
});

onMounted(() => {
  fetchCoverageData();
});
</script>

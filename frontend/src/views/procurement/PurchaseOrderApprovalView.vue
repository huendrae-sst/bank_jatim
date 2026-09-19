<template>
  <div class="po-approvals-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Persetujuan Purchase Order (PO)</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/procurement/orders" class="text-decoration-none text-body">Pengadaan</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Persetujuan PO</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            Antrean Purchase Order (PO) Menunggu Persetujuan
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/procurement/orders" class="btn btn-sm btn-outline-secondary fs-8">
            Data Purchase Orders
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterVendor" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Rekanan Vendor</option>
                <option v-for="vendor in vendorOptions" :key="vendor" :value="vendor">
                  {{ vendor }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterVendor">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No. PO, Vendor, Rincian..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                Cari
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
              <th class="ps-3 py-2 text-uppercase fs-9">Nomor PO</th>
              <th class="py-2 text-uppercase fs-9">Vendor Terpilih</th>
              <th class="py-2 text-uppercase fs-9">Rincian Barang</th>
              <th class="py-2 text-uppercase fs-9 text-end">Total Nilai Kontrak</th>
              <th class="py-2 text-uppercase fs-9">Syarat Pembayaran</th>
              <th class="py-2 text-uppercase fs-9">Status</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi Pemutus</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="po in paginatedPOs" :key="po.id">
              <td class="ps-3 py-2 fw-bold font-monospace text-danger">{{ po.poNumber }}</td>
              <td class="py-2 fw-semibold text-body">{{ po.vendor }}</td>
              <td class="py-2 fs-8 text-body">{{ po.items }}</td>
              <td class="py-2 text-end font-monospace fw-bold text-body">Rp {{ po.totalContract.toLocaleString('id-ID') }}</td>
              <td class="py-2 fs-8 text-secondary">{{ po.paymentTerms }}</td>
              <td class="py-2"><span class="badge text-bg-warning">{{ po.status }}</span></td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="btn-action-icon text-success" @click="approvePO(po)" title="Setujui PO">
                    <i class="bi bi-check2-circle"></i>
                  </button>
                  <button class="btn-action-icon text-danger" @click="rejectPO(po)" title="Tolak PO">
                    <i class="bi bi-x-circle"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPOs.length === 0">
              <td colspan="7" class="text-center py-4 text-secondary">
                <i class="bi bi-check-circle fs-3 d-block mb-1 text-success"></i>
                Tidak ada dokumen purchase order yang membutuhkan otorisasi.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredPOs.length"
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
import { toast } from '@/utils/toast';

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterVendor = ref('');

const describeItems = (items = []) => items
  .map(line => {
    const name = line.item?.name || '-';
    const qty = Number(line.qtyOrdered || 0).toLocaleString('id-ID');
    const uom = line.item?.uom || 'Unit';
    return `${name} (${qty} ${uom})`;
  })
  .join(', ');

const mapPo = (po) => ({
  id: po.id,
  poNumber: po.poNumber || '-',
  vendor: po.vendor?.name || po.vendor || '-',
  items: describeItems(po.items),
  totalContract: Number(po.totalAmount || 0),
  paymentTerms: po.notes || 'Termin sesuai kontrak',
  status: po.status || '-'
});

const pendingPOs = ref([]);

const loadPendingPOs = async () => {
  try {
    const response = await api.get('/procurement/po');
    const data = response.data?.content || response.data || [];
    const valid = data.filter(po => ['DRAFT', 'ISSUED', 'MENUNGGU OTORISASI', 'WAITING_APPROVAL'].includes(po.status));
    if (valid.length > 0) {
      pendingPOs.value = valid.map(mapPo);
    }
  } catch (error) {
    pendingPOs.value = [];
    console.warn('Failed loading pending POs from backend:', error);
  }
};

const vendorOptions = computed(() => [...new Set(pendingPOs.value.map(po => po.vendor).filter(Boolean))]);

const resetFilters = () => {
  searchQuery.value = '';
  filterVendor.value = '';
  currentPage.value = 1;
};

const filteredPOs = computed(() => {
  return pendingPOs.value.filter(po => {
    const matchQuery = !searchQuery.value ||
      po.poNumber.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      po.vendor.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      po.items.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchVendor = !filterVendor.value || po.vendor.includes(filterVendor.value);
    return matchQuery && matchVendor;
  });
});

const paginatedPOs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredPOs.value.slice(start, start + perPage.value);
});

const approvePO = async (po) => {
  try {
    await api.post(`/procurement/po/${po.id}/approve`);
    await loadPendingPOs();
    toast.success(`Purchase Order ${po.poNumber} berhasil disetujui dan siap dikirimkan ke vendor.`);
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal menyetujui PO.');
  }
};

const rejectPO = async (po) => {
  const reason = prompt(`Masukkan alasan penolakan untuk ${po.poNumber}:`);
  if (reason === null) {
    return;
  }
  try {
    await api.post(`/procurement/po/${po.id}/reject`, { reason });
    await loadPendingPOs();
    toast.warn(`Purchase Order ${po.poNumber} ditolak.`);
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal menolak PO.');
  }
};

onMounted(loadPendingPOs);
</script>

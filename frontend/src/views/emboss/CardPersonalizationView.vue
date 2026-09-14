<template>
  <div class="card-personalization-page">
    <!-- Breadcrumb & Header -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Personalisasi Kartu ATM (Emboss Operations)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/emboss/reject-queue" class="btn btn-sm btn-outline-danger fw-bold">
          <i class="bi bi-exclamation-octagon me-1"></i> Antrean Reject ({{ totalRejectedRecords }})
        </router-link>
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="simulateFileUpload">
          <i class="bi bi-cloud-arrow-up me-1"></i> Unggah Berkas Emboss
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Upload Dropzone Card -->
    <div class="card border-2 border-dashed shadow-xs mb-3 text-center py-4 px-3" style="border-color: rgba(217, 37, 42, 0.4) !important; background-color: rgba(217, 37, 42, 0.02);">
      <div class="d-flex flex-column align-items-center justify-content-center cursor-pointer" @click="simulateFileUpload">
        <div class="rounded-circle p-3 mb-2 d-inline-flex align-items-center justify-content-center" style="background-color: rgba(217, 37, 42, 0.1); color: var(--jatim-red); width: 64px; height: 64px;">
          <i class="bi bi-cloud-arrow-up fs-2"></i>
        </div>
        <h5 class="fw-bold mb-1 text-body">Tarik & Letakkan Berkas Ekstraksi Kartu (.CSV / .TXT) ke Sini</h5>
        <p class="text-secondary fs-8 mb-3" style="max-width: 620px;">
          Format standar Core Banking Bank Jatim: Nomor Rekening, Nama Nasabah (Max 26 Karakter), Masked Card Number, Tipe Kartu (GPN/Mastercard), dan Kode KC Pengelola.
        </p>
        <input type="file" ref="fileInputRef" accept=".csv,.txt" class="d-none" @change="handleFileInputChange" />
        <button type="button" class="btn btn-sm btn-outline-danger fw-bold px-3 shadow-xs" @click.stop="triggerFileInput">
          <i class="bi bi-file-earmark-arrow-up me-1"></i> Pilih Berkas dari Komputer
        </button>
      </div>
    </div>

    <!-- Main Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <h3 class="card-title fw-semibold mb-0 fs-6 text-body">
          <i class="bi bi-journal-text text-danger me-1"></i> Riwayat Berkas Personalisasi Kartu Diproses
        </h3>
        <div class="d-flex gap-2">
          <div class="input-group input-group-sm" style="min-width: 280px;">
            <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
            <input
              type="text"
              v-model="searchQuery"
              class="form-control form-control-sm border-start-0 border-end-0 fs-8"
              placeholder="Cari Nama Berkas..."
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

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th>Nama File Berkas Core Banking</th>
              <th class="text-center">Total Nasabah</th>
              <th class="text-center">Valid (Stok Siap)</th>
              <th class="text-center">Reject Queue</th>
              <th class="text-center">Status Pemrosesan</th>
              <th class="text-center" style="width: 200px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="file in paginatedFiles" :key="file.id">
              <td>
                <router-link :to="`/emboss/${file.id}`" class="font-monospace fw-bold text-danger text-decoration-none">
                  {{ file.filename }}
                </router-link>
                <div class="fs-9 text-secondary">Batch ID: #EB-202609-00{{ file.id }}</div>
              </td>
              <td class="text-center font-monospace fw-bold text-body">
                {{ file.total }} Kartu
              </td>
              <td class="text-center font-monospace">
                <span class="badge text-bg-success fs-9">{{ file.valid }} Kartu</span>
              </td>
              <td class="text-center font-monospace">
                <span v-if="file.rejected > 0" class="badge text-bg-danger fs-9">{{ file.rejected }} Kartu</span>
                <span v-else class="text-secondary fs-9">-</span>
              </td>
              <td class="text-center">
                <span class="badge fs-9 text-uppercase" :class="badgeClass(file.status)">
                  {{ file.status }}
                </span>
              </td>
              <td class="text-center">
                <div class="d-inline-flex align-items-center gap-1">
                  <button
                    v-if="file.status === 'VALIDATED'"
                    class="btn-action-icon text-danger"
                    @click="generateOrders(file)"
                    title="Terbitkan Pesanan Cabang Otomatis"
                  >
                    <i class="bi bi-send-plus"></i>
                  </button>
                  <router-link
                    :to="`/emboss/${file.id}`"
                    class="btn-action-icon text-secondary"
                    title="Buka Record Explorer"
                  >
                    <i class="bi bi-eye"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredFiles.length === 0">
              <td colspan="6" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada riwayat berkas emboss yang sesuai.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredFiles.length"
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

const searchQuery = ref('');
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');

const mapEmbossFile = (file) => ({
  id: file.id,
  filename: file.filename || file.batchNumber || '-',
  total: Number(file.totalRecords || 0),
  valid: Number(file.validRecords || (file.completedRecords || file.totalRecords || 0)),
  rejected: Number(file.rejectedRecords || 0),
  status: file.status || 'VALIDATED'
});

const embossFiles = ref([]);

const loadEmbossFiles = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/emboss');
    const data = response.data?.content || response.data || [];
    if (Array.isArray(data) && data.length > 0) {
      embossFiles.value = data.map(mapEmbossFile);
    }
  } catch (error) {
    embossFiles.value = [];
    console.warn('Failed loading emboss files from backend:', error);
  }
};

const filteredFiles = computed(() => {
  if (!searchQuery.value) return embossFiles.value;
  const q = searchQuery.value.toLowerCase();
  return embossFiles.value.filter(f => f.filename.toLowerCase().includes(q));
});

const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredFiles.value.slice(start, start + perPage.value);
});

const totalValidRecords = computed(() => embossFiles.value.reduce((sum, file) => sum + file.valid, 0));
const totalRejectedRecords = computed(() => embossFiles.value.reduce((sum, file) => sum + file.rejected, 0));
const readyToGenerateCount = computed(() => embossFiles.value
  .filter(file => file.status === 'VALIDATED')
  .reduce((sum, file) => sum + file.valid, 0));

const badgeClass = (status) => {
  switch (status) {
    case 'COMPLETED': return 'text-bg-success';
    case 'VALIDATED': return 'text-bg-primary';
    case 'PROCESSING': return 'text-bg-warning';
    default: return 'text-bg-secondary';
  }
};

const fileInputRef = ref(null);

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

const handleFileInputChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  try {
    await api.post('/emboss', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    await loadEmbossFiles();
    alert(`Berkas ${file.name} berhasil diproses.`);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal mengunggah berkas emboss.';
  } finally {
    event.target.value = '';
  }
};

const simulateFileUpload = () => {
  triggerFileInput();
};

const generateOrders = async (file) => {
  try {
    await api.post(`/emboss/${file.id}/generate-orders`);
    await loadEmbossFiles();
    alert('Berhasil menerbitkan Branch Orders untuk ' + file.valid + ' kartu ATM! Pesanan langsung diteruskan ke antrean Gudang & Pengiriman.');
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menerbitkan order dari berkas emboss.';
  }
};

onMounted(loadEmbossFiles);
</script>

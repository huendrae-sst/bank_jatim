<template>
  <div class="reject-queue-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Antrean Reject Personalisasi Kartu (Emboss)</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/emboss/cards" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Riwayat Berkas
        </router-link>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
        <h3 class="card-title fw-semibold mb-0 fs-6">Daftar Rekaman Reject (Perlu Perbaikan Data)</h3>
        <span class="badge text-bg-danger fs-8">{{ rejectList.length }} Rekaman Reject</span>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Nomor Rekening</th>
              <th>Nama Pada Kartu</th>
              <th>Jenis Kartu</th>
              <th>Cabang Penerbit</th>
              <th>Alasan Penolakan / Error</th>
              <th class="text-center pe-3">Aksi Perbaikan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in paginatedRejectList" :key="r.id">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ r.accountNo }}</td>
              <td class="fw-semibold text-body">{{ r.customerName }}</td>
              <td><span class="badge text-bg-secondary">{{ r.cardType }}</span></td>
              <td>{{ r.branch }}</td>
              <td class="fs-8 text-danger fw-semibold">{{ r.errorMessage }}</td>
              <td class="text-center pe-3">
                <button class="btn-action-icon text-danger" @click="reprocess(r)" title="Perbaiki &amp; Proses Ulang">
                  <i class="bi bi-arrow-clockwise"></i>
                </button>
              </td>
            </tr>
            <tr v-if="rejectList.length === 0">
              <td colspan="6" class="text-center py-4 text-secondary">
                <i class="bi bi-check-circle text-success fs-3 d-block mb-2"></i>
                Tidak ada rekaman reject. Semua data berkas emboss valid!
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="rejectList.length"
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
const errorMessage = ref('');

const rejectList = ref([]);

const loadRejectQueue = async () => {
  errorMessage.value = '';
  try {
    const res = await api.get('/emboss/reject-queue');
    rejectList.value = (res.data || []).map(r => ({
      id: r.id,
      accountNo: r.accountNumber || '-',
      customerName: r.customerName || '-',
      cardType: r.cardType || 'GPN',
      branch: r.branchCode || '-',
      errorMessage: r.rejectionReason || 'Karakter nama melebihi batas emboss (max 26 karakter)'
    }));
  } catch (err) {
    errorMessage.value = err?.message || err?.error || 'Gagal memuat antrean reject.';
    rejectList.value = [];
  }
};

const paginatedRejectList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return rejectList.value.slice(start, start + perPage.value);
});

const reprocess = async (r) => {
  const newName = prompt('Sesuaikan nama cetak pada kartu (max 26 karakter):', r.customerName.substring(0, 26));
  if (newName !== null) {
    const trimmed = newName.trim();
    if (!trimmed) {
      alert('Nama tidak boleh kosong.');
      return;
    }
    if (trimmed.length > 26) {
      alert('Nama tidak boleh melebihi 26 karakter.');
      return;
    }
    try {
      await api.put(`/emboss/records/${r.id}/reprocess`, { customerName: trimmed });
      alert(`Rekaman ${r.accountNo} berhasil diperbaiki dan dimasukkan kembali ke antrean cetak valid.`);
      await loadRejectQueue();
    } catch (err) {
      alert('Gagal memproses ulang rekaman: ' + (err?.message || err?.error || 'Server error'));
    }
  }
};

onMounted(loadRejectQueue);
</script>

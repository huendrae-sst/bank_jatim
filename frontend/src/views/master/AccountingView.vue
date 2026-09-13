<template>
  <div class="accounting-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Master Akuntansi: Chart of Accounts (COA) & Cost Center</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateModal">
          <i class="bi bi-plus-lg me-1"></i> Tambah Akun COA
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
        <h3 class="card-title fw-semibold mb-0 fs-6">Daftar Kode Rekening GL (General Ledger)</h3>
        <span class="badge text-bg-danger fs-8">{{ coaList.length }} Rekening Aktif</span>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Nomor Akun COA</th>
              <th>Nama Akun / Deskripsi</th>
              <th>Klasifikasi Akun</th>
              <th>Mata Uang</th>
              <th>Status</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in paginatedCoaList" :key="c.code">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ c.code }}</td>
              <td class="fw-semibold text-body">{{ c.name }}</td>
              <td><span class="badge text-bg-secondary">{{ c.category }}</span></td>
              <td class="font-monospace fs-8">{{ c.currency }}</td>
              <td><span class="badge text-bg-success">{{ c.status }}</span></td>
              <td class="text-center pe-3">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(c)" title="Edit"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteCoa(c)" title="Hapus"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="coaList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Tambah / Edit COA) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h6 class="modal-title fw-bold">
              {{ isEditMode ? 'Edit Rekening COA' : 'Tambah Akun COA Baru' }}
            </h6>
            <button type="button" class="btn-close btn-close-white" @click="showModal = false"></button>
          </div>
          <form @submit.prevent="saveCoa">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Nomor Akun COA <span class="text-danger">*</span></label>
                  <input type="text" v-model="coaForm.code" class="form-control form-control-sm font-monospace" placeholder="Contoh: 1.1.05.03" :disabled="isEditMode" required />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fw-bold mb-1">Nama Akun / Deskripsi <span class="text-danger">*</span></label>
                  <input type="text" v-model="coaForm.name" class="form-control form-control-sm" placeholder="Contoh: Persediaan Formulir Kliring" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Klasifikasi Akun</label>
                  <select v-model="coaForm.category" class="form-select form-select-sm">
                    <option value="ASET LANCAR">ASET LANCAR</option>
                    <option value="BEBAN OPERASIONAL">BEBAN OPERASIONAL</option>
                    <option value="KEWAJIBAN">KEWAJIBAN</option>
                    <option value="EKUITAS">EKUITAS</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Mata Uang</label>
                  <select v-model="coaForm.currency" class="form-select form-select-sm font-monospace">
                    <option value="IDR">IDR (Rupiah)</option>
                    <option value="USD">USD (US Dollar)</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Status</label>
                  <select v-model="coaForm.status" class="form-select form-select-sm">
                    <option value="AKTIF">AKTIF</option>
                    <option value="NONAKTIF">NONAKTIF</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-secondary" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold">
                <i class="bi bi-check2-circle me-1"></i> Simpan COA
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';

const showModal = ref(false);
const isEditMode = ref(false);
const editingCode = ref(null);
const currentPage = ref(1);
const perPage = ref(10);

const coaList = ref([
  { code: '1.1.05.01', name: 'Persediaan Warkat & Formulir Kantor', category: 'ASET LANCAR', currency: 'IDR', status: 'AKTIF' },
  { code: '1.1.05.02', name: 'Persediaan Kartu ATM & PIN Mailer', category: 'ASET LANCAR', currency: 'IDR', status: 'AKTIF' },
  { code: '5.2.01.01', name: 'Beban Pengadaan Cetakan & ATK', category: 'BEBAN OPERASIONAL', currency: 'IDR', status: 'AKTIF' },
  { code: '2.1.02.01', name: 'Hutang Usaha / Rekanan Vendor Logistik', category: 'KEWAJIBAN', currency: 'IDR', status: 'AKTIF' }
]);

const coaForm = reactive({
  code: '',
  name: '',
  category: 'ASET LANCAR',
  currency: 'IDR',
  status: 'AKTIF'
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingCode.value = null;
  Object.assign(coaForm, {
    code: '',
    name: '',
    category: 'ASET LANCAR',
    currency: 'IDR',
    status: 'AKTIF'
  });
  showModal.value = true;
};

const openEditModal = (c) => {
  isEditMode.value = true;
  editingCode.value = c.code;
  Object.assign(coaForm, {
    code: c.code,
    name: c.name,
    category: c.category,
    currency: c.currency,
    status: c.status
  });
  showModal.value = true;
};

const saveCoa = () => {
  if (isEditMode.value) {
    const idx = coaList.value.findIndex(c => c.code === editingCode.value);
    if (idx !== -1) {
      coaList.value[idx] = { ...coaList.value[idx], ...coaForm };
    }
  } else {
    coaList.value.unshift({
      ...coaForm
    });
  }
  showModal.value = false;
};

const deleteCoa = (c) => {
  if (confirm(`Yakin ingin menghapus kode rekening "${c.code} - ${c.name}"?`)) {
    coaList.value = coaList.value.filter(item => item.code !== c.code);
  }
};

const paginatedCoaList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return coaList.value.slice(start, start + perPage.value);
});
</script>

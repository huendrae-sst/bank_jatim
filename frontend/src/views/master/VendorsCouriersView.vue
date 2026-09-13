<template>
  <div class="vendors-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Master Vendor & Mitra Ekspedisi</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateModal">
          <i class="bi bi-plus-lg me-1"></i> Tambah Mitra / Vendor
        </button>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3 d-flex justify-content-between align-items-center">
        <h3 class="card-title fw-semibold mb-0 fs-6">Rekanan & Ekspedisi Terdaftar</h3>
        <span class="badge text-bg-danger fs-8">{{ vendors.length }} Rekanan</span>
      </div>

      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Kode Rekanan</th>
              <th>Nama Perusahaan</th>
              <th>Kategori Kemitraan</th>
              <th>Kontak PIC</th>
              <th>No. Telepon & Email</th>
              <th>Kota Operasional</th>
              <th>Status Kontrak</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in paginatedVendors" :key="v.code">
              <td class="ps-3 fw-bold font-monospace text-danger">{{ v.code }}</td>
              <td class="fw-semibold text-body">{{ v.name }}</td>
              <td><span class="badge text-bg-secondary fs-9">{{ v.type }}</span></td>
              <td class="fs-8 text-body">{{ v.pic }}</td>
              <td class="fs-8 text-secondary font-monospace">{{ v.phone }}</td>
              <td>{{ v.city }}</td>
              <td><span class="badge text-bg-success">{{ v.status }}</span></td>
              <td class="text-center pe-3">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(v)" title="Edit"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteVendor(v)" title="Hapus"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="vendors.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Tambah / Edit Vendor) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h6 class="modal-title fw-bold">
              {{ isEditMode ? 'Edit Mitra / Vendor' : 'Tambah Mitra / Vendor Baru' }}
            </h6>
            <button type="button" class="btn-close btn-close-white" @click="showModal = false"></button>
          </div>
          <form @submit.prevent="saveVendor">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kode Rekanan <span class="text-danger">*</span></label>
                  <input type="text" v-model="vendorForm.code" class="form-control form-control-sm font-monospace" placeholder="Contoh: VND-003" :disabled="isEditMode" required />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fw-bold mb-1">Nama Perusahaan / Rekanan <span class="text-danger">*</span></label>
                  <input type="text" v-model="vendorForm.name" class="form-control form-control-sm" placeholder="Contoh: PT Swadharma Duta Data" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kategori Kemitraan</label>
                  <select v-model="vendorForm.type" class="form-select form-select-sm">
                    <option value="VENDOR KARTU ATM">VENDOR KARTU ATM</option>
                    <option value="PERCETAKAN SEKURITI">PERCETAKAN SEKURITI</option>
                    <option value="EKSPEDISI LOGISTIK">EKSPEDISI LOGISTIK</option>
                    <option value="INTERNAL FLEET">INTERNAL FLEET</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">Kontak PIC <span class="text-danger">*</span></label>
                  <input type="text" v-model="vendorForm.pic" class="form-control form-control-sm" placeholder="Contoh: Anton Wijaya" required />
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fw-bold mb-1">No. Telepon & HP <span class="text-danger">*</span></label>
                  <input type="text" v-model="vendorForm.phone" class="form-control form-control-sm font-monospace" placeholder="Contoh: 0812-3456-7890" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Kota Operasional <span class="text-danger">*</span></label>
                  <input type="text" v-model="vendorForm.city" class="form-control form-control-sm" placeholder="Contoh: Surabaya" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Status Kontrak</label>
                  <select v-model="vendorForm.status" class="form-select form-select-sm">
                    <option value="AKTIF">AKTIF</option>
                    <option value="EXPIRED">EXPIRED</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-secondary" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold">
                <i class="bi bi-check2-circle me-1"></i> Simpan Rekanan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';

const showModal = ref(false);
const isEditMode = ref(false);
const editingCode = ref(null);
const currentPage = ref(1);
const perPage = ref(10);

const vendors = ref([]);

onMounted(() => {
  loadPartners();
});

const loadPartners = async () => {
  const [vendorsRes, couriersRes] = await Promise.all([
    api.get('/master/vendors'),
    api.get('/master/couriers')
  ]);

  const vendorRows = (vendorsRes.data || []).map((vendor) => ({
    code: vendor.code,
    name: vendor.name,
    type: 'VENDOR',
    pic: vendor.email || '-',
    phone: vendor.phone || '-',
    city: vendor.address || '-',
    status: vendor.isActive ? 'AKTIF' : 'NONAKTIF'
  }));

  const courierRows = (couriersRes.data || []).map((courier) => ({
    code: courier.code,
    name: courier.name,
    type: 'EKSPEDISI LOGISTIK',
    pic: '-',
    phone: courier.phone || '-',
    city: `SLA ${courier.slaDays || 0} hari`,
    status: courier.isActive ? 'AKTIF' : 'NONAKTIF'
  }));

  vendors.value = [...vendorRows, ...courierRows];
};

const vendorForm = reactive({
  code: '',
  name: '',
  type: 'VENDOR KARTU ATM',
  pic: '',
  phone: '',
  city: '',
  status: 'AKTIF'
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingCode.value = null;
  Object.assign(vendorForm, {
    code: '',
    name: '',
    type: 'VENDOR KARTU ATM',
    pic: '',
    phone: '',
    city: '',
    status: 'AKTIF'
  });
  showModal.value = true;
};

const openEditModal = (v) => {
  isEditMode.value = true;
  editingCode.value = v.code;
  Object.assign(vendorForm, {
    code: v.code,
    name: v.name,
    type: v.type,
    pic: v.pic,
    phone: v.phone,
    city: v.city,
    status: v.status
  });
  showModal.value = true;
};

const saveVendor = () => {
  alert('Penyimpanan rekanan belum tersedia di backend production.');
  showModal.value = false;
};

const deleteVendor = (v) => {
  alert(`Penghapusan rekanan "${v.name}" belum tersedia di backend production.`);
};

const paginatedVendors = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return vendors.value.slice(start, start + perPage.value);
});
</script>

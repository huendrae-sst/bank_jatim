<template>
  <div class="users-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Manajemen Pengguna & Persona</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/master/items" class="text-decoration-none text-body">Master Data</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Manajemen Pengguna</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-people-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Total Pengguna</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ userList.length }} User</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Akun Terdaftar di JIMS</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-shield-check"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Akun Aktif</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">{{ userList.filter(u => u.status === 'AKTIF').length }} User</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Hak Akses Terverifikasi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-person-badge"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Pejabat Pemutus</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-warning">{{ userList.filter(u => u.role.includes('APPROVER') || u.role.includes('ADMIN')).length }} Pejabat</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 60%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Approval Matrix Aktif</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-person-workspace"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Petugas Operasional</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">{{ userList.filter(u => u.role.includes('OFFICER') || u.role.includes('REQUESTER')).length }} Staf</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 75%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Gudang & Pemohon Cabang</span>
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
            <i class="bi bi-person-lines-fill text-danger me-2"></i>Daftar Akun Pengguna Terdaftar
          </h3>
          <span class="badge text-bg-danger fs-9">{{ userList.length }} Pengguna</span>
          <span v-if="loading" class="badge text-bg-light border text-secondary fs-9">
            <span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
            Memuat
          </span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-person-plus me-1"></i> Tambah Pengguna
          </button>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-person-badge"></i></span>
              <select v-model="filterRole" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Peran (Role)</option>
                <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterRole">
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
                placeholder="Cari Nama, Username, Email..."
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
      <div v-if="errorMessage" class="alert alert-danger rounded-0 border-start-0 border-end-0 mb-0 fs-8">
        {{ errorMessage }}
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">Nama Pegawai</th>
              <th class="py-2 text-uppercase fs-9">Username & Email</th>
              <th class="py-2 text-uppercase fs-9">Unit Kerja / Penempatan</th>
              <th class="py-2 text-uppercase fs-9">Peran Fungsional (Role)</th>
              <th class="py-2 text-uppercase fs-9">Status Akun</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in paginatedUserList" :key="u.id || u.username">
              <td class="ps-3 py-2 fw-bold text-body">
                <div class="d-flex align-items-center gap-2">
                  <div class="avatar-circle-sm">{{ u.name.substring(0, 2).toUpperCase() }}</div>
                  <span>{{ u.name }}</span>
                </div>
              </td>
              <td class="py-2">
                <div class="font-monospace fs-8 fw-bold text-danger">{{ u.username }}</div>
                <div class="fs-9 text-secondary">{{ u.email }}</div>
              </td>
              <td class="py-2 fw-semibold text-body">{{ u.organization }}</td>
              <td class="py-2"><span class="badge text-bg-light border text-secondary">{{ u.role }}</span></td>
              <td class="py-2">
                <span class="badge" :class="u.status === 'AKTIF' ? 'text-bg-success' : 'text-bg-secondary'">
                  {{ u.status || 'AKTIF' }}
                </span>
              </td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(u)" title="Edit Pengguna"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteUser(u)" title="Hapus Pengguna"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredUserList.length === 0">
              <td colspan="6" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada akun pengguna yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredUserList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Modal Form (Tambah / Edit Pengguna) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              {{ isEditMode ? 'Edit Akun Pengguna' : 'Tambah Pengguna Baru' }}
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup"><i class="bi bi-x-lg"></i></button>
          </div>
          <form @submit.prevent="saveUser">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Nama Lengkap Pegawai <span class="text-danger">*</span></label>
                  <input type="text" v-model="userForm.name" class="form-control form-control-sm" placeholder="Contoh: Achmad Subarjo" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">NIP / Username Login</label>
                  <input type="text" v-model="userForm.username" class="form-control form-control-sm font-monospace" placeholder="Contoh: 199001012020122001" />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Alamat Email Bank Jatim <span class="text-danger">*</span></label>
                  <input type="email" v-model="userForm.email" class="form-control form-control-sm" placeholder="Contoh: pegawai@bankjatim.co.id" required />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Password <span v-if="!isEditMode" class="text-danger">*</span></label>
                  <input
                    type="password"
                    v-model="userForm.password"
                    class="form-control form-control-sm"
                    :placeholder="isEditMode ? 'Kosongkan jika tidak diubah' : 'Minimal 6 karakter'"
                    :required="!isEditMode"
                  />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Unit Kerja / Kantor Cabang</label>
                  <select v-model="userForm.organizationId" class="form-select form-select-sm">
                    <option :value="null">Tidak ditentukan</option>
                    <option v-for="org in organizationOptions" :key="org.id" :value="org.id">
                      {{ org.code }} - {{ org.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Gudang / Penempatan</label>
                  <select v-model="userForm.warehouseId" class="form-select form-select-sm">
                    <option :value="null">Tidak ditentukan</option>
                    <option v-for="warehouse in warehouseOptions" :key="warehouse.id" :value="warehouse.id">
                      {{ warehouse.code }} - {{ warehouse.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Peran Fungsional (Role)</label>
                  <select v-model="userForm.role" class="form-select form-select-sm">
                    <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Status Akun</label>
                  <select v-model="userForm.status" class="form-select form-select-sm">
                    <option value="AKTIF">AKTIF</option>
                    <option value="SUSPENDED">SUSPENDED</option>
                  </select>
                </div>
              </div>
              <div v-if="formError" class="alert alert-danger mb-0 py-2 px-3">
                {{ formError }}
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
                <i v-else class="bi bi-check2-circle me-1"></i> Simpan Pengguna
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
const editingUsername = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const filterRole = ref('');
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const formError = ref('');

const userList = ref([]);
const organizationOptions = ref([]);
const warehouseOptions = ref([]);
const roles = [
  'SUPER_ADMIN',
  'USER_ADMIN',
  'MASTER_MAKER',
  'MASTER_APPROVER',
  'BUDGET_OFFICER',
  'PROCUREMENT_OFFICER',
  'PROCUREMENT_APPROVER',
  'INVENTORY_OFFICER',
  'WAREHOUSE_OFFICER',
  'REQUESTER_CABANG',
  'ORDER_APPROVER',
  'SWITCHING_APPROVER',
  'DISTRIBUTION_OFFICER',
  'RECEIVING_OFFICER',
  'FINANCE_OFFICER',
  'FINANCE_APPROVER',
  'AUDITOR',
  'MANAGEMENT'
];

const userForm = reactive({
  name: '',
  username: '',
  email: '',
  password: '',
  organizationId: null,
  warehouseId: null,
  role: 'REQUESTER_CABANG',
  status: 'AKTIF'
});

const resetFilters = () => {
  searchQuery.value = '';
  filterRole.value = '';
  currentPage.value = 1;
};

const mapUser = (user) => ({
  id: user.id,
  name: user.name || '-',
  username: user.nip || user.email?.split('@')[0] || '-',
  email: user.email || '-',
  nip: user.nip || '',
  phone: user.phone || '',
  organizationId: user.organization?.id || null,
  warehouseId: user.warehouse?.id || null,
  organization: user.organization?.name || user.warehouse?.name || '-',
  role: user.role || '-',
  status: user.isActive === false ? 'SUSPENDED' : 'AKTIF'
});

const loadReferenceData = async () => {
  const [orgResponse, warehouseResponse] = await Promise.all([
    api.get('/master/organizations'),
    api.get('/master/warehouses')
  ]);
  organizationOptions.value = orgResponse.data || [];
  warehouseOptions.value = warehouseResponse.data || [];
};

const loadUsers = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await api.get('/master/users');
    userList.value = (response.data || []).map(mapUser);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat data pengguna dari server.';
    userList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadPageData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await Promise.all([loadReferenceData(), loadUsers()]);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat data pengguna dari server.';
  } finally {
    loading.value = false;
  }
};

const filteredUserList = computed(() => {
  return userList.value.filter(u => {
    const matchQuery = !searchQuery.value ||
      u.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.organization.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchR = !filterRole.value || u.role === filterRole.value;
    return matchQuery && matchR;
  });
});

const paginatedUserList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredUserList.value.slice(start, start + perPage.value);
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingUsername.value = null;
  Object.assign(userForm, {
    name: '',
    username: '',
    email: '',
    password: '',
    organizationId: null,
    warehouseId: null,
    role: 'REQUESTER_CABANG',
    status: 'AKTIF'
  });
  formError.value = '';
  showModal.value = true;
};

const openEditModal = (u) => {
  isEditMode.value = true;
  editingUsername.value = u.username;
  Object.assign(userForm, {
    name: u.name,
    username: u.nip || u.username,
    email: u.email,
    password: '',
    organizationId: u.organizationId,
    warehouseId: u.warehouseId,
    role: u.role,
    status: u.status || 'AKTIF'
  });
  formError.value = '';
  showModal.value = true;
};

const buildUserPayload = () => ({
  name: userForm.name,
  email: userForm.email,
  nip: userForm.username || null,
  password: userForm.password || null,
  role: userForm.role,
  organizationId: userForm.organizationId || null,
  warehouseId: userForm.warehouseId || null,
  isActive: userForm.status === 'AKTIF'
});

const saveUser = async () => {
  saving.value = true;
  formError.value = '';
  try {
    const payload = buildUserPayload();
    if (isEditMode.value) {
      const user = userList.value.find(u => u.username === editingUsername.value);
      await api.put(`/master/users/${user.id}`, payload);
    } else {
      await api.post('/master/users', payload);
    }
    await loadUsers();
    showModal.value = false;
  } catch (error) {
    formError.value = error?.message || error?.error || 'Gagal menyimpan pengguna.';
  } finally {
    saving.value = false;
  }
};

const deleteUser = async (u) => {
  if (confirm(`Yakin ingin menghapus pengguna "${u.name}"?`)) {
    try {
      await api.delete(`/master/users/${u.id}`);
      await loadUsers();
    } catch (error) {
      errorMessage.value = error?.message || error?.error || 'Gagal menghapus pengguna.';
    }
  }
};

onMounted(loadPageData);
</script>

<style scoped>
.avatar-circle-sm {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background-color: var(--jatim-red, #D9252A);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}
.btn-close-modal {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  color: var(--bs-secondary);
  cursor: pointer;
}
.btn-close-modal:hover {
  color: var(--bs-dark);
}
</style>

<template>
  <div class="users-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Manajemen Pengguna</h3>
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

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            Daftar Akun Pengguna Terdaftar
          </h3>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/master/roles" class="btn btn-sm btn-outline-secondary fs-8">
            Kelola Peran
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            Tambah
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
                <option v-for="r in roleList" :key="r.code" :value="r.code">
                  {{ r.code }} - {{ r.name }}
                </option>
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
                placeholder="Cari Nama, Username, Email..."
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
                    <option v-for="org in organizationOptions" :key="org.id" :value="Number(org.id)">
                      {{ org.code }} - {{ org.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Gudang / Penempatan</label>
                  <select v-model="userForm.warehouseId" class="form-select form-select-sm">
                    <option :value="null">Tidak ditentukan</option>
                    <option v-for="warehouse in warehouseOptions" :key="warehouse.id" :value="Number(warehouse.id)">
                      {{ warehouse.code }} - {{ warehouse.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">
                    Wilayah Kerja
                    <span v-if="userForm.role === 'REGIONAL_MONITOR'" class="text-danger">*</span>
                  </label>
                  <select v-model="userForm.regionId" class="form-select form-select-sm" :required="userForm.role === 'REGIONAL_MONITOR'">
                    <option :value="null">Tidak ditentukan</option>
                    <option v-for="reg in regionOptions" :key="reg.id" :value="reg.id">
                      {{ reg.code }} - {{ reg.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Peran Fungsional (Role) <span class="text-danger">*</span></label>
                  <select v-model="userForm.role" class="form-select form-select-sm" required>
                    <option v-for="r in roleList" :key="r.code" :value="r.code">
                      {{ r.code }} - {{ r.name }}
                    </option>
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
                <span v-else>Simpan</span>
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
import { useRoleStore } from '@/stores/role';
import api from '@/api/client';
import { extractList } from '@/utils/responseParser';

const roleStore = useRoleStore();

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
const regionOptions = ref([]);

const roleList = computed(() => roleStore.roles);
const roles = computed(() => roleStore.roleCodes);

const userForm = reactive({
  name: '',
  username: '',
  email: '',
  password: '',
  organizationId: null,
  warehouseId: null,
  regionId: null,
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
  organizationId: user.organization?.id != null ? Number(user.organization.id) : (user.organizationId != null ? Number(user.organizationId) : null),
  warehouseId: user.warehouse?.id != null ? Number(user.warehouse.id) : (user.warehouseId != null ? Number(user.warehouseId) : null),
  regionId: user.region?.id != null ? Number(user.region.id) : (user.regionId != null ? Number(user.regionId) : null),
  regionName: user.region?.name || null,
  organization: user.region?.name ? `[Wilayah] ${user.region.name}` : (user.organization?.name || user.warehouse?.name || '-'),
  role: user.role || '-',
  status: user.isActive === false ? 'SUSPENDED' : 'AKTIF'
});

const loadReferenceData = async () => {
  const [orgRes, whRes, regRes] = await Promise.allSettled([
    api.get('/master/organizations'),
    api.get('/master/warehouses'),
    api.get('/master/regions')
  ]);

  if (orgRes.status === 'fulfilled') {
    const list = extractList(orgRes.value);
    if (Array.isArray(list)) organizationOptions.value = list;
  } else {
    console.warn('Failed loading organizations:', orgRes.reason);
  }

  if (whRes.status === 'fulfilled') {
    const list = extractList(whRes.value);
    if (Array.isArray(list)) warehouseOptions.value = list;
  } else {
    console.warn('Failed loading warehouses:', whRes.reason);
  }

  if (regRes.status === 'fulfilled') {
    const list = extractList(regRes.value);
    if (Array.isArray(list)) regionOptions.value = list;
  } else {
    console.warn('Failed loading regions:', regRes.reason);
  }
};

const ensureReferenceData = async () => {
  if (organizationOptions.value.length === 0 || warehouseOptions.value.length === 0) {
    await loadReferenceData();
  }
  if (warehouseOptions.value.length === 0) {
    try {
      const res = await api.get('/master/warehouses');
      const list = extractList(res);
      if (Array.isArray(list) && list.length > 0) {
        warehouseOptions.value = list;
      }
    } catch (err) {
      console.warn('Retry fetch /master/warehouses error:', err);
    }
  }
};

const loadUsers = async () => {
  try {
    const response = await api.get('/master/users');
    const rawUsers = extractList(response);
    userList.value = rawUsers ? rawUsers.map(mapUser) : [];
  } catch (err) {
    console.warn('Backend /master/users unavailable:', err);
    userList.value = [];
  }
};

const loadPageData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await Promise.allSettled([loadReferenceData(), loadUsers(), roleStore.fetchRoles()]);
  } catch (error) {
    console.warn('One or more user page requests failed:', error);
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

const openCreateModal = async () => {
  await ensureReferenceData();
  isEditMode.value = false;
  editingUsername.value = null;
  const initialRole = roles.value.includes('REQUESTER_CABANG')
    ? 'REQUESTER_CABANG'
    : (roles.value[0] || 'REQUESTER_CABANG');
  Object.assign(userForm, {
    name: '',
    username: '',
    email: '',
    password: '',
    organizationId: null,
    warehouseId: null,
    regionId: null,
    role: initialRole,
    status: 'AKTIF'
  });
  formError.value = '';
  showModal.value = true;
};

const openEditModal = async (u) => {
  await ensureReferenceData();
  isEditMode.value = true;
  editingUsername.value = u.username;
  Object.assign(userForm, {
    name: u.name,
    username: u.nip || u.username,
    email: u.email,
    password: '',
    organizationId: u.organizationId != null ? Number(u.organizationId) : null,
    warehouseId: u.warehouseId != null ? Number(u.warehouseId) : null,
    regionId: u.regionId != null ? Number(u.regionId) : null,
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
  regionId: userForm.regionId || null,
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

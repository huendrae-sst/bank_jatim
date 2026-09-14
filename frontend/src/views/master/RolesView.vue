<template>
  <div class="roles-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Manajemen Peran & Hak Akses</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/master/users" class="text-decoration-none text-body">Master Data</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Manajemen Peran</li>
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
            Daftar Peran Fungsional Terdaftar
          </h3>
          <span v-if="loading" class="badge text-bg-light border text-secondary fs-9">
            <span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
            Memuat
          </span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/master/users" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-people me-1"></i> Data Pengguna
          </router-link>
          <router-link to="/master/menus" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-menu-button-wide me-1"></i> Kelola Menu
          </router-link>
          <router-link to="/master/role-menus" class="btn btn-sm btn-outline-danger fs-8 fw-semibold">
            <i class="bi bi-shield-check me-1"></i> Mapping Role-Menu
          </router-link>
          <button class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Tambah Peran Baru
          </button>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-auto" v-if="searchQuery">
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
                placeholder="Cari Nama Peran, Kode, Deskripsi..."
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
              <th class="ps-3 py-2 text-uppercase fs-9">Kode Peran (System Key)</th>
              <th class="py-2 text-uppercase fs-9">Nama Peran Fungsional</th>
              <th class="py-2 text-uppercase fs-9">Jenis</th>
              <th class="py-2 text-uppercase fs-9">Deskripsi Tugas & Wewenang</th>
              <th class="text-center py-2 text-uppercase fs-9">Pengguna Terkait</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in paginatedRoleList" :key="r.id || r.code">
              <td class="ps-3 py-2">
                <span class="font-monospace fw-bold text-danger">{{ r.code }}</span>
              </td>
              <td class="py-2 fw-bold text-body">
                <div class="d-flex align-items-center gap-2">
                  <div class="avatar-circle-sm">{{ r.name.substring(0, 2).toUpperCase() }}</div>
                  <span>{{ r.name }}</span>
                </div>
              </td>
              <td class="py-2">
                <span class="badge" :class="r.systemRole ? 'text-bg-secondary' : 'text-bg-info'">
                  {{ r.systemRole ? 'Sistem' : 'Kustom' }}
                </span>
              </td>
              <td class="py-2 text-secondary fs-9" style="max-width: 320px;">
                {{ r.description || '-' }}
              </td>
              <td class="text-center py-2">
                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle font-monospace">
                  {{ getUserCount(r.code) }} User
                </span>
              </td>
              <td class="text-center pe-3 py-2">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(r)" title="Edit Peran">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button
                    class="btn-action-icon text-danger"
                    :disabled="r.systemRole"
                    @click="deleteRoleItem(r)"
                    :title="r.systemRole ? 'Role sistem tidak dapat dihapus' : 'Hapus Peran'"
                  >
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRoleList.length === 0">
              <td colspan="6" class="text-center py-4 text-secondary">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Tidak ada peran fungsional yang sesuai dengan filter pencarian.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredRoleList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Modal Form (Tambah / Edit Peran) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              {{ isEditMode ? 'Edit Peran Fungsional' : 'Tambah Peran Baru' }}
            </h6>
            <button type="button" class="btn-close-modal" @click="showModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="saveRole">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Kode Peran (System Key) <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="roleForm.code"
                    class="form-control form-control-sm font-monospace text-uppercase"
                    placeholder="Contoh: AUDIT_OFFICER"
                    :disabled="isEditMode"
                    required
                  />
                  <div class="fs-9 text-secondary mt-1">Gunakan huruf kapital dan garis bawah (snake_case).</div>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Nama Peran Fungsional <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="roleForm.name"
                    class="form-control form-control-sm"
                    placeholder="Contoh: Petugas Audit Lapangan"
                    required
                  />
                </div>
                <div class="col-12">
                  <label class="form-label fw-bold mb-1">Deskripsi Tugas & Hak Akses</label>
                  <textarea
                    v-model="roleForm.description"
                    class="form-control form-control-sm"
                    rows="3"
                    placeholder="Jelaskan wewenang dan cakupan peran ini dalam proses bisnis JIMS..."
                  ></textarea>
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
                <i v-else class="bi bi-check2-circle me-1"></i> Simpan Peran
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

const roleStore = useRoleStore();

const showModal = ref(false);
const isEditMode = ref(false);
const editingRoleCode = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const formError = ref('');

const usersList = ref([]);

const roleForm = reactive({
  code: '',
  name: '',
  description: ''
});

const roleList = computed(() => roleStore.roles);
const getUserCount = (roleCode) => {
  if (!usersList.value.length) return 0;
  return usersList.value.filter((u) => u.role === roleCode).length;
};

const resetFilters = () => {
  searchQuery.value = '';
  currentPage.value = 1;
};

const loadUsers = async () => {
  try {
    const res = await api.get('/master/users');
    usersList.value = res.data || [];
  } catch {
    usersList.value = [];
  }
};

const loadData = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await Promise.all([roleStore.fetchRoles(), loadUsers()]);
  } catch (err) {
    errorMessage.value = err?.message || 'Gagal memuat data peran.';
  } finally {
    loading.value = false;
  }
};

const filteredRoleList = computed(() => {
  return roleList.value.filter((r) => {
    const q = searchQuery.value.toLowerCase().trim();
    const matchQuery =
      !q ||
      r.code.toLowerCase().includes(q) ||
      r.name.toLowerCase().includes(q) ||
      (r.description && r.description.toLowerCase().includes(q));
    return matchQuery;
  });
});

const paginatedRoleList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredRoleList.value.slice(start, start + perPage.value);
});

const openCreateModal = () => {
  isEditMode.value = false;
  editingRoleCode.value = null;
  Object.assign(roleForm, {
    code: '',
    name: '',
    description: ''
  });
  formError.value = '';
  showModal.value = true;
};

const openEditModal = (r) => {
  isEditMode.value = true;
  editingRoleCode.value = r.code;
  Object.assign(roleForm, {
    code: r.code,
    name: r.name,
    description: r.description || ''
  });
  formError.value = '';
  showModal.value = true;
};

const saveRole = async () => {
  saving.value = true;
  formError.value = '';
  try {
    if (isEditMode.value) {
      await roleStore.updateRole(editingRoleCode.value, roleForm);
    } else {
      await roleStore.addRole(roleForm);
    }
    showModal.value = false;
  } catch (err) {
    formError.value = err?.message || 'Gagal menyimpan data peran.';
  } finally {
    saving.value = false;
  }
};

const deleteRoleItem = async (r) => {
  if (r.systemRole) return;
  const userCount = getUserCount(r.code);
  if (userCount > 0) {
    alert(
      `Peran "${r.name}" (${r.code}) saat ini digunakan oleh ${userCount} pengguna. Harap pindahkan peran pengguna terkait terlebih dahulu.`
    );
    return;
  }

  if (confirm(`Yakin ingin menghapus peran "${r.name}" (${r.code})?`)) {
    try {
      await roleStore.deleteRole(r.code);
    } catch (err) {
      errorMessage.value = err?.message || 'Gagal menghapus peran.';
    }
  }
};

onMounted(loadData);
</script>

<style scoped>
.avatar-circle-sm {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background-color: var(--jatim-red, #d9252a);
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

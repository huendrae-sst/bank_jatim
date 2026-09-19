<template>
  <div class="expedition-mappings-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Pemetaan Rute Ekspedisi ke Cabang</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-danger fw-bold shadow-xs" @click="openCreateModal">
            Tambah
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Table Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th class="ps-3">Kantor Cabang Tujuan</th>
              <th>Ekspedisi Utama (Default)</th>
              <th>Tipe Layanan</th>
              <th class="text-end">Estimasi SLA Waktu</th>
              <th class="text-center pe-3">Aksi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in paginatedMappings" :key="m.id">
              <td class="ps-3 fw-bold text-body">{{ m.branch }}</td>
              <td class="fw-semibold text-danger">{{ m.primaryCourier }}</td>
              <td><span class="badge text-bg-secondary">{{ m.serviceType }}</span></td>
              <td class="text-end font-monospace fw-bold">{{ m.sla }} Hari</td>
              <td class="text-center pe-3">
                <div class="d-inline-flex gap-1">
                  <button class="btn-action-icon text-secondary" @click="openEditModal(m)" title="Edit"><i class="bi bi-pencil"></i></button>
                  <button class="btn-action-icon text-danger" @click="deleteMapping(m)" title="Hapus"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
            <tr v-if="mappings.length === 0">
              <td colspan="5" class="text-center py-4 text-secondary fs-8">
                <i class="bi bi-inbox fs-3 d-block mb-1"></i>
                Belum ada pemetaan ekspedisi dari backend.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="mappings.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form (Tambah / Edit Pemetaan Rute) -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-danger text-white py-2 px-3">
            <h6 class="modal-title fw-bold">
              {{ isEditMode ? 'Edit Pemetaan Rute Ekspedisi' : 'Tambah Pemetaan Rute Baru' }}
            </h6>
            <button type="button" class="btn-close btn-close-white" @click="showModal = false"></button>
          </div>
          <form @submit.prevent="saveMapping">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Kantor Cabang Tujuan <span class="text-danger">*</span></label>
                  <select v-model="mappingForm.destinationOrganizationId" class="form-select form-select-sm" required>
                    <option value="">Pilih cabang tujuan</option>
                    <option v-for="org in organizations" :key="org.id" :value="org.id">
                      {{ org.code }} - {{ org.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Ekspedisi Utama <span class="text-danger">*</span></label>
                  <select v-model="mappingForm.courierId" class="form-select form-select-sm" required>
                    <option value="">Pilih kurir</option>
                    <option v-for="courier in couriers" :key="courier.id" :value="courier.id">
                      {{ courier.code }} - {{ courier.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Tipe Layanan</label>
                  <input type="text" v-model="mappingForm.serviceType" class="form-control form-control-sm" placeholder="REGULER" />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fw-bold mb-1">Estimasi SLA Waktu (Hari) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="mappingForm.sla" class="form-control form-control-sm font-monospace" min="1" max="14" required />
                </div>
              </div>
            </div>
            <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
              <button type="button" class="btn btn-sm btn-secondary" @click="showModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-3">
                Simpan
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
const editingId = ref(null);
const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');

const mapMapping = (mapping) => ({
  id: mapping.id,
  destinationOrganizationId: mapping.destinationOrganization?.id || '',
  courierId: mapping.courier?.id || '',
  branch: mapping.destinationOrganization?.name || mapping.branch || '-',
  primaryCourier: mapping.courier?.name || mapping.primaryCourier || '-',
  serviceType: mapping.serviceType || 'REGULER',
  sla: Number(mapping.estimatedLeadDays || mapping.sla || 1)
});

const mappings = ref([]);
const organizations = ref([]);
const couriers = ref([]);

const mappingForm = reactive({
  destinationOrganizationId: '',
  courierId: '',
  serviceType: 'REGULER',
  sla: 1
});

const loadOptions = async () => {
  try {
    const [organizationResponse, courierResponse] = await Promise.allSettled([
      api.get('/master/organizations'),
      api.get('/master/couriers')
    ]);
    if (organizationResponse.status === 'fulfilled') {
      const orgData = organizationResponse.value?.data?.data || organizationResponse.value?.data || [];
      organizations.value = Array.isArray(orgData) ? orgData : [];
    }
    if (courierResponse.status === 'fulfilled') {
      const crData = courierResponse.value?.data?.data || courierResponse.value?.data || [];
      couriers.value = Array.isArray(crData) ? crData : [];
    }
  } catch (err) {
    console.warn('Failed to load options from backend:', err);
    organizations.value = [];
    couriers.value = [];
  }
};

const loadMappings = async () => {
  try {
    const response = await api.get('/master/expedition-mappings');
    const items = response.data?.data || response.data || [];
    mappings.value = Array.isArray(items) ? items.map(mapMapping) : [];
  } catch (err) {
    console.warn('Backend /master/expedition-mappings unavailable:', err);
    mappings.value = [];
  }
};

const loadPage = async () => {
  errorMessage.value = '';
  try {
    await Promise.allSettled([loadOptions(), loadMappings()]);
  } catch (error) {
    console.warn('One or more expedition mapping requests failed:', error);
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  editingId.value = null;
  Object.assign(mappingForm, {
    destinationOrganizationId: '',
    courierId: '',
    serviceType: 'REGULER',
    sla: 1
  });
  showModal.value = true;
};

const openEditModal = (m) => {
  isEditMode.value = true;
  editingId.value = m.id;
  Object.assign(mappingForm, {
    destinationOrganizationId: m.destinationOrganizationId,
    courierId: m.courierId,
    serviceType: m.serviceType,
    sla: m.sla
  });
  showModal.value = true;
};

const buildPayload = () => ({
  destinationOrganizationId: mappingForm.destinationOrganizationId,
  courierId: mappingForm.courierId,
  serviceType: mappingForm.serviceType || 'REGULER',
  estimatedLeadDays: mappingForm.sla
});

const saveMapping = async () => {
  errorMessage.value = '';
  try {
    if (isEditMode.value) {
      await api.put(`/master/expedition-mappings/${editingId.value}`, buildPayload());
    } else {
      await api.post('/master/expedition-mappings', buildPayload());
    }
    await loadMappings();
    showModal.value = false;
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menyimpan pemetaan ekspedisi.';
  }
};

const deleteMapping = async (m) => {
  if (confirm(`Yakin ingin menghapus rute ke "${m.branch}"?`)) {
    errorMessage.value = '';
    try {
      await api.delete(`/master/expedition-mappings/${m.id}`);
      await loadMappings();
    } catch (error) {
      errorMessage.value = error?.message || error?.error || 'Gagal menghapus pemetaan ekspedisi.';
    }
  }
};

const paginatedMappings = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return mappings.value.slice(start, start + perPage.value);
});

onMounted(loadPage);
</script>

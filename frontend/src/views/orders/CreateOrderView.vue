<template>
  <div class="create-order-page space-y-3">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Buat Order Permintaan Barang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/orders" class="text-decoration-none text-danger">Orders</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Buat Order Baru</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Form Card -->
    <div class="card card-outline card-danger shadow-xs">
      <form @submit.prevent="submitOrder">
        <div class="card-header border-bottom d-flex flex-column flex-sm-row sm:items-center justify-content-between gap-2 py-3 px-3 px-md-4">
          <h3 class="card-title fs-6 fw-bold mb-0 text-body">
            Informasi Permintaan & Logistik
          </h3>
          <div class="card-tools w-100 w-sm-auto">
            <router-link to="/orders" class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center justify-content-center gap-1 w-100 w-sm-auto">
              <span>Kembali ke Daftar</span>
            </router-link>
          </div>
        </div>

        <div class="card-body p-4 space-y-4">
          <div class="row g-3">
            <!-- Unit Peminta -->
            <div class="col-12 col-md-4">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Unit Kerja Peminta</label>
              <select v-model="selectedOrgId" required class="form-select fs-7">
                <option v-for="org in organizations" :key="org.id" :value="org.id">
                  {{ org.code }} - {{ org.name }}
                </option>
              </select>
            </div>

            <!-- Prioritas -->
            <div class="col-12 col-md-4">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Prioritas Permintaan</label>
              <select v-model="priority" required class="form-select fs-7">
                <option value="NORMAL">Normal (Rutin / Bulanan)</option>
                <option value="HIGH">High (Tinggi)</option>
                <option value="URGENT">Urgent (Mendesak / Stok Teller Menipis)</option>
              </select>
            </div>

            <!-- Target Tanggal Dibutuhkan -->
            <div class="col-12 col-md-4">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Target Tanggal Dibutuhkan</label>
              <input type="date" v-model="requiredDate" required class="form-control fs-7" />
            </div>

            <!-- Catatan -->
            <div class="col-12">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan / Keterangan Order</label>
              <textarea v-model="notes" rows="2" class="form-control fs-7" placeholder="Tuliskan catatan kebutuhan logistik atau nomor disposisi cabang..."></textarea>
            </div>
          </div>

          <!-- Items Dynamic Table -->
          <div class="border-top pt-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h5 class="fw-bold fs-6 mb-0 text-body">
                  Daftar Barang yang Diminta
                </h5>
                <span class="fs-9 text-muted">Pastikan jumlah dan estimasi nilai tidak melebihi sisa anggaran cabang</span>
              </div>
              <button type="button" @click="addRow" class="btn btn-sm btn-outline-danger fw-semibold">
                Tambah
              </button>
            </div>

            <div class="table-responsive border rounded bg-body mb-2" style="max-height: 280px; overflow-y: auto;">
              <table class="table table-sm table-hover align-middle mb-0 fs-8">
                <thead class="table-light text-secondary fs-9 text-uppercase sticky-top">
                  <tr>
                    <th class="ps-3 py-1.5">Pilih Barang / Item</th>
                    <th class="text-end py-1.5" style="width: 140px;">Estimasi Harga</th>
                    <th class="text-center py-1.5" style="width: 130px;">Jumlah (Qty)</th>
                    <th class="text-end py-1.5" style="width: 150px;">Subtotal</th>
                    <th class="text-center py-1.5" style="width: 45px;">Hapus</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, index) in rows" :key="index">
                    <td class="ps-3 py-1.5">
                      <select v-model="row.item_id" class="form-select form-select-sm" required>
                        <option value="">-- Pilih Barang / Item --</option>
                        <option v-for="it in itemsCatalog" :key="it.id" :value="it.id">
                          {{ it.name }} [{{ it.sku }}] ({{ it.uom }})
                        </option>
                      </select>
                    </td>
                    <td class="text-end py-1.5 font-monospace text-muted">
                      {{ formatRupiah(getItemPrice(row.item_id)) }}
                    </td>
                    <td class="text-center py-1.5">
                      <input 
                        type="number" 
                        v-model.number="row.qty" 
                        min="1" 
                        required 
                        class="form-control form-control-sm text-center font-monospace fw-bold fs-8" 
                      />
                    </td>
                    <td class="text-end py-1.5 font-monospace fw-semibold">
                      {{ formatRupiah(getItemPrice(row.item_id) * (row.qty || 0)) }}
                    </td>
                    <td class="text-center py-1.5">
                      <button 
                        type="button" 
                        @click="removeRow(index)" 
                        :disabled="rows.length <= 1" 
                        class="btn btn-sm text-danger py-0 px-1" 
                        title="Hapus baris barang"
                      >
                        <i class="bi bi-trash fs-8"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Real-Time Budget & Order Estimate Summary -->
            <div class="card bg-light border-0 shadow-none mt-3 p-3">
              <div class="row g-3 align-items-center">
                <div class="col-12 col-md-3">
                  <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Estimasi Total Order</span>
                  <span class="fs-6 fw-bold text-dark font-monospace">{{ formatRupiah(totalOrderEstimate) }}</span>
                </div>
                <div v-if="currentBudget" class="col-12 col-md-9">
                  <div class="row g-2">
                    <div class="col-6 col-md-4">
                      <span class="fs-9 text-uppercase text-secondary d-block">Plafon Anggaran:</span>
                      <span class="fs-8 fw-semibold text-secondary font-monospace">{{ formatRupiah(currentBudget.allocated_amount) }}</span>
                    </div>
                    <div class="col-6 col-md-4">
                      <span class="fs-9 text-uppercase text-secondary d-block">Sisa Plafon Tersedia:</span>
                      <span 
                        class="fs-8 fw-bold font-monospace" 
                        :class="budgetAvailable < totalOrderEstimate ? 'text-danger' : 'text-success'"
                      >
                        {{ formatRupiah(budgetAvailable) }}
                      </span>
                    </div>
                    <div class="col-12 col-md-4">
                      <span class="fs-9 text-uppercase text-secondary d-block">Proyeksi Utilisasi:</span>
                      <div class="d-flex align-items-center gap-2">
                        <div class="progress flex-grow-1" style="height: 8px;">
                          <div 
                            class="progress-bar" 
                            :class="{
                              'bg-success': projectedUtilization <= 80,
                              'bg-warning': projectedUtilization > 80 && projectedUtilization <= 100,
                              'bg-danger': projectedUtilization > 100
                            }" 
                            role="progressbar" 
                            :style="'width: ' + Math.min(100, projectedUtilization) + '%'"
                          ></div>
                        </div>
                        <span 
                          class="fs-8 fw-bold font-monospace" 
                          :class="projectedUtilization > 100 ? 'text-danger' : 'text-dark'"
                        >
                          {{ projectedUtilization }}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="col-12 col-md-9 text-muted fs-8">
                  <i class="bi bi-info-circle me-1"></i> Data alokasi anggaran tahun berjalan belum diset untuk unit kerja ini.
                </div>
              </div>

              <!-- Warning Overbudget Banner -->
              <div v-if="isOverbudget" class="alert alert-warning border border-warning d-flex align-items-center gap-2 mt-3 mb-0 py-2 px-3 fs-8">
                <i class="bi bi-exclamation-triangle-fill text-warning fs-5"></i>
                <div>
                  <strong>PERINGATAN OVERBUDGET:</strong> Nilai order melebihi sisa plafon anggaran cabang (proyeksi utilisasi <span class="fw-bold text-danger">{{ projectedUtilization }}%</span>). Order ini akan otomatis ditandai status <strong>OVERBUDGET</strong> dan memerlukan persetujuan khusus serta catatan dispensasi dari Checker/Pimpinan.
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Card Footer Submit -->
        <div class="card-footer bg-body border-top d-flex flex-column-reverse flex-sm-row justify-content-end align-items-stretch align-items-sm-center gap-2 py-3 px-3 px-md-4">
          <button type="button" @click="resetForm" class="btn btn-sm btn-outline-secondary px-3 fw-semibold d-inline-flex align-items-center justify-content-center gap-1">
            <span>Batal</span>
          </button>
          <button type="submit" :disabled="submitting" class="btn btn-sm btn-danger px-4 fw-bold shadow-xs d-inline-flex align-items-center justify-content-center gap-1">
            Simpan
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/client';
import { toast } from '@/utils/toast';

const router = useRouter();
const submitting = ref(false);

const organizations = ref([]);

const selectedOrgId = ref(null);
const priority = ref('NORMAL');

const defaultDate = new Date();
defaultDate.setDate(defaultDate.getDate() + 3);
const requiredDate = ref(defaultDate.toISOString().split('T')[0]);
const notes = ref('');

const itemsCatalog = ref([]);

const budgetsCatalog = ref({});

const rows = ref([{ item_id: '', qty: 1 }]);

const loadOptions = async () => {
  errorMessage.value = '';
  try {
    const [orgResponse, itemResponse, budgetResponse] = await Promise.all([
      api.get('/master/organizations'),
      api.get('/master/items'),
      api.get('/master/budgets')
    ]);
    organizations.value = orgResponse.data || [];
    itemsCatalog.value = itemResponse.data || [];
    selectedOrgId.value = organizations.value[0]?.id || null;
    budgetsCatalog.value = (budgetResponse.data || []).reduce((acc, budget) => {
      const orgId = budget.organization?.id;
      if (!orgId) return acc;
      const current = acc[orgId] || { allocated_amount: 0, committed_amount: 0, realized_amount: 0 };
      current.allocated_amount += Number(budget.allocatedAmount || 0);
      current.committed_amount += Number(budget.committedAmount || 0);
      current.realized_amount += Number(budget.realizedAmount || 0);
      acc[orgId] = current;
      return acc;
    }, {});
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal memuat master data order.', 'Gagal');
  }
};

const addRow = () => {
  rows.value.push({ item_id: '', qty: 1 });
};

const removeRow = (index) => {
  if (rows.value.length > 1) {
    rows.value.splice(index, 1);
  }
};

const resetForm = () => {
  rows.value = [{ item_id: '', qty: 1 }];
  notes.value = '';
};

const getItem = (id) => {
  return itemsCatalog.value.find(it => String(it.id) === String(id));
};

const getItemPrice = (id) => {
  const it = getItem(id);
  return it ? parseFloat(it.estimatedUnitPrice || it.estimated_unit_price || 0) : 0;
};

const currentBudget = computed(() => {
  return budgetsCatalog.value[selectedOrgId.value] || null;
});

const totalOrderEstimate = computed(() => {
  return rows.value.reduce((sum, row) => {
    if (!row.item_id || !row.qty) return sum;
    return sum + (getItemPrice(row.item_id) * parseInt(row.qty || 0));
  }, 0);
});

const budgetAvailable = computed(() => {
  const b = currentBudget.value;
  if (!b) return 0;
  const allocated = parseFloat(b.allocated_amount || 0);
  const committed = parseFloat(b.committed_amount || 0);
  const realized = parseFloat(b.realized_amount || 0);
  return Math.max(0, allocated - committed - realized);
});

const projectedUtilization = computed(() => {
  const b = currentBudget.value;
  if (!b) return 0;
  const allocated = parseFloat(b.allocated_amount || 0);
  if (allocated <= 0) return 0;
  const committed = parseFloat(b.committed_amount || 0);
  const realized = parseFloat(b.realized_amount || 0);
  const totalOrder = totalOrderEstimate.value;
  return Math.round(((committed + realized + totalOrder) / allocated) * 100 * 10) / 10;
});

const isOverbudget = computed(() => {
  return projectedUtilization.value > 100;
});

const formatRupiah = (val) => {
  return 'Rp ' + new Intl.NumberFormat('id-ID').format(Math.round(val || 0));
};

const submitOrder = async () => {
  submitting.value = true;
  try {
    const response = await api.post('/orders', {
      organizationId: selectedOrgId.value,
      priority: priority.value,
      requiredDate: requiredDate.value,
      notes: notes.value,
      items: rows.value
        .filter(row => row.item_id && row.qty)
        .map(row => ({ itemId: row.item_id, qty: row.qty }))
    });
    toast.success('Pesanan berhasil dibuat.', 'Berhasil');
    router.push(`/orders/${response.data.id}`);
  } catch (error) {
    toast.error(error?.message || error?.error || 'Gagal membuat order.', 'Gagal');
  } finally {
    submitting.value = false;
  }
};

onMounted(loadOptions);
</script>

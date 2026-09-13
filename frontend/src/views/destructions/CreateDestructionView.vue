<template>
  <div class="content-wrapper p-3 p-md-4">
    <div class="row justify-content-center">
      <div class="col-12 col-xl-10">
        <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
          <div>
            <h3 class="mb-0 text-body fw-bold">Form Pengajuan Pemusnahan Barang</h3>
            <nav aria-label="breadcrumb">
              <ol class="breadcrumb mb-0 fs-8">
                <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
                <li class="breadcrumb-item"><router-link to="/destructions" class="text-decoration-none text-danger">Pemusnahan Barang</router-link></li>
                <li class="breadcrumb-item active text-secondary" aria-current="page">Pengajuan Baru</li>
              </ol>
            </nav>
          </div>
          <router-link to="/destructions" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i> Batal
          </router-link>
        </div>

        <div class="card card-outline card-danger shadow-xs">
          <div class="card-header bg-white py-3 border-bottom">
            <h5 class="card-title fw-bold mb-1">Pengajuan Pemusnahan Barang Terkontrol</h5>
            <p class="text-secondary fs-8 mb-0">Formulir pemusnahan resmi persediaan rusak permanen, chip kartu kadaluarsa, atau formulir/buku discontinue.</p>
          </div>

          <form @submit.prevent="submitDestruction">
            <div class="card-body p-4 space-y-4">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Lokasi Gudang Pemusnahan <span class="text-danger">*</span></label>
                  <select v-model="form.warehouse_id" class="form-select fs-7" required>
                    <option value="">-- Pilih Lokasi Gudang --</option>
                    <option v-for="wh in warehouses" :key="wh.id" :value="String(wh.id)">
                      {{ wh.code }} - {{ wh.name }} ({{ wh.type }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Alasan Pemusnahan <span class="text-danger">*</span></label>
                  <select v-model="form.reason" class="form-select fs-7" required>
                    <option value="EXPIRED_CHIP">Chip Kartu ATM / KUE Kadaluarsa (Expired)</option>
                    <option value="DAMAGED_UNUSABLE">Barang Rusak Total Tidak Dapat Dipakai</option>
                    <option value="DISCONTINUED_DESIGN">Desain / Format Discontinue (Tidak Berlaku)</option>
                    <option value="FAILED_EMBOSS">Gagal Proses Personalisasi / Cacat Emboss</option>
                    <option value="OTHER">Lainnya</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-semibold text-dark">Penjelasan & Kronologi Pemusnahan</label>
                  <textarea v-model="form.reason_details" class="form-control fs-7" rows="2" placeholder="Uraikan latar belakang teknis atau dasar persetujuan pemusnahan..."></textarea>
                </div>
              </div>

              <hr class="my-4">

              <!-- Data Saksi Pemusnahan (Wajib 2 Pejabat/Petugas) -->
              <h6 class="fw-bold mb-3 text-dark"><i class="bi bi-people me-2"></i>Data Saksi Pemusnahan (Wajib 2 Pejabat/Petugas)</h6>
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <div class="card p-3 bg-light border-0">
                    <span class="fs-8 fw-bold text-secondary mb-2 d-block">Saksi 1 (Pejabat Unit Kerja / Kepatuhan)</span>
                    <div class="mb-2">
                      <label class="form-label fs-9 fw-semibold mb-1">Nama Lengkap Saksi 1 <span class="text-danger">*</span></label>
                      <input type="text" v-model="form.witness_name_1" class="form-control form-control-sm" placeholder="Contoh: Achmad Soebarjo" required>
                    </div>
                    <div>
                      <label class="form-label fs-9 fw-semibold mb-1">Jabatan Saksi 1 <span class="text-danger">*</span></label>
                      <input type="text" v-model="form.witness_title_1" class="form-control form-control-sm" placeholder="Contoh: Pemimpin Cabang Pembantu" required>
                    </div>
                  </div>
                </div>
                <div class="col-12 col-md-6">
                  <div class="card p-3 bg-light border-0">
                    <span class="fs-8 fw-bold text-secondary mb-2 d-block">Saksi 2 (Petugas Logistik / Keamanan)</span>
                    <div class="mb-2">
                      <label class="form-label fs-9 fw-semibold mb-1">Nama Lengkap Saksi 2 <span class="text-danger">*</span></label>
                      <input type="text" v-model="form.witness_name_2" class="form-control form-control-sm" placeholder="Contoh: Bambang Irawan" required>
                    </div>
                    <div>
                      <label class="form-label fs-9 fw-semibold mb-1">Jabatan Saksi 2 <span class="text-danger">*</span></label>
                      <input type="text" v-model="form.witness_title_2" class="form-control form-control-sm" placeholder="Contoh: Supervisor Operasional / Audit" required>
                    </div>
                  </div>
                </div>
              </div>

              <hr class="my-4">

              <!-- Item Barang yang Dimusnahkan -->
              <div class="d-flex align-items-center justify-content-between mb-3">
                <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-box-seam me-2"></i>Item Barang yang Dimusnahkan</h6>
                <button type="button" class="btn btn-sm btn-outline-danger py-1 px-2 fs-8 fw-semibold" @click="addItem">
                  <i class="bi bi-plus-circle me-1"></i> Tambah Item
                </button>
              </div>

              <div class="table-responsive border rounded-3">
                <table class="table table-bordered align-middle mb-0 fs-8">
                  <thead class="table-light text-secondary">
                    <tr>
                      <th style="width: 40%">Pilih Barang Persediaan</th>
                      <th style="width: 15%" class="text-center">Jumlah Unit</th>
                      <th style="width: 25%">No. Batch / Nomor Seri</th>
                      <th>Keterangan Kerusakan</th>
                      <th style="width: 5%" class="text-center">Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, index) in form.items" :key="index">
                      <td>
                        <select v-model="row.item_id" class="form-select form-select-sm" required>
                          <option value="">-- Pilih Barang --</option>
                          <option v-for="it in availableItems" :key="it.id" :value="String(it.id)">
                            {{ it.sku }} - {{ it.name }} ({{ it.uom }})
                          </option>
                        </select>
                      </td>
                      <td>
                        <input type="number" v-model.number="row.qty" class="form-control form-control-sm text-center font-monospace fw-bold" min="1" required>
                      </td>
                      <td>
                        <input type="text" v-model="row.batch" class="form-control form-control-sm font-monospace" placeholder="Contoh: BATCH-2025-Q1">
                      </td>
                      <td>
                        <input type="text" v-model="row.notes" class="form-control form-control-sm" placeholder="Contoh: Chip korosi / cetakan cacat">
                      </td>
                      <td class="text-center">
                        <button type="button" class="btn btn-outline-danger btn-sm p-1" @click="removeItem(index)" v-if="form.items.length > 1">
                          <i class="bi bi-trash"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="card-footer bg-light p-3 d-flex justify-content-between align-items-center">
              <router-link to="/destructions" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-arrow-left me-1"></i> Batal
              </router-link>
              <button type="submit" :disabled="submitting" class="btn btn-danger btn-sm px-4 shadow-xs">
                <i class="bi bi-send me-1"></i> Kirim Pengajuan Pemusnahan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/client';

const router = useRouter();
const submitting = ref(false);
const warehouses = ref([]);
const availableItems = ref([]);

const form = reactive({
  warehouse_id: '',
  reason: 'EXPIRED_CHIP',
  reason_details: '',
  witness_name_1: 'Achmad Soebarjo',
  witness_title_1: 'Pemimpin Cabang Pembantu',
  witness_name_2: 'Bambang Irawan',
  witness_title_2: 'Supervisor Operasional / Audit',
  items: [
    { item_id: '', qty: 50, batch: 'BATCH-2024-EXP', notes: 'Chip tidak terbaca di terminal EDC' }
  ]
});

const loadMasterData = async () => {
  try {
    const [whRes, itRes] = await Promise.all([
      api.get('/master/warehouses'),
      api.get('/master/items')
    ]);
    warehouses.value = whRes.data?.data || whRes.data || [];
    availableItems.value = itRes.data?.data || itRes.data || [];

    if (warehouses.value.length > 0) {
      form.warehouse_id = String(warehouses.value[0].id);
    }
    if (availableItems.value.length > 0 && form.items.length > 0) {
      form.items[0].item_id = String(availableItems.value[0].id);
    }
  } catch (err) {
    console.error('Failed to load master data for destruction', err);
  }
};

const addItem = () => {
  const defaultItemId = availableItems.value.length > 0 ? String(availableItems.value[0].id) : '';
  form.items.push({ item_id: defaultItemId, qty: 1, batch: '', notes: '' });
};

const removeItem = (index) => {
  if (form.items.length > 1) {
    form.items.splice(index, 1);
  }
};

const submitDestruction = async () => {
  submitting.value = true;
  try {
    const baNo = `BA-MUSNAH-2026-${Date.now().toString().slice(-4)}`;
    for (const item of form.items) {
      await api.post('/inventory/destructions', {
        warehouseId: Number(form.warehouse_id),
        itemId: Number(item.item_id),
        qty: Number(item.qty),
        baNo,
        notes: `${form.reason} - ${item.notes || form.reason_details || 'Pemusnahan barang persediaan'}`
      });
    }
    alert('Pengajuan pemusnahan barang berhasil dikirim dan dicatat!');
    router.push('/destructions');
  } catch (err) {
    console.error('Failed to submit destruction', err);
    alert('Gagal mengirim pengajuan pemusnahan: ' + (err.response?.data?.message || err.message));
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadMasterData();
});
</script>

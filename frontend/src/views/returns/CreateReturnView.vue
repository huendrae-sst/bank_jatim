<template>
  <div class="content-wrapper p-3 p-md-4">
    <div class="row justify-content-center">
      <div class="col-12 col-xl-10">
        <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
          <div>
            <h3 class="mb-0 text-body fw-bold">Form Pengajuan Retur Barang</h3>
            <nav aria-label="breadcrumb">
              <ol class="breadcrumb mb-0 fs-8">
                <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
                <li class="breadcrumb-item"><router-link to="/returns" class="text-decoration-none text-danger">Retur Barang</router-link></li>
                <li class="breadcrumb-item active text-secondary" aria-current="page">Pengajuan Baru</li>
              </ol>
            </nav>
          </div>
          <router-link to="/returns" class="btn btn-sm btn-outline-secondary">
            Batal
          </router-link>
        </div>

        <div class="card card-outline card-danger shadow-xs">
          <div class="card-header bg-white py-3 border-bottom">
            <h5 class="card-title fw-bold mb-1">Pengajuan Retur Barang Persediaan</h5>
            <p class="text-secondary fs-8 mb-0">Formulir pengembalian barang rusak, cacat, atau tidak sesuai dari kantor cabang ke gudang logistik pusat.</p>
          </div>

          <form @submit.prevent="submitReturn">
            <div class="card-body p-4 space-y-4">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Gudang Asal Cabang <span class="text-danger">*</span></label>
                  <select v-model="form.origin_warehouse_id" class="form-select fs-7" required>
                    <option value="">-- Pilih Gudang Cabang Pemohon --</option>
                    <option v-for="wh in warehouses" :key="wh.id" :value="String(wh.id)">
                      {{ wh.code }} - {{ wh.name }}
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Gudang Tujuan Retur <span class="text-danger">*</span></label>
                  <select v-model="form.destination_warehouse_id" class="form-select fs-7" required>
                    <option v-for="wh in warehouses" :key="wh.id" :value="String(wh.id)">
                      {{ wh.code }} - {{ wh.name }} ({{ wh.type }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Alasan Retur <span class="text-danger">*</span></label>
                  <select v-model="form.reason" class="form-select fs-7" required>
                    <option value="DAMAGED_ON_ARRIVAL">Barang Rusak Saat Diterima (Damaged on Arrival)</option>
                    <option value="DEFECTIVE">Barang Cacat Produksi / Chip Mati</option>
                    <option value="WRONG_SPECIFICATION">Salah Spesifikasi / Salah Kirim</option>
                    <option value="EXCESS_STOCK">Kelebihan Kirim dari Bon / Order</option>
                    <option value="OTHER">Lainnya</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-semibold text-dark">Referensi Order Asal (Opsional)</label>
                  <input type="text" v-model="form.order_ref" class="form-control fs-7" placeholder="Contoh: ORD-2026-08-0099" />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-semibold text-dark">Keterangan / Kronologi Kerusakan</label>
                  <textarea v-model="form.reason_details" class="form-control fs-7" rows="2" placeholder="Jelaskan kondisi detail fisik atau masalah teknis barang yang diretur..."></textarea>
                </div>
              </div>

              <hr class="my-4">

              <!-- Daftar Item yang Diretur -->
              <div class="d-flex align-items-center justify-content-between mb-3">
                <h6 class="fw-bold mb-0 text-dark">Item Barang yang Diretur</h6>
                <button type="button" class="btn btn-sm btn-outline-danger py-1 px-2 fs-8 fw-semibold" @click="addItem">
                  Tambah
                </button>
              </div>

              <div class="table-responsive border rounded-3">
                <table class="table table-bordered align-middle mb-0 fs-8">
                  <thead class="table-light text-secondary">
                    <tr>
                      <th style="width: 40%">Pilih Barang Persediaan</th>
                      <th style="width: 15%" class="text-center">Jumlah Retur</th>
                      <th style="width: 20%">Kondisi Fisik</th>
                      <th>Catatan Item</th>
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
                        <select v-model="row.condition" class="form-select form-select-sm">
                          <option value="DAMAGED">Rusak Fisik</option>
                          <option value="DEFECTIVE">Cacat Fungsi/Chip</option>
                          <option value="GOOD">Kondisi Baik/Segel</option>
                        </select>
                      </td>
                      <td>
                        <input type="text" v-model="row.notes" class="form-control form-control-sm" placeholder="Catatan opsional...">
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
              <router-link to="/returns" class="btn btn-outline-secondary btn-sm">
                Batal
              </router-link>
              <button type="submit" :disabled="submitting" class="btn btn-danger btn-sm px-4 shadow-xs">
                Kirim Pengajuan Retur
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
  origin_warehouse_id: '',
  destination_warehouse_id: '',
  reason: 'DAMAGED_ON_ARRIVAL',
  order_ref: '',
  reason_details: '',
  items: [
    { item_id: '', qty: 25, condition: 'DEFECTIVE', notes: 'Chip tidak terdeteksi saat inisialisasi awal' }
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
      form.origin_warehouse_id = String(warehouses.value[0].id);
      form.destination_warehouse_id = String(warehouses.value[0].id);
    }
    if (availableItems.value.length > 0 && form.items.length > 0) {
      form.items[0].item_id = String(availableItems.value[0].id);
    }
  } catch (err) {
    console.error('Failed to load master data for return', err);
  }
};

const addItem = () => {
  const defaultItemId = availableItems.value.length > 0 ? String(availableItems.value[0].id) : '';
  form.items.push({ item_id: defaultItemId, qty: 1, condition: 'DAMAGED', notes: '' });
};

const removeItem = (index) => {
  if (form.items.length > 1) {
    form.items.splice(index, 1);
  }
};

const submitReturn = async () => {
  submitting.value = true;
  try {
    await api.post('/returns', {
      destinationWarehouseId: Number(form.destination_warehouse_id || 1),
      reason: form.reason,
      reasonDetails: form.reason_details,
      orderRef: form.order_ref,
      items: form.items.map(it => ({
        itemId: Number(it.item_id),
        qty: Number(it.qty),
        condition: it.condition,
        notes: it.notes
      }))
    });
    alert('Pengajuan retur barang persediaan berhasil dikirim!');
    router.push('/returns');
  } catch (err) {
    console.error('Failed to submit return', err);
    alert('Gagal mengirim pengajuan retur: ' + (err.response?.data?.message || err.message));
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadMasterData();
});
</script>

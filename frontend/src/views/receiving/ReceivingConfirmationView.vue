<template>
  <div class="receiving-confirmation-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Konfirmasi Penerimaan Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-body">Beranda</router-link></li>
              <li class="breadcrumb-item"><router-link to="/receiving" class="text-decoration-none text-body">Penerimaan & QC</router-link></li>
              <li class="breadcrumb-item active text-danger fw-semibold" aria-current="page">Konfirmasi Penerimaan</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. AdminLTE 4 Info-Boxes -->
    <div class="row g-2 g-md-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs"><i class="bi bi-truck"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Dalam Perjalanan</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-danger">{{ incomingShipments.length }} Resi</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-danger" style="width: 50%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Menuju KC Surabaya</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs"><i class="bi bi-check2-all"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Telah Dikonfirmasi</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-success">14 Resi</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-success" style="width: 85%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Bulan Berjalan 2026</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs"><i class="bi bi-exclamation-triangle-fill"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Diskrepansi BAP</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">1 Kasus</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-warning" style="width: 25%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">Klaim Asuransi Ekspedisi</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info shadow-xs"><i class="bi bi-building-check"></i></span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary fw-bold text-uppercase fs-9">Gudang Cabang</span>
            <span class="info-box-number font-monospace fs-4 my-1 text-body">WH-SBY-01</span>
            <div class="progress" style="height: 4px;">
              <div class="progress-bar bg-info" style="width: 100%"></div>
            </div>
            <span class="progress-description text-secondary fs-9 mt-1">KC Surabaya Utama</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- 3. Main Card Outline -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- 4. Card Header with Tabs & Tools -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <div class="d-flex align-items-center gap-2">
          <h3 class="card-title fw-bold mb-0 fs-6 text-body">
            <i class="bi bi-clock-history text-danger me-2"></i>Kiriman Menunggu Konfirmasi Fisik
          </h3>
          <span class="badge text-bg-warning fs-9">{{ incomingShipments.length }} Pending</span>
        </div>
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <router-link to="/receiving" class="btn btn-sm btn-outline-secondary fs-8">
            <i class="bi bi-arrow-left me-1"></i> Penerimaan Cabang
          </router-link>
          <router-link to="/receiving/discrepancies" class="btn btn-sm btn-outline-danger fs-8">
            <i class="bi bi-exclamation-triangle me-1"></i> Berita Acara Selisih (BAP)
          </router-link>
        </div>
      </div>

      <!-- 5. Filter Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-auto" v-if="searchQuery">
            <button
              type="button"
              @click="searchQuery = ''; currentPage = 1"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <div class="col-12 col-md-5 ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Resi / AWB..."
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
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9">Nomor Resi / AWB</th>
              <th class="py-2 text-uppercase fs-9">Asal Pengirim</th>
              <th class="py-2 text-uppercase fs-9 text-center">Jumlah Koli</th>
              <th class="py-2 text-uppercase fs-9">Estimasi Tiba</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9">Aksi Petugas</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="shipment in paginatedShipments" :key="shipment.id">
              <td class="ps-3 py-2">
                <span class="font-monospace fw-bold text-danger">{{ shipment.awb }}</span>
              </td>
              <td class="py-2">
                <div class="fw-semibold text-body">Gudang Logistik Margomulyo</div>
                <div class="fs-9 text-secondary font-monospace">WH-CEN-01</div>
              </td>
              <td class="py-2 text-center font-monospace">
                <span class="badge text-bg-light border text-body">{{ shipment.koli }} Koli</span>
              </td>
              <td class="py-2">
                <span class="badge text-bg-info">{{ shipment.eta }}</span>
              </td>
              <td class="text-center pe-3 py-2">
                <button class="btn-action-icon text-success" @click="openConfirmModal(shipment)" title="Konfirmasi Tiba di Cabang">
                  <i class="bi bi-check2-circle"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredShipments.length === 0">
              <td colspan="5" class="text-center py-5 text-secondary">
                <i class="bi bi-check-circle fs-1 d-block mb-2 text-success"></i>
                Semua kiriman logistik telah berhasil dikonfirmasi tiba di kantor cabang.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 7. Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredShipments.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 8. Standardized Confirmation Modal with Discrepancy Recording -->
    <div v-if="selectedShipment" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1055;">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-bottom bg-body d-flex justify-content-between align-items-center">
            <h5 class="modal-title fw-bold text-body fs-6 mb-0">
              Konfirmasi Penerimaan: {{ selectedShipment.awb }}
            </h5>
            <button type="button" class="btn-close-modal" @click="selectedShipment = null" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body p-3 fs-8 space-y-3">
            <div class="alert alert-light border shadow-xs p-3 mb-3">
              <div>Nomor AWB / Resi: <strong class="text-danger font-monospace">{{ selectedShipment.awb }}</strong></div>
              <div>Muatan: <strong class="text-body">{{ selectedShipment.koli }} Koli</strong> &bull; Asal: <strong>Gudang Sentral Margomulyo</strong></div>
            </div>

            <div>
              <label class="form-label fs-8 fw-bold mb-1">Kondisi Fisik Kiriman Saat Tiba <span class="text-danger">*</span></label>
              <select v-model="receiptCondition" class="form-select form-select-sm fs-8">
                <option value="SESUAI">Sesuai & Kondisi Utuh (Tidak Ada Selisih / Kerusakan Fisik)</option>
                <option value="DISKREPANSI">Terdapat Selisih Kuantitas atau Kerusakan Fisik (Penerbitan BAP)</option>
              </select>
            </div>

            <!-- Discrepancy Details if Applicable -->
            <div v-if="receiptCondition === 'DISKREPANSI'" class="card border-danger bg-danger-subtle p-3 my-3">
              <div class="d-flex align-items-center gap-2 text-danger fw-bold fs-8 mb-2">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span>Catatan Diskrepansi & Berita Acara Pemeriksaan (BAP Otomatis)</span>
              </div>
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-9 fw-bold text-secondary text-uppercase mb-0.5">Jenis Selisih:</label>
                  <select v-model="discrepancyType" class="form-select form-select-sm fs-8">
                    <option value="DAMAGED">Barang Rusak Saat Pengiriman (Damaged)</option>
                    <option value="MISSING">Kuantitas Fisik Kurang (Missing Items)</option>
                    <option value="WRONG_ITEM">Salah Kirim Barang (Wrong Item)</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-9 fw-bold text-secondary text-uppercase mb-0.5">Kuantitas Terdampak (Pcs/Box):</label>
                  <input type="number" v-model.number="discrepancyQty" class="form-control form-control-sm fs-8 font-monospace" min="1" />
                </div>
                <div class="col-12">
                  <label class="form-label fs-9 fw-bold text-secondary text-uppercase mb-0.5">Keterangan Tambahan Berita Acara:</label>
                  <textarea v-model="discrepancyNotes" class="form-control form-control-sm fs-8" rows="2" placeholder="Kardus koli robek terkena rembesan air hujan saat ekspedisi..."></textarea>
                </div>
              </div>
            </div>

            <div>
              <label class="form-label fs-8 fw-bold mb-1">Nama Petugas Penerima Cabang <span class="text-danger">*</span></label>
              <input type="text" v-model="receiverName" class="form-control form-control-sm fs-8" required />
            </div>
          </div>
          <div class="modal-footer border-top bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="selectedShipment = null">
              Batal
            </button>
            <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" @click="submitReceipt">
              <i class="bi bi-check2-circle me-1"></i> Simpan Tanda Terima & Validasi Stok
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const searchQuery = ref('');
const currentPage = ref(1);
const perPage = ref(10);
const selectedShipment = ref(null);
const receiptCondition = ref('SESUAI');
const discrepancyType = ref('DAMAGED');
const discrepancyQty = ref(5);
const discrepancyNotes = ref('');
const receiverName = ref('Lutfi Anshari (Petugas Logistik Cabang)');
const errorMessage = ref('');

const incomingShipments = ref([]);

const mapShipment = (shipment) => ({
  id: shipment.id,
  awb: shipment.trackingNumber || shipment.manifestNumber || '-',
  koli: Number(shipment.koliCount || 0),
  eta: shipment.etaDate || '-',
  originName: shipment.originWarehouse?.name || 'Gudang Logistik'
});

const loadIncomingShipments = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/distribution/shipments');
    incomingShipments.value = (response.data || [])
      .filter(shipment => ['IN_TRANSIT', 'DISPATCHED'].includes(shipment.status))
      .map(mapShipment);
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal memuat kiriman yang menunggu konfirmasi.';
    incomingShipments.value = [];
  }
};

const filteredShipments = computed(() => {
  if (!searchQuery.value) return incomingShipments.value;
  return incomingShipments.value.filter(s => s.awb.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const paginatedShipments = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredShipments.value.slice(start, start + perPage.value);
});

const openConfirmModal = (shipment) => {
  selectedShipment.value = shipment;
  receiptCondition.value = 'SESUAI';
};

const submitReceipt = async () => {
  if (!selectedShipment.value) return;

  if (receiptCondition.value === 'DISKREPANSI') {
    errorMessage.value = 'Konfirmasi dengan diskrepansi membutuhkan pilihan item/order item dari backend. Data tidak disimpan lokal agar tidak menjadi mock.';
    return;
  }

  const discrepancies = [];

  try {
    await api.post(`/receiving/confirm/${selectedShipment.value.id}`, {
      podSignature: receiverName.value,
      notes: discrepancyNotes.value,
      discrepancies
    });
    await loadIncomingShipments();
    alert('Barang resmi diterima di gudang cabang! Saldo persediaan cabang bertambah dan Berita Acara Penerimaan (BAP) tercatat.');
    selectedShipment.value = null;
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menyimpan konfirmasi penerimaan.';
  }
};

onMounted(loadIncomingShipments);
</script>

<style scoped>
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

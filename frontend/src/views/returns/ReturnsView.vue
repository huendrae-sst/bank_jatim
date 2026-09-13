<template>
  <div class="returns-page">
    <!-- Page Header & Breadcrumbs -->
    <div class="app-content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Retur Barang Persediaan Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item"><router-link to="/dashboard">Dashboard</router-link></li>
              <li class="breadcrumb-item">Retur & Pemusnahan</li>
              <li class="breadcrumb-item active" aria-current="page">Retur Barang</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 4 AdminLTE 4 Metric Info-Boxes -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-danger shadow-xs">
            <i class="bi bi-arrow-counterclockwise"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">TOTAL PERMOHONAN</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ returnsList.length }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-danger" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Tahun Anggaran 2026</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-warning shadow-xs">
            <i class="bi bi-hourglass-split"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">MENUNGGU APPROVAL</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ waitingCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-warning" style="width: 50%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Divisi Logistik Pusat</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-info text-white shadow-xs">
            <i class="bi bi-truck"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">DALAM PENGIRIMAN</span>
            <span class="info-box-number text-body fs-4 font-monospace">{{ inTransitCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-info" style="width: 30%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Menuju Gudang Pusat</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="info-box shadow-xs mb-0 h-100 bg-body">
          <span class="info-box-icon text-bg-success shadow-xs">
            <i class="bi bi-check2-all"></i>
          </span>
          <div class="info-box-content">
            <span class="info-box-text text-secondary">SELESAI DITERIMA</span>
            <span class="info-box-number text-success fs-4 font-monospace">{{ completedCount }}</span>
            <div class="progress" style="height: 2px;">
              <div class="progress-bar bg-success" style="width: 100%"></div>
            </div>
            <span class="progress-description fs-9 text-secondary">Restock / Karantina</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Card -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs & Action Button -->
      <div class="card-header border-bottom p-2 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'all' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'all'"
            >
              Semua Retur
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'waiting' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'waiting'"
            >
              Menunggu Approval
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="activeTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="activeTab = 'completed'"
            >
              Selesai Diterima
            </button>
          </li>
        </ul>

        <div class="card-tools ms-md-auto">
          <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs fs-8" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Buat Permohonan Retur
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-building"></i>
              </span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Cabang Pengirim</option>
                <option value="KC Malang">KC Malang</option>
                <option value="KC Surabaya Utama">KC Surabaya Utama</option>
                <option value="KC Sidoarjo">KC Sidoarjo</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-info-circle"></i>
              </span>
              <select v-model="filterReason" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Alasan Retur</option>
                <option value="DEFECT">Cacat Pabrik / Defect</option>
                <option value="EXPIRED">Kadaluarsa / Usang</option>
                <option value="DAMAGE">Kerusakan Fisik Cabang</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBranch !== 'ALL' || filterReason !== 'ALL'">
            <button type="button" class="btn btn-sm btn-outline-danger fs-8" @click="resetFilters" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>
          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-search"></i>
              </span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No. Retur, Cabang, atau SKU Barang..."
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button"><i class="bi bi-search me-1"></i> Cari</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="table-light text-secondary fs-8 fw-bold text-uppercase">
            <tr>
              <th class="ps-3">No. Retur</th>
              <th>Cabang Pengirim</th>
              <th>Nama Barang / SKU</th>
              <th class="text-end">Jumlah Retur</th>
              <th>Alasan Retur</th>
              <th class="text-center">Status Alur</th>
              <th class="text-center pe-3" style="width: 100px;">Aksi</th>
            </tr>
          </thead>
          <tbody class="fs-7">
            <tr v-for="r in paginatedReturnsList" :key="r.id">
              <td class="ps-3 fw-bold font-monospace text-danger">
                <a href="#" @click.prevent="openDetailModal(r)" class="text-decoration-none text-danger">
                  {{ r.retNo }}
                </a>
              </td>
              <td class="fw-semibold text-body">{{ r.branch }}</td>
              <td>
                <div class="fw-medium text-body">{{ r.item }}</div>
                <div class="fs-9 text-secondary font-monospace">{{ r.sku || 'SKU-LOG-001' }}</div>
              </td>
              <td class="text-end font-monospace fw-bold text-body">{{ r.qty.toLocaleString('id-ID') }}</td>
              <td class="fs-8 text-secondary">{{ r.reason }}</td>
              <td class="text-center">
                <span class="badge fs-9" :class="badgeClass(r.status)">{{ r.status }}</span>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <button type="button" class="btn-action-icon text-secondary border-0 bg-transparent" @click="openDetailModal(r)" title="Lihat Detail">
                    <i class="bi bi-eye"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredReturnsList.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                Tidak ada data permohonan retur yang sesuai filter.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="filteredReturnsList.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- Modal Form Buat Permohonan Retur -->
    <div v-if="showCreateModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showCreateModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Form Pengajuan Retur Barang
            </h6>
            <button type="button" class="btn-close-modal" @click="showCreateModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form @submit.prevent="saveReturn">
            <div class="modal-body p-3 fs-8">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Gudang / Cabang Pemohon <span class="text-danger">*</span></label>
                  <select v-model="returnForm.branch" class="form-select form-select-sm fs-8" required>
                    <option value="KC Malang">KC Malang</option>
                    <option value="KC Surabaya Utama">KC Surabaya Utama</option>
                    <option value="KC Sidoarjo">KC Sidoarjo</option>
                    <option value="KC Kediri">KC Kediri</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Gudang Tujuan Retur <span class="text-danger">*</span></label>
                  <input type="text" class="form-control form-control-sm fs-8 bg-body-secondary" value="WH-CEN-01 - Gudang Sentral Margomulyo" readonly />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alasan Retur <span class="text-danger">*</span></label>
                  <select v-model="returnForm.reasonType" class="form-select form-select-sm fs-8" required @change="onReasonTypeChange">
                    <option value="DEFECT">Cacat Pabrik / Defect (DEFECTIVE)</option>
                    <option value="EXPIRED">Kadaluarsa / Usang (EXPIRED)</option>
                    <option value="DAMAGE">Kerusakan Fisik Cabang (DAMAGED)</option>
                    <option value="OTHER">Lainnya</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Referensi Order (Opsional)</label>
                  <input type="text" v-model="returnForm.orderRef" class="form-control form-control-sm fs-8" placeholder="Contoh: ORD-2026-08-0099" />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Barang Persediaan <span class="text-danger">*</span></label>
                  <input type="text" v-model="returnForm.item" class="form-control form-control-sm fs-8" placeholder="Contoh: Buku Tabungan Simpeda" required />
                </div>
                <div class="col-12 col-md-3">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode / SKU <span class="text-danger">*</span></label>
                  <input type="text" v-model="returnForm.sku" class="form-control form-control-sm fs-8 font-monospace" placeholder="TBG-SMPD-001" required />
                </div>
                <div class="col-12 col-md-3">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Unit <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="returnForm.qty" class="form-control form-control-sm fs-8 font-monospace" min="1" required />
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Keterangan / Kronologi Retur <span class="text-danger">*</span></label>
                  <textarea v-model="returnForm.reason" class="form-control form-control-sm fs-8" rows="2" placeholder="Jelaskan kondisi detail fisik atau masalah teknis barang..." required></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showCreateModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
                <i class="bi bi-send me-1"></i> Ajukan Permohonan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Detail Permohonan Retur -->
    <div v-if="showDetailModal && selectedReturn" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showDetailModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Detail Permohonan Retur - <span class="font-monospace text-danger">{{ selectedReturn.retNo }}</span>
            </h6>
            <button type="button" class="btn-close-modal" @click="showDetailModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body p-3 fs-8">
            <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
              <div>
                <span class="fs-9 text-uppercase text-secondary fw-bold d-block">Status Permohonan</span>
                <span class="badge fs-8 mt-1" :class="badgeClass(selectedReturn.status)">{{ selectedReturn.status }}</span>
              </div>
              <div class="text-end">
                <span class="fs-9 text-uppercase text-secondary fw-bold d-block">Nomor Retur</span>
                <span class="font-monospace fw-bold text-danger fs-7">{{ selectedReturn.retNo }}</span>
              </div>
            </div>

            <div class="row g-3 mb-3">
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Cabang Pemohon (Asal)</span>
                  <span class="fw-bold text-body fs-7">{{ selectedReturn.branch }}</span>
                </div>
              </div>
              <div class="col-12 col-md-6">
                <div class="p-2 border rounded bg-body-tertiary">
                  <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Gudang Tujuan (Pusat)</span>
                  <span class="fw-bold text-body fs-7">WH-CEN-01 - Gudang Sentral Margomulyo</span>
                </div>
              </div>
            </div>

            <div class="table-responsive border rounded mb-3">
              <table class="table table-sm table-bordered align-middle mb-0 fs-8">
                <thead class="table-light text-secondary text-uppercase fs-9">
                  <tr>
                    <th class="ps-3">Nama Barang Persediaan</th>
                    <th>Kode / SKU</th>
                    <th class="text-end">Jumlah Retur</th>
                    <th>Kategori Alasan</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td class="ps-3 fw-bold text-body">{{ selectedReturn.item }}</td>
                    <td class="font-monospace text-secondary">{{ selectedReturn.sku || 'SKU-LOG-001' }}</td>
                    <td class="text-end font-monospace fw-bold text-danger">{{ selectedReturn.qty.toLocaleString('id-ID') }} unit</td>
                    <td>
                      <span class="badge text-bg-light border text-uppercase fs-9">{{ selectedReturn.reasonType }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="p-3 border rounded bg-body-tertiary">
              <span class="fs-9 text-uppercase text-secondary fw-bold d-block mb-1">Keterangan / Alasan Retur</span>
              <p class="mb-0 text-body fs-8">{{ selectedReturn.reason }}</p>
            </div>
          </div>
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showDetailModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/api/client';

const showCreateModal = ref(false);
const showDetailModal = ref(false);
const selectedReturn = ref(null);
const isLoading = ref(true);

const returnForm = reactive({
  branch: 'KC Malang',
  origin_warehouse_id: '1',
  destination_warehouse_id: '1',
  reasonType: 'DEFECT',
  reason: '',
  orderRef: '',
  item: '',
  sku: '',
  itemId: '',
  qty: 1
});

const warehouses = ref([]);
const availableItems = ref([]);

const openCreateModal = () => {
  returnForm.branch = 'KC Malang';
  returnForm.reasonType = 'DEFECT';
  returnForm.reason = 'Nomor seri salah cetak dari vendor (defect)';
  returnForm.orderRef = '';
  returnForm.item = '';
  returnForm.sku = '';
  returnForm.qty = 1;
  showCreateModal.value = true;
};

const openDetailModal = (r) => {
  selectedReturn.value = r;
  showDetailModal.value = true;
};

const onReasonTypeChange = () => {
  if (returnForm.reasonType === 'DEFECT') {
    returnForm.reason = 'Nomor seri salah cetak dari vendor (defect)';
  } else if (returnForm.reasonType === 'EXPIRED') {
    returnForm.reason = 'Format cetakan lama kadaluarsa / tidak terpakai';
  } else if (returnForm.reasonType === 'DAMAGE') {
    returnForm.reason = 'Kerusakan fisik saat penyimpanan di cabang';
  } else {
    returnForm.reason = '';
  }
};

const returnsList = ref([]);

const fetchReturns = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/returns');
    const items = res.data?.data || res.data || [];
    returnsList.value = items.map(r => {
      const firstItem = r.items && r.items.length > 0 ? r.items[0] : {};
      return {
        id: r.id,
        retNo: r.returnNumber,
        branch: r.organizationName || 'Kantor Cabang',
        item: firstItem.name || 'Barang Persediaan',
        sku: firstItem.sku || '-',
        qty: r.totalQty || firstItem.qtyReturned || 0,
        reason: r.reason || 'Retur Barang',
        reasonType: 'DEFECT',
        status: r.status === 'REQUESTED' ? 'MENUNGGU APPROVAL PUSAT' : (r.status === 'SHIPPED' ? 'DALAM PENGIRIMAN' : (r.status === 'RECEIVED' ? 'SELESAI DITERIMA' : r.status))
      };
    });
  } catch (err) {
    console.error('Failed to load returns', err);
  } finally {
    isLoading.value = false;
  }
};

const loadMasterData = async () => {
  try {
    const [whRes, itRes] = await Promise.all([
      api.get('/master/warehouses'),
      api.get('/master/items')
    ]);
    warehouses.value = whRes.data?.data || whRes.data || [];
    availableItems.value = itRes.data?.data || itRes.data || [];
    if (warehouses.value.length > 0) {
      returnForm.destination_warehouse_id = String(warehouses.value[0].id);
    }
    if (availableItems.value.length > 0) {
      returnForm.itemId = String(availableItems.value[0].id);
    }
  } catch (err) {
    console.error('Failed to load master data', err);
  }
};

const saveReturn = async () => {
  try {
    await api.post('/returns', {
      destinationWarehouseId: Number(returnForm.destination_warehouse_id || 1),
      reason: returnForm.reason || 'Retur persediaan cacat/rusak',
      reasonDetails: returnForm.reason,
      orderRef: returnForm.orderRef,
      items: [
        {
          itemId: Number(returnForm.itemId || 1),
          qty: Number(returnForm.qty || 1),
          condition: returnForm.reasonType === 'DEFECT' ? 'DEFECTIVE' : 'DAMAGED',
          notes: returnForm.reason
        }
      ]
    });
    showCreateModal.value = false;
    await fetchReturns();
  } catch (err) {
    console.error('Failed to save return', err);
    alert('Gagal membuat retur: ' + (err.response?.data?.message || err.message));
  }
};

const currentPage = ref(1);
const perPage = ref(10);
const searchQuery = ref('');
const activeTab = ref('all');
const filterBranch = ref('ALL');
const filterReason = ref('ALL');

const resetFilters = () => {
  searchQuery.value = '';
  filterBranch.value = 'ALL';
  filterReason.value = 'ALL';
  currentPage.value = 1;
};

const waitingCount = computed(() => {
  return returnsList.value.filter(r => r.status.includes('MENUNGGU')).length;
});

const inTransitCount = computed(() => {
  return returnsList.value.filter(r => r.status.includes('PENGIRIMAN')).length;
});

const completedCount = computed(() => {
  return returnsList.value.filter(r => r.status.includes('SELESAI')).length;
});

const filteredReturnsList = computed(() => {
  return returnsList.value.filter(r => {
    if (activeTab.value === 'waiting' && !r.status.includes('MENUNGGU')) return false;
    if (activeTab.value === 'completed' && !r.status.includes('SELESAI')) return false;

    if (filterBranch.value !== 'ALL' && r.branch !== filterBranch.value) return false;
    if (filterReason.value !== 'ALL' && r.reasonType !== filterReason.value) return false;

    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase();
      const matchNo = r.retNo.toLowerCase().includes(q);
      const matchBranch = r.branch.toLowerCase().includes(q);
      const matchItem = r.item.toLowerCase().includes(q);
      const matchSku = r.sku.toLowerCase().includes(q);
      if (!matchNo && !matchBranch && !matchItem && !matchSku) return false;
    }

    return true;
  });
});

const paginatedReturnsList = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredReturnsList.value.slice(start, start + perPage.value);
});

const paginatedReturns = paginatedReturnsList;

const badgeClass = (status) => {
  if (status.includes('SELESAI')) return 'text-bg-success';
  if (status.includes('MENUNGGU')) return 'text-bg-warning';
  if (status.includes('PENGIRIMAN')) return 'text-bg-info';
  return 'text-bg-secondary';
};

onMounted(() => {
  fetchReturns();
  loadMasterData();
});
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


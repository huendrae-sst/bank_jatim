<template>
  <div class="production-detail-page space-y-3">
    <!-- Breadcrumb & Top Bar -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <div class="d-flex align-items-center gap-2">
          <h3 class="mb-0 text-body fw-bold">{{ prodOrder.productionNumber }}</h3>
          <span class="badge fs-8 text-uppercase" :class="badgeClass(prodOrder.status)">
            {{ formatStatus(prodOrder.status) }}
          </span>
          <span v-if="prodOrder.materialIssueStatus" class="badge bg-secondary-subtle text-secondary border fs-9">
            Bahan: {{ prodOrder.materialIssueStatus }}
          </span>
        </div>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 fs-8">
            <li class="breadcrumb-item"><router-link to="/dashboard" class="text-decoration-none text-danger">Beranda</router-link></li>
            <li class="breadcrumb-item"><router-link to="/production" class="text-decoration-none text-danger">SPK Produksi</router-link></li>
            <li class="breadcrumb-item active text-secondary" aria-current="page">{{ prodOrder.productionNumber }}</li>
          </ol>
        </nav>
      </div>

      <div class="d-flex align-items-center gap-2 flex-wrap">
        <router-link to="/production" class="btn btn-sm btn-outline-secondary">
          Kembali
        </router-link>
        <router-link :to="`/production/${prodOrder.id}/manifest`" target="_blank" class="btn btn-sm btn-outline-danger fw-bold">
          Cetak Rekap Manifest
        </router-link>

        <!-- Dynamic Action Buttons based on Status -->
        <button
          v-if="prodOrder.status === 'DRAFT'"
          type="button"
          class="btn btn-sm btn-warning fw-bold shadow-xs px-3"
          :disabled="isSubmitting"
          @click="actionRequestMaterial"
        >
          Ajukan Pengeluaran Blank
        </button>

        <button
          v-if="prodOrder.status === 'MATERIAL_REQUESTED'"
          type="button"
          class="btn btn-sm btn-primary fw-bold shadow-xs px-3"
          :disabled="isSubmitting"
          @click="actionApproveMaterial"
        >
          Otorisasi Pengeluaran (INVENTORY_OFFICER)
        </button>

        <button
          v-if="prodOrder.status === 'MATERIAL_APPROVED'"
          type="button"
          class="btn btn-sm btn-danger fw-bold shadow-xs px-3"
          :disabled="isSubmitting"
          @click="actionIssueMaterial"
        >
          Keluarkan Kartu Blank dari Gudang
        </button>

        <button
          v-if="prodOrder.status === 'IN_PRODUCTION' || prodOrder.status === 'QC_REVIEW'"
          type="button"
          class="btn btn-sm btn-info text-white fw-bold shadow-xs px-3"
          @click="openResultModal"
        >
          Input Hasil Cetak &amp; QC
        </button>

        <button
          v-if="prodOrder.status === 'QC_REVIEW'"
          type="button"
          class="btn btn-sm btn-success fw-bold shadow-xs px-3"
          :disabled="isSubmitting"
          @click="actionCompleteProduction"
        >
          Selesaikan QC &amp; Masukkan Stok Gudang
        </button>

        <button
          v-if="prodOrder.status === 'COMPLETED'"
          type="button"
          class="btn btn-sm btn-danger fw-bold shadow-xs px-3"
          :disabled="isSubmitting"
          @click="actionFulfillOrders"
        >
          Pemenuhan Bulk Order Cabang
        </button>
      </div>
    </div>

    <!-- Workflow Status Tracker -->
    <div class="card shadow-xs border-0 p-3 mb-3 bg-body">
      <div class="d-flex align-items-center justify-content-between mb-2">
        <span class="fs-8 fw-bold text-secondary text-uppercase">Alur Eksekusi SPK Produksi</span>
        <span class="badge" :class="badgeClass(prodOrder.status)">Tahap: {{ formatStatus(prodOrder.status) }}</span>
      </div>
      <div class="d-flex align-items-center justify-content-between position-relative px-2 py-1 overflow-x-auto text-nowrap">
        <template v-for="(step, idx) in workflowSteps" :key="step.code">
          <div class="d-flex flex-column align-items-center position-relative z-1 px-1 text-center" style="min-width: 80px;">
            <div
              class="rounded-circle d-flex align-items-center justify-content-center fw-bold fs-9 mb-1 shadow-xs"
              :class="getWorkflowCircleClass(prodOrder.status, step.code)"
              style="width: 32px; height: 32px;"
            >
              <i v-if="isWorkflowStepPassed(prodOrder.status, step.code)" class="bi bi-check-lg"></i>
              <span v-else>{{ idx + 1 }}</span>
            </div>
            <span class="fs-9" :class="getWorkflowTextClass(prodOrder.status, step.code)">{{ step.label }}</span>
          </div>
          <div
            v-if="idx < workflowSteps.length - 1"
            class="flex-grow-1 border-top"
            :class="isWorkflowLinePassed(prodOrder.status, step.code) ? 'border-danger border-2' : 'border-secondary-subtle border-2'"
            style="min-width: 16px; margin-top: -18px;"
          ></div>
        </template>
      </div>
    </div>

    <!-- Main Detail Card -->
    <div class="card card-outline card-danger shadow-xs mb-3">
      <div class="card-body p-4">
        <!-- Info Grid -->
        <div class="row g-3 mb-4">
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Gudang Sumber Bahan</span>
            <span class="fw-bold text-dark fs-7">{{ prodOrder.warehouse?.name || '-' }}</span>
            <span class="text-muted fs-8 d-block font-monospace">{{ prodOrder.warehouse?.code || '-' }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Dibuat &amp; Diajukan Oleh</span>
            <span class="fw-bold text-dark fs-7">{{ prodOrder.createdByUser?.name || '-' }}</span>
            <span class="text-muted fs-8 d-block">Tgl: {{ formatDate(prodOrder.productionDate) }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Otorisasi Pengeluaran Blank</span>
            <span v-if="prodOrder.approvedByUser" class="fw-bold text-success fs-7">
              <i class="bi bi-check-circle me-1"></i>{{ prodOrder.approvedByUser?.name }}
            </span>
            <span v-else class="text-secondary fs-8">Menunggu Persetujuan INVENTORY_OFFICER</span>
            <span v-if="prodOrder.approvedAt" class="text-muted fs-9 d-block">{{ formatDate(prodOrder.approvedAt) }}</span>
          </div>
          <div class="col-12 col-md-3">
            <span class="fs-9 text-uppercase text-secondary fw-semibold d-block">Realisasi Hasil Produksi</span>
            <div class="d-flex align-items-center gap-2 mt-1">
              <span class="badge bg-success fs-8">Bagus: {{ prodOrder.totalProduced || 0 }}</span>
              <span class="badge bg-danger fs-8">Gagal: {{ prodOrder.totalDamaged || 0 }}</span>
              <span class="badge bg-secondary fs-8">Rencana: {{ prodOrder.totalQty || 0 }}</span>
            </div>
            <span v-if="prodOrder.completedAt" class="text-muted fs-9 d-block mt-0.5">Selesai: {{ formatDate(prodOrder.completedAt) }}</span>
          </div>
        </div>

        <!-- Items Table -->
        <h6 class="fw-bold mb-3 text-dark">Rincian Kartu Debit Emboss (Finished Goods via BOM)</h6>
        <div class="table-responsive border rounded-3 mb-4">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3">Target SKU Finished Good</th>
                <th>Nama Produk Kartu Emboss</th>
                <th>Bahan Baku Blank (BOM)</th>
                <th class="text-center">Kuantitas Rencana</th>
                <th class="text-center">Hasil Bagus (QC Pass)</th>
                <th class="text-center">Hasil Gagal (Damaged)</th>
                <th class="text-center">Status Barang</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in prodOrder.items" :key="item.id">
                <td class="ps-3 font-monospace fw-bold text-danger">{{ item.itemSku }}</td>
                <td class="fw-semibold">{{ item.itemName }}</td>
                <td>
                  <span class="badge bg-body-secondary text-body border font-monospace">
                    {{ item.materialSku || 'ATM-INST-001' }}
                  </span>
                  <span class="text-muted fs-9 d-block">{{ item.materialName || 'Kartu Blank Chip' }}</span>
                </td>
                <td class="text-center font-monospace fw-bold">{{ item.qtyPlanned }} {{ item.uom }}</td>
                <td class="text-center font-monospace fw-bold text-success">{{ item.qtyProduced || 0 }}</td>
                <td class="text-center font-monospace fw-bold text-danger">{{ item.qtyDamaged || 0 }}</td>
                <td class="text-center">
                  <span v-if="prodOrder.status === 'COMPLETED'" class="badge bg-success-subtle text-success border border-success-subtle">
                    Tersedia di Gudang (PRODUCTION_IN)
                  </span>
                  <span v-else-if="prodOrder.status === 'IN_PRODUCTION' || prodOrder.status === 'QC_REVIEW'" class="badge bg-warning-subtle text-warning border border-warning-subtle">
                    Dalam Pengerjaan
                  </span>
                  <span v-else class="badge bg-secondary-subtle text-secondary">
                    Menunggu Bahan
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Fulfillments Summary -->
        <h6 class="fw-bold mb-3 text-dark">Order Cabang Terhubung (Konsolidasi &amp; Pemenuhan Bulk)</h6>
        <div class="table-responsive border rounded-3 mb-4">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3">Nomor Order Cabang</th>
                <th>Kantor Cabang Pemesan</th>
                <th class="text-center">Kuantitas Dipenuhi</th>
                <th>Waktu Pemenuhan</th>
                <th class="text-center pe-3">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in prodOrder.fulfillments" :key="f.id">
                <td class="ps-3 font-monospace fw-bold text-danger">{{ f.orderNumber }}</td>
                <td class="fw-semibold">{{ f.branchName }}</td>
                <td class="text-center font-monospace fw-bold text-success">{{ f.qtyFulfilled || prodOrder.totalProduced }} Keping</td>
                <td class="text-secondary fs-9">{{ formatDate(f.fulfilledAt) }}</td>
                <td class="text-center pe-3">
                  <router-link :to="'/orders/' + f.orderId" class="btn btn-sm btn-outline-secondary py-0 px-2 fs-9">
                    Buka Order
                  </router-link>
                </td>
              </tr>
              <tr v-if="!prodOrder.fulfillments || prodOrder.fulfillments.length === 0">
                <td colspan="5" class="text-center py-3 text-secondary">
                  Tidak ada order cabang yang terhubung langsung pada SPK ini.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Per-Card Traceability Table -->
        <div class="d-flex align-items-center justify-content-between mb-3">
          <h6 class="fw-bold mb-0 text-dark">
            Daftar Kartu Nasabah (Traceability Per Kartu &amp; Rekam Mesin)
          </h6>
          <span class="badge bg-secondary fs-9">{{ cardRecords.length }} Nasabah Terdaftar</span>
        </div>
        <div class="table-responsive border rounded-3" style="max-height: 360px; overflow-y: auto;">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9 sticky-top">
              <tr>
                <th class="ps-3" style="width: 40px;">No.</th>
                <th>Nama Nasabah</th>
                <th>Nomor Rekening</th>
                <th>Masked Card No.</th>
                <th>Kode Cabang</th>
                <th>Tipe Kartu</th>
                <th class="text-center">Status Produksi Kartu</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(card, idx) in cardRecords" :key="card.id">
                <td class="ps-3 text-secondary font-monospace">{{ idx + 1 }}</td>
                <td class="fw-bold text-body">
                 {{ card.customerName }}
                </td>
                <td class="font-monospace text-secondary">{{ card.accountNumber }}</td>
                <td class="font-monospace text-secondary">{{ card.cardNumberMasked }}</td>
                <td><span class="badge bg-light text-dark border font-monospace">{{ card.branchCode }}</span></td>
                <td>
                  <span class="badge" :class="card.cardType?.includes('MC') ? 'bg-primary-subtle text-primary' : 'bg-success-subtle text-success'">
                    {{ card.cardType }}
                  </span>
                </td>
                <td class="text-center">
                  <span class="badge fs-9" :class="cardStatusBadge(card.productionStatus)">
                    {{ card.productionStatus || 'QUEUED' }}
                  </span>
                </td>
              </tr>
              <tr v-if="cardRecords.length === 0">
                <td colspan="7" class="text-center py-4 text-secondary">
                  Belum ada data rekaman nasabah terhubung pada SPK ini.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Input Hasil Cetak & QC -->
    <div v-if="showResultModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="showResultModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Input Hasil Cetak / Emboss Mesin Personalisasi
            </h6>
            <button type="button" class="btn-close" @click="showResultModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitResult">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="alert alert-info py-2 px-3 fs-8 border-0 mb-3">
                <i class="bi bi-shield-check me-1"></i> Kartu bagus akan otomatis siap masuk stok gudang, sedangkan kartu cacat/gagal cetak akan dialokasikan ke stok <strong>DAMAGED</strong> gudang.
              </div>

              <!-- Mode Selection -->
              <ul class="nav nav-pills nav-fill mb-3 border rounded p-1 bg-body-tertiary">
                <li class="nav-item">
                  <button type="button" class="nav-link py-1 fs-8" :class="resultInputMode === 'aggregate' ? 'active bg-danger fw-bold text-white' : 'text-body'" @click="resultInputMode = 'aggregate'">
                    Mode Agregat (Cepat per Item)
                  </button>
                </li>
                <li class="nav-item">
                  <button type="button" class="nav-link py-1 fs-8" :class="resultInputMode === 'granular' ? 'active bg-danger fw-bold text-white' : 'text-body'" @click="resultInputMode = 'granular'">
                    Mode Granular (Centang Per Kartu Nasabah)
                  </button>
                </li>
              </ul>

              <!-- Mode 1: Agregat Input -->
              <div v-if="resultInputMode === 'aggregate'">
                <table class="table table-bordered table-sm align-middle fs-8 mb-0">
                  <thead class="table-light">
                    <tr>
                      <th>Produk Kartu Emboss</th>
                      <th class="text-center">Rencana</th>
                      <th class="text-center" style="width: 30%">Qty Bagus (Pass)</th>
                      <th class="text-center" style="width: 30%">Qty Gagal (Reject)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in prodOrder.items" :key="it.id">
                      <td>
                        <strong>{{ it.itemName }}</strong>
                        <span class="text-muted d-block font-monospace fs-9">{{ it.itemSku }}</span>
                      </td>
                      <td class="text-center font-monospace">{{ it.qtyPlanned }}</td>
                      <td>
                        <input type="number" v-model.number="resultForm[it.id].qtyProduced" class="form-control form-control-sm text-center font-monospace fw-bold fs-8" min="0" :max="it.qtyPlanned" />
                      </td>
                      <td>
                        <input type="number" v-model.number="resultForm[it.id].qtyDamaged" class="form-control form-control-sm text-center font-monospace fw-bold text-danger fs-8" min="0" :max="it.qtyPlanned" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Mode 2: Granular Input Per Kartu -->
              <div v-else style="max-height: 280px; overflow-y: auto;">
                <table class="table table-sm table-bordered align-middle fs-8 mb-0">
                  <thead class="table-light sticky-top">
                    <tr>
                      <th style="width: 40px;">No.</th>
                      <th>Nama Nasabah</th>
                      <th>No Rekening</th>
                      <th class="text-center">Hasil Cetak</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(card, cIdx) in cardRecords" :key="card.id">
                      <td>{{ cIdx + 1 }}</td>
                      <td class="fw-semibold">{{ card.customerName }}</td>
                      <td class="font-monospace text-secondary">{{ card.accountNumber }}</td>
                      <td class="text-center">
                        <div class="btn-group btn-group-sm" role="group">
                          <button
                            type="button"
                            class="btn btn-sm py-0 px-2 fs-9"
                            :class="granularCardResults[card.id] === 'PRODUCED' ? 'btn-success text-white fw-bold' : 'btn-outline-success'"
                            @click="granularCardResults[card.id] = 'PRODUCED'"
                          >
                            Bagus
                          </button>
                          <button
                            type="button"
                            class="btn btn-sm py-0 px-2 fs-9"
                            :class="granularCardResults[card.id] === 'PRODUCTION_FAILED' ? 'btn-danger text-white fw-bold' : 'btn-outline-danger'"
                            @click="granularCardResults[card.id] = 'PRODUCTION_FAILED'"
                          >
                            Gagal
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showResultModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-4 fs-8" :disabled="isSubmitting">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                Simpan Hasil QC
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/client';
import { toast } from '@/utils/toast';

const route = useRoute();
const isLoading = ref(true);
const isSubmitting = ref(false);
const showResultModal = ref(false);
const resultInputMode = ref('aggregate');

const prodOrder = ref({
  id: route.params.id,
  productionNumber: '',
  status: 'DRAFT',
  materialIssueStatus: 'PENDING',
  productionDate: null,
  totalQty: 0,
  totalProduced: 0,
  totalDamaged: 0,
  warehouse: null,
  createdByUser: null,
  approvedByUser: null,
  items: [],
  fulfillments: []
});

const cardRecords = ref([]);
const resultForm = reactive({});
const granularCardResults = reactive({});

const workflowSteps = [
  { code: 'DRAFT', label: '1. Draf SPK' },
  { code: 'MATERIAL_REQUESTED', label: '2. Pengajuan Bahan' },
  { code: 'MATERIAL_APPROVED', label: '3. Otorisasi Bahan' },
  { code: 'IN_PRODUCTION', label: '4. Proses Cetak' },
  { code: 'QC_REVIEW', label: '5. Kontrol Kualitas' },
  { code: 'COMPLETED', label: '6. Selesai Masuk Stok' }
];

const statusOrder = ['DRAFT', 'MATERIAL_REQUESTED', 'MATERIAL_APPROVED', 'IN_PRODUCTION', 'QC_REVIEW', 'COMPLETED'];

const isWorkflowStepPassed = (currentStatus, stepCode) => {
  const currentIdx = statusOrder.indexOf(currentStatus);
  const stepIdx = statusOrder.indexOf(stepCode);
  return currentIdx > stepIdx;
};

const isWorkflowLinePassed = (currentStatus, stepCode) => {
  const currentIdx = statusOrder.indexOf(currentStatus);
  const stepIdx = statusOrder.indexOf(stepCode);
  return currentIdx > stepIdx;
};

const getWorkflowCircleClass = (currentStatus, stepCode) => {
  if (currentStatus === stepCode) {
    return 'bg-danger text-white border-2 border-danger';
  }
  if (isWorkflowStepPassed(currentStatus, stepCode)) {
    return 'bg-success text-white';
  }
  return 'bg-body-secondary text-secondary border';
};

const getWorkflowTextClass = (currentStatus, stepCode) => {
  if (currentStatus === stepCode) {
    return 'fw-bold text-danger';
  }
  if (isWorkflowStepPassed(currentStatus, stepCode)) {
    return 'fw-semibold text-success';
  }
  return 'text-secondary';
};

const formatDate = (val) => {
  if (!val) return '-';
  try {
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return val;
  }
};

const formatStatus = (status) => {
  switch (status) {
    case 'DRAFT': return 'DRAFT';
    case 'MATERIAL_REQUESTED': return 'PERMINTAAN BAHAN';
    case 'MATERIAL_APPROVED': return 'BAHAN DISETUJUI';
    case 'IN_PRODUCTION': return 'PROSES CETAK';
    case 'QC_REVIEW': return 'QC REVIEW';
    case 'COMPLETED': return 'SELESAI';
    default: return status || '-';
  }
};

const badgeClass = (status) => {
  switch (status) {
    case 'COMPLETED': return 'text-bg-success';
    case 'IN_PRODUCTION': return 'text-bg-primary';
    case 'QC_REVIEW': return 'text-bg-info text-white';
    case 'MATERIAL_REQUESTED': return 'text-bg-warning';
    case 'MATERIAL_APPROVED': return 'text-bg-secondary';
    default: return 'text-bg-light border text-dark';
  }
};

const cardStatusBadge = (status) => {
  switch (status) {
    case 'PRODUCED': return 'bg-success-subtle text-success border border-success-subtle';
    case 'PRODUCTION_FAILED': return 'bg-danger-subtle text-danger border border-danger-subtle';
    case 'QUEUED': return 'bg-warning-subtle text-warning border border-warning-subtle';
    default: return 'bg-secondary-subtle text-secondary';
  }
};

const fetchDetail = async () => {
  isLoading.value = true;
  try {
    const res = await api.get(`/production-orders/${route.params.id}`);
    prodOrder.value = res.data?.data || res.data;

    // Inisialisasi result form
    if (prodOrder.value.items) {
      prodOrder.value.items.forEach(it => {
        resultForm[it.id] = {
          qtyProduced: it.qtyProduced || it.qtyPlanned || 0,
          qtyDamaged: it.qtyDamaged || 0
        };
      });
    }

    // Ambil kartu nasabah untuk traceability
    const cardRes = await api.get(`/production-orders/${route.params.id}/cards`);
    cardRecords.value = cardRes.data?.data || cardRes.data || [];
    cardRecords.value.forEach(c => {
      granularCardResults[c.id] = c.productionStatus || 'PRODUCED';
    });
  } catch (err) {
    console.error('Failed to load production detail:', err);
  } finally {
    isLoading.value = false;
  }
};

const actionRequestMaterial = async () => {
  if (!confirm('Ajukan pengeluaran kartu blank dari gudang ke approver (INVENTORY_OFFICER)?')) return;
  isSubmitting.value = true;
  try {
    await api.post(`/production-orders/${route.params.id}/request-material`);
    toast.success('Permintaan pengeluaran kartu blank berhasil diajukan.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal mengajukan pengeluaran bahan: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const actionApproveMaterial = async () => {
  if (!confirm('Setujui pengeluaran bahan baku kartu blank untuk SPK ini?')) return;
  isSubmitting.value = true;
  try {
    await api.post(`/production-orders/${route.params.id}/approve-material`);
    toast.success('Pengeluaran bahan baku berhasil disetujui.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal menyetujui pengeluaran: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const actionIssueMaterial = async () => {
  if (!confirm('Keluarkan bahan baku kartu blank dari gudang? Stok On-Hand fisik akan berkurang dan tercatat di kartu stok (MATERIAL_ISSUE).')) return;
  isSubmitting.value = true;
  try {
    await api.post(`/production-orders/${route.params.id}/issue-material`);
    toast.success('Kartu blank berhasil dikeluarkan dari gudang. Proses cetak mesin dapat dimulai.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal mengeluarkan bahan: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const openResultModal = () => {
  showResultModal.value = true;
};

const submitResult = async () => {
  isSubmitting.value = true;
  try {
    let payload = {};
    if (resultInputMode.value === 'granular') {
      payload.cards = Object.keys(granularCardResults).map(cardId => ({
        embossRecordId: Number(cardId),
        result: granularCardResults[cardId]
      }));
    } else {
      payload.items = Object.keys(resultForm).map(itId => ({
        productionOrderItemId: Number(itId),
        qtyProduced: resultForm[itId].qtyProduced,
        qtyDamaged: resultForm[itId].qtyDamaged
      }));
    }

    await api.post(`/production-orders/${route.params.id}/record-result`, payload);
    showResultModal.value = false;
    toast.success('Hasil cetak dan status kartu nasabah berhasil dicatat.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal mencatat hasil: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const actionCompleteProduction = async () => {
  if (!confirm('Selesaikan QC produksi? Kartu debit emboss yang lolos QC akan otomatis dimasukkan ke stok gudang (PRODUCTION_IN).')) return;
  isSubmitting.value = true;
  try {
    await api.post(`/production-orders/${route.params.id}/complete`);
    toast.success('Produksi selesai! Kartu emboss telah masuk ke stok gudang.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal menyelesaikan produksi: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const actionFulfillOrders = async () => {
  if (!confirm('Penuhi bulk order cabang dari SPK ini? Order cabang akan disetujui dan kartu dialokasikan ke antrean picking gudang.')) return;
  isSubmitting.value = true;
  try {
    await api.post(`/production-orders/${route.params.id}/fulfill-orders`);
    toast.success('Order cabang berhasil dipenuhi dan siap diproses di picking gudang.');
    await fetchDetail();
  } catch (err) {
    toast.error('Gagal memenuhi order cabang: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
</style>

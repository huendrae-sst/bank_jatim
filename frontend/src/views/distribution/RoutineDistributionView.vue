<template>
  <div class="routine-distribution-page space-y-3">
    <!-- Header & Breadcrumb -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Inisiasi Distribusi Rutin Pusat ke Cabang</h3>
        <p class="fs-8 text-secondary mb-0 mt-0.5">
          Penerbitan alokasi stok produk &amp; kuota rutin langsung dari Gudang Pusat ke kantor cabang tujuan.
        </p>
      </div>
      <div class="d-flex align-items-center gap-2">
        <router-link to="/warehouse/picking-packing" class="btn btn-sm btn-outline-secondary">
          Ke Gudang Picking / Packing
        </router-link>
        <router-link to="/distribution/shipments" class="btn btn-sm btn-outline-danger">
          Manifest Pengiriman
        </router-link>
      </div>
    </div>

    <!-- Form Utama Inisiasi Distribusi Rutin -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header bg-body py-2.5 px-3 border-bottom">
        <h6 class="card-title fw-bold mb-0 fs-6 text-body">
          Form Distribusi Rutin (Drop Kuota)
        </h6>
      </div>

      <form @submit.prevent="submitRoutineDrop">
        <div class="card-body p-3 p-md-4 space-y-4">
          <!-- 1. Parameter Memo & Periode -->
          <div class="row g-3">
            <div class="col-12 col-md-5">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                Nama Periode / Memo Distribusi <span class="text-danger">*</span>
              </label>
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary"><i class="bi bi-calendar-event"></i></span>
                <input
                  type="text"
                  v-model="formRoutine.routinePeriod"
                  class="form-control fs-8"
                  placeholder="Contoh: Drop Kuota Rutin April 2026"
                  required
                />
              </div>
            </div>

            <div class="col-12 col-md-3">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                Jadwal Target Tiba
              </label>
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary"><i class="bi bi-clock"></i></span>
                <input
                  type="date"
                  v-model="formRoutine.requiredDate"
                  class="form-control fs-8"
                />
              </div>
            </div>

            <div class="col-12 col-md-4">
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                Catatan Logistik
              </label>
              <input
                type="text"
                v-model="formRoutine.notes"
                class="form-control form-control-sm fs-8"
                placeholder="Instruksi khusus armada / ekspedisi..."
              />
            </div>
          </div>

          <hr class="my-3 opacity-25" />

          <!-- 2. Pemilihan Cabang & Alokasi Barang Khusus Tiap Cabang -->
          <div>
            <div class="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-2 mb-2">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-0">
                  <i class="bi bi-building me-1 text-danger"></i> Daftar Cabang Tujuan &amp; Alokasi Barang:
                </label>
              </div>
              <div class="d-flex gap-2 flex-wrap">
                <button type="button" class="btn btn-xs btn-outline-secondary" @click="toggleExpandAll">
                  {{ isAllExpanded ? 'Tutup Semua Rincian' : 'Buka Semua Rincian' }}
                </button>
                <button type="button" class="btn btn-xs btn-outline-secondary" @click="selectAllOrgs">
                  Pilih Semua Cabang
                </button>
                <button type="button" class="btn btn-xs btn-outline-secondary" @click="selectKcuOrgs">
                  Hanya KCU
                </button>
                <button type="button" class="btn btn-xs btn-outline-danger" @click="addBranchRow">
                  Tambah
                </button>
              </div>
            </div>

            <!-- Tabel Daftar Cabang dengan Sub-Tabel Barang (Expandable / Accordion) -->
            <div class="table-responsive border rounded-3 bg-body">
              <table class="table table-sm align-middle mb-0 fs-8">
                <thead class="table-light text-secondary fs-9 text-uppercase sticky-top shadow-xs">
                  <tr>
                    <th class="ps-3 py-2" style="min-width: 250px;">Kantor Cabang Penerima</th>
                    <th class="py-2 text-center" style="width: 120px;">Tipe Unit</th>
                    <th class="py-2 text-center" style="width: 150px;">Wilayah / Supervisi</th>
                    <th class="py-2 text-center" style="width: 180px;">Rincian Barang Cabang</th>
                    <th class="py-2 text-center" style="width: 120px;">Atur Barang</th>
                    <th class="py-2 text-center" style="width: 50px;">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="(branch, idx) in formRoutine.branches" :key="idx">
                    <!-- Baris Utama Cabang -->
                    <tr class="table-hover transition" :class="{ 'bg-danger-subtle border-danger-subtle': branch.expanded }">
                      <td class="ps-3 py-2">
                        <select v-model="branch.orgId" class="form-select form-select-sm fs-8 fw-semibold" required>
                          <option :value="null">-- Pilih Kantor Cabang --</option>
                          <option
                            v-for="org in availableOrganizations"
                            :key="org.id"
                            :value="org.id"
                            :disabled="isBranchDisabled(org.id, idx)"
                          >
                            {{ org.name }} [{{ org.code }}] ({{ org.type === 'CABANG_UTAMA' ? 'KCU' : (org.type === 'CABANG_PEMBANTU' ? 'KCP' : org.type) }})
                          </option>
                        </select>
                      </td>
                      <td class="text-center py-2">
                        <span v-if="getOrgMeta(branch.orgId)" class="badge" :class="getOrgMeta(branch.orgId)?.type === 'CABANG_UTAMA' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-secondary-subtle text-secondary border'">
                          {{ getOrgMeta(branch.orgId)?.type === 'CABANG_UTAMA' ? 'KCU / Utama' : (getOrgMeta(branch.orgId)?.type === 'CABANG_PEMBANTU' ? 'KCP / Kas' : getOrgMeta(branch.orgId)?.type) }}
                        </span>
                        <span v-else class="text-muted fs-9">-</span>
                      </td>
                      <td class="text-center py-2 fs-9 text-secondary">
                        <span v-if="getOrgMeta(branch.orgId)">
                          {{ getOrgMeta(branch.orgId)?.region?.name || getOrgMeta(branch.orgId)?.region?.code || 'Wilayah Jawa Timur' }}
                        </span>
                        <span v-else class="text-muted">-</span>
                      </td>
                      <td class="text-center py-2">
                        <span class="badge" :class="getBranchValidItems(branch).length > 0 ? 'bg-danger-subtle text-danger border border-danger-subtle' : 'bg-secondary-subtle text-secondary'">
                          {{ getBranchValidItems(branch).length }} Item &bull; {{ getBranchTotalQty(branch).toLocaleString('id-ID') }} pcs
                        </span>
                      </td>
                      <td class="text-center py-2">
                        <button
                          type="button"
                          class="btn btn-xs fw-semibold"
                          :class="branch.expanded ? 'btn-danger text-white' : 'btn-outline-danger'"
                          @click="branch.expanded = !branch.expanded"
                          title="Buka/Tutup pengaturan barang cabang ini"
                        >
                          {{ branch.expanded ? 'Tutup' : 'Atur Barang' }}
                        </button>
                      </td>
                      <td class="text-center py-2">
                        <button
                          type="button"
                          class="btn btn-sm text-danger p-0"
                          @click="removeBranchRow(idx)"
                          :disabled="formRoutine.branches.length === 1"
                          title="Hapus cabang ini dari daftar"
                        >
                          <i class="bi bi-trash fs-8"></i>
                        </button>
                      </td>
                    </tr>

                    <!-- Baris Sub-Tabel Barang Khusus Cabang Ini (Expandable / Accordion) -->
                    <tr v-if="branch.expanded" class="bg-body-tertiary">
                      <td colspan="6" class="p-2.5">
                        <div class="card card-body bg-body shadow-xs border border-danger-subtle rounded-3 p-2.5">
                          <div class="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-2 mb-2 pb-1.5 border-bottom">
                            <div class="d-flex align-items-center gap-2">
                              <i class="bi bi-box-seam text-danger"></i>
                              <span class="fs-8 fw-bold text-body">
                                Alokasi Barang untuk:
                                <span class="text-danger">{{ getOrgMeta(branch.orgId)?.name || 'Cabang Belum Dipilih' }}</span>
                              </span>
                            </div>
                            <div class="d-flex gap-1.5 flex-wrap">
                              <button
                                type="button"
                                class="btn btn-xs btn-outline-secondary"
                                @click="applyPresetToBranch(branch)"
                                title="Gunakan set produk operasional standar"
                              >
                                Preset Standar
                              </button>
                              <button
                                type="button"
                                class="btn btn-xs btn-outline-secondary"
                                @click="copyItemsToAllBranches(branch)"
                                :disabled="getBranchValidItems(branch).length === 0"
                                title="Salin jenis & jumlah barang ini ke seluruh cabang lain"
                              >
                                Terapkan ke Semua Cabang
                              </button>
                              <button
                                type="button"
                                class="btn btn-xs btn-outline-danger"
                                @click="addItemToBranch(branch)"
                              >
                                Tambah
                              </button>
                            </div>
                          </div>

                          <!-- Sub-Tabel Barang -->
                          <div class="table-responsive border rounded-2">
                            <table class="table table-sm table-hover align-middle mb-0 fs-8 bg-body">
                              <thead class="table-light text-secondary fs-9 text-uppercase">
                                <tr>
                                  <th class="ps-3 py-1.5" style="min-width: 260px;">Pilih Barang / Produk Operasional</th>
                                  <th class="py-1.5 text-center" style="width: 140px;">Kategori / Satuan</th>
                                  <th class="py-1.5 text-center" style="width: 140px;">Jumlah Kuota</th>
                                  <th class="py-1.5 text-center" style="width: 45px;">Aksi</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="(it, itIdx) in branch.items" :key="itIdx">
                                  <td class="ps-3 py-1.5">
                                    <select v-model="it.itemId" class="form-select form-select-sm fs-8" required>
                                      <option :value="null">-- Pilih Barang dari Master Data --</option>
                                      <option v-for="item in masterItems" :key="item.id" :value="item.id">
                                        {{ item.name }} [{{ item.sku }}]
                                      </option>
                                    </select>
                                  </td>
                                  <td class="text-center py-1.5 fs-9 text-secondary font-monospace">
                                    {{ getItemMeta(it.itemId)?.category?.name || 'Produk' }} ({{ getItemMeta(it.itemId)?.uom || 'Unit' }})
                                  </td>
                                  <td class="text-center py-1.5">
                                    <div class="input-group input-group-sm">
                                      <input
                                        type="number"
                                        v-model.number="it.qty"
                                        min="1"
                                        class="form-control form-control-sm text-center font-monospace fw-bold fs-8"
                                        placeholder="Jumlah"
                                        required
                                      />
                                      <span class="input-group-text bg-body text-secondary fs-9">
                                        {{ getItemMeta(it.itemId)?.uom || 'Unit' }}
                                      </span>
                                    </div>
                                  </td>
                                  <td class="text-center py-1.5">
                                    <button
                                      type="button"
                                      class="btn btn-sm text-danger p-0"
                                      @click="removeItemFromBranch(branch, itIdx)"
                                      :disabled="branch.items.length === 1"
                                      title="Hapus baris barang ini"
                                    >
                                      <i class="bi bi-trash fs-8"></i>
                                    </button>
                                  </td>
                                </tr>
                              </tbody>
                              <tfoot class="table-light border-top">
                                <tr>
                                  <td colspan="4" class="ps-3 py-1.5">
                                    <div class="d-flex justify-content-between align-items-center">
                                      <button type="button" class="btn btn-xs btn-link text-danger text-decoration-none p-0" @click="addItemToBranch(branch)">
                                        Tambah
                                      </button>
                                      <span class="fs-9 text-secondary">
                                        Subtotal Cabang Ini: <strong>{{ getBranchTotalQty(branch).toLocaleString('id-ID') }}</strong> pcs
                                      </span>
                                    </div>
                                  </td>
                                </tr>
                              </tfoot>
                            </table>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </template>
                </tbody>
                <tfoot class="table-light border-top sticky-bottom">
                  <tr>
                    <td colspan="6" class="ps-3 py-2.5">
                      <div class="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-2">
                        <button type="button" class="btn btn-xs btn-outline-danger" @click="addBranchRow">
                          Tambah
                        </button>
                        <div class="d-flex align-items-center gap-3">
                          <span class="fs-8 text-secondary">
                            Cabang Terpilih: <strong class="text-body">{{ validBranches.length }}</strong> Cabang
                          </span>
                          <span class="fs-8 text-secondary border-start ps-3">
                            Akumulasi Fisik Seluruh Cabang: <strong class="text-danger fs-7">{{ totalAccumulatedPhysicalQty.toLocaleString('id-ID') }}</strong> pcs
                          </span>
                          <button
                            v-if="formRoutine.branches.length > 0"
                            type="button"
                            class="btn btn-link text-secondary text-decoration-none fs-9 p-0 border-start ps-3"
                            @click="clearBranches"
                          >
                            Kosongkan Pilihan
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>

        <!-- Card Footer -->
        <div class="card-footer bg-body-tertiary border-top p-3 d-flex flex-column flex-sm-row justify-content-between align-items-center gap-2">
          <div class="fs-8 text-secondary">
            <i class="bi bi-shield-check text-success me-1"></i>
            Order otomatis berstatus <strong>APPROVED</strong> dan langsung masuk ke antrean <strong>Picking Gudang</strong>.
          </div>
          <div class="d-flex align-items-center gap-2">
            <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="resetForm" :disabled="submitting">
              Reset
            </button>
            <button
              type="submit"
              class="btn btn-sm btn-danger fw-bold shadow-xs px-4 fs-8"
              :disabled="submitting || validBranches.length === 0"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              Terbitkan Distribusi Rutin ({{ validBranches.length }} Cabang)
            </button>
          </div>
        </div>
      </form>
    </div>

    <!-- Riwayat Penerbitan Distribusi Rutin Terakhir (Tabel Sederhana Tanpa Tabs) -->
    <div class="card card-outline card-secondary shadow-xs mt-4">
      <div class="card-header bg-body p-3 border-bottom d-flex align-items-center justify-content-between">
        <div>
          <h6 class="card-title fw-bold mb-0 fs-6 text-body">
            Riwayat Penerbitan Distribusi Rutin Terakhir
          </h6>
          <div class="fs-9 text-secondary">Order rutin yang telah diterbitkan dan sedang dalam proses pemenuhan gudang.</div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <button type="button" class="btn btn-xs btn-outline-secondary" @click="loadRoutineHistory" title="Refresh Riwayat">
            Segarkan
          </button>
        </div>
      </div>

      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-8">
            <thead class="table-light text-secondary text-uppercase fs-9">
              <tr>
                <th class="ps-3 py-2">Nomor Order</th>
                <th class="py-2">Periode / Memo</th>
                <th class="py-2">Cabang Penerima</th>
                <th class="py-2">Item Barang</th>
                <th class="py-2 text-center">Status Alur Gudang</th>
                <th class="py-2 text-center">Status Pemenuhan</th>
                <th class="py-2 text-center pe-3">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="ord in routineHistory" :key="ord.id">
                <td class="ps-3 py-2.5 font-monospace fw-bold text-danger">
                  <router-link :to="`/orders/${ord.id}`" class="text-decoration-none text-danger">
                    {{ ord.orderNumber }}
                  </router-link>
                  <div class="fs-9 text-secondary">{{ formatDate(ord.createdAt) }}</div>
                </td>
                <td class="py-2.5">
                  <span class="badge bg-secondary-subtle text-secondary border font-monospace fs-9">
                    {{ ord.routinePeriod || 'Distribusi Rutin' }}
                  </span>
                </td>
                <td class="py-2.5">
                  <div class="fw-semibold text-body">{{ ord.requestingOrganization?.name || '-' }}</div>
                  <div class="fs-9 text-secondary font-monospace">{{ ord.requestingOrganization?.code || '-' }}</div>
                </td>
                <td class="py-2.5 text-body">
                  <span class="badge text-bg-light border font-monospace fs-9">{{ ord.items?.length || 0 }} Items</span>
                </td>
                <td class="py-2.5 text-center">
                  <span class="badge fs-9 text-uppercase" :class="statusBadgeClass(ord.status)">
                    {{ ord.status ? ord.status.replace(/_/g, ' ') : 'APPROVED' }}
                  </span>
                </td>
                <td class="py-2.5 text-center">
                  <span class="badge fs-9 text-uppercase" :class="fulfillmentBadgeClass(ord.fulfillmentStatus)">
                    {{ ord.fulfillmentStatus || 'UNFULFILLED' }}
                  </span>
                </td>
                <td class="py-2.5 text-center pe-3">
                  <router-link :to="`/orders/${ord.id}`" class="btn btn-xs btn-outline-secondary py-1 px-2" title="Lihat Detail Order">
                    Detail
                  </router-link>
                </td>
              </tr>
              <tr v-if="routineHistory.length === 0">
                <td colspan="7" class="text-center py-4 text-secondary">
                  <i class="bi bi-inbox fs-2 text-muted d-block mb-1"></i>
                  Belum ada riwayat distribusi rutin yang diterbitkan. Silakan isi form di atas.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import { toast } from '@/utils/toast';

const submitting = ref(false);
const loadingRefs = ref(false);

const organizations = ref([]);
const masterItems = ref([]);
const routineHistory = ref([]);

const formRoutine = ref({
  routinePeriod: '',
  requiredDate: new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0],
  notes: 'Inisiasi drop kuota persediaan rutin dari Gudang Logistik Pusat',
  branches: [
    {
      orgId: null,
      expanded: true,
      items: [
        { itemId: null, qty: 100 }
      ]
    }
  ]
});

const createDefaultItems = () => {
  if (masterItems.value.length === 0) {
    return [{ itemId: null, qty: 100 }];
  }
  return [
    { itemId: masterItems.value[0]?.id || null, qty: 100 }
  ];
};

const getBranchValidItems = (branch) => {
  if (!branch || !branch.items) return [];
  return branch.items.filter(it => it.itemId && Number(it.qty) > 0);
};

const getBranchTotalQty = (branch) => {
  return getBranchValidItems(branch).reduce((acc, it) => acc + (Number(it.qty) || 0), 0);
};

const validBranches = computed(() => {
  return formRoutine.value.branches.filter(b => b.orgId && getBranchValidItems(b).length > 0);
});

const totalAccumulatedPhysicalQty = computed(() => {
  return validBranches.value.reduce((total, b) => total + getBranchTotalQty(b), 0);
});

const extractList = (res) => {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (Array.isArray(res.data)) return res.data;
  if (Array.isArray(res.data?.content)) return res.data.content;
  if (Array.isArray(res.content)) return res.content;
  return [];
};

const loadReferences = async () => {
  loadingRefs.value = true;
  try {
    const [orgsRes, itemsRes] = await Promise.allSettled([
      api.get('/master/organizations'),
      api.get('/master/items')
    ]);

    if (orgsRes.status === 'fulfilled') {
      organizations.value = extractList(orgsRes.value);
    } else {
      console.warn('Gagal memuat /master/organizations:', orgsRes.reason);
      organizations.value = [];
    }

    if (itemsRes.status === 'fulfilled') {
      masterItems.value = extractList(itemsRes.value);
    } else {
      console.warn('Gagal memuat /master/items:', itemsRes.reason);
      masterItems.value = [];
    }

    // Set default period name with current Indonesian month
    const currentMonth = new Date().toLocaleString('id-ID', { month: 'long', year: 'numeric' });
    if (!formRoutine.value.routinePeriod) {
      formRoutine.value.routinePeriod = `Drop Kuota Rutin ${currentMonth}`;
    }

    // Apply default branch row if availableOrganizations loaded
    if (availableOrganizations.value.length > 0 && formRoutine.value.branches[0].orgId === null) {
      formRoutine.value.branches[0].orgId = availableOrganizations.value[0].id;
      if (masterItems.value.length > 0) {
        formRoutine.value.branches[0].items = createDefaultItems();
      }
    }
  } catch (err) {
    console.error('Error saat loadReferences:', err);
  } finally {
    loadingRefs.value = false;
  }
};

const loadRoutineHistory = async () => {
  try {
    const res = await api.get('/distribution/routine-drops');
    routineHistory.value = extractList(res);
  } catch (err) {
    console.warn('Gagal memuat riwayat /distribution/routine-drops:', err);
    routineHistory.value = [];
  }
};

const availableOrganizations = computed(() => {
  // Hanya tampilkan cabang/unit kerja yang bukan kantor pusat bila type === 'KANTOR_PUSAT'
  return organizations.value.filter(org => org.type !== 'KANTOR_PUSAT');
});

const getOrgMeta = (orgId) => {
  return organizations.value.find(org => org.id === orgId) || null;
};

const getItemMeta = (itemId) => {
  return masterItems.value.find(item => item.id === itemId) || null;
};

const isBranchDisabled = (orgId, currentIdx) => {
  return formRoutine.value.branches.some((b, idx) => idx !== currentIdx && b.orgId === orgId);
};

const addBranchRow = () => {
  const available = availableOrganizations.value.find(o => !formRoutine.value.branches.some(b => b.orgId === o.id));
  formRoutine.value.branches.push({
    orgId: available ? available.id : null,
    expanded: true,
    items: createDefaultItems()
  });
};

const removeBranchRow = (idx) => {
  if (formRoutine.value.branches.length > 1) {
    formRoutine.value.branches.splice(idx, 1);
  }
};

const selectAllOrgs = () => {
  if (availableOrganizations.value.length === 0) return;
  formRoutine.value.branches = availableOrganizations.value.map((o, idx) => ({
    orgId: o.id,
    expanded: idx === 0,
    items: createDefaultItems()
  }));
};

const selectKcuOrgs = () => {
  const kcuList = availableOrganizations.value.filter(o => o.type === 'CABANG_UTAMA');
  if (kcuList.length > 0) {
    formRoutine.value.branches = kcuList.map((o, idx) => ({
      orgId: o.id,
      expanded: idx === 0,
      items: createDefaultItems()
    }));
  } else {
    selectAllOrgs();
  }
};

const clearBranches = () => {
  formRoutine.value.branches = [{
    orgId: null,
    expanded: true,
    items: createDefaultItems()
  }];
};

const isAllExpanded = computed(() => {
  return formRoutine.value.branches.length > 0 && formRoutine.value.branches.every(b => b.expanded);
});

const toggleExpandAll = () => {
  const target = !isAllExpanded.value;
  formRoutine.value.branches.forEach(b => {
    b.expanded = target;
  });
};

const toggleAllExpanded = (status) => {
  formRoutine.value.branches.forEach(b => {
    b.expanded = status;
  });
};

const addItemToBranch = (branch) => {
  const nextItem = masterItems.value.find(m => !branch.items.some(r => r.itemId === m.id)) || masterItems.value[0];
  branch.items.push({
    itemId: nextItem ? nextItem.id : null,
    qty: 50
  });
};

const removeItemFromBranch = (branch, itIdx) => {
  if (branch.items.length > 1) {
    branch.items.splice(itIdx, 1);
  }
};

const applyPresetToBranch = (branch) => {
  if (masterItems.value.length === 0) return;
  branch.items = masterItems.value.slice(0, 4).map((m, idx) => ({
    itemId: m.id,
    qty: [100, 50, 200, 20][idx % 4]
  }));
};

const copyItemsToAllBranches = (sourceBranch) => {
  const valid = getBranchValidItems(sourceBranch);
  if (valid.length === 0) {
    toast.warn('Silakan pilih minimal 1 item barang yang valid pada cabang ini sebelum menyalin.');
    return;
  }
  const orgName = getOrgMeta(sourceBranch.orgId)?.name || 'cabang ini';
  if (!confirm(`Salin susunan ${valid.length} barang dari ${orgName} ke seluruh cabang tujuan lainnya?`)) {
    return;
  }
  formRoutine.value.branches.forEach(b => {
    if (b !== sourceBranch) {
      b.items = valid.map(it => ({ itemId: it.itemId, qty: it.qty }));
    }
  });
  toast.success(`Berhasil menyalin susunan barang ke seluruh ${formRoutine.value.branches.length - 1} cabang lainnya.`);
};

const resetForm = () => {
  const currentMonth = new Date().toLocaleString('id-ID', { month: 'long', year: 'numeric' });
  formRoutine.value = {
    routinePeriod: `Drop Kuota Rutin ${currentMonth}`,
    requiredDate: new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0],
    notes: 'Inisiasi drop kuota persediaan rutin dari Gudang Logistik Pusat',
    branches: [
      {
        orgId: availableOrganizations.value[0]?.id || null,
        expanded: true,
        items: createDefaultItems()
      }
    ]
  };
};

const submitRoutineDrop = async () => {
  if (validBranches.value.length === 0) {
    toast.warn('Silakan pilih minimal 1 kantor cabang tujuan dan tentukan barang alokasinya.');
    return;
  }

  const confirmMsg = `Konfirmasi penerbitan Distribusi Rutin (${formRoutine.value.routinePeriod}) untuk ${validBranches.value.length} cabang dengan total ${totalAccumulatedPhysicalQty.value.toLocaleString('id-ID')} fisik barang?\n\nOrder spesifik masing-masing cabang akan otomatis dibuat dan masuk ke Antrean Picking Gudang.`;
  if (!confirm(confirmMsg)) return;

  submitting.value = true;

  try {
    const branchAllocations = validBranches.value.map(b => ({
      organizationId: b.orgId,
      items: getBranchValidItems(b).map(it => ({
        itemId: it.itemId,
        qty: it.qty
      }))
    }));

    // Fallback items array for dual compatibility
    const fallbackItems = [];
    const itemMap = new Map();
    branchAllocations.forEach(ba => {
      ba.items.forEach(it => {
        if (!itemMap.has(it.itemId)) {
          itemMap.set(it.itemId, it.qty);
        }
      });
    });
    itemMap.forEach((qty, itemId) => {
      fallbackItems.push({ itemId, qty });
    });

    const payload = {
      organizationIds: validBranches.value.map(b => b.orgId),
      items: fallbackItems.length > 0 ? fallbackItems : (branchAllocations[0]?.items || []),
      routinePeriod: formRoutine.value.routinePeriod,
      notes: formRoutine.value.notes,
      branchAllocations: branchAllocations
    };

    const res = await api.post('/distribution/routine-drops', payload);
    const createdOrders = extractList(res);

    toast.success(`Sukses! Berhasil menerbitkan Distribusi Rutin untuk ${createdOrders.length || validBranches.value.length} kantor cabang dengan rincian barang spesifik masing-masing. Seluruh order telah berstatus APPROVED dan langsung masuk ke Antrean Picking Gudang.`);

    await loadRoutineHistory();
    formRoutine.value.branches = [{
      orgId: availableOrganizations.value[0]?.id || null,
      expanded: true,
      items: createDefaultItems()
    }];
  } catch (err) {
    console.error('Error submitRoutineDrop:', err);
    const serverErr = err?.response?.data?.message || err?.response?.data?.error || err?.message || err;
    toast.error('Gagal menerbitkan distribusi rutin: ' + serverErr);
  } finally {
    submitting.value = false;
  }
};

const statusBadgeClass = (status) => {
  switch (status) {
    case 'APPROVED': return 'bg-primary text-white';
    case 'PICKING': return 'bg-warning text-dark';
    case 'PACKING': return 'bg-info text-dark';
    case 'READY_TO_SHIP': return 'bg-primary-subtle text-primary border border-primary';
    case 'IN_TRANSIT': return 'bg-info-subtle text-info border border-info';
    case 'COMPLETED':
    case 'RECEIVED': return 'bg-success text-white';
    default: return 'bg-secondary text-white';
  }
};

const fulfillmentBadgeClass = (fStatus) => {
  switch (fStatus) {
    case 'FULFILLED': return 'bg-success text-white';
    case 'PARTIALLY_FULFILLED': return 'bg-warning text-dark';
    default: return 'bg-secondary text-white';
  }
};

const formatDate = (dt) => {
  if (!dt) return '-';
  try {
    return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch (e) {
    return dt;
  }
};

onMounted(() => {
  loadReferences();
  loadRoutineHistory();
});
</script>

<style scoped>
.transition {
  transition: all 0.15s ease-in-out;
}
.btn-xs {
  padding: 0.2rem 0.5rem;
  font-size: 0.75rem;
  line-height: 1.2;
}
</style>

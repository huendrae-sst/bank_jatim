<template>
  <div class="orders-page">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Daftar Order Permintaan Cabang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/orders" class="text-decoration-none text-danger">Orders</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Daftar Order</li>
            </ol>
          </div>
        </div>
      </div>
    </div>


    <!-- 3. Main Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Tabs and Section Tombol Tambah Aligned Side-by-Side -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <!-- Status Navigation Tabs -->
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="currentTab === 'open' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="currentTab = 'open'"
            >
              Order Terbuka
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="currentTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="currentTab = 'completed'"
            >
              Riwayat Order
            </button>
          </li>
        </ul>

        <!-- Section Tombol Tambah Aligned with Tabs -->
        <div v-if="!authStore.isRegionalUser" class="card-tools ms-md-auto">
          <button type="button" @click="openCreateModal" class="btn btn-sm btn-danger fw-bold shadow-xs">
            <i class="bi bi-plus-lg me-1"></i> Buat Order Baru
          </button>
        </div>
      </div>

      <!-- 4. Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <!-- Organization Filter -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterOrg" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Unit Kerja</option>
                <option v-for="org in organizations" :key="org.id" :value="org.id">
                  {{ org.name }}
                </option>
              </select>
            </div>
          </div>

          <!-- Status Filter -->
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="ALL">Semua Status</option>
                <option value="DRAFT">Draft</option>
                <option value="SUBMITTED">Diajukan</option>
                <option value="APPROVED">Disetujui</option>
                <option value="ALLOCATED">Teralokasi</option>
                <option value="PICKING">Picking</option>
                <option value="READY_TO_SHIP">Siap Kirim</option>
                <option value="IN_TRANSIT">Dalam Perjalanan</option>
                <option value="RECEIVED">Diterima</option>
                <option value="COMPLETED">Selesai</option>
                <option value="REJECTED">Ditolak</option>
              </select>
            </div>
          </div>

          <!-- Sort Field Filter -->
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-sort-down"></i></span>
              <select v-model="sortBy" class="form-select form-select-sm border-start-0 fs-8">
                <option value="created_at">Tanggal Dibuat</option>
                <option value="order_number">Nomor Order</option>
                <option value="total_estimated_value">Total Estimasi Nilai</option>
                <option value="status">Status Transaksi</option>
              </select>
            </div>
          </div>

          <!-- Reset Button -->
          <div class="col-auto" v-if="isFiltered">
            <button type="button" @click="resetFilters" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
          </div>

          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                placeholder="Cari nomor order atau pemohon..."
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
              />
              <button type="button" @click="currentPage = 1" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">
                <i class="bi bi-search me-1"></i> Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table Responsive Container -->
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-7">
            <thead class="border-bottom fs-8 text-uppercase fw-semibold text-secondary bg-body-tertiary">
              <tr>
                <th class="ps-3 ps-md-4 py-3" style="min-width: 170px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('order_number')">
                    Nomor Order
                    <i v-if="sortBy === 'order_number'" class="bi" :class="sortDir === 'asc' ? 'bi-sort-up text-danger fw-bold' : 'bi-sort-down text-danger fw-bold'"></i>
                    <i v-else class="bi bi-arrow-down-up opacity-25 fs-9"></i>
                  </span>
                </th>
                <th class="py-3" style="min-width: 190px;">Unit Kerja Peminta</th>
                <th class="text-center py-3" style="width: 150px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('status')">
                    Status Alur
                    <i v-if="sortBy === 'status'" class="bi" :class="sortDir === 'asc' ? 'bi-sort-up text-danger fw-bold' : 'bi-sort-down text-danger fw-bold'"></i>
                    <i v-else class="bi bi-arrow-down-up opacity-25 fs-9"></i>
                  </span>
                </th>
                <th class="text-end py-3" style="min-width: 140px;">
                  <span class="d-inline-flex align-items-center justify-content-end gap-1 cursor-pointer" @click="toggleSort('total_estimated_value')">
                    Total Nilai
                    <i v-if="sortBy === 'total_estimated_value'" class="bi" :class="sortDir === 'asc' ? 'bi-sort-numeric-up text-danger fw-bold' : 'bi-sort-numeric-down text-danger fw-bold'"></i>
                    <i v-else class="bi bi-arrow-down-up opacity-25 fs-9"></i>
                  </span>
                </th>
                <th class="text-end py-3" style="min-width: 130px;">Ongkir Ekspedisi</th>
                <th class="py-3" style="min-width: 160px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('created_at')">
                    Tanggal Order
                    <i v-if="sortBy === 'created_at'" class="bi" :class="sortDir === 'asc' ? 'bi-sort-up text-danger fw-bold' : 'bi-sort-down text-danger fw-bold'"></i>
                    <i v-else class="bi bi-arrow-down-up opacity-25 fs-9"></i>
                  </span>
                </th>
                <th class="text-center pe-3 pe-md-4 py-3" style="width: 120px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="ord in paginatedOrders" :key="ord.id">
                <!-- Order Number & Maker -->
                <td class="ps-3 ps-md-4">
                  <a href="#" @click.prevent="openViewModal(ord)" class="fw-bold font-monospace text-danger text-decoration-none">
                    {{ ord.order_number }}
                  </a>
                  <div class="fs-8 text-secondary">
                    <i class="bi bi-person me-1"></i>{{ ord.requester?.name || '-' }}
                  </div>
                </td>

                <!-- Organization -->
                <td>
                  <span class="fw-semibold text-body">{{ ord.requesting_organization?.name || '-' }}</span>
                  <div class="fs-8 font-monospace text-secondary">
                    <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle fs-8">
                      {{ ord.requesting_organization?.code || '-' }}
                    </span>
                  </div>
                </td>

                <!-- Status Badge -->
                <td class="text-center">
                  <span class="badge fs-8 text-uppercase" :class="getBadgeClass(ord.status)">
                    {{ getStatusLabel(ord.status) }}
                  </span>
                </td>

                <!-- Total Value -->
                <td class="text-end fw-bold font-monospace text-body-emphasis">
                  {{ formatRupiah(ord.total_estimated_value) }}
                </td>

                <!-- Shipping Cost -->
                <td class="text-end font-monospace text-secondary">
                  {{ ord.shipping_cost ? formatRupiah(ord.shipping_cost) : '-' }}
                </td>

                <!-- Date -->
                <td class="text-secondary fs-8">
                  {{ formatDate(ord.created_at) }}
                </td>

                <!-- 5. Tombol Aksi -->
                <td class="text-center pe-3 pe-md-4 py-2">
                  <div class="d-inline-flex align-items-center gap-1">
                    <button
                      type="button"
                      @click="openViewModal(ord)"
                      class="btn-action-icon text-secondary"
                      title="Lihat Detail Order"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <router-link
                      :to="`/orders/${ord.id}/print`"
                      target="_blank"
                      class="btn-action-icon text-dark"
                      title="Cetak Dokumen Order"
                    >
                      <i class="bi bi-printer"></i>
                    </router-link>
                    <button
                      v-if="!authStore.isRegionalUser"
                      type="button"
                      @click="openEditModal(ord)"
                      class="btn-action-icon text-primary"
                      title="Edit Order"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      v-if="!authStore.isRegionalUser"
                      type="button"
                      @click="openDeleteModal(ord)"
                      class="btn-action-icon text-danger"
                      title="Hapus Order"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>

              <!-- Empty State -->
              <tr v-if="paginatedOrders.length === 0">
                <td colspan="7" class="text-center py-5 text-secondary">
                  <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                  <p class="fw-bold mb-1">Tidak ada data order ditemukan</p>
                  <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau bersihkan filter.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 7. Paging / Pagination Footer -->
      <PaginationFooter
        :total="filteredOrders.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- 6. MODALS FROM JATIM_PHP -->

    <!-- ==================== MODAL 1: BUAT ORDER BARU ==================== -->
    <div v-if="createModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="createModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Buat Order Permintaan Baru
            </h6>
            <button type="button" class="btn-close-modal" @click="createModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Form Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <!-- Parameter Order -->
            <div class="row g-2.5 mb-3">
              <div class="col-12 col-md-4">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                  Unit Kerja Peminta <span class="text-danger">*</span>
                </label>
                <select v-model="newOrderForm.organization_id" class="form-select form-select-sm fs-8" required>
                  <option v-for="org in organizations" :key="org.id" :value="org.id">
                    {{ org.name }} ({{ org.code }})
                  </option>
                </select>
              </div>
              <div class="col-12 col-md-4">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                  Prioritas Permintaan <span class="text-danger">*</span>
                </label>
                <select v-model="newOrderForm.priority" class="form-select form-select-sm fs-8" required>
                  <option value="NORMAL">Normal (Standar Operasional)</option>
                  <option value="HIGH">High (Prioritas Tinggi)</option>
                  <option value="URGENT">Urgent (Sangat Mendesak)</option>
                </select>
              </div>
              <div class="col-12 col-md-4">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                  Target Tanggal Dibutuhkan <span class="text-danger">*</span>
                </label>
                <input type="date" v-model="newOrderForm.required_date" class="form-control form-control-sm fs-8" required />
              </div>
              <div class="col-12">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan / Keterangan Order</label>
                <input type="text" v-model="newOrderForm.notes" placeholder="Tuliskan catatan kebutuhan atau alasan pesanan..." class="form-control form-control-sm fs-8" />
              </div>
            </div>

            <!-- Section: Daftar Item -->
            <div>
              <div class="d-flex align-items-center justify-content-between mb-1.5">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-0">
                  Daftar Barang yang Diminta ({{ createRows.length }} item)
                </label>
                <button type="button" @click="addCreateRow" class="btn btn-sm btn-outline-danger py-1 px-2 fs-8 fw-semibold">
                  <i class="bi bi-plus-circle me-1"></i> Tambah Item
                </button>
              </div>

              <div class="table-responsive border rounded bg-body mb-2" style="max-height: 220px; overflow-y: auto;">
                <table class="table table-sm table-hover align-middle mb-0 fs-8">
                  <thead class="table-light text-secondary fs-9 text-uppercase sticky-top">
                    <tr>
                      <th class="ps-3 py-1.5">Pilih Barang / Item</th>
                      <th class="text-center py-1.5" style="width: 140px;">Jumlah (Qty)</th>
                      <th class="text-end py-1.5" style="width: 140px;">Subtotal Ref</th>
                      <th class="text-center py-1.5" style="width: 45px;">Hapus</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, index) in createRows" :key="index">
                      <td class="ps-3 py-1.5">
                        <select
                          v-model="row.item_id"
                          @change="onCreateItemChange(index)"
                          class="form-select form-select-sm fs-8"
                          required
                        >
                          <option value="">-- Pilih Barang / Item --</option>
                          <option v-for="it in itemsCatalog" :key="it.id" :value="it.id">
                            {{ it.name }} [{{ it.sku }}] ({{ it.uom }})
                          </option>
                        </select>
                        <div class="fs-9 text-secondary font-monospace mt-1" v-if="row.item_id">
                          Harga Ref: {{ formatRupiah(row.price) }}
                        </div>
                      </td>
                      <td class="text-center py-1.5">
                        <input
                          type="number"
                          v-model.number="row.qty"
                          @input="onCreateQtyChange(index)"
                          min="1"
                          required
                          class="form-control form-control-sm text-center fw-bold font-monospace fs-8"
                        />
                      </td>
                      <td class="text-end font-monospace pe-2 py-1.5">
                        <span class="fw-semibold text-body">{{ formatRupiah(row.subtotal) }}</span>
                      </td>
                      <td class="text-center py-1.5">
                        <button
                          type="button"
                          @click="removeCreateRow(index)"
                          :disabled="createRows.length <= 1"
                          class="btn btn-sm text-danger py-0 px-1"
                          title="Hapus baris barang"
                        >
                          <i class="bi bi-trash fs-8"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                  <tfoot class="bg-body-secondary border-top fw-bold">
                    <tr>
                      <td colspan="2" class="ps-3 py-2 text-secondary">Total Estimasi Nilai:</td>
                      <td class="text-end font-monospace text-danger fs-7 pe-2 py-2">{{ formatRupiah(createTotal) }}</td>
                      <td></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </div>
          </div>

          <!-- Modal Footer (Tombol Batal dan Submit Order Rata Kanan) -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" @click="createModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8">
              Batal
            </button>
            <button type="button" @click="submitCreateOrder" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8">
              <i class="bi bi-send me-1"></i> Submit Order
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL 2: DETAIL ORDER ==================== -->
    <div v-if="viewModal && viewOrder" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="viewModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <h6 class="modal-title fw-bold text-body fs-6 mb-0">
                Detail Order: <span class="font-monospace text-danger">{{ viewOrder.order_number }}</span>
              </h6>
              <span class="badge fs-8 text-uppercase" :class="getBadgeClass(viewOrder.status)">
                {{ getStatusLabel(viewOrder.status) }}
              </span>
            </div>
            <button type="button" class="btn-close-modal" @click="viewModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Modal Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <!-- Summary Cards -->
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-2 fs-8">
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Unit Pemohon:</span>
                  <strong class="text-body">{{ viewOrder.requesting_organization?.name || '-' }}</strong>
                  <div class="font-monospace text-secondary fs-9">
                    Kode: {{ viewOrder.requesting_organization?.code || '-' }}
                  </div>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Dibuat Oleh:</span>
                  <span class="text-body fw-semibold">{{ viewOrder.requester?.name || '-' }}</span>
                  <div class="font-monospace text-secondary fs-9">
                    NIP: {{ viewOrder.requester?.nip || '-' }}
                  </div>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Prioritas Permintaan:</span>
                  <span class="badge bg-danger-subtle text-danger border border-danger-subtle fs-8">
                    {{ viewOrder.priority || 'NORMAL' }}
                  </span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Tanggal Transaksi:</span>
                  <span class="text-body font-monospace">{{ formatDate(viewOrder.created_at) }}</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Target Kebutuhan:</span>
                  <span class="text-body font-monospace">{{ viewOrder.required_date || '-' }}</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Total Nilai Estimasi:</span>
                  <span class="fw-bold font-monospace fs-7 text-danger">{{ formatRupiah(viewOrder.total_estimated_value) }}</span>
                </div>
              </div>

              <div v-if="viewOrder.notes" class="mt-2.5 pt-2 border-top border-secondary-subtle fs-8">
                <span class="text-secondary fw-semibold">Catatan:</span>
                <span class="text-body ms-1 fst-italic">{{ viewOrder.notes }}</span>
              </div>
            </div>

            <!-- Items Table -->
            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1.5">Rincian Barang Diminta</label>
              <div class="table-responsive rounded border border-secondary-subtle" style="max-height: 220px; overflow-y: auto;">
                <table class="table table-sm table-hover align-middle mb-0 fs-8">
                  <thead class="bg-body-tertiary text-secondary sticky-top">
                    <tr>
                      <th class="ps-3" style="width: 40px;">No</th>
                      <th>Nama / SKU Barang</th>
                      <th class="text-center" style="width: 90px;">Satuan</th>
                      <th class="text-center" style="width: 90px;">Jumlah</th>
                      <th class="text-end" style="width: 130px;">Harga Ref</th>
                      <th class="text-end pe-3" style="width: 140px;">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(it, idx) in viewOrder.items" :key="idx">
                      <td class="ps-3 text-secondary font-monospace">{{ idx + 1 }}</td>
                      <td>
                        <span class="fw-semibold text-body">{{ it.item?.name || ('Item #' + it.item_id) }}</span>
                        <div class="fs-9 text-secondary font-monospace">{{ it.item?.sku || '' }}</div>
                      </td>
                      <td class="text-center">
                        <span class="badge bg-secondary-subtle text-secondary">{{ it.item?.uom || '-' }}</span>
                      </td>
                      <td class="text-center font-monospace fw-bold">{{ it.qty_requested }}</td>
                      <td class="text-end font-monospace text-secondary">
                        {{ formatRupiah(it.unit_price_ref || it.item?.estimated_unit_price) }}
                      </td>
                      <td class="text-end font-monospace pe-3 fw-bold text-body">
                        {{ formatRupiah(it.subtotal_ref || ((it.unit_price_ref || it.item?.estimated_unit_price || 0) * it.qty_requested)) }}
                      </td>
                    </tr>
                    <tr v-if="!viewOrder.items || viewOrder.items.length === 0">
                      <td colspan="6" class="text-center py-3 text-secondary">Tidak ada rincian item.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-between align-items-center py-2 px-3 border-top">
            <router-link
              :to="`/orders/${viewOrder.id}/print`"
              target="_blank"
              class="btn btn-sm btn-outline-danger d-inline-flex align-items-center gap-1 shadow-xs fs-8"
            >
              <i class="bi bi-printer"></i>
              <span>Cetak Order</span>
            </router-link>
            <button type="button" @click="viewModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL 3: EDIT ORDER ==================== -->
    <div v-if="editModal && editOrder" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="editModal = false">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <h6 class="modal-title fw-bold text-body fs-6 mb-0">
                Edit Order: <span class="font-monospace text-danger">{{ editOrder.order_number }}</span>
              </h6>
              <span class="badge fs-8 text-uppercase" :class="getBadgeClass(editOrder.status)">
                {{ getStatusLabel(editOrder.status) }}
              </span>
            </div>
            <button type="button" class="btn-close-modal" @click="editModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Modal Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <!-- Status Notice if not editable -->
            <div v-if="!editOrder.is_editable" class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-center gap-2">
              <i class="bi bi-exclamation-triangle-fill text-warning fs-6 flex-shrink-0"></i>
              <div>
                Order berstatus <strong>{{ getStatusLabel(editOrder.status) }}</strong>. Prioritas dan tanggal kebutuhan terkunci; hanya catatan operasional yang dapat diubah.
              </div>
            </div>

            <!-- Order Summary Box -->
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-2 fs-8">
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Unit Pemohon:</span>
                  <strong class="text-body">{{ editOrder.organization_name }}</strong>
                </div>
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Dibuat Oleh:</span>
                  <span class="text-body fw-semibold">{{ editOrder.requester_name }}</span>
                </div>
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Waktu Transaksi:</span>
                  <span class="text-body">{{ editOrder.created_at_formatted }}</span>
                </div>
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Total Estimasi:</span>
                  <span class="fw-bold font-monospace text-danger">{{ formatRupiah(editOrder.total_estimated_value) }}</span>
                </div>
              </div>
            </div>

            <!-- Priority & Required Date -->
            <div class="row g-2.5">
              <div class="col-12 col-md-6">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                  Prioritas Permintaan <span class="text-danger" v-if="editOrder.is_editable">*</span>
                </label>
                <select
                  v-model="editOrder.priority"
                  :disabled="!editOrder.is_editable"
                  class="form-select form-select-sm fs-8"
                  required
                >
                  <option value="NORMAL">Normal (Standar Operasional)</option>
                  <option value="HIGH">High (Prioritas Tinggi)</option>
                  <option value="URGENT">Urgent (Kebutuhan Sangat Mendesak)</option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tanggal Kebutuhan</label>
                <input
                  type="date"
                  v-model="editOrder.required_date"
                  :disabled="!editOrder.is_editable"
                  class="form-control form-control-sm fs-8"
                />
              </div>
            </div>

            <!-- Notes -->
            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan & Keterangan Tambahan</label>
              <input type="text" v-model="editOrder.notes" class="form-control form-control-sm fs-8" />
            </div>

            <!-- Items Editor -->
            <div>
              <div class="d-flex align-items-center justify-content-between mb-1.5">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-0">
                  Daftar Barang Pesanan ({{ editOrder.items?.length || 0 }} item)
                </label>
                <button
                  v-if="editOrder.is_editable"
                  type="button"
                  @click="addEditItemRow"
                  class="btn btn-sm btn-outline-danger py-1 px-2 fs-8 fw-semibold"
                >
                  <i class="bi bi-plus-circle me-1"></i> Tambah Item
                </button>
              </div>

              <div class="table-responsive border rounded bg-body mb-2" style="max-height: 220px; overflow-y: auto;">
                <table class="table table-sm table-hover align-middle mb-0 fs-8">
                  <thead class="table-light text-secondary fs-9 text-uppercase sticky-top">
                    <tr>
                      <th class="ps-3 py-1.5">Pilih Barang / Item</th>
                      <th class="text-center py-1.5" style="width: 140px;">Jumlah (Qty)</th>
                      <th class="text-end py-1.5" style="width: 140px;">Subtotal</th>
                      <th v-if="editOrder.is_editable" class="text-center py-1.5" style="width: 45px;">Hapus</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in editOrder.items" :key="index">
                      <td class="ps-3 py-1.5">
                        <template v-if="editOrder.is_editable">
                          <select
                            v-model="item.item_id"
                            @change="onEditItemChange(index)"
                            class="form-select form-select-sm fs-8"
                            required
                          >
                            <option value="">-- Pilih Barang / Item --</option>
                            <option v-for="it in itemsCatalog" :key="it.id" :value="it.id">
                              {{ it.name }} [{{ it.sku }}] ({{ it.uom }})
                            </option>
                          </select>
                          <div class="fs-9 text-secondary font-monospace mt-1" v-if="item.item_id">
                            Harga Ref: {{ formatRupiah(item.price) }}
                          </div>
                        </template>
                        <template v-else>
                          <span class="fw-semibold text-body">
                            {{ (itemsCatalog.find(c => c.id === item.item_id)?.name) || 'Item #' + item.item_id }}
                          </span>
                          <div class="fs-9 text-secondary font-monospace">
                            {{ itemsCatalog.find(c => c.id === item.item_id)?.sku || '' }}
                          </div>
                        </template>
                      </td>
                      <td class="text-center py-1.5">
                        <input
                          v-if="editOrder.is_editable"
                          type="number"
                          v-model.number="item.qty"
                          @input="onEditQtyChange(index)"
                          min="1"
                          required
                          class="form-control form-control-sm text-center fw-bold font-monospace fs-8"
                        />
                        <span v-else class="fw-bold font-monospace">{{ item.qty }}</span>
                      </td>
                      <td class="text-end font-monospace pe-2 py-1.5">
                        <span class="fw-semibold text-body">{{ formatRupiah(item.subtotal) }}</span>
                      </td>
                      <td v-if="editOrder.is_editable" class="text-center py-1.5">
                        <button
                          type="button"
                          @click="removeEditItemRow(index)"
                          :disabled="editOrder.items.length <= 1"
                          class="btn btn-sm text-danger py-0 px-1"
                          title="Hapus baris barang"
                        >
                          <i class="bi bi-trash fs-8"></i>
                        </button>
                      </td>
                    </tr>
                    <tr v-if="!editOrder.items || editOrder.items.length === 0">
                      <td colspan="4" class="text-center py-3 text-secondary">Tidak ada rincian item.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Modal Footer (Tombol Batal dan Simpan Perubahan Rata Kanan) -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" @click="editModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8">
              Batal
            </button>
            <button type="button" @click="saveEditOrder" class="btn btn-sm btn-primary fw-bold shadow-xs px-3 fs-8 d-inline-flex align-items-center gap-1">
              <i class="bi bi-save me-1"></i>
              <span>Simpan Perubahan</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL 4: HAPUS ORDER ==================== -->
    <div v-if="deleteModal && deleteOrder" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;" @click.self="deleteModal = false">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-danger fs-6 mb-0">
              Konfirmasi Hapus Order
            </h6>
            <button type="button" class="btn-close-modal" @click="deleteModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Modal Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <p class="text-body mb-0">
              Apakah Anda yakin ingin menghapus order permintaan berikut dari sistem?
            </p>

            <div class="p-2.5 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-0.5">Nomor & Unit Pemohon:</div>
              <div class="font-monospace fw-bold text-danger fs-7">{{ deleteOrder.order_number }}</div>
              <div class="fs-8 text-body fw-semibold">{{ deleteOrder.organization_name }}</div>
              <div class="fs-8 text-secondary font-monospace">{{ formatRupiah(deleteOrder.total_estimated_value) }}</div>
            </div>

            <div class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-start gap-2">
              <i class="bi bi-shield-exclamation text-warning fs-6 flex-shrink-0 mt-0.5"></i>
              <div>
                Order yang telah dihapus tidak dapat diproses lagi dan rincian alokasi barang akan dibatalkan.
              </div>
            </div>
          </div>

          <!-- Modal Footer (Tombol Batal dan Hapus Rata Kanan) -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" @click="deleteModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8">
              Batal
            </button>
            <button type="button" @click="confirmDeleteOrder" class="btn btn-sm btn-danger fw-bold px-3 fs-8 shadow-xs">
              <i class="bi bi-trash me-1"></i> Ya, Hapus Order
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import PaginationFooter from '@/components/PaginationFooter.vue';
import api from '@/api/client';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

// --- State & Filters ---
const currentTab = ref('open');
const searchQuery = ref('');
const filterOrg = ref('ALL');
const filterStatus = ref('ALL');
const sortBy = ref('created_at');
const sortDir = ref('desc');

// Pagination State
const currentPage = ref(1);
const perPage = ref(10);

const mapOrder = (order) => ({
  id: order.id,
  order_number: order.orderNumber,
  organization_id: order.requestingOrganization?.id,
  requesting_organization: {
    id: order.requestingOrganization?.id,
    name: order.requestingOrganization?.name || '-',
    code: order.requestingOrganization?.code || '-'
  },
  requester: {
    name: order.createdByUser?.name || '-',
    nip: order.createdByUser?.nip || '-'
  },
  required_date: order.requiredDate,
  priority: order.priority,
  status: order.status,
  total_estimated_value: Number(order.totalEstimatedValue || 0),
  shipping_cost: 0,
  notes: order.notes,
  created_at: order.createdAt,
  items: (order.items || []).map((line) => ({
    item_id: line.item?.id,
    qty_requested: line.qtyRequested,
    unit_price_ref: Number(line.unitPriceRef || 0),
    subtotal_ref: Number(line.subtotalRef || 0),
    item: line.item ? {
      id: line.item.id,
      name: line.item.name,
      sku: line.item.sku,
      uom: line.item.uom,
      estimated_unit_price: Number(line.item.estimatedUnitPrice || 0)
    } : null
  }))
});

const organizations = ref([]);

const itemsCatalog = ref([]);

// Orders state (initialized empty, populated from backend API or fallback on error)
const orders = ref([]);
const loading = ref(false);

const extractOrdersList = (res) => {
  if (!res) return null;
  if (Array.isArray(res)) return res;
  if (Array.isArray(res.data?.data?.content)) return res.data.data.content;
  if (Array.isArray(res.data?.content)) return res.data.content;
  if (Array.isArray(res.data?.data)) return res.data.data;
  if (Array.isArray(res.data)) return res.data;
  if (Array.isArray(res.content)) return res.content;
  return null;
};

onMounted(() => {
  loadReferences();
  loadOrders();
});

const loadReferences = async () => {
  try {
    const [orgRes, itemRes] = await Promise.allSettled([
      api.get('/master/organizations'),
      api.get('/master/items')
    ]);

    if (orgRes.status === 'fulfilled' && Array.isArray(orgRes.value.data) && orgRes.value.data.length > 0) {
      let rawOrgs = orgRes.value.data;
      if (authStore.isRegionalUser && authStore.regionId) {
        rawOrgs = rawOrgs.filter(org => (org.region?.id === authStore.regionId) || (org.parent?.region?.id === authStore.regionId));
      }
      organizations.value = rawOrgs.map((org) => ({
        id: org.id,
        name: org.name,
        code: org.code
      }));
    }

    if (itemRes.status === 'fulfilled' && Array.isArray(itemRes.value.data) && itemRes.value.data.length > 0) {
      itemsCatalog.value = itemRes.value.data.map((item) => ({
        id: item.id,
        name: item.name,
        sku: item.sku,
        uom: item.uom,
        estimated_unit_price: Number(item.estimatedUnitPrice || 0)
      }));
    }
  } catch (err) {
    organizations.value = [];
    itemsCatalog.value = [];
    console.warn('Failed loading order references from backend:', err);
  }
};

const loadOrders = async () => {
  loading.value = true;
  try {
    const response = await api.get('/orders', {
      params: {
        page: 0,
        size: 1000,
        sort: 'createdAt,desc'
      }
    });

    const list = extractOrdersList(response);
    if (list !== null) {
      orders.value = list.map(mapOrder);
    }
  } catch (err) {
    console.warn('Failed loading orders from backend', err);
    orders.value = [];
  } finally {
    loading.value = false;
  }
};

// Computed Metric Counts
const openOrdersCount = computed(() => {
  return orders.value.filter(o => !['COMPLETED', 'RECEIVED', 'REJECTED', 'CANCELLED'].includes(o.status)).length;
});

const completedOrdersCount = computed(() => {
  return orders.value.filter(o => ['COMPLETED', 'RECEIVED'].includes(o.status)).length;
});

const averageOrderValue = computed(() => {
  if (orders.value.length === 0) return 0;
  const sum = orders.value.reduce((acc, o) => acc + (o.total_estimated_value || 0), 0);
  return sum / orders.value.length;
});

const isFiltered = computed(() => {
  return (
    searchQuery.value.trim() !== '' ||
    filterOrg.value !== 'ALL' ||
    filterStatus.value !== 'ALL' ||
    sortBy.value !== 'created_at'
  );
});

// Filtering and Sorting
const filteredOrders = computed(() => {
  let list = [...orders.value];

  // Tab Filtering
  if (currentTab.value === 'open') {
    list = list.filter(o => !['COMPLETED', 'RECEIVED', 'REJECTED', 'CANCELLED'].includes(o.status));
  } else if (currentTab.value === 'completed') {
    list = list.filter(o => ['COMPLETED', 'RECEIVED'].includes(o.status));
  }

  // Organization Filter
  if (filterOrg.value !== 'ALL') {
    list = list.filter(o => o.organization_id === Number(filterOrg.value) || o.requesting_organization?.id === Number(filterOrg.value));
  }

  // Status Filter
  if (filterStatus.value !== 'ALL') {
    list = list.filter(o => o.status === filterStatus.value);
  }

  // Search Query
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(
      o =>
        o.order_number.toLowerCase().includes(q) ||
        o.requesting_organization?.name.toLowerCase().includes(q) ||
        o.requester?.name.toLowerCase().includes(q)
    );
  }

  // Sorting
  list.sort((a, b) => {
    let valA = a[sortBy.value];
    let valB = b[sortBy.value];

    if (sortBy.value === 'order_number') {
      valA = a.order_number;
      valB = b.order_number;
    } else if (sortBy.value === 'total_estimated_value') {
      valA = Number(a.total_estimated_value || 0);
      valB = Number(b.total_estimated_value || 0);
    } else if (sortBy.value === 'created_at') {
      valA = new Date(a.created_at).getTime();
      valB = new Date(b.created_at).getTime();
    }

    if (valA < valB) return sortDir.value === 'asc' ? -1 : 1;
    if (valA > valB) return sortDir.value === 'asc' ? 1 : -1;
    return 0;
  });

  return list;
});

// Paginated Orders for current page
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredOrders.value.slice(start, start + perPage.value);
});

// Helpers
const toggleSort = (col) => {
  if (sortBy.value === col) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = col;
    sortDir.value = 'desc';
  }
};

const resetFilters = () => {
  searchQuery.value = '';
  filterOrg.value = 'ALL';
  filterStatus.value = 'ALL';
  sortBy.value = 'created_at';
  sortDir.value = 'desc';
  currentPage.value = 1;
};

const formatRupiah = (num) => {
  return 'Rp ' + Number(num || 0).toLocaleString('id-ID');
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }) + ' WIB';
};

const statusMap = {
  DRAFT: 'Draft',
  SUBMITTED: 'Diajukan',
  WAITING_APPROVAL: 'Menunggu Approval',
  APPROVED: 'Disetujui',
  ALLOCATED: 'Teralokasi',
  PICKING: 'Picking',
  READY_TO_SHIP: 'Siap Kirim',
  IN_TRANSIT: 'Dalam Perjalanan',
  RECEIVED: 'Diterima',
  COMPLETED: 'Selesai',
  REJECTED: 'Ditolak',
  CANCELLED: 'Dibatalkan'
};

const getStatusLabel = (status) => {
  return statusMap[status] || (status || '').replace(/_/g, ' ');
};

const getBadgeClass = (status) => {
  switch (status) {
    case 'COMPLETED':
    case 'RECEIVED':
      return 'text-bg-success';
    case 'IN_TRANSIT':
      return 'text-bg-info';
    case 'READY_TO_SHIP':
    case 'ALLOCATED':
      return 'text-bg-primary';
    case 'WAITING_APPROVAL':
    case 'PICKING':
      return 'text-bg-warning';
    case 'SUBMITTED':
      return 'text-bg-secondary';
    case 'CANCELLED':
    case 'REJECTED':
      return 'text-bg-danger';
    default:
      return 'text-bg-light border';
  }
};

// ==================== MODAL 1: CREATE ====================
const createModal = ref(false);
const newOrderForm = ref({
  organization_id: 1,
  priority: 'NORMAL',
  required_date: new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0],
  notes: ''
});
const createRows = ref([]);

const openCreateModal = () => {
  newOrderForm.value = {
    organization_id: 1,
    priority: 'NORMAL',
    required_date: new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0],
    notes: ''
  };
  createRows.value = [
    { item_id: '', qty: 1, price: 0, subtotal: 0 }
  ];
  createModal.value = true;
};

const addCreateRow = () => {
  createRows.value.push({ item_id: '', qty: 1, price: 0, subtotal: 0 });
};

const removeCreateRow = (index) => {
  if (createRows.value.length > 1) {
    createRows.value.splice(index, 1);
  }
};

const onCreateItemChange = (index) => {
  const row = createRows.value[index];
  const item = itemsCatalog.value.find(i => i.id === row.item_id);
  const p = item ? item.estimated_unit_price : 0;
  row.price = p;
  row.subtotal = p * (parseInt(row.qty, 10) || 0);
};

const onCreateQtyChange = (index) => {
  const row = createRows.value[index];
  const q = parseInt(row.qty, 10) || 0;
  row.subtotal = (row.price || 0) * q;
};

const createTotal = computed(() => {
  return createRows.value.reduce((acc, it) => acc + (parseFloat(it.subtotal) || 0), 0);
});

const submitCreateOrder = () => {
  alert('Pembuatan order belum tersedia di backend production.');
  createModal.value = false;
};

// ==================== MODAL 2: DETAIL ====================
const viewModal = ref(false);
const viewOrder = ref(null);

const openViewModal = (ord) => {
  viewOrder.value = ord;
  viewModal.value = true;
};

// ==================== MODAL 3: EDIT ====================
const editModal = ref(false);
const editOrder = ref(null);

const openEditModal = (ord) => {
  let mappedItems = [];
  if (ord.items && ord.items.length > 0) {
    mappedItems = ord.items.map(it => {
      const unitP = parseFloat(it.unit_price_ref || (it.item ? it.item.estimated_unit_price : 0));
      const q = parseInt(it.qty_requested || 1, 10);
      return {
        item_id: it.item_id,
        qty: q,
        price: unitP,
        subtotal: parseFloat(it.subtotal_ref) || (unitP * q)
      };
    });
  } else {
    mappedItems = [{ item_id: '', qty: 1, price: 0, subtotal: 0 }];
  }

  editOrder.value = {
    id: ord.id,
    order_number: ord.order_number,
    organization_name: ord.requesting_organization?.name || '-',
    requester_name: ord.requester?.name || '-',
    priority: ord.priority || 'NORMAL',
    required_date: ord.required_date ? ord.required_date.substring(0, 10) : '',
    notes: ord.notes || '',
    status: ord.status,
    total_estimated_value: parseFloat(ord.total_estimated_value || 0),
    created_at_formatted: formatDate(ord.created_at),
    items: mappedItems,
    is_editable: ['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(ord.status)
  };
  recalculateEditTotal();
  editModal.value = true;
};

const addEditItemRow = () => {
  editOrder.value.items.push({ item_id: '', qty: 1, price: 0, subtotal: 0 });
  recalculateEditTotal();
};

const removeEditItemRow = (index) => {
  if (editOrder.value.items.length > 1) {
    editOrder.value.items.splice(index, 1);
    recalculateEditTotal();
  }
};

const onEditItemChange = (index) => {
  const row = editOrder.value.items[index];
  const item = itemsCatalog.value.find(i => i.id === row.item_id);
  const p = item ? item.estimated_unit_price : 0;
  row.price = p;
  row.subtotal = p * (parseInt(row.qty, 10) || 0);
  recalculateEditTotal();
};

const onEditQtyChange = (index) => {
  const row = editOrder.value.items[index];
  const q = parseInt(row.qty, 10) || 0;
  row.subtotal = (row.price || 0) * q;
  recalculateEditTotal();
};

const recalculateEditTotal = () => {
  let total = 0;
  editOrder.value.items.forEach(it => {
    total += (parseFloat(it.subtotal) || 0);
  });
  editOrder.value.total_estimated_value = total;
};

const saveEditOrder = () => {
  alert('Perubahan order belum tersedia di backend production.');
  editModal.value = false;
};

// ==================== MODAL 4: DELETE ====================
const deleteModal = ref(false);
const deleteOrder = ref(null);

const openDeleteModal = (ord) => {
  deleteOrder.value = {
    id: ord.id,
    order_number: ord.order_number,
    organization_name: ord.requesting_organization?.name || '-',
    total_estimated_value: ord.total_estimated_value
  };
  deleteModal.value = true;
};

const confirmDeleteOrder = () => {
  alert('Penghapusan order belum tersedia di backend production.');
  deleteModal.value = false;
};
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.space-y-3 > * + * {
  margin-top: 0.75rem;
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

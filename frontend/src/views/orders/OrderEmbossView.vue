<template>
  <div class="order-emboss-page">
    <!-- 1. Judul Halaman & Breadcrumb -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Order Emboss Kartu ATM / Debit</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Overview</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/orders" class="text-decoration-none text-danger">Orders</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Order Emboss</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Feedback Alerts -->
    <div v-if="alertMessage" :class="`alert alert-${alertType} alert-dismissible fade show fs-8 py-2 px-3 mb-3 shadow-xs`">
      <i :class="alertType === 'success' ? 'bi bi-check-circle-fill me-2' : 'bi bi-exclamation-triangle-fill me-2'"></i>
      <span>{{ alertMessage }}</span>
      <button type="button" class="btn-close py-2" @click="alertMessage = ''"></button>
    </div>

    <!-- Main Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Tabs and Section Action Button -->
      <div class="card-header border-bottom p-3 d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-center gap-2">
        <!-- Status Navigation Tabs (UI sama dengan /orders) -->
        <ul class="nav nav-pills card-header-pills fs-7 pb-1 pb-md-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="currentTab === 'open' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="currentTab = 'open'; currentPage = 1"
            >
              Order Terbuka
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="currentTab === 'completed' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="currentTab = 'completed'; currentPage = 1"
            >
              Riwayat Order
            </button>
          </li>
        </ul>

        <!-- Section Action Buttons -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <button type="button" @click="openCreateModal" class="btn btn-sm btn-danger fw-bold shadow-xs">
            Tambah
          </button>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <!-- Unit Kerja / Cabang Filter -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8" @change="currentPage = 1">
                <option value="ALL">Semua Unit Cabang</option>
                <option v-for="branch in branchOptions" :key="branch" :value="branch">{{ branch }}</option>
              </select>
            </div>
          </div>

          <!-- Status Filter -->
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8" @change="currentPage = 1">
                <option value="ALL">Semua Status</option>
                <option value="DRAFT">Draft</option>
                <option value="APPROVED">Disetujui</option>
                <option value="PRODUCTION">Produksi</option>
                <option value="PICKING">Picking</option>
                <option value="PACKING">Packing</option>
                <option value="READY_TO_SHIP">Siap Kirim</option>
                <option value="IN_TRANSIT">Dalam Pengiriman</option>
                <option value="RECEIVED">Diterima Cabang</option>
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
                <option value="total_items">Total Kartu</option>
                <option value="status">Status Alur</option>
              </select>
            </div>
          </div>

          <!-- Reset Button -->
          <div class="col-auto" v-if="isFiltered">
            <button type="button" @click="resetFilters" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
              Reset
            </button>
          </div>

          <!-- Search Bar -->
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari Nomor Order, Cabang, Catatan..."
                @input="currentPage = 1"
              />
              <button class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" type="button" @click="currentPage = 1">
                Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table Content -->
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 fs-7">
            <thead class="border-bottom fs-8 text-uppercase fw-semibold text-secondary bg-body-tertiary">
              <tr>
                <th class="ps-3 ps-md-4 py-3" style="min-width: 170px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('order_number')">
                    Nomor Order
                   
                   
                  </span>
                </th>
                <th class="py-3" style="min-width: 190px;">Cabang Pemesan</th>
                <th class="text-center py-3" style="width: 130px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('total_items')">
                    Total Kartu
                   
                   
                  </span>
                </th>
                <th class="text-center py-3" style="width: 150px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('status')">
                    Status Alur
                   
                   
                  </span>
                </th>
                <th class="text-center py-3" style="min-width: 160px;">Pelacakan Pemenuhan</th>
                <th class="py-3" style="min-width: 150px;">
                  <span class="d-inline-flex align-items-center gap-1 cursor-pointer" @click="toggleSort('created_at')">
                    Tanggal Order
                   
                   
                  </span>
                </th>
                <th class="text-center pe-3 pe-md-4 py-3" style="width: 120px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <template v-if="!isLoading || paginatedOrders.length > 0">
                <tr v-for="order in paginatedOrders" :key="order.id">
                  <!-- Nomor Order -->
                  <td class="ps-3 ps-md-4">
                    <span class="fw-bold font-monospace text-dark">
                      {{ order.orderNumber }}
                    </span>
                    <div class="fs-8 text-secondary mt-0.5">
                     {{ order.createdByUser?.name || '-' }}
                    </div>
                  </td>

                  <!-- Cabang Pemesan -->
                  <td>
                    <span class="fw-semibold text-body">{{ order.requestingOrganization?.name || '-' }}</span>
                    <div class="fs-8 font-monospace text-secondary">
                      <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle fs-8">
                        {{ order.requestingOrganization?.code || '-' }}
                      </span>
                    </div>
                  </td>

                  <!-- Total Kartu -->
                  <td class="text-center font-monospace fw-bold fs-7">
                    {{ order.totalItems || 0 }} Keping
                  </td>

                  <!-- Status Alur -->
                  <td class="text-center">
                    <span class="badge fs-8 text-uppercase" :class="statusBadgeClass(order.status)">
                      {{ getStatusLabel(order.status) }}
                    </span>
                  </td>

                  <!-- Pelacakan Pemenuhan -->
                  <td class="text-center">
                    <div class="d-flex flex-column align-items-center">
                      <span class="badge fs-9 mb-1" :class="fulfillmentBadgeClass(order.fulfillmentStatus)">
                        {{ getFulfillmentLabel(order.fulfillmentStatus) }}
                      </span>
                      <div class="progress w-100" style="height: 6px;">
                        <div
                          class="progress-bar bg-danger"
                          role="progressbar"
                          :style="{ width: getFulfillmentPercentage(order) + '%' }"
                        ></div>
                      </div>
                      <span class="fs-9 text-secondary mt-0.5">{{ getFulfillmentPercentage(order) }}% Terpenuhi</span>
                    </div>
                  </td>

                  <!-- Tanggal Order -->
                  <td class="text-secondary fs-8">
                    {{ formatDate(order.createdAt) }}
                  </td>

                  <!-- Tombol Aksi -->
                  <td class="text-center pe-3 pe-md-4 py-2">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button
                        type="button"
                        @click="openViewModal(order)"
                        class="btn-action-icon text-secondary"
                        title="Lihat Detail Order"
                      >
                        <i class="bi bi-eye"></i>
                      </button>
                      <router-link
                        :to="`/orders/${order.id}/print`"
                        target="_blank"
                        class="btn-action-icon text-dark"
                        title="Cetak Dokumen Order"
                      >
                        <i class="bi bi-printer"></i>
                      </router-link>
                      <button
                        type="button"
                        :disabled="isOrderApproved(order)"
                        @click="!isOrderApproved(order) && openDeleteModal(order)"
                        class="btn-action-icon"
                        :class="isOrderApproved(order) ? 'text-secondary opacity-25 pe-none' : 'text-danger'"
                        :title="isOrderApproved(order) ? 'Order telah disetujui, tidak dapat dihapus' : 'Hapus Order'"
                      >
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="!isLoading && paginatedOrders.length === 0">
                  <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data order emboss ditemukan</p>
                    <p class="fs-8 text-muted mb-0">
                      {{ currentTab === 'open' ? 'Tidak ada order terbuka yang memerlukan tindakan.' : 'Belum ada riwayat order emboss yang telah selesai dipenuhi.' }}
                    </p>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Paging / Pagination Footer -->
      <PaginationFooter
        v-if="!isLoading && filteredOrders.length > 0"
        :total="filteredOrders.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- ==================== MODAL 1: BUAT ORDER EMBOSS BARU ==================== -->
    <div
      v-if="showCreateModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.5); z-index: 1060;"
      @click.self="showCreateModal = false"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Pengajuan Order Emboss Kartu Nasabah
            </h6>
            <button type="button" class="btn-close-modal" @click="showCreateModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Form Body -->
          <form @submit.prevent="submitCreateEmbossOrder">
            <div class="modal-body p-3 fs-8 space-y-3">
              <div class="row g-2.5">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                    Kantor Cabang Pemesan <span class="text-danger">*</span>
                  </label>
                  <select v-model="formOrder.organizationId" class="form-select form-select-sm fs-8" required>
                    <option :value="null">-- Pilih Cabang --</option>
                    <option v-for="org in organizations" :key="org.id" :value="org.id">
                      {{ org.name }} ({{ org.code }})
                    </option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
                    Berkas Data Nasabah (.CSV / .TXT) <span class="text-danger">*</span>
                  </label>
                  <input
                    type="file"
                    ref="modalFileInputRef"
                    accept=".csv,.txt"
                    class="form-control form-control-sm fs-8"
                    required
                    @change="onModalFileSelected"
                  />
                  <div class="text-secondary fs-9 mt-1">Format: No Rekening, Nama Nasabah, Masked Card, Card Type, Kode Cabang</div>
                </div>
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Catatan Kebutuhan / Keterangan</label>
                  <textarea
                    v-model="formOrder.notes"
                    class="form-control form-control-sm fs-8"
                    rows="3"
                    placeholder="Contoh: Kebutuhan pencetakan kartu debit nasabah baru program pembukaan rekening massal..."
                  ></textarea>
                </div>
              </div>
            </div>

            <!-- Modal Footer -->
            <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showCreateModal = false">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" :disabled="submitting">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL 2: DETAIL ORDER EMBOSS ==================== -->
    <div
      v-if="viewModal && viewOrder"
      class="modal fade show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.5); z-index: 1060;"
      @click.self="viewModal = false"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <!-- Modal Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <h6 class="modal-title fw-bold text-body fs-6 mb-0">
                Detail Order: <span class="font-monospace text-danger">{{ viewOrder.orderNumber }}</span>
              </h6>
              <span class="badge fs-8 text-uppercase" :class="statusBadgeClass(viewOrder.status)">
                {{ getStatusLabel(viewOrder.status) }}
              </span>
            </div>
            <button type="button" class="btn-close-modal" @click="viewModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Modal Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <!-- Summary Box -->
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-2 fs-8">
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Unit Pemohon:</span>
                  <strong class="text-body">{{ viewOrder.requestingOrganization?.name || '-' }}</strong>
                  <div class="font-monospace text-secondary fs-9">
                    Kode: {{ viewOrder.requestingOrganization?.code || '-' }}
                  </div>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Dibuat Oleh:</span>
                  <span class="text-body fw-semibold">{{ viewOrder.createdByUser?.name || '-' }}</span>
                  <div class="font-monospace text-secondary fs-9">
                    NIP: {{ viewOrder.createdByUser?.nip || '-' }}
                  </div>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Prioritas:</span>
                  <span class="badge bg-danger-subtle text-danger border border-danger-subtle fs-8">
                    {{ viewOrder.priority || 'NORMAL' }}
                  </span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Tanggal Transaksi:</span>
                  <span class="text-body font-monospace">{{ formatDate(viewOrder.createdAt) }}</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Total Kartu:</span>
                  <span class="text-body font-monospace fw-bold fs-7">{{ viewOrder.totalItems || 0 }} Keping</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Pelacakan Pemenuhan:</span>
                  <span class="badge fs-9" :class="fulfillmentBadgeClass(viewOrder.fulfillmentStatus)">
                    {{ getFulfillmentLabel(viewOrder.fulfillmentStatus) }}
                  </span>
                  <div class="fs-9 text-secondary font-monospace mt-0.5">
                    {{ getFulfillmentPercentage(viewOrder) }}% Terpenuhi
                  </div>
                </div>
              </div>

              <div v-if="viewOrder.notes" class="mt-2.5 pt-2 border-top border-secondary-subtle fs-8">
                <span class="text-secondary fw-semibold">Catatan:</span>
                <span class="text-body ms-1 fst-italic">{{ viewOrder.notes }}</span>
              </div>
            </div>

            <!-- Status Alur Workflow Tracker -->
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-0">
                  Status Alur Transaksi
                </label>
                <span class="badge text-uppercase fs-8" :class="statusBadgeClass(viewOrder.status)">
                  {{ getStatusLabel(viewOrder.status) }}
                </span>
              </div>
              
              <div class="d-flex align-items-center justify-content-between position-relative px-1 py-1 overflow-x-auto text-nowrap">
                <template v-for="(step, idx) in workflowSteps" :key="step.code">
                  <div class="d-flex flex-column align-items-center position-relative z-1 px-1 text-center" style="min-width: 60px;">
                    <div
                      class="rounded-circle d-flex align-items-center justify-content-center fw-bold fs-9 mb-1 shadow-xs"
                      :class="getWorkflowCircleClass(viewOrder.status, step.code)"
                      style="width: 28px; height: 28px;"
                    >
                      <i v-if="isWorkflowStepPassed(viewOrder.status, step.code)" class="bi bi-check-lg"></i>
                      <span v-else>{{ idx + 1 }}</span>
                    </div>
                    <span
                      class="fs-9"
                      :class="getWorkflowTextClass(viewOrder.status, step.code)"
                    >
                      {{ step.label }}
                    </span>
                  </div>
                  <div
                    v-if="idx < workflowSteps.length - 1"
                    class="flex-grow-1 border-top"
                    :class="isWorkflowLinePassed(viewOrder.status, step.code) ? 'border-danger border-2' : 'border-secondary-subtle border-2'"
                    style="min-width: 12px; margin-top: -18px;"
                  ></div>
                </template>
              </div>
            </div>

            <!-- Items Table -->
            <div>
              <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1.5">Rincian Kartu & Produk Diminta</label>
              <div class="table-responsive rounded border border-secondary-subtle" style="max-height: 220px; overflow-y: auto;">
                <table class="table table-sm table-hover align-middle mb-0 fs-8">
                  <thead class="bg-body-tertiary text-secondary sticky-top">
                    <tr>
                      <th class="ps-3" style="width: 40px;">No</th>
                      <th>Produk</th>
                      <th class="text-center" style="width: 100px;">Satuan</th>
                      <th class="text-center" style="width: 110px;">Jumlah</th>
                      <th class="text-center" style="width: 150px;">Status Item</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(it, idx) in viewOrder.items" :key="idx">
                      <td class="ps-3 text-secondary font-monospace">{{ idx + 1 }}</td>
                      <td>
                        <span class="fw-semibold text-body">{{ it.item?.name || ('Item #' + (it.item?.id || it.id)) }}</span>
                        <div class="fs-9 text-secondary font-monospace">{{ it.item?.sku || '' }}</div>
                      </td>
                      <td class="text-center">
                        <span class="badge bg-secondary-subtle text-secondary">{{ it.item?.uom || 'Pcs' }}</span>
                      </td>
                      <td class="text-center font-monospace fw-bold">{{ it.qtyRequested }}</td>
                      <td class="text-center">
                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle fs-9">
                          Terpenuhi: {{ it.qtyPicked || it.qtyShipped || 0 }} / {{ it.qtyRequested || 0 }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="!viewOrder.items || viewOrder.items.length === 0">
                      <td colspan="5" class="text-center py-3 text-secondary">Tidak ada rincian item.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-between align-items-center py-2 px-3 border-top">
            <div class="d-flex align-items-center gap-2">
              <router-link
                :to="`/orders/${viewOrder.id}/print`"
                target="_blank"
                class="btn btn-sm btn-outline-danger d-inline-flex align-items-center gap-1 shadow-xs fs-8"
              >
                <span>Cetak Order</span>
              </router-link>
            </div>
            <button type="button" @click="viewModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL 3: DIALOG PROSES DATA UPLOAD ==================== -->
    <div
      v-if="showProcessModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.65); z-index: 1070;"
      @click.self="processState !== 'processing' ? showProcessModal = false : null"
    >
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-lg">
          <!-- Header -->
          <div class="modal-header bg-body border-bottom d-flex justify-content-between align-items-center">
            <h6 class="modal-title fw-bold text-body fs-6 mb-0">
              Proses Data Upload Emboss
            </h6>
            <button
              v-if="processState !== 'processing'"
              type="button"
              class="btn-close-modal"
              @click="showProcessModal = false"
              aria-label="Tutup"
            >
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Body -->
          <div class="modal-body p-4 text-center">
            <!-- 1. State: Processing -->
            <div v-if="processState === 'processing'" class="py-2">
              <div class="mb-3">
                <div class="spinner-border text-danger mb-2" style="width: 3rem; height: 3rem;" role="status">
                  <span class="visually-hidden">Memproses...</span>
                </div>
                <h6 class="fw-bold text-body mb-1">{{ processStatusText }}</h6>
                <p class="fs-8 text-secondary mb-3">Mohon tunggu, sistem sedang membaca dan memproses berkas data nasabah...</p>
              </div>

              <!-- Progress Bar -->
              <div class="progress mb-3 shadow-xs" style="height: 10px;">
                <div
                  class="progress-bar progress-bar-striped progress-bar-animated bg-danger"
                  role="progressbar"
                  :style="{ width: processProgress + '%' }"
                ></div>
              </div>

              <!-- Step Checklist -->
              <div class="list-group list-group-flush text-start fs-8 border rounded-3 bg-body-tertiary p-2">
                <div class="list-group-item bg-transparent d-flex align-items-center justify-content-between py-1.5 px-2 border-0">
                  <span class="d-flex align-items-center gap-2">
                    <i v-if="processStep > 1" class="bi bi-check-circle-fill text-success"></i>
                    <span v-else-if="processStep === 1" class="spinner-border spinner-border-sm text-danger"></span>
                    <i v-else class="bi bi-circle text-secondary opacity-50"></i>
                    <span>1. Unggah berkas CSV ke server</span>
                  </span>
                  <span class="badge" :class="processStep > 1 ? 'bg-success-subtle text-success' : (processStep === 1 ? 'bg-danger-subtle text-danger' : 'text-secondary')">
                    {{ processStep > 1 ? 'Selesai' : (processStep === 1 ? 'Berjalan' : 'Menunggu') }}
                  </span>
                </div>

                <div class="list-group-item bg-transparent d-flex align-items-center justify-content-between py-1.5 px-2 border-0">
                  <span class="d-flex align-items-center gap-2">
                    <i v-if="processStep > 2" class="bi bi-check-circle-fill text-success"></i>
                    <span v-else-if="processStep === 2" class="spinner-border spinner-border-sm text-danger"></span>
                    <i v-else class="bi bi-circle text-secondary opacity-50"></i>
                    <span>2. Parsing format & validasi nama nasabah</span>
                  </span>
                  <span class="badge" :class="processStep > 2 ? 'bg-success-subtle text-success' : (processStep === 2 ? 'bg-danger-subtle text-danger' : 'text-secondary')">
                    {{ processStep > 2 ? 'Selesai' : (processStep === 2 ? 'Berjalan' : 'Menunggu') }}
                  </span>
                </div>

                <div class="list-group-item bg-transparent d-flex align-items-center justify-content-between py-1.5 px-2 border-0">
                  <span class="d-flex align-items-center gap-2">
                    <i v-if="processStep > 3" class="bi bi-check-circle-fill text-success"></i>
                    <span v-else-if="processStep === 3" class="spinner-border spinner-border-sm text-danger"></span>
                    <i v-else class="bi bi-circle text-secondary opacity-50"></i>
                    <span>3. Penerbitan order cabang & antrean picking</span>
                  </span>
                  <span class="badge" :class="processStep > 3 ? 'bg-success-subtle text-success' : (processStep === 3 ? 'bg-danger-subtle text-danger' : 'text-secondary')">
                    {{ processStep > 3 ? 'Selesai' : (processStep === 3 ? 'Berjalan' : 'Menunggu') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 2. State: Success -->
            <div v-else-if="processState === 'success'" class="py-2">
              <div class="rounded-circle bg-success-subtle text-success d-inline-flex align-items-center justify-content-center p-3 mb-3" style="width: 64px; height: 64px;">
                <i class="bi bi-check-lg fs-1 fw-bold"></i>
              </div>
              <h5 class="fw-bold text-body mb-1">Order Emboss Berhasil Diterbitkan!</h5>
              <p class="fs-8 text-secondary mb-3">Berkas data nasabah telah divalidasi dan pesanan emboss kartu telah diterbitkan ke antrean picking gudang.</p>

              <div class="card bg-body-tertiary border text-start p-3 fs-8 mb-3">
                <div class="row g-2">
                  <div class="col-6">
                    <span class="text-secondary d-block">Nama Berkas:</span>
                    <span class="fw-bold text-body font-monospace text-truncate d-block">{{ processResult.fileName }}</span>
                  </div>
                  <div class="col-6">
                    <span class="text-secondary d-block">ID Berkas:</span>
                    <span class="fw-bold text-danger font-monospace">{{ processResult.fileId || '-' }}</span>
                  </div>
                  <div class="col-6">
                    <span class="text-secondary d-block">Total Baris:</span>
                    <span class="fw-bold text-body font-monospace">{{ processResult.totalRecords?.toLocaleString('id-ID') }} Nasabah</span>
                  </div>
                  <div class="col-6">
                    <span class="text-secondary d-block">Status Alur:</span>
                    <span class="badge bg-secondary text-white">DRAFT (Menunggu Persetujuan)</span>
                  </div>
                </div>
                <div v-if="processResult.rawFileId" class="mt-2.5 pt-2 border-top text-center">
                  <router-link :to="`/emboss/${processResult.rawFileId}`" class="btn btn-sm btn-outline-danger fw-bold fs-8">
                    Buka Hasil Upload File Detail & Order
                  </router-link>
                </div>
              </div>
            </div>

            <!-- 3. State: Error -->
            <div v-else class="py-2">
              <div class="rounded-circle bg-danger-subtle text-danger d-inline-flex align-items-center justify-content-center p-3 mb-3" style="width: 64px; height: 64px;">
                <i class="bi bi-x-lg fs-1 fw-bold"></i>
              </div>
              <h5 class="fw-bold text-danger mb-1">Gagal Memproses Berkas Emboss</h5>
              <p class="fs-8 text-secondary mb-3">Terjadi kendala saat memproses berkas nasabah atau menerbitkan order.</p>

              <div class="alert alert-danger text-start fs-8 py-2 px-3 mb-3">
                <div class="fw-bold mb-0.5"><i class="bi bi-exclamation-circle-fill me-1"></i> Detail Error:</div>
                <div class="font-monospace fs-9 text-break">{{ processError }}</div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <template v-if="processState === 'processing'">
              <span class="fs-9 text-secondary font-monospace me-auto">
                <i class="bi bi-hourglass-split me-1"></i> Proses berlangsung di latar belakang...
              </span>
            </template>
            <template v-else-if="processState === 'success'">
              <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" @click="closeProcessModal">
                Lihat Daftar Order
              </button>
            </template>
            <template v-else>
              <button type="button" class="btn btn-sm btn-outline-secondary px-3 fs-8" @click="showProcessModal = false">
                Tutup
              </button>
              <button type="button" class="btn btn-sm btn-danger fw-bold shadow-xs px-3 fs-8" @click="retryProcess">
                Coba Lagi
              </button>
            </template>
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
              Konfirmasi Hapus Order Emboss
            </h6>
            <button type="button" class="btn-close-modal" @click="deleteModal = false" aria-label="Tutup">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <!-- Modal Body -->
          <div class="modal-body p-3 fs-8 space-y-3">
            <p class="text-body mb-0">
              Apakah Anda yakin ingin menghapus order emboss berikut dari sistem?
            </p>

            <div class="p-2.5 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-0.5">Nomor & Cabang Pemesan:</div>
              <div class="font-monospace fw-bold text-danger fs-7">{{ deleteOrder.order_number }}</div>
              <div class="fs-8 text-body fw-semibold">{{ deleteOrder.organization_name }}</div>
              <div class="fs-8 text-secondary font-monospace">{{ deleteOrder.total_items }} Keping Kartu</div>
            </div>

            <div class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-start gap-2">
              <i class="bi bi-shield-exclamation text-warning fs-6 flex-shrink-0 mt-0.5"></i>
              <div>
                Order yang telah dihapus tidak dapat diproses lagi dan data pesanan akan dibatalkan dari sistem.
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="modal-footer bg-body-secondary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
            <button type="button" @click="deleteModal = false" class="btn btn-sm btn-outline-secondary px-3 fs-8" :disabled="isDeleting">
              Batal
            </button>
            <button type="button" @click="confirmDeleteOrder" class="btn btn-sm btn-danger fw-bold px-3 fs-8 shadow-xs" :disabled="isDeleting">
              <span v-if="isDeleting" class="spinner-border spinner-border-sm me-1"></span>
              <span>Ya, Hapus Order</span>
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

// State & Filters
const isLoading = ref(true);
const currentTab = ref('open'); // 'open' | 'completed'
const orders = ref([]);
const organizations = ref([]);
const searchQuery = ref('');
const filterBranch = ref('ALL');
const filterStatus = ref('ALL');
const sortBy = ref('created_at');
const sortDir = ref('desc');

// Delete Modal State
const deleteModal = ref(false);
const deleteOrder = ref(null);
const isDeleting = ref(false);

// Pagination State
const currentPage = ref(1);
const perPage = ref(10);

// Modal states
const showCreateModal = ref(false);
const submitting = ref(false);
const alertMessage = ref('');
const alertType = ref('success');
const modalSelectedFile = ref(null);
const modalFileInputRef = ref(null);

const viewModal = ref(false);
const viewOrder = ref(null);

// Upload Processing Dialog State
const showProcessModal = ref(false);
const processState = ref('processing'); // 'processing' | 'success' | 'error'
const processStep = ref(1);
const processProgress = ref(0);
const processStatusText = ref('');
const processError = ref('');
const processResult = ref({
  fileName: '',
  fileId: '',
  totalRecords: 0,
  validRecords: 0,
  rejectedRecords: 0
});

const formOrder = ref({
  organizationId: null,
  notes: ''
});

const loadData = async () => {
  isLoading.value = true;
  try {
    const [ordersRes, orgsRes] = await Promise.allSettled([
      api.get('/emboss/orders'),
      api.get('/master/organizations')
    ]);

    if (ordersRes.status === 'fulfilled') {
      orders.value = ordersRes.value.data?.data || ordersRes.value.data || [];
    }
    if (orgsRes.status === 'fulfilled') {
      organizations.value = orgsRes.value.data?.data || orgsRes.value.data || [];
    } else {
      try {
        const fallbackRes = await api.get('/organizations');
        organizations.value = fallbackRes.data?.data || fallbackRes.data || [];
      } catch (e) {
        console.warn('Fallback loading organizations failed:', e);
      }
    }
  } catch (err) {
    console.error('Failed to load emboss orders:', err);
  } finally {
    isLoading.value = false;
  }
};

const branchOptions = computed(() => {
  const set = new Set();
  orders.value.forEach(o => {
    if (o.requestingOrganization?.name) set.add(o.requestingOrganization.name);
  });
  return Array.from(set).sort();
});

const isFiltered = computed(() => {
  return (
    searchQuery.value.trim() !== '' ||
    filterBranch.value !== 'ALL' ||
    filterStatus.value !== 'ALL' ||
    sortBy.value !== 'created_at' ||
    sortDir.value !== 'desc'
  );
});

// Fulfillment calculation & tab determination
const getFulfillmentPercentage = (order) => {
  if (['COMPLETED', 'RECEIVED'].includes(order.status) || order.fulfillmentStatus === 'RECEIVED') return 100;
  if (order.fulfillmentStatus === 'FULLY_FULFILLED' || order.status === 'IN_TRANSIT') return 100;
  if (order.fulfillmentStatus === 'PARTIALLY_FULFILLED') return 50;
  if (order.status === 'READY_TO_SHIP') return 80;
  if (order.status === 'PACKING') return 65;
  if (order.status === 'PICKING') return 50;
  if (['PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI'].includes(order.status)) return 35;
  if (order.status === 'APPROVED' || order.status === 'ALLOCATED') return 20;
  return 0;
};

const isHistoryOrder = (o) => {
  // Masuk ke dalam riwayat order jika status alur disetujui (atau alur selesai) dan pelacakan pemenuhan 100%
  const isApprovedOrDone = ['APPROVED', 'COMPLETED', 'RECEIVED'].includes(o.status);
  const is100Percent = getFulfillmentPercentage(o) >= 100;
  return isApprovedOrDone && is100Percent;
};

const isOrderApproved = (order) => {
  if (!order || !order.status) return false;
  return ['APPROVED', 'PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI', 'ALLOCATED', 'PICKING', 'PACKING', 'READY_TO_SHIP', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(order.status);
};

// Workflow Steps: Draft -> Disetujui -> Produksi -> Picking -> Packing -> Siap Kirim -> Pengiriman -> Diterima
const workflowSteps = [
  { code: 'DRAFT', label: 'Draft' },
  { code: 'APPROVED', label: 'Disetujui' },
  { code: 'PRODUCTION', label: 'Produksi' },
  { code: 'PICKING', label: 'Picking' },
  { code: 'PACKING', label: 'Packing' },
  { code: 'READY_TO_SHIP', label: 'Siap Kirim' },
  { code: 'IN_TRANSIT', label: 'Pengiriman' },
  { code: 'RECEIVED', label: 'Diterima' }
];

const getWorkflowStepIndex = (statusCode) => {
  const code = (statusCode || '').toUpperCase();
  if (code === 'DRAFT' || code === 'SUBMITTED' || code === 'WAITING_APPROVAL') return 0;
  if (code === 'APPROVED' || code === 'ALLOCATED') return 1;
  if (code === 'PRODUCTION' || code === 'IN_PRODUCTION' || code === 'PRODUKSI') return 2;
  if (code === 'PICKING') return 3;
  if (code === 'PACKING') return 4;
  if (code === 'READY_TO_SHIP') return 5;
  if (code === 'IN_TRANSIT') return 6;
  if (code === 'RECEIVED' || code === 'COMPLETED') return 7;
  return 0;
};

const isWorkflowStepPassed = (orderStatus, stepCode) => {
  const currentIdx = getWorkflowStepIndex(orderStatus);
  const targetIdx = workflowSteps.findIndex(s => s.code === stepCode);
  return currentIdx > targetIdx;
};

const isWorkflowLinePassed = (orderStatus, stepCode) => {
  const currentIdx = getWorkflowStepIndex(orderStatus);
  const targetIdx = workflowSteps.findIndex(s => s.code === stepCode);
  return currentIdx > targetIdx;
};

const getWorkflowCircleClass = (orderStatus, stepCode) => {
  const currentIdx = getWorkflowStepIndex(orderStatus);
  const targetIdx = workflowSteps.findIndex(s => s.code === stepCode);
  if (currentIdx > targetIdx) {
    return 'bg-danger text-white';
  } else if (currentIdx === targetIdx) {
    return 'bg-danger text-white ring-2 ring-danger ring-opacity-50 fw-bold';
  } else {
    return 'bg-body border border-secondary text-secondary';
  }
};

const getWorkflowTextClass = (orderStatus, stepCode) => {
  const currentIdx = getWorkflowStepIndex(orderStatus);
  const targetIdx = workflowSteps.findIndex(s => s.code === stepCode);
  if (currentIdx === targetIdx) {
    return 'fw-bold text-danger';
  } else if (currentIdx > targetIdx) {
    return 'fw-semibold text-body';
  } else {
    return 'text-secondary';
  }
};

const filteredOrders = computed(() => {
  let list = [...orders.value];

  // 0. Tab Filtering: Order Terbuka vs Riwayat Order
  if (currentTab.value === 'open') {
    list = list.filter(o => !isHistoryOrder(o));
  } else if (currentTab.value === 'completed') {
    list = list.filter(o => isHistoryOrder(o));
  }

  // 1. Branch Filter
  if (filterBranch.value !== 'ALL') {
    list = list.filter(o => o.requestingOrganization?.name === filterBranch.value);
  }

  // 3. Status Filter
  if (filterStatus.value !== 'ALL') {
    if (filterStatus.value === 'PRODUCTION') {
      list = list.filter(o => ['PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI'].includes(o.status));
    } else {
      list = list.filter(o => o.status === filterStatus.value);
    }
  }

  // 4. Search Query
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase();
    list = list.filter(o => {
      const matchNum = (o.orderNumber || '').toLowerCase().includes(q);
      const matchBranch = (o.requestingOrganization?.name || '').toLowerCase().includes(q);
      const matchRequester = (o.createdByUser?.name || '').toLowerCase().includes(q);
      const matchNotes = (o.notes || '').toLowerCase().includes(q);
      return matchNum || matchBranch || matchRequester || matchNotes;
    });
  }

  // 5. Sorting
  list.sort((a, b) => {
    let valA, valB;
    if (sortBy.value === 'order_number') {
      valA = a.orderNumber || '';
      valB = b.orderNumber || '';
    } else if (sortBy.value === 'total_items') {
      valA = Number(a.totalItems || 0);
      valB = Number(b.totalItems || 0);
    } else if (sortBy.value === 'status') {
      const orderRank = {
        DRAFT: 1,
        SUBMITTED: 2,
        WAITING_APPROVAL: 3,
        APPROVED: 4,
        PRODUCTION: 5,
        IN_PRODUCTION: 5,
        PRODUKSI: 5,
        ALLOCATED: 6,
        PICKING: 7,
        PACKING: 8,
        READY_TO_SHIP: 9,
        IN_TRANSIT: 10,
        RECEIVED: 11,
        COMPLETED: 12,
        REJECTED: 98,
        CANCELLED: 99
      };
      valA = orderRank[a.status] || 50;
      valB = orderRank[b.status] || 50;
    } else {
      valA = new Date(a.createdAt || 0).getTime();
      valB = new Date(b.createdAt || 0).getTime();
    }

    if (valA < valB) return sortDir.value === 'asc' ? -1 : 1;
    if (valA > valB) return sortDir.value === 'asc' ? 1 : -1;
    return 0;
  });

  return list;
});

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredOrders.value.slice(start, start + perPage.value);
});

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
  filterBranch.value = 'ALL';
  filterStatus.value = 'ALL';
  sortBy.value = 'created_at';
  sortDir.value = 'desc';
  currentPage.value = 1;
};

// Modal handlers
const openCreateModal = () => {
  formOrder.value = {
    organizationId: organizations.value[0]?.id || null,
    notes: ''
  };
  modalSelectedFile.value = null;
  if (modalFileInputRef.value) modalFileInputRef.value.value = '';
  showCreateModal.value = true;
};

const openViewModal = (order) => {
  viewOrder.value = order;
  viewModal.value = true;
};

const onModalFileSelected = (event) => {
  modalSelectedFile.value = event.target.files?.[0] || null;
};

const submitCreateEmbossOrder = async () => {
  if (!modalSelectedFile.value) {
    alert('Silakan pilih berkas data nasabah (.CSV atau .TXT)');
    return;
  }

  const selectedFile = modalSelectedFile.value;

  // Tutup modal pengajuan dan buka dialog proses
  showCreateModal.value = false;
  showProcessModal.value = true;
  processState.value = 'processing';
  processStep.value = 1;
  processProgress.value = 25;
  processStatusText.value = `Mengunggah berkas ${selectedFile.name}...`;
  processError.value = '';

  submitting.value = true;
  try {
    // Step 1: Upload file to /emboss (timeout 5 menit)
    const formData = new FormData();
    formData.append('file', selectedFile);
    
    processStep.value = 1;
    processProgress.value = 30;
    processStatusText.value = `Mengunggah & memproses berkas ${selectedFile.name}...`;

    const uploadRes = await api.post('/emboss', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300000
    });
    const fileData = uploadRes?.data || uploadRes;

    // Step 2: Validasi Selesai
    processStep.value = 2;
    processProgress.value = 65;
    processStatusText.value = 'Data nasabah dan kuota kartu berhasil divalidasi...';

    // Step 3: Generate orders from emboss file
    if (fileData && fileData.id) {
      processStep.value = 3;
      processProgress.value = 85;
      processStatusText.value = 'Menerbitkan order emboss (Draft)...';

      await api.post(`/emboss/${fileData.id}/generate-orders`, null, {
        timeout: 300000
      });
    }

    // Step 4: Selesai
    processStep.value = 4;
    processProgress.value = 100;
    processState.value = 'success';
    processResult.value = {
      fileName: selectedFile.name,
      fileId: fileData?.fileId || `#EB-${fileData?.id}`,
      rawFileId: fileData?.id,
      totalRecords: fileData?.totalRecords || 0,
      validRecords: fileData?.validRecords || 0,
      rejectedRecords: fileData?.rejectedRecords || 0
    };

    alertType.value = 'success';
    alertMessage.value = `Order Emboss berhasil dibuat dengan status Draft untuk berkas ${selectedFile.name}. Silakan lakukan otorisasi di menu Persetujuan Order.`;
    await loadData();
  } catch (err) {
    processState.value = 'error';
    const msg = typeof err === 'string'
      ? err
      : (err?.response?.data?.message || err?.message || 'Gagal memproses berkas dan membuat Order Emboss');
    processError.value = msg;
    alertType.value = 'danger';
    alertMessage.value = msg;
  } finally {
    submitting.value = false;
  }
};

const closeProcessModal = () => {
  showProcessModal.value = false;
};

const retryProcess = () => {
  showProcessModal.value = false;
  showCreateModal.value = true;
};

// Formatters & Helpers
const statusMap = {
  DRAFT: 'Draft',
  SUBMITTED: 'Diajukan',
  WAITING_APPROVAL: 'Menunggu Approval',
  APPROVED: 'Disetujui',
  PRODUCTION: 'Produksi',
  IN_PRODUCTION: 'Produksi',
  PRODUKSI: 'Produksi',
  ALLOCATED: 'Teralokasi',
  PICKING: 'Picking',
  PACKING: 'Packing',
  READY_TO_SHIP: 'Siap Kirim',
  IN_TRANSIT: 'Dalam Pengiriman',
  RECEIVED: 'Diterima Cabang',
  COMPLETED: 'Selesai',
  REJECTED: 'Ditolak',
  CANCELLED: 'Dibatalkan'
};

const getStatusLabel = (status) => {
  return statusMap[status] || (status || '').replace(/_/g, ' ');
};

const statusBadgeClass = (status) => {
  switch (status) {
    case 'COMPLETED':
    case 'RECEIVED':
      return 'text-bg-success';
    case 'IN_TRANSIT':
      return 'text-bg-info';
    case 'READY_TO_SHIP':
    case 'ALLOCATED':
      return 'text-bg-primary';
    case 'PRODUCTION':
    case 'IN_PRODUCTION':
    case 'PRODUKSI':
      return 'text-bg-warning text-dark border border-warning-subtle';
    case 'WAITING_APPROVAL':
    case 'PICKING':
      return 'text-bg-warning';
    case 'PACKING':
      return 'text-bg-info text-dark';
    case 'SUBMITTED':
      return 'text-bg-secondary';
    case 'CANCELLED':
    case 'REJECTED':
      return 'text-bg-danger';
    case 'APPROVED':
      return 'text-bg-primary';
    case 'DRAFT':
      return 'text-bg-secondary';
    default:
      return 'text-bg-light border';
  }
};

const getFulfillmentLabel = (fStatus) => {
  switch (fStatus) {
    case 'RECEIVED': return 'Diterima';
    case 'FULLY_FULFILLED': return 'Terpenuhi';
    case 'PARTIALLY_FULFILLED': return 'Sebagian';
    case 'UNFULFILLED': return 'Belum Dipenuhi';
    default: return fStatus || '-';
  }
};

const fulfillmentBadgeClass = (fStatus) => {
  switch (fStatus) {
    case 'RECEIVED': return 'bg-success text-white';
    case 'FULLY_FULFILLED': return 'bg-primary text-white';
    case 'PARTIALLY_FULFILLED': return 'bg-warning text-dark';
    default: return 'bg-secondary text-white';
  }
};

const formatDate = (dt) => {
  if (!dt) return '-';
  try {
    return new Date(dt).toLocaleDateString('id-ID', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }) + ' WIB';
  } catch (e) {
    return dt;
  }
};

// Delete Order Handlers
const openDeleteModal = (order) => {
  if (isOrderApproved(order)) return;
  deleteOrder.value = {
    id: order.id,
    order_number: order.orderNumber,
    organization_name: order.requestingOrganization?.name || '-',
    total_items: order.totalItems || 0
  };
  deleteModal.value = true;
};

const confirmDeleteOrder = async () => {
  if (!deleteOrder.value?.id) return;
  isDeleting.value = true;
  try {
    await api.delete(`/orders/${deleteOrder.value.id}`);
    deleteModal.value = false;
    alertType.value = 'success';
    alertMessage.value = `Order emboss ${deleteOrder.value.order_number} berhasil dihapus.`;
    deleteOrder.value = null;
    await loadData();
  } catch (err) {
    alertType.value = 'danger';
    alertMessage.value = err?.response?.data?.message || err?.message || 'Gagal menghapus order emboss.';
  } finally {
    isDeleting.value = false;
  }
};

onMounted(loadData);
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
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

<template>
  <div class="vendors-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Vendor & Ekspedisi</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/master/vendors" class="text-decoration-none text-danger">Master Data</router-link>
              </li>
              <li class="breadcrumb-item active text-body fw-semibold" aria-current="page">Vendor & Ekspedisi</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. Main Tabbed Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs (Hanya Label Tanpa Icon dan Tanpa Jumlah Record) -->
      <div class="card-header bg-body p-2 px-3 border-bottom d-flex flex-column flex-md-row align-items-stretch align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills nav-pills-scroll m-0" role="tablist" aria-label="Tabs Vendor & Ekspedisi">
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('vendors')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'vendors' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Vendor
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('couriers')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'couriers' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Jasa Ekspedisi
            </button>
          </li>
        </ul>

        <!-- Right: Action Buttons -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <template v-if="activeMainTab === 'vendors'">
            <button
              type="button"
              @click="openCreateVendorModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <template v-if="activeMainTab === 'couriers'">
            <button
              type="button"
              @click="openCreateCourierModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <button
            type="button"
            @click="printPage"
            class="btn btn-sm btn-outline-secondary fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            title="Cetak Halaman Ini"
          >
            <span>Cetak</span>
          </button>
        </div>
      </div>

      <!-- ==================== TAB 1: VENDORS ==================== -->
      <div v-show="activeMainTab === 'vendors'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Rating Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-star"></i></span>
                <select v-model="vendorRatingFilter" @change="vendorPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Rating Vendor</option>
                  <option value="4.5">Rating &ge; 4.5 Bintang</option>
                  <option value="4.0">Rating &ge; 4.0 Bintang</option>
                  <option value="UNDER_4">Rating &lt; 4.0 Bintang</option>
                </select>
              </div>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="vendorSearch"
                  @input="vendorPage = 1"
                  placeholder="Cari nama, kode vendor, termin, alamat..."
                  class="form-control form-control-sm border-start-0 fs-8"
                />
                <button type="button" class="btn btn-sm btn-danger px-3" @click="vendorPage = 1">
                  Cari
                </button>
              </div>
            </div>

            <!-- Reset Button -->
            <div v-if="vendorSearch || vendorRatingFilter !== 'ALL'" class="col-auto">
              <button type="button" @click="resetVendorFilters" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
                Reset
              </button>
            </div>
          </div>
        </div>

        <!-- Table Responsive -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-4 py-3" style="width: 280px;">Kode & Nama Vendor</th>
                  <th class="py-3" style="width: 140px;">SLA Pengadaan</th>
                  <th class="py-3" style="width: 160px;">Termin Pembayaran</th>
                  <th class="py-3" style="min-width: 220px;">Alamat Operasional</th>
                  <th class="py-3 text-center" style="width: 120px;">Rating</th>
                  <th class="py-3 text-center" style="width: 100px;">Status</th>
                  <th class="pe-4 py-3 text-center" style="width: 120px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="v in paginatedVendorList" :key="v.id || v.code">
                  <!-- Vendor Name & Code -->
                  <td class="ps-4">
                    <div class="d-flex align-items-center gap-2">
                      <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fw-bold">{{ v.code }}</span>
                      <span class="fw-bold text-body">{{ v.name }}</span>
                    </div>
                  </td>

                  <!-- SLA -->
                  <td>
                    <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle fs-8">
                     {{ v.sla_days || v.slaDays || 7 }} Hari
                    </span>
                  </td>

                  <!-- Payment Terms -->
                  <td>
                    <span class="badge bg-body-secondary text-secondary-emphasis font-monospace border">
                      {{ v.payment_terms || v.paymentTerms || '-' }}
                    </span>
                  </td>

                  <!-- Address -->
                  <td>
                    <div class="text-secondary fs-9 text-truncate" style="max-width: 260px;" :title="v.address || '-'">
                      {{ v.address || '-' }}
                    </div>
                  </td>

                  <!-- Rating -->
                  <td class="text-center">
                    <div class="d-inline-flex align-items-center gap-1 bg-amber-50 text-warning-emphasis border border-warning-subtle rounded-pill px-2 py-0.5 fs-8 fw-bold">
                     
                      <span>{{ Number(v.rating || 5.0).toFixed(1) }}</span>
                    </div>
                  </td>

                  <!-- Status -->
                  <td class="text-center">
                    <span
                      class="badge fs-9 py-1 px-2"
                      :class="(v.is_active !== undefined ? v.is_active : v.isActive) ? 'text-bg-success' : 'text-bg-secondary'"
                    >
                      {{ (v.is_active !== undefined ? v.is_active : v.isActive) ? 'Aktif' : 'Non-Aktif' }}
                    </span>
                  </td>

                  <!-- Aksi -->
                  <td class="pe-4 text-center">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button
                        type="button"
                        @click="openViewVendorModal(v)"
                        class="btn btn-sm btn-outline-secondary btn-action-icon"
                        title="Detail Vendor"
                      >
                        <i class="bi bi-eye"></i>
                      </button>
                      <button
                        type="button"
                        @click="openEditVendorModal(v)"
                        class="btn btn-sm btn-outline-primary btn-action-icon"
                        title="Edit Vendor"
                      >
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button
                        type="button"
                        @click="toggleVendorStatus(v)"
                        class="btn btn-sm btn-action-icon"
                        :class="(v.is_active !== undefined ? v.is_active : v.isActive) ? 'btn-outline-warning' : 'btn-outline-success'"
                        :title="(v.is_active !== undefined ? v.is_active : v.isActive) ? 'Nonaktifkan Vendor' : 'Aktifkan Vendor'"
                      >
                        <i class="bi" :class="(v.is_active !== undefined ? v.is_active : v.isActive) ? 'bi-toggle2-on' : 'bi-toggle2-off'"></i>
                      </button>
                      <button
                        type="button"
                        @click="openDeleteVendorModal(v)"
                        class="btn btn-sm btn-outline-danger btn-action-icon"
                        title="Hapus Vendor"
                      >
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <tr v-if="paginatedVendorList.length === 0">
                  <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-building fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data vendor rekanan ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau bersihkan filter.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredVendorList.length"
          v-model:currentPage="vendorPage"
          v-model:perPage="vendorPerPage"
        />
      </div>

      <!-- ==================== TAB 2: COURIERS ==================== -->
      <div v-show="activeMainTab === 'couriers'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="courierStatusFilter" @change="courierPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Status Mitra</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="courierSearch"
                  @input="courierPage = 1"
                  placeholder="Cari nama, kode ekspedisi, layanan..."
                  class="form-control form-control-sm border-start-0 fs-8"
                />
                <button type="button" class="btn btn-sm btn-danger px-3" @click="courierPage = 1">
                  Cari
                </button>
              </div>
            </div>

            <!-- Reset Button -->
            <div v-if="courierSearch || courierStatusFilter" class="col-auto">
              <button type="button" @click="resetCourierFilters" class="btn btn-sm btn-outline-danger fs-8" title="Reset Filter">
                Reset
              </button>
            </div>
          </div>
        </div>

        <!-- Table Responsive -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-4 py-3" style="width: 260px;">Kode & Nama Ekspedisi</th>
                  <th class="py-3" style="width: 160px;">SLA Estimasi Pengiriman</th>
                  <th class="py-3" style="min-width: 240px;">Jenis Layanan Pengiriman</th>
                  <th class="py-3 text-center" style="width: 140px;">Total Pengiriman</th>
                  <th class="py-3 text-center" style="width: 100px;">Status</th>
                  <th class="pe-4 py-3 text-center" style="width: 120px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in paginatedCourierList" :key="c.id || c.code">
                  <!-- Courier Name & Code -->
                  <td class="ps-4">
                    <div class="d-flex align-items-center gap-2">
                      <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fw-bold">{{ c.code }}</span>
                      <span class="fw-bold text-body">{{ c.name }}</span>
                    </div>
                  </td>

                  <!-- SLA -->
                  <td>
                    <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle fs-8">
                     {{ c.sla_days || c.slaDays || 2 }} Hari
                    </span>
                  </td>

                  <!-- Service Types -->
                  <td>
                    <div class="d-flex flex-wrap gap-1">
                      <span
                        v-for="st in getCourierServices(c)"
                        :key="st"
                        class="badge bg-primary-subtle text-primary border border-primary-subtle fs-9 font-monospace"
                      >
                        {{ st }}
                      </span>
                    </div>
                  </td>

                  <!-- Total Shipments -->
                  <td class="text-center">
                    <span class="badge bg-body-secondary text-secondary-emphasis fs-8">
                     {{ c.shipments_count || 0 }} Surat Jalan
                    </span>
                  </td>

                  <!-- Status -->
                  <td class="text-center">
                    <span
                      class="badge fs-9 py-1 px-2"
                      :class="(c.is_active !== undefined ? c.is_active : c.isActive) ? 'text-bg-success' : 'text-bg-secondary'"
                    >
                      {{ (c.is_active !== undefined ? c.is_active : c.isActive) ? 'Aktif' : 'Non-Aktif' }}
                    </span>
                  </td>

                  <!-- Aksi -->
                  <td class="pe-4 text-center">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button
                        type="button"
                        @click="openViewCourierModal(c)"
                        class="btn btn-sm btn-outline-secondary btn-action-icon"
                        title="Detail Ekspedisi"
                      >
                        <i class="bi bi-eye"></i>
                      </button>
                      <button
                        type="button"
                        @click="openEditCourierModal(c)"
                        class="btn btn-sm btn-outline-primary btn-action-icon"
                        title="Edit Ekspedisi"
                      >
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button
                        type="button"
                        @click="toggleCourierStatus(c)"
                        class="btn btn-sm btn-action-icon"
                        :class="(c.is_active !== undefined ? c.is_active : c.isActive) ? 'btn-outline-warning' : 'btn-outline-success'"
                        :title="(c.is_active !== undefined ? c.is_active : c.isActive) ? 'Nonaktifkan Ekspedisi' : 'Aktifkan Ekspedisi'"
                      >
                        <i class="bi" :class="(c.is_active !== undefined ? c.is_active : c.isActive) ? 'bi-toggle2-on' : 'bi-toggle2-off'"></i>
                      </button>
                      <button
                        type="button"
                        @click="openDeleteCourierModal(c)"
                        class="btn btn-sm btn-outline-danger btn-action-icon"
                        title="Hapus Ekspedisi"
                      >
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <tr v-if="paginatedCourierList.length === 0">
                  <td colspan="6" class="text-center py-5 text-secondary">
                    <i class="bi bi-truck fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data jasa ekspedisi ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau bersihkan filter.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredCourierList.length"
          v-model:currentPage="courierPage"
          v-model:perPage="courierPerPage"
        />
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: VENDOR -->
    <!-- ========================================================================= -->

    <!-- Modal Detail Vendor -->
    <div v-if="viewVendorModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Profil Rekanan Pengadaan: <span class="font-monospace text-danger">{{ selectedVendor ? selectedVendor.code : '' }}</span>
            </h6>
            <button type="button" class="btn-close" @click="viewVendorModalOpen = false" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedVendor" style="max-height: 75vh; overflow-y: auto;">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <div class="fs-8 text-secondary mb-0.5">Nama Perusahaan:</div>
                  <h5 class="fw-bold text-body mb-0">{{ selectedVendor.name || '-' }}</h5>
                </div>
                <div class="d-inline-flex align-items-center gap-1 bg-amber-50 text-warning-emphasis border border-warning-subtle rounded-pill px-2.5 py-1 fs-8 fw-bold">
                  <i class="bi bi-star-fill text-warning"></i>
                  <span>{{ Number(selectedVendor.rating || 5.0).toFixed(1) }}</span> / 5.0
                </div>
              </div>
            </div>

            <div class="row g-3 fs-8">
              <div class="col-sm-6">
                <span class="text-secondary d-block">Kode Vendor:</span>
                <span class="font-monospace fw-bold text-body">{{ selectedVendor.code || '-' }}</span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">SLA Pengadaan:</span>
                <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">
                  {{ (selectedVendor.sla_days || selectedVendor.slaDays || 7) }} Hari Kerja
                </span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Termin Pembayaran:</span>
                <span class="font-monospace fw-semibold text-body">{{ selectedVendor.payment_terms || selectedVendor.paymentTerms || '-' }}</span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Status Operasional:</span>
                <span
                  class="badge"
                  :class="(selectedVendor.is_active !== undefined ? selectedVendor.is_active : selectedVendor.isActive) ? 'text-bg-success' : 'text-bg-secondary'"
                >
                  {{ (selectedVendor.is_active !== undefined ? selectedVendor.is_active : selectedVendor.isActive) ? 'Aktif' : 'Non-Aktif' }}
                </span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">No. Telepon / Kontak:</span>
                <span class="text-body font-monospace">{{ selectedVendor.phone || '-' }}</span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Email Resmi:</span>
                <span class="text-body">{{ selectedVendor.email || '-' }}</span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block">Alamat Kantor / Pabrik:</span>
                <span class="text-body">{{ selectedVendor.address || '-' }}</span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block">Total Purchase Order (PO):</span>
                <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle">
                  {{ (selectedVendor.purchase_orders_count || 0) }} Transaksi PO
                </span>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewVendorModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Create Vendor -->
    <div v-if="createVendorModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Vendor Rekanan Baru</h6>
            <button type="button" class="btn-close" @click="createVendorModalOpen = false" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitCreateVendor">
            <div class="modal-body p-4 space-y-3 fs-8" style="max-height: 75vh; overflow-y: auto;">
              <!-- Row 1: Kode & Nama -->
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Vendor <span class="text-danger">*</span></label>
                  <input type="text" v-model="createVendorForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8" placeholder="Contoh: VND-005" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Perusahaan / Rekanan <span class="text-danger">*</span></label>
                  <input type="text" v-model="createVendorForm.name" required class="form-control form-control-sm fw-bold fs-8" placeholder="Contoh: PT Graha Media Pratama" />
                </div>
              </div>

              <!-- Row 2: SLA & Termin Pembayaran -->
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">SLA Pengadaan (Hari) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="createVendorForm.sla_days" min="1" required class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Termin Pembayaran <span class="text-danger">*</span></label>
                  <select v-model="createVendorForm.payment_terms" required class="form-select form-select-sm fs-8">
                    <option value="TOP 30 Hari">TOP 30 Hari</option>
                    <option value="TOP 14 Hari">TOP 14 Hari</option>
                    <option value="TOP 45 Hari">TOP 45 Hari</option>
                    <option value="TOP 60 Hari">TOP 60 Hari</option>
                    <option value="Cash On Delivery (COD)">Cash On Delivery (COD)</option>
                    <option value="Pembayaran di Muka (Advance)">Pembayaran di Muka (Advance)</option>
                  </select>
                </div>
              </div>

              <!-- Row 3: Rating & Telepon -->
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Rating Kinerja Awal (1 - 5)</label>
                  <input type="number" v-model.number="createVendorForm.rating" min="1" max="5" step="0.1" class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">No. Telepon / Kontak</label>
                  <input type="text" v-model="createVendorForm.phone" class="form-control form-control-sm fs-8" placeholder="031-87654321" />
                </div>
              </div>

              <!-- Email -->
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Email Resmi</label>
                <input type="email" v-model="createVendorForm.email" class="form-control form-control-sm fs-8" placeholder="sales@rekanan.co.id" />
              </div>

              <!-- Alamat -->
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Kantor Operasional</label>
                <textarea v-model="createVendorForm.address" rows="2" class="form-control form-control-sm fs-8" placeholder="Alamat lengkap rekanan vendor..."></textarea>
              </div>
            </div>

            <!-- Modal Footer -->
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="createVendorModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Edit Vendor -->
    <div v-if="editVendorModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Edit Vendor Rekanan</h6>
            <button type="button" class="btn-close" @click="editVendorModalOpen = false" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitEditVendor">
            <div class="modal-body p-4 space-y-3 fs-8" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Vendor <span class="text-danger">*</span></label>
                  <input type="text" v-model="editVendorForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Perusahaan / Rekanan <span class="text-danger">*</span></label>
                  <input type="text" v-model="editVendorForm.name" required class="form-control form-control-sm fw-bold fs-8" />
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">SLA Pengadaan (Hari) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="editVendorForm.sla_days" min="1" required class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Termin Pembayaran <span class="text-danger">*</span></label>
                  <select v-model="editVendorForm.payment_terms" required class="form-select form-select-sm fs-8">
                    <option value="TOP 30 Hari">TOP 30 Hari</option>
                    <option value="TOP 14 Hari">TOP 14 Hari</option>
                    <option value="TOP 45 Hari">TOP 45 Hari</option>
                    <option value="TOP 60 Hari">TOP 60 Hari</option>
                    <option value="Cash On Delivery (COD)">Cash On Delivery (COD)</option>
                    <option value="Pembayaran di Muka (Advance)">Pembayaran di Muka (Advance)</option>
                  </select>
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Rating Kinerja (1 - 5)</label>
                  <input type="number" v-model.number="editVendorForm.rating" min="1" max="5" step="0.1" class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">No. Telepon / Kontak</label>
                  <input type="text" v-model="editVendorForm.phone" class="form-control form-control-sm fs-8" />
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Email Resmi</label>
                <input type="email" v-model="editVendorForm.email" class="form-control form-control-sm fs-8" />
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Kantor Operasional</label>
                <textarea v-model="editVendorForm.address" rows="2" class="form-control form-control-sm fs-8"></textarea>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Status Operasional</label>
                <select v-model="editVendorForm.is_active" class="form-select form-select-sm fs-8">
                  <option :value="true">Aktif</option>
                  <option :value="false">Non-Aktif</option>
                </select>
              </div>
            </div>

            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="editVendorModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Delete Vendor -->
    <div v-if="deleteVendorModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <h6 class="modal-title fw-bold mb-0 text-danger">Konfirmasi Hapus Vendor</h6>
            <button type="button" class="btn-close" @click="deleteVendorModalOpen = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedVendor">
            <p class="text-secondary mb-0">Apakah Anda yakin ingin menghapus rekanan vendor ini dari master data?</p>
            <div class="p-3 bg-body-tertiary rounded border">
              <div><strong>Kode Vendor:</strong> <span class="font-monospace fw-bold text-dark">{{ selectedVendor.code }}</span></div>
              <div><strong>Nama Perusahaan:</strong> <span class="fw-semibold text-dark">{{ selectedVendor.name }}</span></div>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="deleteVendorModalOpen = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger shadow-xs d-inline-flex align-items-center gap-1" @click="submitDeleteVendor">
              <span>Ya, Hapus Vendor</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: COURIER (JASA EKSPEDISI) -->
    <!-- ========================================================================= -->

    <!-- Modal Detail Ekspedisi -->
    <div v-if="viewCourierModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Profil Mitra Ekspedisi: <span class="font-monospace text-danger">{{ selectedCourier ? selectedCourier.code : '' }}</span>
            </h6>
            <button type="button" class="btn-close" @click="viewCourierModalOpen = false" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedCourier" style="max-height: 75vh; overflow-y: auto;">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-0.5">Nama Jasa Kurir:</div>
              <h5 class="fw-bold text-body mb-0">{{ selectedCourier.name || '-' }}</h5>
            </div>

            <div class="row g-3 fs-8">
              <div class="col-sm-6">
                <span class="text-secondary d-block">Kode Kurir:</span>
                <span class="font-monospace fw-bold text-body">{{ selectedCourier.code || '-' }}</span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">SLA Distribusi:</span>
                <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle">
                  {{ (selectedCourier.sla_days || selectedCourier.slaDays || 2) }} Hari Kerja
                </span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Status Operasional:</span>
                <span
                  class="badge"
                  :class="(selectedCourier.is_active !== undefined ? selectedCourier.is_active : selectedCourier.isActive) ? 'text-bg-success' : 'text-bg-secondary'"
                >
                  {{ (selectedCourier.is_active !== undefined ? selectedCourier.is_active : selectedCourier.isActive) ? 'Aktif' : 'Non-Aktif' }}
                </span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Pengiriman Terproses:</span>
                <span class="badge bg-secondary-subtle text-secondary-emphasis">
                  {{ (selectedCourier.shipments_count || 0) }} Surat Jalan
                </span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block">No. Kontak / Hotline CS:</span>
                <span class="text-body font-monospace">{{ selectedCourier.phone || '-' }}</span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block mb-1">Cakupan Layanan Pengiriman:</span>
                <div class="d-flex flex-wrap gap-1">
                  <span
                    v-for="st in getCourierServices(selectedCourier)"
                    :key="st"
                    class="badge bg-primary-subtle text-primary border border-primary-subtle fs-8 font-monospace px-2 py-1"
                  >
                    {{ st }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewCourierModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Create Ekspedisi -->
    <div v-if="createCourierModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Mitra Ekspedisi Baru</h6>
            <button type="button" class="btn-close" @click="createCourierModalOpen = false" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitCreateCourier">
            <div class="modal-body p-4 space-y-3 fs-8" style="max-height: 75vh; overflow-y: auto;">
              <!-- Row 1: Kode & Nama -->
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Ekspedisi <span class="text-danger">*</span></label>
                  <input type="text" v-model="createCourierForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8" placeholder="Contoh: JNE-01" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Jasa Ekspedisi <span class="text-danger">*</span></label>
                  <input type="text" v-model="createCourierForm.name" required class="form-control form-control-sm fw-bold fs-8" placeholder="Contoh: JNE Express Jawa Timur" />
                </div>
              </div>

              <!-- Row 2: SLA & Telepon -->
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">SLA Distribusi (Hari) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="createCourierForm.sla_days" min="1" required class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">No. Kontak / Hotline CS</label>
                  <input type="text" v-model="createCourierForm.phone" class="form-control form-control-sm fs-8" placeholder="031-98765432" />
                </div>
              </div>

              <!-- Row 3: Cakupan Layanan -->
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-2">Cakupan Layanan Pengiriman</label>
                <div class="d-flex flex-wrap gap-3 p-3 rounded-2 border bg-body-tertiary">
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="REGULER" id="srv_reguler" v-model="createCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="srv_reguler">REGULER</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="EXPRESS" id="srv_express" v-model="createCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="srv_express">EXPRESS</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="CARGO" id="srv_cargo" v-model="createCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="srv_cargo">CARGO</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="INTERNAL" id="srv_internal" v-model="createCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="srv_internal">ARMADA INTERNAL</label>
                  </div>
                </div>
              </div>
            </div>

            <!-- Modal Footer -->
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="createCourierModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Edit Ekspedisi -->
    <div v-if="editCourierModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Edit Mitra Ekspedisi</h6>
            <button type="button" class="btn-close" @click="editCourierModalOpen = false" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitEditCourier">
            <div class="modal-body p-4 space-y-3 fs-8" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Ekspedisi <span class="text-danger">*</span></label>
                  <input type="text" v-model="editCourierForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Jasa Ekspedisi <span class="text-danger">*</span></label>
                  <input type="text" v-model="editCourierForm.name" required class="form-control form-control-sm fw-bold fs-8" />
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">SLA Distribusi (Hari) <span class="text-danger">*</span></label>
                  <input type="number" v-model.number="editCourierForm.sla_days" min="1" required class="form-control form-control-sm fs-8" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">No. Kontak / Hotline CS</label>
                  <input type="text" v-model="editCourierForm.phone" class="form-control form-control-sm fs-8" />
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-2">Cakupan Layanan Pengiriman</label>
                <div class="d-flex flex-wrap gap-3 p-3 rounded-2 border bg-body-tertiary">
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="REGULER" id="edit_srv_reguler" v-model="editCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="edit_srv_reguler">REGULER</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="EXPRESS" id="edit_srv_express" v-model="editCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="edit_srv_express">EXPRESS</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="CARGO" id="edit_srv_cargo" v-model="editCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="edit_srv_cargo">CARGO</label>
                  </div>
                  <div class="form-check form-check-inline mb-0">
                    <input class="form-check-input" type="checkbox" value="INTERNAL" id="edit_srv_internal" v-model="editCourierForm.service_types" />
                    <label class="form-check-label fs-8 fw-semibold" for="edit_srv_internal">ARMADA INTERNAL</label>
                  </div>
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Status Mitra</label>
                <select v-model="editCourierForm.is_active" class="form-select form-select-sm fs-8">
                  <option :value="true">Aktif</option>
                  <option :value="false">Non-Aktif</option>
                </select>
              </div>
            </div>

            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="editCourierModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Delete Ekspedisi -->
    <div v-if="deleteCourierModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <h6 class="modal-title fw-bold mb-0 text-danger">Konfirmasi Hapus Ekspedisi</h6>
            <button type="button" class="btn-close" @click="deleteCourierModalOpen = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedCourier">
            <p class="text-secondary mb-0">Apakah Anda yakin ingin menghapus mitra ekspedisi ini dari master data?</p>
            <div class="p-3 bg-body-tertiary rounded border">
              <div><strong>Kode Ekspedisi:</strong> <span class="font-monospace fw-bold text-dark">{{ selectedCourier.code }}</span></div>
              <div><strong>Nama Jasa Ekspedisi:</strong> <span class="fw-semibold text-dark">{{ selectedCourier.name }}</span></div>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="deleteCourierModalOpen = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger shadow-xs d-inline-flex align-items-center gap-1" @click="submitDeleteCourier">
              <span>Ya, Hapus Ekspedisi</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api/client';
import { extractList } from '@/utils/responseParser';
import PaginationFooter from '@/components/PaginationFooter.vue';
import { toast } from '@/utils/toast';

const route = useRoute();
const router = useRouter();

// ==========================================
// 1. Navigation & Tab Management
// ==========================================
const validTabs = ['vendors', 'couriers'];
const activeMainTab = ref('vendors');

const initTabFromQuery = () => {
  const queryTab = route.query.tab;
  if (queryTab && validTabs.includes(queryTab)) {
    activeMainTab.value = queryTab;
  }
};

const switchTab = (tab) => {
  if (validTabs.includes(tab)) {
    activeMainTab.value = tab;
    router.replace({ query: { ...route.query, tab } }).catch(() => {});
  }
};

// ==========================================
// 2. Toast Alert System
// ==========================================
const showAlert = (message, type = 'success') => {
  if (type === 'success') {
    toast.success(message, 'Berhasil');
  } else if (type === 'warning' || type === 'warn') {
    toast.warn(message, 'Peringatan');
  } else {
    toast.error(message, 'Gagal');
  }
};

// ==========================================
// 3. Data Lists & Initial Defaults
// ==========================================
const defaultVendors = [
  { id: 1, code: 'VND-001', name: 'PT Swadharma Duta Data', sla_days: 7, payment_terms: 'TOP 30 Hari', address: 'Jl. Pemuda No. 45, Surabaya', rating: 4.8, is_active: true, purchase_orders_count: 14, phone: '031-5312345', email: 'kontrak@swadharma.co.id' },
  { id: 2, code: 'VND-002', name: 'PT Wahyu Abadi Percetakan Sekuriti', sla_days: 10, payment_terms: 'TOP 14 Hari', address: 'Kawasan Industri Rungkut SIER, Surabaya', rating: 4.6, is_active: true, purchase_orders_count: 9, phone: '031-8431122', email: 'sales@wahyuabadi.com' },
  { id: 3, code: 'VND-003', name: 'PT Pura Barutama (Pura Group Kudus)', sla_days: 14, payment_terms: 'TOP 45 Hari', address: 'Jl. AKBP R. Agil Kusumadya, Kudus', rating: 4.9, is_active: true, purchase_orders_count: 22, phone: '0291-432244', email: 'security.print@kudus.pura.co.id' },
  { id: 4, code: 'VND-004', name: 'CV Sinar Jaya Stationery Mandiri', sla_days: 3, payment_terms: 'TOP 30 Hari', address: 'Jl. Dharmawangsa No. 12, Surabaya', rating: 4.2, is_active: true, purchase_orders_count: 6, phone: '031-5038899', email: 'sinarjaya.atk@gmail.com' }
];

const defaultCouriers = [
  { id: 1, code: 'EXP-JNE', name: 'JNE Express Logistik Cabang Surabaya', sla_days: 2, phone: '031-8672233', service_types: ['REGULER', 'EXPRESS'], is_active: true, shipments_count: 45 },
  { id: 2, code: 'EXP-JNT', name: 'J&T Cargo & Distribusi Perbankan', sla_days: 2, phone: '031-7548811', service_types: ['REGULER', 'EXPRESS', 'CARGO'], is_active: true, shipments_count: 38 },
  { id: 3, code: 'EXP-POS', name: 'PT Pos Indonesia (Persero) KCU Surabaya', sla_days: 3, phone: '031-3522201', service_types: ['REGULER', 'CARGO'], is_active: true, shipments_count: 19 },
  { id: 4, code: 'INT-FLT', name: 'Armada Khusus Ekspedisi Internal Bank Jatim', sla_days: 1, phone: 'Ext 144 Divisi Logistik', service_types: ['INTERNAL'], is_active: true, shipments_count: 62 }
];

const vendorList = ref([...defaultVendors]);
const courierList = ref([...defaultCouriers]);

const getCourierServices = (c) => {
  if (Array.isArray(c.service_types)) return c.service_types;
  if (Array.isArray(c.serviceTypes)) return c.serviceTypes;
  return ['REGULER'];
};

// ==========================================
// 4. Filters & Pagination: Vendors
// ==========================================
const vendorRatingFilter = ref('ALL');
const vendorSearch = ref('');
const vendorPage = ref(1);
const vendorPerPage = ref(10);

const resetVendorFilters = () => {
  vendorRatingFilter.value = 'ALL';
  vendorSearch.value = '';
  vendorPage.value = 1;
};

const filteredVendorList = computed(() => {
  let list = [...vendorList.value];

  if (vendorRatingFilter.value !== 'ALL') {
    if (vendorRatingFilter.value === '4.5') {
      list = list.filter((v) => Number(v.rating || 0) >= 4.5);
    } else if (vendorRatingFilter.value === '4.0') {
      list = list.filter((v) => Number(v.rating || 0) >= 4.0);
    } else if (vendorRatingFilter.value === 'UNDER_4') {
      list = list.filter((v) => Number(v.rating || 0) < 4.0);
    }
  }

  if (vendorSearch.value.trim()) {
    const q = vendorSearch.value.trim().toLowerCase();
    list = list.filter(
      (v) =>
        (v.code && v.code.toLowerCase().includes(q)) ||
        (v.name && v.name.toLowerCase().includes(q)) ||
        (v.payment_terms && v.payment_terms.toLowerCase().includes(q)) ||
        (v.paymentTerms && v.paymentTerms.toLowerCase().includes(q)) ||
        (v.address && v.address.toLowerCase().includes(q)) ||
        (v.email && v.email.toLowerCase().includes(q)) ||
        (v.phone && v.phone.toLowerCase().includes(q))
    );
  }

  return list;
});

const paginatedVendorList = computed(() => {
  const start = (vendorPage.value - 1) * vendorPerPage.value;
  return filteredVendorList.value.slice(start, start + vendorPerPage.value);
});

// ==========================================
// 5. Filters & Pagination: Couriers
// ==========================================
const courierStatusFilter = ref('');
const courierSearch = ref('');
const courierPage = ref(1);
const courierPerPage = ref(10);

const resetCourierFilters = () => {
  courierStatusFilter.value = '';
  courierSearch.value = '';
  courierPage.value = 1;
};

const filteredCourierList = computed(() => {
  let list = [...courierList.value];

  if (courierStatusFilter.value === 'ACTIVE') {
    list = list.filter((c) => (c.is_active !== undefined ? c.is_active : c.isActive));
  } else if (courierStatusFilter.value === 'INACTIVE') {
    list = list.filter((c) => !(c.is_active !== undefined ? c.is_active : c.isActive));
  }

  if (courierSearch.value.trim()) {
    const q = courierSearch.value.trim().toLowerCase();
    list = list.filter(
      (c) =>
        (c.code && c.code.toLowerCase().includes(q)) ||
        (c.name && c.name.toLowerCase().includes(q)) ||
        (c.phone && c.phone.toLowerCase().includes(q)) ||
        getCourierServices(c).some((s) => s.toLowerCase().includes(q))
    );
  }

  return list;
});

const paginatedCourierList = computed(() => {
  const start = (courierPage.value - 1) * courierPerPage.value;
  return filteredCourierList.value.slice(start, start + courierPerPage.value);
});

// ==========================================
// 6. Modal States & CRUD: Vendor
// ==========================================
const viewVendorModalOpen = ref(false);
const createVendorModalOpen = ref(false);
const editVendorModalOpen = ref(false);
const deleteVendorModalOpen = ref(false);
const selectedVendor = ref(null);

const createVendorForm = reactive({
  code: '',
  name: '',
  sla_days: 7,
  payment_terms: 'TOP 30 Hari',
  rating: 5.0,
  phone: '',
  email: '',
  address: ''
});

const editVendorForm = reactive({
  id: null,
  code: '',
  name: '',
  sla_days: 7,
  payment_terms: 'TOP 30 Hari',
  rating: 5.0,
  phone: '',
  email: '',
  address: '',
  is_active: true
});

const openViewVendorModal = (v) => {
  selectedVendor.value = v;
  viewVendorModalOpen.value = true;
};

const openCreateVendorModal = () => {
  Object.assign(createVendorForm, {
    code: '',
    name: '',
    sla_days: 7,
    payment_terms: 'TOP 30 Hari',
    rating: 5.0,
    phone: '',
    email: '',
    address: ''
  });
  createVendorModalOpen.value = true;
};

const submitCreateVendor = () => {
  const newId = vendorList.value.length ? Math.max(...vendorList.value.map((v) => v.id || 0)) + 1 : 1;
  const newV = {
    id: newId,
    code: createVendorForm.code.trim().toUpperCase(),
    name: createVendorForm.name.trim(),
    sla_days: createVendorForm.sla_days || 7,
    payment_terms: createVendorForm.payment_terms,
    rating: createVendorForm.rating || 5.0,
    phone: createVendorForm.phone.trim(),
    email: createVendorForm.email.trim(),
    address: createVendorForm.address.trim(),
    purchase_orders_count: 0,
    is_active: true
  };
  vendorList.value.unshift(newV);
  createVendorModalOpen.value = false;
  showAlert(`Vendor Rekanan "${newV.code} - ${newV.name}" berhasil ditambahkan.`);
};

const openEditVendorModal = (v) => {
  selectedVendor.value = v;
  Object.assign(editVendorForm, {
    id: v.id,
    code: v.code,
    name: v.name,
    sla_days: v.sla_days || v.slaDays || 7,
    payment_terms: v.payment_terms || v.paymentTerms || 'TOP 30 Hari',
    rating: v.rating || 5.0,
    phone: v.phone || '',
    email: v.email || '',
    address: v.address || '',
    is_active: v.is_active !== undefined ? Boolean(v.is_active) : Boolean(v.isActive)
  });
  editVendorModalOpen.value = true;
};

const submitEditVendor = () => {
  const index = vendorList.value.findIndex((v) => v.id === editVendorForm.id || v.code === editVendorForm.code);
  if (index !== -1) {
    vendorList.value[index] = {
      ...vendorList.value[index],
      code: editVendorForm.code.trim().toUpperCase(),
      name: editVendorForm.name.trim(),
      sla_days: editVendorForm.sla_days,
      payment_terms: editVendorForm.payment_terms,
      rating: editVendorForm.rating,
      phone: editVendorForm.phone.trim(),
      email: editVendorForm.email.trim(),
      address: editVendorForm.address.trim(),
      is_active: editVendorForm.is_active
    };
    editVendorModalOpen.value = false;
    showAlert(`Vendor Rekanan "${editVendorForm.code}" berhasil diperbarui.`);
  }
};

const toggleVendorStatus = (v) => {
  const current = v.is_active !== undefined ? v.is_active : v.isActive;
  v.is_active = !current;
  v.isActive = !current;
  showAlert(`Status vendor "${v.code}" diubah menjadi ${!current ? 'Aktif' : 'Non-Aktif'}.`, 'warning');
};

const openDeleteVendorModal = (v) => {
  selectedVendor.value = v;
  deleteVendorModalOpen.value = true;
};

const submitDeleteVendor = () => {
  if (selectedVendor.value) {
    vendorList.value = vendorList.value.filter((v) => v.id !== selectedVendor.value.id && v.code !== selectedVendor.value.code);
    deleteVendorModalOpen.value = false;
    showAlert(`Vendor Rekanan "${selectedVendor.value.code}" telah dihapus.`, 'danger');
  }
};

// ==========================================
// 7. Modal States & CRUD: Courier
// ==========================================
const viewCourierModalOpen = ref(false);
const createCourierModalOpen = ref(false);
const editCourierModalOpen = ref(false);
const deleteCourierModalOpen = ref(false);
const selectedCourier = ref(null);

const createCourierForm = reactive({
  code: '',
  name: '',
  sla_days: 2,
  phone: '',
  service_types: ['REGULER', 'EXPRESS']
});

const editCourierForm = reactive({
  id: null,
  code: '',
  name: '',
  sla_days: 2,
  phone: '',
  service_types: ['REGULER'],
  is_active: true
});

const openViewCourierModal = (c) => {
  selectedCourier.value = c;
  viewCourierModalOpen.value = true;
};

const openCreateCourierModal = () => {
  Object.assign(createCourierForm, {
    code: '',
    name: '',
    sla_days: 2,
    phone: '',
    service_types: ['REGULER', 'EXPRESS']
  });
  createCourierModalOpen.value = true;
};

const submitCreateCourier = () => {
  const newId = courierList.value.length ? Math.max(...courierList.value.map((c) => c.id || 0)) + 1 : 1;
  const newC = {
    id: newId,
    code: createCourierForm.code.trim().toUpperCase(),
    name: createCourierForm.name.trim(),
    sla_days: createCourierForm.sla_days || 2,
    phone: createCourierForm.phone.trim(),
    service_types: [...createCourierForm.service_types],
    shipments_count: 0,
    is_active: true
  };
  courierList.value.unshift(newC);
  createCourierModalOpen.value = false;
  showAlert(`Mitra Ekspedisi "${newC.code} - ${newC.name}" berhasil ditambahkan.`);
};

const openEditCourierModal = (c) => {
  selectedCourier.value = c;
  Object.assign(editCourierForm, {
    id: c.id,
    code: c.code,
    name: c.name,
    sla_days: c.sla_days || c.slaDays || 2,
    phone: c.phone || '',
    service_types: [...getCourierServices(c)],
    is_active: c.is_active !== undefined ? Boolean(c.is_active) : Boolean(c.isActive)
  });
  editCourierModalOpen.value = true;
};

const submitEditCourier = () => {
  const index = courierList.value.findIndex((c) => c.id === editCourierForm.id || c.code === editCourierForm.code);
  if (index !== -1) {
    courierList.value[index] = {
      ...courierList.value[index],
      code: editCourierForm.code.trim().toUpperCase(),
      name: editCourierForm.name.trim(),
      sla_days: editCourierForm.sla_days,
      phone: editCourierForm.phone.trim(),
      service_types: [...editCourierForm.service_types],
      is_active: editCourierForm.is_active
    };
    editCourierModalOpen.value = false;
    showAlert(`Mitra Ekspedisi "${editCourierForm.code}" berhasil diperbarui.`);
  }
};

const toggleCourierStatus = (c) => {
  const current = c.is_active !== undefined ? c.is_active : c.isActive;
  c.is_active = !current;
  c.isActive = !current;
  showAlert(`Status ekspedisi "${c.code}" diubah menjadi ${!current ? 'Aktif' : 'Non-Aktif'}.`, 'warning');
};

const openDeleteCourierModal = (c) => {
  selectedCourier.value = c;
  deleteCourierModalOpen.value = true;
};

const submitDeleteCourier = () => {
  if (selectedCourier.value) {
    courierList.value = courierList.value.filter((c) => c.id !== selectedCourier.value.id && c.code !== selectedCourier.value.code);
    deleteCourierModalOpen.value = false;
    showAlert(`Mitra Ekspedisi "${selectedCourier.value.code}" telah dihapus.`, 'danger');
  }
};

const printPage = () => {
  window.print();
};

// ==========================================
// 8. Lifecycle & Backend Fetching
// ==========================================
onMounted(async () => {
  initTabFromQuery();
  try {
    const [vendorsRes, couriersRes] = await Promise.allSettled([
      api.get('/master/vendors'),
      api.get('/master/couriers')
    ]);

    if (vendorsRes.status === 'fulfilled') {
      const vData = extractList(vendorsRes.value);
      if (vData && Array.isArray(vData) && vData.length > 0) {
        vendorList.value = vData.map((v, idx) => ({
          ...v,
          id: v.id || idx + 1,
          sla_days: v.slaDays || v.sla_days || 7,
          payment_terms: v.paymentTerms || v.payment_terms || 'TOP 30 Hari',
          rating: v.rating ? Number(v.rating) : 5.0,
          is_active: v.isActive !== undefined ? v.isActive : v.is_active !== undefined ? v.is_active : true
        }));
      }
    }

    if (couriersRes.status === 'fulfilled') {
      const cData = extractList(couriersRes.value);
      if (cData && Array.isArray(cData) && cData.length > 0) {
        courierList.value = cData.map((c, idx) => ({
          ...c,
          id: c.id || idx + 1,
          sla_days: c.slaDays || c.sla_days || 2,
          service_types: c.serviceTypes || c.service_types || ['REGULER', 'EXPRESS'],
          is_active: c.isActive !== undefined ? c.isActive : c.is_active !== undefined ? c.is_active : true
        }));
      }
    }
  } catch (err) {
    console.warn('Gagal memuat vendor dan ekspedisi dari backend:', err);
  }
});

watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab && validTabs.includes(newTab) && newTab !== activeMainTab.value) {
      activeMainTab.value = newTab;
    }
  }
);
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.fs-7 {
  font-size: 0.875rem;
}

.fs-8 {
  font-size: 0.8125rem;
}

.fs-9 {
  font-size: 0.75rem;
}

.nav-pills-scroll {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.btn-action-icon {
  width: 28px;
  height: 28px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem;
}

.space-y-3 > * + * {
  margin-top: 0.75rem;
}

.space-y-2 > * + * {
  margin-top: 0.5rem;
}

@media print {
  .app-content-header,
  .card-header-pills,
  .card-tools,
  .bg-body-tertiary,
  .btn-action-icon,
  .pagination-container,
  th:last-child,
  td:last-child {
    display: none !important;
  }
  .card {
    border: none !important;
    box-shadow: none !important;
  }
}
</style>

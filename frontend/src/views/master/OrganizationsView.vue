<template>
  <div class="organizations-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Data Unit Kerja & Gudang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item text-secondary">Master Data</li>
              <li class="breadcrumb-item active text-body fw-semibold" aria-current="page">Unit Kerja & Gudang</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- Alert Feedback Message -->
    <div v-if="alertMessage" class="alert alert-dismissible fade show fs-8 py-2 px-3 mb-3" :class="alertClass" role="alert">
      <i :class="alertIcon" class="me-1"></i>
      <span>{{ alertMessage }}</span>
      <button type="button" class="btn-close py-2" @click="alertMessage = ''" aria-label="Close"></button>
    </div>

    <!-- 2. Main Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs (Tanpa Jumlah Record) & Action Buttons -->
      <div class="card-header bg-body p-2 px-3 border-bottom d-flex flex-column flex-md-row align-items-stretch align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills nav-pills-scroll m-0" role="tablist" aria-label="Tabs Master Organisasi">
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('organizations')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'organizations' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Unit Kerja
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('warehouses')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'warehouses' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Lokasi Gudang
            </button>
          </li>
        </ul>

        <!-- Right: Action Button -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <template v-if="activeMainTab === 'organizations'">
            <button
              @click="openCreateOrgModal"
              type="button"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              <i class="bi bi-plus-circle"></i>
              <span>Tambah Unit Kerja Baru</span>
            </button>
          </template>
          <template v-if="activeMainTab === 'warehouses'">
            <button
              @click="openCreateWhModal"
              type="button"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              <i class="bi bi-plus-circle"></i>
              <span>Tambah Gudang Baru</span>
            </button>
          </template>
          <button
            type="button"
            @click="printPage"
            class="btn btn-sm btn-outline-secondary fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            title="Cetak Halaman Ini"
          >
            <i class="bi bi-printer"></i>
            <span>Cetak</span>
          </button>
        </div>
      </div>

      <!-- ==================== TAB 1: UNIT KERJA ==================== -->
      <div v-show="activeMainTab === 'organizations'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Type Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
                <select v-model="orgType" @change="orgPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Tipe Organisasi</option>
                  <option value="HEAD_OFFICE">Kantor Pusat (HEAD_OFFICE)</option>
                  <option value="MAIN_BRANCH">Cabang Utama (MAIN_BRANCH)</option>
                  <option value="SUB_BRANCH">Cabang Pembantu (SUB_BRANCH)</option>
                  <option value="WAREHOUSE">Hub Logistik (WAREHOUSE)</option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="orgStatus" @change="orgPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="orgSearch || (orgType && orgType !== 'ALL') || orgStatus">
              <button
                type="button"
                @click="resetOrgFilters"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                <i class="bi bi-x-circle me-1"></i> Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="orgSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Kode, Nama, Kota, Cost Center..."
                  @keyup.enter="orgPage = 1"
                />
                <button type="button" @click="orgPage = 1" class="btn btn-danger btn-sm">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Unit Kerja -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-3 py-3" style="width: 240px;">Kode & Nama Unit</th>
                  <th class="py-3" style="width: 140px;">Tipe Organisasi</th>
                  <th class="py-3" style="min-width: 180px;">Induk Unit (Parent)</th>
                  <th class="py-3" style="min-width: 180px;">Kota & Alamat</th>
                  <th class="text-center py-3" style="width: 120px;">Cost Center</th>
                  <th class="text-center py-3" style="width: 130px;">Gudang / User</th>
                  <th class="text-center py-3" style="width: 90px;">Status</th>
                  <th class="text-center pe-3 py-3" style="width: 120px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="org in paginatedOrgs" :key="org.id">
                  <!-- Kode & Nama Unit -->
                  <td class="ps-3">
                    <div class="d-flex align-items-center gap-2">
                      <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fw-bold">{{ org.code }}</span>
                      <div class="fw-bold text-body">{{ org.name }}</div>
                    </div>
                    <div v-if="org.phone" class="fs-9 text-secondary mt-0.5">
                      <i class="bi bi-telephone me-1"></i>{{ org.phone }}
                    </div>
                  </td>

                  <!-- Tipe Organisasi -->
                  <td>
                    <span class="badge fs-9 text-uppercase" :class="getOrgTypeBadgeClass(org.type)">
                      {{ formatType(org.type) }}
                    </span>
                  </td>

                  <!-- Induk Unit -->
                  <td>
                    <div v-if="org.parent">
                      <div class="fw-semibold text-body">{{ org.parent.name }}</div>
                      <div class="font-monospace fs-9 text-secondary">{{ org.parent.code }}</div>
                    </div>
                    <span v-else class="text-muted fst-italic fs-8">- Tingkat Teratas -</span>
                  </td>

                  <!-- Kota & Alamat -->
                  <td>
                    <div class="fw-semibold text-body">{{ org.city || '-' }}</div>
                    <div class="text-secondary fs-9 text-truncate" style="max-width: 220px;" :title="org.address || '-'">
                      {{ org.address || '-' }}
                    </div>
                  </td>

                  <!-- Cost Center -->
                  <td class="text-center font-monospace fw-bold fs-8 text-secondary">
                    <span class="badge text-bg-light border">{{ org.cost_center_code || org.costCenterCode || '-' }}</span>
                  </td>

                  <!-- Gudang / User -->
                  <td class="text-center">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button
                        type="button"
                        @click="filterWarehouseByOrg(org.id)"
                        class="btn btn-xs btn-outline-warning py-0.5 px-1.5 fs-9 fw-bold"
                        title="Lihat Gudang Terkait"
                      >
                        <i class="bi bi-box-seam me-1"></i><span>{{ getOrgWarehouseCount(org) }}</span>
                      </button>
                      <span class="badge bg-primary-subtle text-primary fs-9" title="User Pegawai">
                        <i class="bi bi-people me-1"></i><span>{{ getOrgUserCount(org) }}</span>
                      </span>
                    </div>
                  </td>

                  <!-- Status -->
                  <td class="text-center">
                    <button
                      type="button"
                      @click="toggleOrgStatus(org)"
                      class="badge border-0 cursor-pointer fs-9 py-1 px-2"
                      :class="isOrgActive(org) ? 'text-bg-success' : 'text-bg-secondary'"
                      title="Klik untuk mengubah status"
                    >
                      {{ isOrgActive(org) ? 'Aktif' : 'Non-Aktif' }}
                    </button>
                  </td>

                  <!-- Aksi -->
                  <td class="text-center pe-3">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewOrgModal(org)" class="btn-action-icon text-secondary" title="Detail Unit Kerja">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditOrgModal(org)" class="btn-action-icon text-primary" title="Edit Unit Kerja">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteOrgModal(org)" class="btn-action-icon text-danger" title="Hapus Unit Kerja">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredOrgs.length === 0">
                  <td colspan="8" class="text-center py-5 text-secondary">
                    <i class="bi bi-building fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data unit kerja ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau bersihkan filter.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredOrgs.length"
          v-model:currentPage="orgPage"
          v-model:perPage="orgPerPage"
        />
      </div>

      <!-- ==================== TAB 2: LOKASI GUDANG ==================== -->
      <div v-show="activeMainTab === 'warehouses'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Type Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-funnel"></i></span>
                <select v-model="whType" @change="whPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Tipe Gudang</option>
                  <option value="CENTRAL_LOGISTICS">Gudang Logistik Pusat</option>
                  <option value="BRANCH_STORAGE">Penyimpanan Cabang</option>
                </select>
              </div>
            </div>

            <!-- Organization Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
                <select v-model="whOrg" @change="whPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Unit Kerja</option>
                  <option v-for="org in orgList" :key="org.id" :value="String(org.id)">
                    {{ org.name }} ({{ org.code }})
                  </option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="whStatus" @change="whPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="whSearch || (whType && whType !== 'ALL') || (whOrg && whOrg !== 'ALL') || whStatus">
              <button
                type="button"
                @click="resetWhFilters"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                <i class="bi bi-x-circle me-1"></i> Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="whSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Kode atau Nama Gudang..."
                  @keyup.enter="whPage = 1"
                />
                <button type="button" @click="whPage = 1" class="btn btn-danger btn-sm">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Lokasi Gudang -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-3 py-3" style="width: 220px;">Kode & Nama Gudang</th>
                  <th class="py-3" style="min-width: 200px;">Unit Kerja Pemilik</th>
                  <th class="py-3" style="width: 170px;">Tipe Gudang</th>
                  <th class="py-3" style="min-width: 200px;">Alamat Lokasi</th>
                  <th class="text-center py-3" style="width: 140px;">Variasi SKU Stok</th>
                  <th class="text-center py-3" style="width: 90px;">Status</th>
                  <th class="text-center pe-3 py-3" style="width: 110px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="wh in paginatedWarehouses" :key="wh.id">
                  <!-- Kode & Nama Gudang -->
                  <td class="ps-3">
                    <div>
                      <span class="badge bg-warning-subtle text-warning-emphasis font-monospace fw-bold">{{ wh.code }}</span>
                      <div class="fw-bold text-body mt-1">{{ wh.name }}</div>
                    </div>
                  </td>

                  <!-- Unit Kerja Pemilik -->
                  <td>
                    <div class="fw-semibold text-body">{{ getWhOrgName(wh) }}</div>
                    <div class="font-monospace fs-9 text-secondary">{{ getWhOrgMeta(wh) }}</div>
                  </td>

                  <!-- Tipe Gudang -->
                  <td>
                    <span class="badge fs-9" :class="wh.type === 'CENTRAL_LOGISTICS' ? 'text-bg-warning' : 'text-bg-secondary'">
                      {{ wh.type === 'CENTRAL_LOGISTICS' ? 'Gudang Pusat' : 'Penyimpanan Cabang' }}
                    </span>
                  </td>

                  <!-- Alamat Lokasi -->
                  <td>
                    <div class="text-secondary fs-9 text-truncate" style="max-width: 240px;" :title="wh.address || '-'">
                      {{ wh.address || '-' }}
                    </div>
                  </td>

                  <!-- Variasi SKU Stok -->
                  <td class="text-center">
                    <span class="badge bg-body-secondary text-secondary-emphasis font-monospace fs-8">
                      {{ getWhStockCount(wh) }} SKU
                    </span>
                  </td>

                  <!-- Status -->
                  <td class="text-center">
                    <button
                      type="button"
                      @click="toggleWhStatus(wh)"
                      class="badge border-0 cursor-pointer fs-9 py-1 px-2"
                      :class="isWhActive(wh) ? 'text-bg-success' : 'text-bg-secondary'"
                      title="Klik untuk mengubah status"
                    >
                      {{ isWhActive(wh) ? 'Aktif' : 'Non-Aktif' }}
                    </button>
                  </td>

                  <!-- Aksi -->
                  <td class="text-center pe-3">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewWhModal(wh)" class="btn-action-icon text-secondary" title="Detail Gudang">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditWhModal(wh)" class="btn-action-icon text-primary" title="Edit Gudang">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteWhModal(wh)" class="btn-action-icon text-danger" title="Hapus Gudang">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredWarehouses.length === 0">
                  <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-box-seam fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data lokasi gudang ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau bersihkan filter.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredWarehouses.length"
          v-model:currentPage="whPage"
          v-model:perPage="whPerPage"
        />
      </div>
    </div>

    <!-- ==================== 1. MODAL: DETAIL UNIT KERJA (VIEW) ==================== -->
    <div v-if="viewOrgModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-secondary-subtle text-secondary p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-building fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">
                  Detail Unit Kerja: <span class="font-monospace text-danger">{{ viewOrgData?.code }}</span>
                </h6>
                <span class="fs-8 text-secondary">Informasi identitas unit dan hierarki organisasi</span>
              </div>
            </div>
            <button type="button" @click="viewOrgModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;" v-if="viewOrgData">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-2 fs-8">
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Nama Unit:</span>
                  <strong class="text-body fs-7">{{ viewOrgData.name }}</strong>
                </div>
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Tipe Organisasi:</span>
                  <span class="badge text-bg-light border">{{ formatType(viewOrgData.type) }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block">Cost Center:</span>
                  <span class="font-monospace fw-bold text-danger">{{ viewOrgData.cost_center_code || viewOrgData.costCenterCode || '-' }}</span>
                </div>
                <div class="col-6 col-sm-4">
                  <span class="text-secondary d-block">Kota:</span>
                  <span class="fw-bold text-body">{{ viewOrgData.city || '-' }}</span>
                </div>
                <div class="col-12 col-sm-4">
                  <span class="text-secondary d-block">Telepon:</span>
                  <span class="text-body">{{ viewOrgData.phone || '-' }}</span>
                </div>
                <div class="col-12">
                  <span class="text-secondary d-block">Alamat Lengkap:</span>
                  <span class="text-body">{{ viewOrgData.address || '-' }}</span>
                </div>
              </div>
            </div>

            <div class="border rounded-2 p-2.5 bg-body-tertiary">
              <div class="fs-8 fw-bold text-uppercase text-secondary mb-2 d-flex align-items-center">
                <i class="bi bi-link-45deg text-danger me-1"></i> Entitas Terhubung
              </div>
              <div class="row g-2 text-center fs-8">
                <div class="col-6">
                  <div class="p-2 rounded bg-body border">
                    <span class="fs-9 text-secondary d-block">Gudang Penyimpanan</span>
                    <strong class="fs-6 text-warning-emphasis font-monospace">{{ getOrgWarehouseCount(viewOrgData) }}</strong>
                  </div>
                </div>
                <div class="col-6">
                  <div class="p-2 rounded bg-body border">
                    <span class="fs-9 text-secondary d-block">Pegawai Terdaftar</span>
                    <strong class="fs-6 text-primary font-monospace">{{ getOrgUserCount(viewOrgData) }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewOrgModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 2. MODAL: TAMBAH UNIT KERJA (CREATE) ==================== -->
    <div v-if="createOrgModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-danger-subtle text-danger p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-plus-circle fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">Tambah Unit Kerja Baru</h6>
                <span class="fs-8 text-secondary">Registrasi Kantor Pusat, Cabang Utama, atau Capem Bank Jatim</span>
              </div>
            </div>
            <button type="button" @click="createOrgModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitCreateOrg">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Unit Kerja <span class="text-danger">*</span></label>
                  <input type="text" v-model="createOrgForm.code" required class="form-control form-control-sm font-monospace text-uppercase fw-bold fs-8" placeholder="Contoh: KCP-WRU" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Unit <span class="text-danger">*</span></label>
                  <select v-model="createOrgForm.type" required class="form-select form-select-sm fs-8">
                    <option value="SUB_BRANCH">Cabang Pembantu (SUB_BRANCH)</option>
                    <option value="MAIN_BRANCH">Cabang Utama (MAIN_BRANCH)</option>
                    <option value="HEAD_OFFICE">Kantor Pusat (HEAD_OFFICE)</option>
                    <option value="WAREHOUSE">Hub Logistik (WAREHOUSE)</option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Unit Kerja <span class="text-danger">*</span></label>
                  <input type="text" v-model="createOrgForm.name" required class="form-control form-control-sm fs-8" placeholder="Contoh: Kantor Cabang Pembantu Waru" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Induk Unit (Parent)</label>
                  <select v-model="createOrgForm.parent_id" class="form-select form-select-sm fs-8">
                    <option value="">-- Tingkat Teratas (Tanpa Induk) --</option>
                    <option v-for="p in parentOptions" :key="p.id" :value="p.id">
                      {{ p.name }} ({{ p.code }})
                    </option>
                  </select>
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Cost Center Code</label>
                  <input type="text" v-model="createOrgForm.cost_center_code" class="form-control form-control-sm font-monospace text-uppercase fs-8" placeholder="Contoh: CC-KCP-WRU" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kota / Kabupaten <span class="text-danger">*</span></label>
                  <input type="text" v-model="createOrgForm.city" required class="form-control form-control-sm fs-8" placeholder="Contoh: Sidoarjo" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Telepon</label>
                  <input type="text" v-model="createOrgForm.phone" class="form-control form-control-sm fs-8" placeholder="Contoh: 031-8531234" />
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Lengkap</label>
                  <textarea v-model="createOrgForm.address" rows="2" class="form-control form-control-sm fs-8" placeholder="Alamat lengkap gedung kantor..."></textarea>
                </div>

                <div class="col-12">
                  <div class="form-check p-2.5 rounded border bg-body-tertiary">
                    <input class="form-check-input ms-0 me-2" type="checkbox" v-model="createOrgForm.create_warehouse" id="cb_wh_create" />
                    <label class="form-check-label fs-8 text-body fw-semibold" for="cb_wh_create">
                      Otomatis Buat Lokasi Gudang Penyimpanan Terkait
                      <span class="d-block fs-9 text-secondary fw-normal">Membuat entitas gudang untuk mengelola mutasi dan saldo stok unit kerja ini.</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="createOrgModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                <i class="bi bi-save me-1"></i> Simpan Unit Kerja
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ==================== 3. MODAL: EDIT UNIT KERJA ==================== -->
    <div v-if="editOrgModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-primary-subtle text-primary p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-pencil-square fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">
                  Edit Unit Kerja: <span class="font-monospace text-danger">{{ editOrgForm.code }}</span>
                </h6>
                <span class="fs-8 text-secondary">Perbarui profil atau struktur hierarki</span>
              </div>
            </div>
            <button type="button" @click="editOrgModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitEditOrg">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Unit Kerja <span class="text-danger">*</span></label>
                  <input type="text" v-model="editOrgForm.code" required class="form-control form-control-sm font-monospace text-uppercase fw-bold fs-8" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Unit <span class="text-danger">*</span></label>
                  <select v-model="editOrgForm.type" required class="form-select form-select-sm fs-8">
                    <option value="SUB_BRANCH">Cabang Pembantu (SUB_BRANCH)</option>
                    <option value="MAIN_BRANCH">Cabang Utama (MAIN_BRANCH)</option>
                    <option value="HEAD_OFFICE">Kantor Pusat (HEAD_OFFICE)</option>
                    <option value="WAREHOUSE">Hub Logistik (WAREHOUSE)</option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Unit Kerja <span class="text-danger">*</span></label>
                  <input type="text" v-model="editOrgForm.name" required class="form-control form-control-sm fs-8" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Induk Unit (Parent)</label>
                  <select v-model="editOrgForm.parent_id" class="form-select form-select-sm fs-8">
                    <option value="">-- Tingkat Teratas (Tanpa Induk) --</option>
                    <option v-for="p in parentOptions" :key="p.id" :value="p.id" :disabled="p.id === editOrgForm.id">
                      {{ p.name }} ({{ p.code }})
                    </option>
                  </select>
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Cost Center Code</label>
                  <input type="text" v-model="editOrgForm.cost_center_code" class="form-control form-control-sm font-monospace text-uppercase fs-8" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kota / Kabupaten <span class="text-danger">*</span></label>
                  <input type="text" v-model="editOrgForm.city" required class="form-control form-control-sm fs-8" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nomor Telepon</label>
                  <input type="text" v-model="editOrgForm.phone" class="form-control form-control-sm fs-8" />
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Lengkap</label>
                  <textarea v-model="editOrgForm.address" rows="2" class="form-control form-control-sm fs-8"></textarea>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Status Operasional <span class="text-danger">*</span></label>
                  <select v-model="editOrgForm.is_active" class="form-select form-select-sm fs-8">
                    <option :value="1">Aktif (Operasional)</option>
                    <option :value="0">Non-Aktif (Tutup / Suspended)</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="editOrgModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                <i class="bi bi-check2-circle me-1"></i> Simpan Perubahan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ==================== 4. MODAL: HAPUS UNIT KERJA ==================== -->
    <div v-if="deleteOrgModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-danger text-white p-2 d-flex align-items-center justify-content-center" style="width: 36px; height: 36px;">
                <i class="bi bi-trash-fill fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-danger">Konfirmasi Hapus Unit Kerja</h6>
                <span class="fs-8 text-secondary">Tindakan ini memerlukan verifikasi</span>
              </div>
            </div>
            <button type="button" @click="deleteOrgModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3">
            <p class="text-body mb-2 fs-7">
              Apakah Anda yakin ingin menghapus unit kerja berikut?
            </p>

            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-1">Kode & Nama Unit:</div>
              <div class="font-monospace fw-bold text-danger fs-7">{{ deleteOrgForm.code }} - {{ deleteOrgForm.name }}</div>
            </div>

            <div class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-start gap-2">
              <i class="bi bi-shield-exclamation text-warning fs-6 flex-shrink-0 mt-0.5"></i>
              <div>
                <strong>Pemeriksaan Integritas:</strong> Unit yang masih memiliki user pegawai (<strong>{{ deleteOrgForm.users_count }}</strong>) atau gudang (<strong>{{ deleteOrgForm.warehouses_count }}</strong>) tidak dapat dihapus permanen. Anda dapat menonaktifkan statusnya.
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteOrgModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Batal
            </button>
            <button type="button" @click="submitDeleteOrg" class="btn btn-sm btn-danger fw-bold px-3 shadow-xs">
              <i class="bi bi-trash me-1"></i> Ya, Hapus Unit
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 5. MODAL: DETAIL GUDANG (VIEW) ==================== -->
    <div v-if="viewWhModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-secondary-subtle text-secondary p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-box-seam fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">
                  Detail Lokasi Gudang: <span class="font-monospace text-danger">{{ viewWhData?.code }}</span>
                </h6>
                <span class="fs-8 text-secondary">Informasi spesifikasi dan kapasitas gudang</span>
              </div>
            </div>
            <button type="button" @click="viewWhModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;" v-if="viewWhData">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-2 fs-8">
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Nama Gudang:</span>
                  <strong class="text-body fs-7">{{ viewWhData.name }}</strong>
                </div>
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Tipe Gudang:</span>
                  <span class="badge text-bg-light border">
                    {{ viewWhData.type === 'CENTRAL_LOGISTICS' ? 'Gudang Logistik Pusat' : 'Penyimpanan Cabang' }}
                  </span>
                </div>
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Unit Kerja Pemilik:</span>
                  <span class="fw-bold text-body">{{ getWhOrgName(viewWhData) }}</span>
                </div>
                <div class="col-12 col-sm-6">
                  <span class="text-secondary d-block">Status:</span>
                  <span class="badge" :class="isWhActive(viewWhData) ? 'text-bg-success' : 'text-bg-secondary'">
                    {{ isWhActive(viewWhData) ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </div>
                <div class="col-12">
                  <span class="text-secondary d-block">Alamat Lokasi:</span>
                  <span class="text-body">{{ viewWhData.address || '-' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewWhModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Tutup
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 6. MODAL: TAMBAH GUDANG BARU (CREATE) ==================== -->
    <div v-if="createWhModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-warning-subtle text-warning-emphasis p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-box-seam fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">Tambah Lokasi Gudang Baru</h6>
                <span class="fs-8 text-secondary">Registrasi fisik lokasi penyimpanan persediaan</span>
              </div>
            </div>
            <button type="button" @click="createWhModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitCreateWh">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Unit Kerja Pemilik <span class="text-danger">*</span></label>
                  <select v-model="createWhForm.organization_id" required class="form-select form-select-sm fs-8">
                    <option value="" disabled>-- Pilih Unit Kerja Pemilik --</option>
                    <option v-for="org in orgList" :key="org.id" :value="org.id">
                      {{ org.name }} ({{ org.code }} &bull; {{ org.city || 'Jatim' }})
                    </option>
                  </select>
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Gudang <span class="text-danger">*</span></label>
                  <input type="text" v-model="createWhForm.code" required class="form-control form-control-sm font-monospace text-uppercase fw-bold fs-8" placeholder="Contoh: WH-KCPWRU" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Gudang <span class="text-danger">*</span></label>
                  <select v-model="createWhForm.type" required class="form-select form-select-sm fs-8">
                    <option value="BRANCH_STORAGE">Penyimpanan Cabang (BRANCH_STORAGE)</option>
                    <option value="CENTRAL_LOGISTICS">Gudang Logistik Pusat (CENTRAL_LOGISTICS)</option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Lokasi Gudang <span class="text-danger">*</span></label>
                  <input type="text" v-model="createWhForm.name" required class="form-control form-control-sm fs-8" placeholder="Contoh: Gudang Capem Waru" />
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Fisik Gudang</label>
                  <textarea v-model="createWhForm.address" rows="2" class="form-control form-control-sm fs-8" placeholder="Alamat lengkap fisik gudang..."></textarea>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="createWhModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-warning text-white fw-bold px-4 shadow-xs">
                <i class="bi bi-save me-1"></i> Simpan Gudang Baru
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ==================== 7. MODAL: EDIT GUDANG ==================== -->
    <div v-if="editWhModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-primary-subtle text-primary p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                <i class="bi bi-pencil-square fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-body">
                  Edit Gudang: <span class="font-monospace text-danger">{{ editWhForm.code }}</span>
                </h6>
                <span class="fs-8 text-secondary">Perbarui identitas fisik atau kepemilikan unit kerja</span>
              </div>
            </div>
            <button type="button" @click="editWhModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitEditWh">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Unit Kerja Pemilik <span class="text-danger">*</span></label>
                  <select v-model="editWhForm.organization_id" required class="form-select form-select-sm fs-8">
                    <option v-for="org in orgList" :key="org.id" :value="org.id">
                      {{ org.name }} ({{ org.code }} &bull; {{ org.city || 'Jatim' }})
                    </option>
                  </select>
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Gudang <span class="text-danger">*</span></label>
                  <input type="text" v-model="editWhForm.code" required class="form-control form-control-sm font-monospace text-uppercase fw-bold fs-8" />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Tipe Gudang <span class="text-danger">*</span></label>
                  <select v-model="editWhForm.type" required class="form-select form-select-sm fs-8">
                    <option value="BRANCH_STORAGE">Penyimpanan Cabang (BRANCH_STORAGE)</option>
                    <option value="CENTRAL_LOGISTICS">Gudang Logistik Pusat (CENTRAL_LOGISTICS)</option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Lokasi Gudang <span class="text-danger">*</span></label>
                  <input type="text" v-model="editWhForm.name" required class="form-control form-control-sm fs-8" />
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alamat Fisik Gudang</label>
                  <textarea v-model="editWhForm.address" rows="2" class="form-control form-control-sm fs-8"></textarea>
                </div>

                <div class="col-12">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Status Operasional <span class="text-danger">*</span></label>
                  <select v-model="editWhForm.is_active" class="form-select form-select-sm fs-8">
                    <option :value="1">Aktif (Dapat Menerima Stok)</option>
                    <option :value="0">Non-Aktif (Tutup / Non-Operasional)</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="editWhModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
                Batal
              </button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                <i class="bi bi-check2-circle me-1"></i> Simpan Perubahan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- ==================== 8. MODAL: HAPUS GUDANG ==================== -->
    <div v-if="deleteWhModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <div class="d-flex align-items-center gap-2">
              <div class="rounded-circle bg-danger text-white p-2 d-flex align-items-center justify-content-center" style="width: 36px; height: 36px;">
                <i class="bi bi-trash-fill fs-6"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold text-danger">Konfirmasi Hapus Gudang</h6>
                <span class="fs-8 text-secondary">Tindakan ini memerlukan verifikasi</span>
              </div>
            </div>
            <button type="button" @click="deleteWhModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3">
            <p class="text-body mb-2 fs-7">
              Apakah Anda yakin ingin menghapus gudang berikut?
            </p>

            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-1">Kode & Nama Gudang:</div>
              <div class="font-monospace fw-bold text-danger fs-7">{{ deleteWhForm.code }} - {{ deleteWhForm.name }}</div>
            </div>

            <div class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-start gap-2">
              <i class="bi bi-shield-exclamation text-warning fs-6 flex-shrink-0 mt-0.5"></i>
              <div>
                <strong>Pemeriksaan Saldo Stok:</strong> Terdapat <strong>{{ deleteWhForm.stock_count }}</strong> catatan saldo barang. Gudang dengan riwayat stok tidak dapat dihapus permanen.
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteWhModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Batal
            </button>
            <button type="button" @click="submitDeleteWh" class="btn btn-sm btn-danger fw-bold px-3 shadow-xs">
              <i class="bi bi-trash me-1"></i> Ya, Hapus Gudang
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
import PaginationFooter from '@/components/PaginationFooter.vue';
import api from '@/api/client';
import { extractList } from '@/utils/responseParser';

const route = useRoute();
const router = useRouter();

// Active Tab State (organizations | warehouses)
const activeMainTab = ref(
  (route.query.tab === 'warehouses' || window.location.hash.includes('warehouses'))
    ? 'warehouses'
    : 'organizations'
);

const switchTab = (tab) => {
  activeMainTab.value = tab;
  router.replace({ query: { ...route.query, tab } });
};

watch(() => route.query.tab, (newTab) => {
  if (newTab === 'warehouses' || newTab === 'organizations') {
    activeMainTab.value = newTab;
  }
});

// Feedback alerts
const alertMessage = ref('');
const alertClass = ref('alert-success');
const alertIcon = ref('bi bi-check-circle-fill');

const showAlert = (message, type = 'success') => {
  alertMessage.value = message;
  if (type === 'success') {
    alertClass.value = 'alert-success border-success-subtle text-success-emphasis';
    alertIcon.value = 'bi bi-check-circle-fill';
  } else if (type === 'danger') {
    alertClass.value = 'alert-danger border-danger-subtle text-danger-emphasis';
    alertIcon.value = 'bi bi-exclamation-triangle-fill';
  } else {
    alertClass.value = 'alert-info border-info-subtle text-info-emphasis';
    alertIcon.value = 'bi bi-info-circle-fill';
  }
};

// ==================== DATA STATE (Pure Backend / No Hardcoded Seed Data) ====================
const orgList = ref([]);
const warehouseList = ref([]);
const loading = ref(false);

// Load Data from Backend APIs
const loadData = async () => {
  loading.value = true;
  try {
    const [resOrgs, resWhs] = await Promise.allSettled([
      api.get('/master/organizations'),
      api.get('/master/warehouses')
    ]);

    if (resOrgs.status === 'fulfilled') {
      const orgs = extractList(resOrgs.value);
      if (Array.isArray(orgs)) {
        orgList.value = orgs;
      }
    }

    if (resWhs.status === 'fulfilled') {
      const whs = extractList(resWhs.value);
      if (Array.isArray(whs)) {
        warehouseList.value = whs;
      }
    }
  } catch (err) {
    console.warn('Gagal memuat master data organisasi / gudang dari backend:', err?.message || err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
  // If wh_org filter query exists in route
  if (route.query.wh_org) {
    whOrg.value = String(route.query.wh_org);
    activeMainTab.value = 'warehouses';
  }
});

// ==================== TAB 1: UNIT KERJA LOGIC ====================
const orgType = ref('ALL');
const orgStatus = ref('');
const orgSearch = ref('');
const orgPage = ref(1);
const orgPerPage = ref(10);

const resetOrgFilters = () => {
  orgType.value = 'ALL';
  orgStatus.value = '';
  orgSearch.value = '';
  orgPage.value = 1;
};

const isOrgActive = (org) => {
  if (org.is_active !== undefined) return Boolean(org.is_active);
  if (org.isActive !== undefined) return Boolean(org.isActive);
  if (org.status !== undefined) return org.status === 'ACTIVE' || org.status === 'AKTIF';
  return true;
};

const formatType = (type) => {
  if (!type) return '-';
  return String(type).replace(/_/g, ' ');
};

const getOrgTypeBadgeClass = (type) => {
  switch (type) {
    case 'HEAD_OFFICE':
      return 'bg-danger-subtle text-danger border border-danger-subtle';
    case 'MAIN_BRANCH':
      return 'bg-primary-subtle text-primary border border-primary-subtle';
    case 'SUB_BRANCH':
      return 'bg-info-subtle text-info-emphasis border border-info-subtle';
    case 'WAREHOUSE':
      return 'bg-warning-subtle text-warning-emphasis border border-warning-subtle';
    default:
      return 'bg-secondary-subtle text-secondary border';
  }
};

const getOrgWarehouseCount = (org) => {
  if (org.warehouses_count !== undefined) return org.warehouses_count;
  if (Array.isArray(org.warehouses)) return org.warehouses.length;
  // Match with warehouseList
  return warehouseList.value.filter((wh) => {
    const orgId = wh.organization_id || wh.organization?.id;
    return String(orgId) === String(org.id);
  }).length;
};

const getOrgUserCount = (org) => {
  if (org.users_count !== undefined) return org.users_count;
  if (Array.isArray(org.users)) return org.users.length;
  return 0;
};

const parentOptions = computed(() => {
  return orgList.value.filter(
    (o) => isOrgActive(o) && (o.type === 'HEAD_OFFICE' || o.type === 'MAIN_BRANCH')
  );
});

const filteredOrgs = computed(() => {
  return orgList.value.filter((org) => {
    // Type Filter
    if (orgType.value && orgType.value !== 'ALL' && org.type !== orgType.value) {
      return false;
    }
    // Status Filter
    if (orgStatus.value === 'ACTIVE' && !isOrgActive(org)) return false;
    if (orgStatus.value === 'INACTIVE' && isOrgActive(org)) return false;
    // Search Filter
    if (orgSearch.value) {
      const q = orgSearch.value.toLowerCase().trim();
      const code = (org.code || '').toLowerCase();
      const name = (org.name || '').toLowerCase();
      const city = (org.city || '').toLowerCase();
      const cc = (org.cost_center_code || org.costCenterCode || '').toLowerCase();
      if (!code.includes(q) && !name.includes(q) && !city.includes(q) && !cc.includes(q)) {
        return false;
      }
    }
    return true;
  });
});

const paginatedOrgs = computed(() => {
  const start = (orgPage.value - 1) * orgPerPage.value;
  return filteredOrgs.value.slice(start, start + orgPerPage.value);
});

const toggleOrgStatus = (org) => {
  const current = isOrgActive(org);
  org.is_active = !current;
  org.isActive = !current;
  showAlert(`Status unit kerja ${org.name} diubah menjadi ${!current ? 'Aktif' : 'Non-Aktif'}.`);
};

const filterWarehouseByOrg = (orgId) => {
  whOrg.value = String(orgId);
  switchTab('warehouses');
};

// ==================== TAB 2: LOKASI GUDANG LOGIC ====================
const whType = ref('ALL');
const whOrg = ref('ALL');
const whStatus = ref('');
const whSearch = ref('');
const whPage = ref(1);
const whPerPage = ref(10);

const resetWhFilters = () => {
  whType.value = 'ALL';
  whOrg.value = 'ALL';
  whStatus.value = '';
  whSearch.value = '';
  whPage.value = 1;
};

const isWhActive = (wh) => {
  if (wh.is_active !== undefined) return Boolean(wh.is_active);
  if (wh.isActive !== undefined) return Boolean(wh.isActive);
  if (wh.status !== undefined) return wh.status === 'ACTIVE' || wh.status === 'AKTIF';
  return true;
};

const getWhOrgName = (wh) => {
  if (wh.organization?.name) return wh.organization.name;
  const match = orgList.value.find((o) => String(o.id) === String(wh.organization_id || wh.organizationId));
  return match ? match.name : '-';
};

const getWhOrgMeta = (wh) => {
  const org = wh.organization || orgList.value.find((o) => String(o.id) === String(wh.organization_id || wh.organizationId));
  if (!org) return '-';
  const city = org.city || 'Surabaya';
  return `${org.code} • ${city}`;
};

const getWhStockCount = (wh) => {
  if (wh.stock_count !== undefined) return wh.stock_count;
  if (Array.isArray(wh.stock_balances)) return wh.stock_balances.length;
  if (Array.isArray(wh.stockBalances)) return wh.stockBalances.length;
  return 0;
};

const filteredWarehouses = computed(() => {
  return warehouseList.value.filter((wh) => {
    // Type Filter
    if (whType.value && whType.value !== 'ALL' && wh.type !== whType.value) {
      return false;
    }
    // Org Filter
    if (whOrg.value && whOrg.value !== 'ALL') {
      const orgId = String(wh.organization_id || wh.organization?.id || '');
      if (orgId !== String(whOrg.value)) return false;
    }
    // Status Filter
    if (whStatus.value === 'ACTIVE' && !isWhActive(wh)) return false;
    if (whStatus.value === 'INACTIVE' && isWhActive(wh)) return false;
    // Search Filter
    if (whSearch.value) {
      const q = whSearch.value.toLowerCase().trim();
      const code = (wh.code || '').toLowerCase();
      const name = (wh.name || '').toLowerCase();
      if (!code.includes(q) && !name.includes(q)) return false;
    }
    return true;
  });
});

const paginatedWarehouses = computed(() => {
  const start = (whPage.value - 1) * whPerPage.value;
  return filteredWarehouses.value.slice(start, start + whPerPage.value);
});

const toggleWhStatus = (wh) => {
  const current = isWhActive(wh);
  wh.is_active = !current;
  wh.isActive = !current;
  showAlert(`Status gudang ${wh.name} diubah menjadi ${!current ? 'Aktif' : 'Non-Aktif'}.`);
};

// ==================== MODAL HANDLERS ====================

// 1. View Org
const viewOrgModalOpen = ref(false);
const viewOrgData = ref(null);
const openViewOrgModal = (org) => {
  viewOrgData.value = org;
  viewOrgModalOpen.value = true;
};

// 2. Create Org
const createOrgModalOpen = ref(false);
const createOrgForm = reactive({
  code: '',
  name: '',
  type: 'SUB_BRANCH',
  parent_id: '',
  cost_center_code: '',
  city: 'Surabaya',
  phone: '',
  address: '',
  create_warehouse: true
});

const openCreateOrgModal = () => {
  Object.assign(createOrgForm, {
    code: '',
    name: '',
    type: 'SUB_BRANCH',
    parent_id: '',
    cost_center_code: '',
    city: 'Surabaya',
    phone: '',
    address: '',
    create_warehouse: true
  });
  createOrgModalOpen.value = true;
};

const submitCreateOrg = () => {
  const newId = Date.now();
  const parentObj = orgList.value.find((o) => o.id === createOrgForm.parent_id) || null;
  const newOrg = {
    id: newId,
    code: createOrgForm.code.toUpperCase(),
    name: createOrgForm.name,
    type: createOrgForm.type,
    parent_id: createOrgForm.parent_id || null,
    parent: parentObj,
    cost_center_code: createOrgForm.cost_center_code.toUpperCase(),
    city: createOrgForm.city,
    phone: createOrgForm.phone,
    address: createOrgForm.address,
    is_active: 1,
    warehouses_count: createOrgForm.create_warehouse ? 1 : 0,
    users_count: 0
  };
  orgList.value.unshift(newOrg);

  if (createOrgForm.create_warehouse) {
    const newWh = {
      id: Date.now() + 1,
      organization_id: newId,
      organization: newOrg,
      code: 'WH-' + newOrg.code.replace(/[^A-Za-z0-9]/g, ''),
      name: 'Gudang ' + newOrg.name,
      type: newOrg.type === 'HEAD_OFFICE' || newOrg.type === 'WAREHOUSE' ? 'CENTRAL_LOGISTICS' : 'BRANCH_STORAGE',
      address: newOrg.address,
      is_active: 1,
      stock_count: 0
    };
    warehouseList.value.unshift(newWh);
  }

  createOrgModalOpen.value = false;
  showAlert(`Unit kerja ${newOrg.name} (${newOrg.code}) berhasil ditambahkan.`);
};

// 3. Edit Org
const editOrgModalOpen = ref(false);
const editOrgForm = reactive({
  id: null,
  code: '',
  name: '',
  type: 'SUB_BRANCH',
  parent_id: '',
  cost_center_code: '',
  city: '',
  phone: '',
  address: '',
  is_active: 1
});

const openEditOrgModal = (org) => {
  Object.assign(editOrgForm, {
    id: org.id,
    code: org.code,
    name: org.name,
    type: org.type,
    parent_id: org.parent_id || (org.parent ? org.parent.id : ''),
    cost_center_code: org.cost_center_code || org.costCenterCode || '',
    city: org.city || 'Surabaya',
    phone: org.phone || '',
    address: org.address || '',
    is_active: isOrgActive(org) ? 1 : 0
  });
  editOrgModalOpen.value = true;
};

const submitEditOrg = () => {
  const org = orgList.value.find((o) => o.id === editOrgForm.id);
  if (org) {
    const parentObj = orgList.value.find((o) => o.id === editOrgForm.parent_id) || null;
    org.code = editOrgForm.code.toUpperCase();
    org.name = editOrgForm.name;
    org.type = editOrgForm.type;
    org.parent_id = editOrgForm.parent_id || null;
    org.parent = parentObj;
    org.cost_center_code = editOrgForm.cost_center_code.toUpperCase();
    org.city = editOrgForm.city;
    org.phone = editOrgForm.phone;
    org.address = editOrgForm.address;
    org.is_active = editOrgForm.is_active === 1;
    org.isActive = editOrgForm.is_active === 1;
    showAlert(`Perubahan data unit kerja ${org.name} berhasil disimpan.`);
  }
  editOrgModalOpen.value = false;
};

// 4. Delete Org
const deleteOrgModalOpen = ref(false);
const deleteOrgForm = reactive({
  id: null,
  code: '',
  name: '',
  users_count: 0,
  warehouses_count: 0
});

const openDeleteOrgModal = (org) => {
  Object.assign(deleteOrgForm, {
    id: org.id,
    code: org.code,
    name: org.name,
    users_count: getOrgUserCount(org),
    warehouses_count: getOrgWarehouseCount(org)
  });
  deleteOrgModalOpen.value = true;
};

const submitDeleteOrg = () => {
  if (deleteOrgForm.users_count > 0 || deleteOrgForm.warehouses_count > 0) {
    showAlert(`Gagal menghapus: Unit kerja "${deleteOrgForm.name}" masih memiliki pegawai atau gudang terhubung. Nonaktifkan status unit sebagai alternatif.`, 'danger');
    deleteOrgModalOpen.value = false;
    return;
  }
  orgList.value = orgList.value.filter((o) => o.id !== deleteOrgForm.id);
  deleteOrgModalOpen.value = false;
  showAlert(`Unit kerja ${deleteOrgForm.name} telah berhasil dihapus.`);
};

// 5. View Wh
const viewWhModalOpen = ref(false);
const viewWhData = ref(null);
const openViewWhModal = (wh) => {
  viewWhData.value = wh;
  viewWhModalOpen.value = true;
};

// 6. Create Wh
const createWhModalOpen = ref(false);
const createWhForm = reactive({
  organization_id: '',
  code: '',
  name: '',
  type: 'BRANCH_STORAGE',
  address: ''
});

const openCreateWhModal = () => {
  Object.assign(createWhForm, {
    organization_id: orgList.value.length ? orgList.value[0].id : '',
    code: '',
    name: '',
    type: 'BRANCH_STORAGE',
    address: ''
  });
  createWhModalOpen.value = true;
};

const submitCreateWh = () => {
  const parentOrg = orgList.value.find((o) => String(o.id) === String(createWhForm.organization_id));
  const newWh = {
    id: Date.now(),
    organization_id: createWhForm.organization_id,
    organization: parentOrg,
    code: createWhForm.code.toUpperCase(),
    name: createWhForm.name,
    type: createWhForm.type,
    address: createWhForm.address || (parentOrg ? parentOrg.address : ''),
    is_active: 1,
    stock_count: 0
  };
  warehouseList.value.unshift(newWh);
  if (parentOrg) {
    parentOrg.warehouses_count = (parentOrg.warehouses_count || 0) + 1;
  }
  createWhModalOpen.value = false;
  showAlert(`Lokasi gudang ${newWh.name} (${newWh.code}) berhasil didaftarkan.`);
};

// 7. Edit Wh
const editWhModalOpen = ref(false);
const editWhForm = reactive({
  id: null,
  organization_id: '',
  code: '',
  name: '',
  type: 'BRANCH_STORAGE',
  address: '',
  is_active: 1
});

const openEditWhModal = (wh) => {
  Object.assign(editWhForm, {
    id: wh.id,
    organization_id: wh.organization_id || (wh.organization ? wh.organization.id : ''),
    code: wh.code,
    name: wh.name,
    type: wh.type,
    address: wh.address || '',
    is_active: isWhActive(wh) ? 1 : 0
  });
  editWhModalOpen.value = true;
};

const submitEditWh = () => {
  const wh = warehouseList.value.find((w) => w.id === editWhForm.id);
  if (wh) {
    const parentOrg = orgList.value.find((o) => String(o.id) === String(editWhForm.organization_id));
    wh.organization_id = editWhForm.organization_id;
    wh.organization = parentOrg;
    wh.code = editWhForm.code.toUpperCase();
    wh.name = editWhForm.name;
    wh.type = editWhForm.type;
    wh.address = editWhForm.address;
    wh.is_active = editWhForm.is_active === 1;
    wh.isActive = editWhForm.is_active === 1;
    showAlert(`Perubahan data gudang ${wh.name} berhasil disimpan.`);
  }
  editWhModalOpen.value = false;
};

// 8. Delete Wh
const deleteWhModalOpen = ref(false);
const deleteWhForm = reactive({
  id: null,
  code: '',
  name: '',
  stock_count: 0
});

const openDeleteWhModal = (wh) => {
  Object.assign(deleteWhForm, {
    id: wh.id,
    code: wh.code,
    name: wh.name,
    stock_count: getWhStockCount(wh)
  });
  deleteWhModalOpen.value = true;
};

const submitDeleteWh = () => {
  if (deleteWhForm.stock_count > 0) {
    showAlert(`Gagal menghapus: Gudang "${deleteWhForm.name}" masih memiliki riwayat saldo persediaan. Nonaktifkan status operasional gudang sebagai gantinya.`, 'danger');
    deleteWhModalOpen.value = false;
    return;
  }
  warehouseList.value = warehouseList.value.filter((w) => w.id !== deleteWhForm.id);
  deleteWhModalOpen.value = false;
  showAlert(`Gudang ${deleteWhForm.name} telah berhasil dihapus.`);
};

// Print page
const printPage = () => {
  window.print();
};
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

.btn-action-icon {
  background: transparent;
  border: 1px solid transparent;
  border-radius: 0.25rem;
  padding: 0.25rem 0.45rem;
  font-size: 0.875rem;
  transition: all 0.15s ease-in-out;
  cursor: pointer;
}

.btn-action-icon:hover {
  background-color: var(--bs-secondary-bg);
  border-color: var(--bs-border-color);
}

.btn-xs {
  font-size: 0.725rem;
  padding: 0.15rem 0.35rem;
  line-height: 1.2;
  border-radius: 0.2rem;
}

.nav-pills-scroll {
  flex-wrap: nowrap;
  overflow-x: auto;
  scrollbar-width: thin;
}

.nav-pills .nav-link {
  border-radius: 0.375rem;
  transition: all 0.2s ease;
}

.nav-pills .nav-link:not(.active):hover {
  background-color: var(--bs-secondary-bg);
  color: var(--bs-body-color) !important;
}

@media print {
  .app-content-header,
  .card-header,
  .card-body.bg-body-tertiary,
  .btn-action-icon,
  .card-footer {
    display: none !important;
  }
  .card {
    border: none !important;
    box-shadow: none !important;
  }
  .table-responsive {
    overflow: visible !important;
  }
}
</style>

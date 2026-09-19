<template>
  <div class="accounting-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Data Accounting</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item text-secondary">Master Data</li>
              <li class="breadcrumb-item active text-body fw-semibold" aria-current="page">Accounting & Cost Center</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. Main Tabbed Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs (Hanya Label Tanpa Icon dan Tanpa Jumlah Record) -->
      <div class="card-header bg-body p-2 px-3 border-bottom d-flex flex-column flex-md-row align-items-stretch align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills nav-pills-scroll m-0" role="tablist" aria-label="Tabs Master Accounting">
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('coa')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'coa' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Chart of Account (Rekening GL)
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('cost_centers')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'cost_centers' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Master Cost Center
            </button>
          </li>
        </ul>

        <!-- Right: Action Buttons -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <template v-if="activeMainTab === 'coa'">
            <button
              type="button"
              @click="openCreateCoaModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <template v-if="activeMainTab === 'cost_centers'">
            <button
              type="button"
              @click="openCreateCcModal"
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

      <!-- ==================== TAB 1: CHART OF ACCOUNTS (REKENING GL) ==================== -->
      <div v-show="activeMainTab === 'coa'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Type Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tag"></i></span>
                <select v-model="coaType" @change="coaPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Tipe Akun</option>
                  <option value="ASSET">Aset (Asset)</option>
                  <option value="LIABILITY">Kewajiban (Liability)</option>
                  <option value="EQUITY">Ekuitas / RAK (Equity)</option>
                  <option value="REVENUE">Pendapatan (Revenue)</option>
                  <option value="EXPENSE">Beban (Expense)</option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="coaStatus" @change="coaPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Search Box -->
            <div class="col-12 col-sm-6 col-md">
              <div class="input-group input-group-sm">
                <input
                  type="text"
                  v-model="coaSearch"
                  @input="coaPage = 1"
                  placeholder="Cari kode akun, nama, klasifikasi..."
                  class="form-control form-control-sm border-end-0 fs-8"
                />
                <button type="button" class="btn btn-sm btn-danger px-3" @click="coaPage = 1">
                  <i class="bi bi-search"></i>
                </button>
              </div>
            </div>

            <!-- Reset Filter -->
            <div v-if="coaSearch || coaType !== 'ALL' || coaStatus !== 'ALL'" class="col-12 col-sm-auto">
              <button type="button" @click="resetCoaFilters" class="btn btn-sm btn-outline-secondary w-100 fs-8">
                Reset
              </button>
            </div>
          </div>
        </div>

        <!-- Table Responsive -->
        <div class="table-responsive">
          <table class="table table-striped table-hover align-middle mb-0 fs-7">
            <thead class="table-light text-secondary border-bottom">
              <tr>
                <th class="py-2.5 px-3" style="width: 140px;">Kode Rekening</th>
                <th class="py-2.5 px-3">Nama Akun Buku Besar (GL)</th>
                <th class="py-2.5 px-3">Tipe Akun</th>
                <th class="py-2.5 px-3">Klasifikasi</th>
                <th class="py-2.5 px-3 text-center" style="width: 120px;">Saldo Normal</th>
                <th class="py-2.5 px-3 text-center" style="width: 100px;">Status</th>
                <th class="py-2.5 px-3 text-center" style="width: 140px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="coa in paginatedCoaList" :key="coa.id || coa.account_code">
                <td class="py-2.5 px-3 font-monospace fw-bold text-dark fs-8">
                  {{ coa.account_code }}
                </td>
                <td class="py-2.5 px-3">
                  <div class="fw-semibold text-dark">{{ coa.account_name }}</div>
                  <div v-if="coa.description" class="fs-9 text-muted text-truncate" style="max-width: 320px;">
                    {{ coa.description }}
                  </div>
                </td>
                <td class="py-2.5 px-3">
                  <span class="badge px-2 py-0.5 fs-9 fw-semibold" :class="getAccountTypeBadgeClass(coa.account_type)">
                    {{ coa.account_type }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-secondary fs-8">
                  {{ coa.classification || '-' }}
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span
                    class="badge px-2 py-0.5 fs-9 font-monospace fw-bold"
                    :class="coa.normal_balance === 'DEBIT' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-warning-subtle text-warning border border-warning-subtle'"
                  >
                    {{ coa.normal_balance }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span
                    class="badge px-2 py-0.5 fs-9"
                    :class="coa.is_active ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'"
                  >
                    {{ coa.is_active ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <div class="d-inline-flex align-items-center gap-1">
                    <!-- View Button -->
                    <button
                      type="button"
                      @click="openViewCoaModal(coa)"
                      class="btn btn-sm btn-outline-secondary btn-action-icon"
                      title="Lihat Rincian Akun"
                    >
                      <i class="bi bi-eye"></i>
                    </button>

                    <!-- Edit Button -->
                    <button
                      type="button"
                      @click="openEditCoaModal(coa)"
                      class="btn btn-sm btn-outline-primary btn-action-icon"
                      title="Edit Akun GL"
                    >
                      <i class="bi bi-pencil-square"></i>
                    </button>

                    <!-- Toggle Status Button -->
                    <button
                      type="button"
                      @click="toggleCoaStatus(coa)"
                      class="btn btn-sm btn-action-icon"
                      :class="coa.is_active ? 'btn-outline-warning' : 'btn-outline-success'"
                      :title="coa.is_active ? 'Nonaktifkan Akun' : 'Aktifkan Akun'"
                    >
                      <i class="bi" :class="coa.is_active ? 'bi-toggle2-on' : 'bi-toggle2-off'"></i>
                    </button>

                    <!-- Delete Button -->
                    <button
                      type="button"
                      @click="openDeleteCoaModal(coa)"
                      class="btn btn-sm btn-outline-danger btn-action-icon"
                      title="Hapus Akun GL"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="paginatedCoaList.length === 0">
                <td colspan="7" class="text-center py-5 text-secondary">
                  <i class="bi bi-journal-x fs-1 d-block mb-2 text-muted"></i>
                  <div class="fw-semibold">Tidak ada Rekening Akun GL yang ditemukan.</div>
                  <div class="fs-8 text-muted mt-1">Gunakan tombol "Tambah Rekening GL" untuk mendaftarkan akun baru.</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredCoaList.length"
          v-model:currentPage="coaPage"
          v-model:perPage="coaPerPage"
        />
      </div>

      <!-- ==================== TAB 2: MASTER COST CENTER ==================== -->
      <div v-show="activeMainTab === 'cost_centers'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Organization Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-building"></i></span>
                <select v-model="ccOrgId" @change="ccPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Unit Kerja</option>
                  <option v-for="org in organizationList" :key="org.id" :value="org.id">
                    {{ org.code }} - {{ org.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="ccStatus" @change="ccPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Search Box -->
            <div class="col-12 col-sm-6 col-md">
              <div class="input-group input-group-sm">
                <input
                  type="text"
                  v-model="ccSearch"
                  @input="ccPage = 1"
                  placeholder="Cari kode CC, nama, bagian, PIC..."
                  class="form-control form-control-sm border-end-0 fs-8"
                />
                <button type="button" class="btn btn-sm btn-danger px-3" @click="ccPage = 1">
                  <i class="bi bi-search"></i>
                </button>
              </div>
            </div>

            <!-- Reset Filter -->
            <div v-if="ccSearch || ccOrgId !== 'ALL' || ccStatus !== 'ALL'" class="col-12 col-sm-auto">
              <button type="button" @click="resetCcFilters" class="btn btn-sm btn-outline-secondary w-100 fs-8">
                Reset
              </button>
            </div>
          </div>
        </div>

        <!-- Table Responsive -->
        <div class="table-responsive">
          <table class="table table-striped table-hover align-middle mb-0 fs-7">
            <thead class="table-light text-secondary border-bottom">
              <tr>
                <th class="py-2.5 px-3" style="width: 140px;">Kode Cost Center</th>
                <th class="py-2.5 px-3">Nama Pusat Biaya (Cost Center)</th>
                <th class="py-2.5 px-3">Unit Kerja Terkait</th>
                <th class="py-2.5 px-3">Departemen / Bagian</th>
                <th class="py-2.5 px-3">Penanggung Jawab (PIC)</th>
                <th class="py-2.5 px-3 text-center" style="width: 100px;">Status</th>
                <th class="py-2.5 px-3 text-center" style="width: 140px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cc in paginatedCcList" :key="cc.id || cc.code">
                <td class="py-2.5 px-3 font-monospace fw-bold text-danger fs-8">
                  {{ cc.code }}
                </td>
                <td class="py-2.5 px-3">
                  <div class="fw-semibold text-dark">{{ cc.name }}</div>
                  <div v-if="cc.notes" class="fs-9 text-muted text-truncate" style="max-width: 280px;">
                    {{ cc.notes }}
                  </div>
                </td>
                <td class="py-2.5 px-3">
                  <div class="text-dark">{{ getOrgName(cc.organization_id) }}</div>
                  <div class="fs-9 text-muted font-monospace">{{ getOrgCode(cc.organization_id) }}</div>
                </td>
                <td class="py-2.5 px-3 text-secondary fs-8">
                  {{ cc.department || '-' }}
                </td>
                <td class="py-2.5 px-3 text-secondary fs-8">
                 {{ cc.pic_name || '-' }}
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span
                    class="badge px-2 py-0.5 fs-9"
                    :class="cc.is_active ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'"
                  >
                    {{ cc.is_active ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <div class="d-inline-flex align-items-center gap-1">
                    <!-- View Button -->
                    <button
                      type="button"
                      @click="openViewCcModal(cc)"
                      class="btn btn-sm btn-outline-secondary btn-action-icon"
                      title="Lihat Rincian Cost Center"
                    >
                      <i class="bi bi-eye"></i>
                    </button>

                    <!-- Edit Button -->
                    <button
                      type="button"
                      @click="openEditCcModal(cc)"
                      class="btn btn-sm btn-outline-primary btn-action-icon"
                      title="Edit Cost Center"
                    >
                      <i class="bi bi-pencil-square"></i>
                    </button>

                    <!-- Toggle Status Button -->
                    <button
                      type="button"
                      @click="toggleCcStatus(cc)"
                      class="btn btn-sm btn-action-icon"
                      :class="cc.is_active ? 'btn-outline-warning' : 'btn-outline-success'"
                      :title="cc.is_active ? 'Nonaktifkan Cost Center' : 'Aktifkan Cost Center'"
                    >
                      <i class="bi" :class="cc.is_active ? 'bi-toggle2-on' : 'bi-toggle2-off'"></i>
                    </button>

                    <!-- Delete Button -->
                    <button
                      type="button"
                      @click="openDeleteCcModal(cc)"
                      class="btn btn-sm btn-outline-danger btn-action-icon"
                      title="Hapus Cost Center"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="paginatedCcList.length === 0">
                <td colspan="7" class="text-center py-5 text-secondary">
                  <i class="bi bi-diagram-3 fs-1 d-block mb-2 text-muted"></i>
                  <div class="fw-semibold">Tidak ada Cost Center yang ditemukan.</div>
                  <div class="fs-8 text-muted mt-1">Gunakan tombol "Tambah Cost Center" untuk mendaftarkan pusat biaya baru.</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredCcList.length"
          v-model:currentPage="ccPage"
          v-model:perPage="ccPerPage"
        />
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: CHART OF ACCOUNTS (COA) -->
    <!-- ========================================================================= -->

    <!-- Modal Create CoA -->
    <div v-if="createCoaModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Tambah Rekening Akun GL Baru</h6>
            <button type="button" class="btn-close" @click="createCoaModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitCreateCoa">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Kode Akun / Rekening GL <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="createCoaForm.account_code"
                  required
                  placeholder="Contoh: 11301"
                  class="form-control form-control-sm fs-8 font-monospace"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Nama Akun Buku Besar <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="createCoaForm.account_name"
                  required
                  placeholder="Contoh: Persediaan Alat Tulis Kantor"
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Tipe Akun <span class="text-danger">*</span></label>
                  <select v-model="createCoaForm.account_type" required class="form-select form-select-sm fs-8">
                    <option value="ASSET">ASSET (Aset)</option>
                    <option value="LIABILITY">LIABILITY (Kewajiban)</option>
                    <option value="EQUITY">EQUITY (Ekuitas/RAK)</option>
                    <option value="REVENUE">REVENUE (Pendapatan)</option>
                    <option value="EXPENSE">EXPENSE (Beban)</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Saldo Normal <span class="text-danger">*</span></label>
                  <select v-model="createCoaForm.normal_balance" required class="form-select form-select-sm fs-8 font-monospace">
                    <option value="DEBIT">DEBIT</option>
                    <option value="CREDIT">CREDIT</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Klasifikasi Akun</label>
                <input
                  type="text"
                  v-model="createCoaForm.classification"
                  placeholder="Contoh: Aset Lancar / Beban Operasional"
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Deskripsi / Peruntukan Akun</label>
                <textarea
                  v-model="createCoaForm.description"
                  rows="2"
                  placeholder="Keterangan peruntukan posting akun ini..."
                  class="form-control form-control-sm fs-8"
                ></textarea>
              </div>
            </div>
            <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="createCoaModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Edit CoA -->
    <div v-if="editCoaModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Edit Rekening Akun GL</h6>
            <button type="button" class="btn-close" @click="editCoaModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitEditCoa">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Kode Akun / Rekening GL <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="editCoaForm.account_code"
                  required
                  class="form-control form-control-sm fs-8 font-monospace"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Nama Akun Buku Besar <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="editCoaForm.account_name"
                  required
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Tipe Akun <span class="text-danger">*</span></label>
                  <select v-model="editCoaForm.account_type" required class="form-select form-select-sm fs-8">
                    <option value="ASSET">ASSET (Aset)</option>
                    <option value="LIABILITY">LIABILITY (Kewajiban)</option>
                    <option value="EQUITY">EQUITY (Ekuitas/RAK)</option>
                    <option value="REVENUE">REVENUE (Pendapatan)</option>
                    <option value="EXPENSE">EXPENSE (Beban)</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Saldo Normal <span class="text-danger">*</span></label>
                  <select v-model="editCoaForm.normal_balance" required class="form-select form-select-sm fs-8 font-monospace">
                    <option value="DEBIT">DEBIT</option>
                    <option value="CREDIT">CREDIT</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Klasifikasi Akun</label>
                <input
                  type="text"
                  v-model="editCoaForm.classification"
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Deskripsi / Peruntukan Akun</label>
                <textarea
                  v-model="editCoaForm.description"
                  rows="2"
                  class="form-control form-control-sm fs-8"
                ></textarea>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Status Akun</label>
                <select v-model="editCoaForm.is_active" class="form-select form-select-sm fs-8">
                  <option :value="true">Aktif</option>
                  <option :value="false">Non-Aktif</option>
                </select>
              </div>
            </div>
            <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="editCoaModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal View CoA -->
    <div v-if="viewCoaModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Rincian Rekening Akun GL</h6>
            <button type="button" class="btn-close" @click="viewCoaModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-2.5 fs-8" v-if="selectedCoa">
            <div class="p-3 bg-body-tertiary rounded border space-y-2">
              <div>
                <span class="text-secondary">Kode Rekening:</span>
                <div class="fw-bold font-monospace text-dark fs-6">{{ selectedCoa.account_code }}</div>
              </div>
              <div>
                <span class="text-secondary">Nama Akun GL:</span>
                <div class="fw-bold text-dark fs-7">{{ selectedCoa.account_name }}</div>
              </div>
              <div class="row g-2 pt-1 border-top">
                <div class="col-6">
                  <span class="text-secondary">Tipe Akun:</span>
                  <div class="fw-bold text-dark">{{ selectedCoa.account_type }}</div>
                </div>
                <div class="col-6">
                  <span class="text-secondary">Saldo Normal:</span>
                  <div class="fw-bold font-monospace text-dark">{{ selectedCoa.normal_balance }}</div>
                </div>
              </div>
              <div>
                <span class="text-secondary">Klasifikasi:</span>
                <div class="fw-semibold text-dark">{{ selectedCoa.classification || '-' }}</div>
              </div>
              <div>
                <span class="text-secondary">Status:</span>
                <div>
                  <span
                    class="badge"
                    :class="selectedCoa.is_active ? 'text-bg-success' : 'text-bg-secondary'"
                  >
                    {{ selectedCoa.is_active ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </div>
              </div>
            </div>
            <div v-if="selectedCoa.description" class="p-2.5 bg-body-tertiary rounded border">
              <strong class="text-dark">Deskripsi / Peruntukan:</strong>
              <p class="text-secondary mb-0 mt-0.5">{{ selectedCoa.description }}</p>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="viewCoaModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Delete CoA -->
    <div v-if="deleteCoaModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <h6 class="modal-title fw-bold mb-0 text-danger">Konfirmasi Hapus Akun GL</h6>
            <button type="button" class="btn-close" @click="deleteCoaModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedCoa">
            <p class="text-secondary mb-0">Apakah Anda yakin ingin menghapus rekening akun buku besar ini dari master data?</p>
            <div class="p-3 bg-body-tertiary rounded border">
              <div><strong>Kode Akun:</strong> <span class="font-monospace fw-bold text-dark">{{ selectedCoa.account_code }}</span></div>
              <div><strong>Nama Akun:</strong> <span class="fw-semibold text-dark">{{ selectedCoa.account_name }}</span></div>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="deleteCoaModal = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger shadow-xs d-inline-flex align-items-center gap-1" @click="submitDeleteCoa">
              <span>Ya, Hapus Akun</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: COST CENTER -->
    <!-- ========================================================================= -->

    <!-- Modal Create Cost Center -->
    <div v-if="createCcModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Tambah Master Cost Center Baru</h6>
            <button type="button" class="btn-close" @click="createCcModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitCreateCc">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Kode Cost Center <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="createCcForm.code"
                  required
                  placeholder="Contoh: CC-KC-SBY"
                  class="form-control form-control-sm fs-8 font-monospace text-uppercase"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Nama Pusat Biaya (Cost Center) <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="createCcForm.name"
                  required
                  placeholder="Contoh: Cost Center Cabang Utama Surabaya"
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Unit Kerja / Cabang Terkait</label>
                <select v-model="createCcForm.organization_id" class="form-select form-select-sm fs-8">
                  <option value="">-- Tanpa Relasi Unit Khusus --</option>
                  <option v-for="org in organizationList" :key="org.id" :value="org.id">
                    {{ org.code }} - {{ org.name }}
                  </option>
                </select>
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Departemen / Bagian</label>
                  <input
                    type="text"
                    v-model="createCcForm.department"
                    placeholder="Contoh: Bagian Operasional"
                    class="form-control form-control-sm fs-8"
                  />
                </div>
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Penanggung Jawab (PIC)</label>
                  <input
                    type="text"
                    v-model="createCcForm.pic_name"
                    placeholder="Nama PIC..."
                    class="form-control form-control-sm fs-8"
                  />
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Catatan / Keterangan</label>
                <textarea
                  v-model="createCcForm.notes"
                  rows="2"
                  placeholder="Catatan peruntukan cost center..."
                  class="form-control form-control-sm fs-8"
                ></textarea>
              </div>
            </div>
            <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="createCcModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Edit Cost Center -->
    <div v-if="editCcModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Edit Cost Center</h6>
            <button type="button" class="btn-close" @click="editCcModal = false" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitEditCc">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Kode Cost Center <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="editCcForm.code"
                  required
                  class="form-control form-control-sm fs-8 font-monospace text-uppercase"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Nama Pusat Biaya (Cost Center) <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="editCcForm.name"
                  required
                  class="form-control form-control-sm fs-8"
                />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Unit Kerja / Cabang Terkait</label>
                <select v-model="editCcForm.organization_id" class="form-select form-select-sm fs-8">
                  <option value="">-- Tanpa Relasi Unit Khusus --</option>
                  <option v-for="org in organizationList" :key="org.id" :value="org.id">
                    {{ org.code }} - {{ org.name }}
                  </option>
                </select>
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Departemen / Bagian</label>
                  <input
                    type="text"
                    v-model="editCcForm.department"
                    class="form-control form-control-sm fs-8"
                  />
                </div>
                <div class="col-6">
                  <label class="form-label fs-8 fw-bold text-secondary mb-1">Penanggung Jawab (PIC)</label>
                  <input
                    type="text"
                    v-model="editCcForm.pic_name"
                    class="form-control form-control-sm fs-8"
                  />
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Catatan / Keterangan</label>
                <textarea
                  v-model="editCcForm.notes"
                  rows="2"
                  class="form-control form-control-sm fs-8"
                ></textarea>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary mb-1">Status</label>
                <select v-model="editCcForm.is_active" class="form-select form-select-sm fs-8">
                  <option :value="true">Aktif</option>
                  <option :value="false">Non-Aktif</option>
                </select>
              </div>
            </div>
            <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
              <button type="button" class="btn btn-sm btn-outline-secondary" @click="editCcModal = false">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal View Cost Center -->
    <div v-if="viewCcModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fw-bold mb-0 text-body">Rincian Cost Center</h6>
            <button type="button" class="btn-close" @click="viewCcModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-2.5 fs-8" v-if="selectedCc">
            <div class="p-3 bg-body-tertiary rounded border space-y-2">
              <div>
                <span class="text-secondary">Kode Cost Center:</span>
                <div class="fw-bold font-monospace text-danger fs-6">{{ selectedCc.code }}</div>
              </div>
              <div>
                <span class="text-secondary">Nama Cost Center:</span>
                <div class="fw-bold text-dark fs-7">{{ selectedCc.name }}</div>
              </div>
              <div>
                <span class="text-secondary">Unit Kerja:</span>
                <div class="fw-semibold text-dark">
                  {{ getOrgCode(selectedCc.organization_id) ? (getOrgCode(selectedCc.organization_id) + ' - ' + getOrgName(selectedCc.organization_id)) : '-' }}
                </div>
              </div>
              <div class="row g-2 pt-1 border-top">
                <div class="col-6">
                  <span class="text-secondary">Departemen:</span>
                  <div class="fw-semibold text-dark">{{ selectedCc.department || '-' }}</div>
                </div>
                <div class="col-6">
                  <span class="text-secondary">PIC:</span>
                  <div class="fw-semibold text-dark">{{ selectedCc.pic_name || '-' }}</div>
                </div>
              </div>
              <div>
                <span class="text-secondary">Status:</span>
                <div>
                  <span
                    class="badge"
                    :class="selectedCc.is_active ? 'text-bg-success' : 'text-bg-secondary'"
                  >
                    {{ selectedCc.is_active ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </div>
              </div>
            </div>
            <div v-if="selectedCc.notes" class="p-2.5 bg-body-tertiary rounded border">
              <strong class="text-dark">Catatan / Keterangan:</strong>
              <p class="text-secondary mb-0 mt-0.5">{{ selectedCc.notes }}</p>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="viewCcModal = false">Tutup</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Delete Cost Center -->
    <div v-if="deleteCcModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border border-secondary-subtle shadow-2xl">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <h6 class="modal-title fw-bold mb-0 text-danger">Konfirmasi Hapus Cost Center</h6>
            <button type="button" class="btn-close" @click="deleteCcModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3 fs-8" v-if="selectedCc">
            <p class="text-secondary mb-0">Apakah Anda yakin ingin menghapus Cost Center ini dari master data?</p>
            <div class="p-3 bg-body-tertiary rounded border">
              <div><strong>Kode Cost Center:</strong> <span class="font-monospace fw-bold text-danger">{{ selectedCc.code }}</span></div>
              <div><strong>Nama:</strong> <span class="fw-semibold text-dark">{{ selectedCc.name }}</span></div>
            </div>
          </div>
          <div class="modal-footer bg-body-tertiary d-flex justify-content-end gap-2 py-2.5 px-4 border-top">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="deleteCcModal = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger shadow-xs d-inline-flex align-items-center gap-1" @click="submitDeleteCc">
              <span>Ya, Hapus Cost Center</span>
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
const validTabs = ['coa', 'cost_centers'];
const activeMainTab = ref('coa');

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
// 3. Master Data Lists
// ==========================================
const organizationList = ref([]);

// Default initial banking standard Chart of Accounts
const defaultCoaList = [
  { id: 1, account_code: '11101', account_name: 'Kas Besar Kantor Pusat', account_type: 'ASSET', classification: 'Kas & Setara Kas', normal_balance: 'DEBIT', description: 'Kas fisik vault utama kantor operasional', is_active: true },
  { id: 2, account_code: '11301', account_name: 'Persediaan Alat Tulis Kantor (ATK)', account_type: 'ASSET', classification: 'Aset Lancar Logistik', normal_balance: 'DEBIT', description: 'Persediaan perlengkapan kantor dan ATK seluruh cabang', is_active: true },
  { id: 3, account_code: '11302', account_name: 'Persediaan Formulir Produk & Kliring', account_type: 'ASSET', classification: 'Aset Lancar Sekuriti', normal_balance: 'DEBIT', description: 'Persediaan cetakan produk bilyet giro dan cek sekuriti', is_active: true },
  { id: 4, account_code: '11303', account_name: 'Persediaan Blanko Kartu ATM & Chip Debit', account_type: 'ASSET', classification: 'Aset Lancar Kartu', normal_balance: 'DEBIT', description: 'Kartu debit chip belum diemboss di gudang kartu', is_active: true },
  { id: 5, account_code: '21101', account_name: 'Hutang Dagang Rekanan Pengadaan', account_type: 'LIABILITY', classification: 'Kewajiban Lancar', normal_balance: 'CREDIT', description: 'Kewajiban jangka pendek terhadap vendor logistik', is_active: true },
  { id: 6, account_code: '31101', account_name: 'Modal Saham Ditempatkan', account_type: 'EQUITY', classification: 'Ekuitas Utama', normal_balance: 'CREDIT', description: 'Modal disetor perbankan', is_active: true },
  { id: 7, account_code: '41101', account_name: 'Pendapatan Bunga dan Jasa Giro', account_type: 'REVENUE', classification: 'Pendapatan Operasional', normal_balance: 'CREDIT', description: 'Pendapatan operasional perbankan', is_active: true },
  { id: 8, account_code: '52101', account_name: 'Beban Pemeliharaan & Operasional Kantor', account_type: 'EXPENSE', classification: 'Beban Operasional', normal_balance: 'DEBIT', description: 'Beban pemeliharaan logistik dan perkantoran', is_active: true },
  { id: 9, account_code: '52102', account_name: 'Beban Ekspedisi & Pengiriman Logistik', account_type: 'EXPENSE', classification: 'Beban Distribusi', normal_balance: 'DEBIT', description: 'Ongkos kirim ekspedisi antar cabang dan unit kerja', is_active: true }
];

// Default initial banking standard Cost Centers
const defaultCostCenters = [
  { id: 1, code: 'CC-KP-LOG', name: 'Cost Center Divisi Logistik Kantor Pusat', organization_id: 1, department: 'Divisi Logistik & Pengadaan', pic_name: 'Bambang Soeharto', notes: 'Pusat biaya logistik dan pergudangan utama', is_active: true },
  { id: 2, code: 'CC-KP-IT', name: 'Cost Center Divisi Teknologi Informasi', organization_id: 1, department: 'Divisi IT & Infrastruktur', pic_name: 'Rahmat Hidayat', notes: 'Pusat biaya pengadaan hardware dan sistem TI', is_active: true },
  { id: 3, code: 'CC-KC-SBY', name: 'Cost Center Kantor Cabang Utama Surabaya', organization_id: 2, department: 'Operasional Cabang', pic_name: 'Siti Aminah', notes: 'Biaya operasional kantor cabang utama', is_active: true },
  { id: 4, code: 'CC-KC-MLG', name: 'Cost Center Kantor Cabang Malang', organization_id: 3, department: 'Operasional Cabang', pic_name: 'Doni Pratama', notes: 'Biaya operasional kantor cabang Malang', is_active: true }
];

const coaList = ref([...defaultCoaList]);
const costCenterList = ref([...defaultCostCenters]);

// Helper for organization info
const getOrgName = (orgId) => {
  if (!orgId) return '-';
  const org = organizationList.value.find((o) => String(o.id) === String(orgId));
  return org ? org.name : `Unit #${orgId}`;
};

const getOrgCode = (orgId) => {
  if (!orgId) return '';
  const org = organizationList.value.find((o) => String(o.id) === String(orgId));
  return org ? org.code : '';
};

const getAccountTypeBadgeClass = (type) => {
  switch (type) {
    case 'ASSET':
      return 'text-bg-success';
    case 'LIABILITY':
      return 'text-bg-warning';
    case 'EQUITY':
      return 'text-bg-info';
    case 'REVENUE':
      return 'text-bg-primary';
    case 'EXPENSE':
      return 'text-bg-danger';
    default:
      return 'text-bg-secondary';
  }
};

// ==========================================
// 4. Filters & Pagination: CoA
// ==========================================
const coaType = ref('ALL');
const coaStatus = ref('ALL');
const coaSearch = ref('');
const coaPage = ref(1);
const coaPerPage = ref(10);

const resetCoaFilters = () => {
  coaType.value = 'ALL';
  coaStatus.value = 'ALL';
  coaSearch.value = '';
  coaPage.value = 1;
};

const filteredCoaList = computed(() => {
  let list = [...coaList.value];
  if (coaType.value !== 'ALL') {
    list = list.filter((c) => c.account_type === coaType.value);
  }
  if (coaStatus.value === 'ACTIVE') {
    list = list.filter((c) => c.is_active);
  } else if (coaStatus.value === 'INACTIVE') {
    list = list.filter((c) => !c.is_active);
  }
  if (coaSearch.value.trim()) {
    const q = coaSearch.value.trim().toLowerCase();
    list = list.filter(
      (c) =>
        (c.account_code && c.account_code.toLowerCase().includes(q)) ||
        (c.account_name && c.account_name.toLowerCase().includes(q)) ||
        (c.classification && c.classification.toLowerCase().includes(q)) ||
        (c.description && c.description.toLowerCase().includes(q))
    );
  }
  return list;
});

const paginatedCoaList = computed(() => {
  const start = (coaPage.value - 1) * coaPerPage.value;
  return filteredCoaList.value.slice(start, start + coaPerPage.value);
});

// ==========================================
// 5. Filters & Pagination: Cost Centers
// ==========================================
const ccOrgId = ref('ALL');
const ccStatus = ref('ALL');
const ccSearch = ref('');
const ccPage = ref(1);
const ccPerPage = ref(10);

const resetCcFilters = () => {
  ccOrgId.value = 'ALL';
  ccStatus.value = 'ALL';
  ccSearch.value = '';
  ccPage.value = 1;
};

const filteredCcList = computed(() => {
  let list = [...costCenterList.value];
  if (ccOrgId.value !== 'ALL') {
    list = list.filter((cc) => String(cc.organization_id) === String(ccOrgId.value));
  }
  if (ccStatus.value === 'ACTIVE') {
    list = list.filter((cc) => cc.is_active);
  } else if (ccStatus.value === 'INACTIVE') {
    list = list.filter((cc) => !cc.is_active);
  }
  if (ccSearch.value.trim()) {
    const q = ccSearch.value.trim().toLowerCase();
    list = list.filter(
      (cc) =>
        (cc.code && cc.code.toLowerCase().includes(q)) ||
        (cc.name && cc.name.toLowerCase().includes(q)) ||
        (cc.department && cc.department.toLowerCase().includes(q)) ||
        (cc.pic_name && cc.pic_name.toLowerCase().includes(q)) ||
        (cc.notes && cc.notes.toLowerCase().includes(q)) ||
        getOrgName(cc.organization_id).toLowerCase().includes(q) ||
        getOrgCode(cc.organization_id).toLowerCase().includes(q)
    );
  }
  return list;
});

const paginatedCcList = computed(() => {
  const start = (ccPage.value - 1) * ccPerPage.value;
  return filteredCcList.value.slice(start, start + ccPerPage.value);
});

// ==========================================
// 6. Modal States & CRUD: Chart of Accounts
// ==========================================
const createCoaModal = ref(false);
const editCoaModal = ref(false);
const viewCoaModal = ref(false);
const deleteCoaModal = ref(false);
const selectedCoa = ref(null);

const createCoaForm = reactive({
  account_code: '',
  account_name: '',
  account_type: 'ASSET',
  normal_balance: 'DEBIT',
  classification: '',
  description: ''
});

const editCoaForm = reactive({
  id: null,
  account_code: '',
  account_name: '',
  account_type: 'ASSET',
  normal_balance: 'DEBIT',
  classification: '',
  description: '',
  is_active: true
});

const openCreateCoaModal = () => {
  Object.assign(createCoaForm, {
    account_code: '',
    account_name: '',
    account_type: 'ASSET',
    normal_balance: 'DEBIT',
    classification: '',
    description: ''
  });
  createCoaModal.value = true;
};

const submitCreateCoa = () => {
  const newId = coaList.value.length ? Math.max(...coaList.value.map((c) => c.id || 0)) + 1 : 1;
  const newCoa = {
    id: newId,
    account_code: createCoaForm.account_code.trim(),
    account_name: createCoaForm.account_name.trim(),
    account_type: createCoaForm.account_type,
    normal_balance: createCoaForm.normal_balance,
    classification: createCoaForm.classification.trim(),
    description: createCoaForm.description.trim(),
    is_active: true
  };
  coaList.value.unshift(newCoa);
  createCoaModal.value = false;
  showAlert(`Rekening Akun GL "${newCoa.account_code} - ${newCoa.account_name}" berhasil ditambahkan.`);
};

const openEditCoaModal = (coa) => {
  selectedCoa.value = coa;
  Object.assign(editCoaForm, {
    id: coa.id,
    account_code: coa.account_code,
    account_name: coa.account_name,
    account_type: coa.account_type,
    normal_balance: coa.normal_balance,
    classification: coa.classification || '',
    description: coa.description || '',
    is_active: Boolean(coa.is_active)
  });
  editCoaModal.value = true;
};

const submitEditCoa = () => {
  const index = coaList.value.findIndex((c) => c.id === editCoaForm.id);
  if (index !== -1) {
    coaList.value[index] = {
      ...coaList.value[index],
      account_code: editCoaForm.account_code.trim(),
      account_name: editCoaForm.account_name.trim(),
      account_type: editCoaForm.account_type,
      normal_balance: editCoaForm.normal_balance,
      classification: editCoaForm.classification.trim(),
      description: editCoaForm.description.trim(),
      is_active: editCoaForm.is_active
    };
    editCoaModal.value = false;
    showAlert(`Rekening Akun GL "${editCoaForm.account_code}" berhasil diperbarui.`);
  }
};

const openViewCoaModal = (coa) => {
  selectedCoa.value = coa;
  viewCoaModal.value = true;
};

const toggleCoaStatus = (coa) => {
  coa.is_active = !coa.is_active;
  showAlert(`Status Akun GL "${coa.account_code}" diubah menjadi ${coa.is_active ? 'Aktif' : 'Non-Aktif'}.`, 'warning');
};

const openDeleteCoaModal = (coa) => {
  selectedCoa.value = coa;
  deleteCoaModal.value = true;
};

const submitDeleteCoa = () => {
  if (selectedCoa.value) {
    coaList.value = coaList.value.filter((c) => c.id !== selectedCoa.value.id);
    deleteCoaModal.value = false;
    showAlert(`Rekening Akun GL "${selectedCoa.value.account_code}" telah dihapus.`, 'danger');
  }
};

// ==========================================
// 7. Modal States & CRUD: Cost Centers
// ==========================================
const createCcModal = ref(false);
const editCcModal = ref(false);
const viewCcModal = ref(false);
const deleteCcModal = ref(false);
const selectedCc = ref(null);

const createCcForm = reactive({
  code: '',
  name: '',
  organization_id: '',
  department: '',
  pic_name: '',
  notes: ''
});

const editCcForm = reactive({
  id: null,
  code: '',
  name: '',
  organization_id: '',
  department: '',
  pic_name: '',
  notes: '',
  is_active: true
});

const openCreateCcModal = () => {
  Object.assign(createCcForm, {
    code: '',
    name: '',
    organization_id: '',
    department: '',
    pic_name: '',
    notes: ''
  });
  createCcModal.value = true;
};

const submitCreateCc = () => {
  const newId = costCenterList.value.length ? Math.max(...costCenterList.value.map((c) => c.id || 0)) + 1 : 1;
  const newCc = {
    id: newId,
    code: createCcForm.code.trim().toUpperCase(),
    name: createCcForm.name.trim(),
    organization_id: createCcForm.organization_id ? Number(createCcForm.organization_id) : null,
    department: createCcForm.department.trim(),
    pic_name: createCcForm.pic_name.trim(),
    notes: createCcForm.notes.trim(),
    is_active: true
  };
  costCenterList.value.unshift(newCc);
  createCcModal.value = false;
  showAlert(`Cost Center "${newCc.code} - ${newCc.name}" berhasil ditambahkan.`);
};

const openEditCcModal = (cc) => {
  selectedCc.value = cc;
  Object.assign(editCcForm, {
    id: cc.id,
    code: cc.code,
    name: cc.name,
    organization_id: cc.organization_id || '',
    department: cc.department || '',
    pic_name: cc.pic_name || '',
    notes: cc.notes || '',
    is_active: Boolean(cc.is_active)
  });
  editCcModal.value = true;
};

const submitEditCc = () => {
  const index = costCenterList.value.findIndex((c) => c.id === editCcForm.id);
  if (index !== -1) {
    costCenterList.value[index] = {
      ...costCenterList.value[index],
      code: editCcForm.code.trim().toUpperCase(),
      name: editCcForm.name.trim(),
      organization_id: editCcForm.organization_id ? Number(editCcForm.organization_id) : null,
      department: editCcForm.department.trim(),
      pic_name: editCcForm.pic_name.trim(),
      notes: editCcForm.notes.trim(),
      is_active: editCcForm.is_active
    };
    editCcModal.value = false;
    showAlert(`Cost Center "${editCcForm.code}" berhasil diperbarui.`);
  }
};

const openViewCcModal = (cc) => {
  selectedCc.value = cc;
  viewCcModal.value = true;
};

const toggleCcStatus = (cc) => {
  cc.is_active = !cc.is_active;
  showAlert(`Status Cost Center "${cc.code}" diubah menjadi ${cc.is_active ? 'Aktif' : 'Non-Aktif'}.`, 'warning');
};

const openDeleteCcModal = (cc) => {
  selectedCc.value = cc;
  deleteCcModal.value = true;
};

const submitDeleteCc = () => {
  if (selectedCc.value) {
    costCenterList.value = costCenterList.value.filter((c) => c.id !== selectedCc.value.id);
    deleteCcModal.value = false;
    showAlert(`Cost Center "${selectedCc.value.code}" telah dihapus.`, 'danger');
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
    const res = await api.get('/master/organizations');
    const orgs = extractList(res);
    if (orgs && Array.isArray(orgs)) {
      organizationList.value = orgs;
    }
  } catch (err) {
    console.warn('Gagal memuat unit kerja untuk filter cost center:', err);
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

.space-y-2\.5 > * + * {
  margin-top: 0.625rem;
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

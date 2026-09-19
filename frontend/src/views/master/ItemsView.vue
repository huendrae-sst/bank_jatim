<template>
  <div class="items-page space-y-3">
    <!-- 1. Page Header & Breadcrumbs -->
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs">
      <div class="container-fluid p-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Master Data Barang</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/master/items" class="text-decoration-none text-danger">Master Data</router-link>
              </li>
              <li class="breadcrumb-item active text-body fw-semibold" aria-current="page">Master Barang</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. Main Card Container -->
    <div class="card card-outline card-danger shadow-xs">
      <!-- Card Header with Navigation Tabs (Hanya Label Tanpa Icon dan Tanpa Jumlah Record) -->
      <div class="card-header bg-body p-2 px-3 border-bottom d-flex flex-column flex-md-row align-items-stretch align-items-md-center justify-content-between gap-2">
        <ul class="nav nav-pills card-header-pills nav-pills-scroll m-0" role="tablist" aria-label="Tabs Master Barang">
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('items')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'items' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Master Barang
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('categories')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'categories' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Kategori Barang
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('uoms')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'uoms' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Satuan Unit
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              @click="switchTab('conversions')"
              class="nav-link py-1.5 px-3 fs-8 fw-bold text-nowrap cursor-pointer"
              :class="activeMainTab === 'conversions' ? 'active bg-danger text-white' : 'text-body-secondary'"
            >
              Konversi Satuan
            </button>
          </li>
        </ul>

        <!-- Right: Action Buttons -->
        <div class="card-tools ms-md-auto d-flex align-items-center gap-2">
          <template v-if="activeMainTab === 'items'">
            <button
              type="button"
              @click="openCreateItemModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <template v-if="activeMainTab === 'categories'">
            <button
              type="button"
              @click="openCreateCategoryModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <template v-if="activeMainTab === 'uoms'">
            <button
              type="button"
              @click="openCreateUomModal"
              class="btn btn-sm btn-danger fw-bold shadow-xs d-inline-flex align-items-center gap-1 fs-8"
            >
              Tambah
            </button>
          </template>
          <template v-if="activeMainTab === 'conversions'">
            <button
              type="button"
              @click="openCreateConversionModal"
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

      <!-- ==================== TAB 1: MASTER BARANG ==================== -->
      <div v-show="activeMainTab === 'items'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Category Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tag"></i></span>
                <select v-model="filterCategoryId" @change="itemPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Kategori</option>
                  <option v-for="cat in categoryList" :key="cat.id" :value="String(cat.id)">
                    {{ cat.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="filterItemStatus" @change="itemPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="itemSearch || (filterCategoryId && filterCategoryId !== 'ALL') || filterItemStatus">
              <button
                type="button"
                @click="resetItemFilters"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="itemSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Kode SKU, Nama Barang, Spesifikasi..."
                  @keyup.enter="itemPage = 1"
                />
                <button type="button" @click="itemPage = 1" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Master Barang -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-3 py-3" style="width: 150px;">SKU / Barcode</th>
                  <th class="py-3" style="min-width: 220px;">Nama Barang</th>
                  <th class="py-3" style="width: 140px;">Kategori</th>
                  <th class="text-center py-3" style="width: 80px;">Satuan</th>
                  <th class="text-center py-3" style="width: 100px;">Safety Stock</th>
                  <th class="text-center py-3" style="width: 125px;" title="Reorder Point (Titik Pemesanan Ulang)">Reorder Point (ROP)</th>
                  <th class="text-center py-3" style="width: 90px;">Lead Time</th>
                  <th class="text-end py-3" style="min-width: 130px;">Est. Harga Satuan</th>
                  <th class="text-center py-3" style="width: 90px;">Status</th>
                  <th class="text-center pe-3 py-3" style="width: 110px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="it in paginatedItems" :key="it.id">
                  <!-- SKU / Barcode -->
                  <td class="ps-3">
                    <div class="font-monospace fw-bold text-danger">{{ it.sku }}</div>
                    <div v-if="it.barcode" class="font-monospace text-secondary fs-9">
                     {{ it.barcode }}
                    </div>
                  </td>

                  <!-- Nama Barang -->
                  <td>
                    <div class="fw-bold text-body">{{ it.name }}</div>
                    <div v-if="it.specification" class="text-secondary fs-8 text-truncate" style="max-width: 280px;" :title="it.specification">
                      {{ it.specification }}
                    </div>
                  </td>

                  <!-- Kategori -->
                  <td>
                    <span class="badge text-bg-light border text-secondary fs-8 fw-semibold">
                      {{ it.category ? it.category.name : (getCategoryName(it.category_id || it.categoryId) || '-') }}
                    </span>
                  </td>

                  <!-- Satuan -->
                  <td class="text-center">
                    <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fw-bold">
                      {{ it.uom }}
                    </span>
                  </td>

                  <!-- Safety Stock -->
                  <td class="text-center">
                    <span class="badge bg-primary-subtle text-primary fw-bold">
                      {{ formatNumber(it.safety_stock ?? it.safetyStock ?? 0) }}
                    </span>
                  </td>

                  <!-- Reorder Point -->
                  <td class="text-center">
                    <span class="badge bg-warning-subtle text-warning-emphasis fw-bold">
                      {{ formatNumber(it.reorder_point ?? it.reorderPoint ?? 0) }}
                    </span>
                  </td>

                  <!-- Lead Time -->
                  <td class="text-center text-secondary fs-8">
                    {{ it.lead_time_days ?? it.leadTimeDays ?? 0 }} hari
                  </td>

                  <!-- Est. Harga Satuan -->
                  <td class="text-end font-monospace fw-bold text-body">
                    {{ formatRupiah(it.estimated_unit_price ?? it.estimatedUnitPrice ?? 0) }}
                  </td>

                  <!-- Status -->
                  <td class="text-center">
                    <button
                      type="button"
                      @click="toggleItemStatus(it)"
                      class="badge border-0 cursor-pointer fs-9 py-1 px-2"
                      :class="isItemActive(it) ? 'text-bg-success' : 'text-bg-secondary'"
                      title="Klik untuk mengubah status"
                    >
                      {{ isItemActive(it) ? 'Aktif' : 'Non-Aktif' }}
                    </button>
                  </td>

                  <!-- Action Buttons -->
                  <td class="text-center pe-3">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewItemModal(it)" class="btn-action-icon text-secondary" title="Lihat Detail Barang">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditItemModal(it)" class="btn-action-icon text-primary" title="Edit Master Barang">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteItemModal(it)" class="btn-action-icon text-danger" title="Hapus Master Barang">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredItems.length === 0">
                  <td colspan="10" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada data master barang ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau sesuaikan filter kategori / status.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredItems.length"
          v-model:currentPage="itemPage"
          v-model:perPage="itemPerPage"
        />
      </div>

      <!-- ==================== TAB 2: KATEGORI BARANG ==================== -->
      <div v-show="activeMainTab === 'categories'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Status / Relasi Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-box-seam"></i></span>
                <select v-model="filterCatRelasi" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Kategori</option>
                  <option value="WITH_ITEMS">Memiliki Barang Persediaan</option>
                  <option value="NO_ITEMS">Belum Ada Barang</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="catSearch || filterCatRelasi">
              <button
                type="button"
                @click="catSearch = ''; filterCatRelasi = ''"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="catSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Kode atau Nama Kategori..."
                />
                <button type="button" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Kategori -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-4 py-3" style="width: 160px;">Kode Kategori</th>
                  <th class="py-3" style="width: 260px;">Nama Kategori</th>
                  <th class="py-3" style="min-width: 250px;">Deskripsi Pengelompokan</th>
                  <th class="text-center py-3" style="width: 150px;">Barang Terkait</th>
                  <th class="text-center py-3" style="width: 100px;">Status</th>
                  <th class="text-center pe-4 py-3" style="width: 100px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in filteredCategories" :key="cat.id">
                  <td class="ps-4">
                    <span class="font-monospace fw-bold text-danger">{{ cat.code }}</span>
                  </td>
                  <td>
                    <span class="fw-bold text-body">{{ cat.name }}</span>
                  </td>
                  <td class="text-secondary fs-8">
                    {{ cat.description || '-' }}
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      @click="filterItemsByCategory(cat.id)"
                      class="badge bg-secondary-subtle text-secondary-emphasis font-monospace border-0 cursor-pointer"
                      title="Filter barang dalam kategori ini"
                    >
                      {{ getCategoryItemsCount(cat.id) }} Barang
                    </button>
                  </td>
                  <td class="text-center">
                    <span class="badge text-bg-success">Aktif</span>
                  </td>
                  <td class="text-center pe-4">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewCategoryModal(cat)" class="btn-action-icon text-secondary" title="Lihat Detail Kategori">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditCategoryModal(cat)" class="btn-action-icon text-primary" title="Edit Kategori">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteCategoryModal(cat)" class="btn-action-icon text-danger" title="Hapus Kategori">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredCategories.length === 0">
                  <td colspan="6" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada kategori yang ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau sesuaikan filter status kategori.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Footer -->
        <div class="card-footer bg-body border-top py-3 px-4">
          <div class="fs-8 text-secondary">
            Menampilkan {{ filteredCategories.length }} total kategori barang terdaftar di sistem
          </div>
        </div>
      </div>

      <!-- ==================== TAB 3: SATUAN UKURAN (UOM) ==================== -->
      <div v-show="activeMainTab === 'uoms'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Klasifikasi Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-tags"></i></span>
                <select v-model="filterUomType" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Klasifikasi</option>
                  <option v-for="t in uomTypes" :key="t" :value="t">{{ t }}</option>
                </select>
              </div>
            </div>

            <!-- Penggunaan Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-box-seam"></i></span>
                <select v-model="filterUomUsage" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Penggunaan</option>
                  <option value="USED">Digunakan Barang</option>
                  <option value="UNUSED">Belum Digunakan</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="uomSearch || (filterUomType && filterUomType !== 'ALL') || filterUomUsage">
              <button
                type="button"
                @click="uomSearch = ''; filterUomType = 'ALL'; filterUomUsage = ''"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="uomSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Kode Satuan, Nama, Klasifikasi..."
                />
                <button type="button" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table UOM -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-4 py-3" style="width: 140px;">Kode Satuan (UOM)</th>
                  <th class="py-3" style="width: 220px;">Nama Satuan</th>
                  <th class="py-3" style="width: 140px;">Klasifikasi</th>
                  <th class="py-3" style="min-width: 260px;">Deskripsi & Contoh Penggunaan</th>
                  <th class="text-center py-3" style="width: 140px;">Jumlah Barang</th>
                  <th class="text-center pe-4 py-3" style="width: 140px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="uom in filteredUoms" :key="uom.code">
                  <td class="ps-4">
                    <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fs-7 px-2.5 py-1.5 border border-secondary-subtle">
                      {{ uom.code }}
                    </span>
                  </td>
                  <td>
                    <span class="fw-bold text-body">{{ uom.name }}</span>
                  </td>
                  <td>
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle fs-8">
                      {{ uom.type || 'Kuantitas' }}
                    </span>
                  </td>
                  <td class="text-secondary fs-8">
                    {{ uom.description || '-' }}
                  </td>
                  <td class="text-center">
                    <span class="badge text-bg-light border font-monospace">
                      {{ getUomUsageCount(uom.code) }} Barang
                    </span>
                  </td>
                  <td class="text-center pe-4">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewUomModal(uom)" class="btn-action-icon text-secondary" title="Lihat Detail Satuan">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditUomModal(uom)" class="btn-action-icon text-primary" title="Edit Satuan">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteUomModal(uom)" class="btn-action-icon text-danger" title="Hapus Satuan">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredUoms.length === 0">
                  <td colspan="6" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada satuan unit yang ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau sesuaikan filter klasifikasi / penggunaan.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Footer -->
        <div class="card-footer bg-body border-top py-3 px-4">
          <div class="fs-8 text-secondary">
            Menampilkan {{ filteredUoms.length }} total satuan unit ukuran (UOM) terkonfigurasi di sistem
          </div>
        </div>
      </div>

      <!-- ==================== TAB 4: KONVERSI SATUAN BARANG ==================== -->
      <div v-show="activeMainTab === 'conversions'">
        <!-- Filter & Search Toolbar -->
        <div class="card-body p-3 bg-body-tertiary border-bottom">
          <div class="row g-2 align-items-center">
            <!-- Target Barang Filter -->
            <div class="col-12 col-sm-6 col-md-3">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-box-seam"></i></span>
                <select v-model="filterConvItemId" @change="convPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="">Semua Target Barang</option>
                  <option value="global">Konversi Standar (Global / Umum)</option>
                  <option v-for="it in itemList" :key="it.id" :value="String(it.id)">
                    [{{ it.sku }}] {{ it.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Status Filter -->
            <div class="col-12 col-sm-6 col-md-2">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-toggle-on"></i></span>
                <select v-model="filterConvStatus" @change="convPage = 1" class="form-select form-select-sm border-start-0 fs-8">
                  <option value="ALL">Semua Status</option>
                  <option value="ACTIVE">Aktif</option>
                  <option value="INACTIVE">Non-Aktif</option>
                </select>
              </div>
            </div>

            <!-- Reset Button -->
            <div class="col-auto" v-if="convSearch || filterConvItemId || (filterConvStatus && filterConvStatus !== 'ALL')">
              <button
                type="button"
                @click="resetConvFilters"
                class="btn btn-sm btn-outline-danger fs-8"
                title="Reset Filter"
              >
                Reset
              </button>
            </div>

            <!-- Search Bar -->
            <div class="col-12 col-md ms-md-auto">
              <div class="input-group input-group-sm">
                <span class="input-group-text bg-body text-secondary border-end-0 fs-8"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="convSearch"
                  class="form-control form-control-sm border-start-0 fs-8"
                  placeholder="Cari Satuan Asal / Tujuan, Deskripsi..."
                  @keyup.enter="convPage = 1"
                />
                <button type="button" @click="convPage = 1" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs">Cari</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Table Konversi -->
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0 fs-7">
              <thead class="bg-body-tertiary text-secondary border-bottom">
                <tr>
                  <th class="ps-4 py-3" style="min-width: 220px;">Target Barang</th>
                  <th class="py-3" style="width: 130px;">Satuan Asal</th>
                  <th class="py-3" style="min-width: 220px;">Formula & Rasio Konversi</th>
                  <th class="py-3" style="width: 130px;">Satuan Tujuan</th>
                  <th class="py-3" style="min-width: 220px;">Deskripsi / Catatan</th>
                  <th class="text-center py-3" style="width: 100px;">Status</th>
                  <th class="text-center pe-4 py-3" style="width: 140px;">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="conv in paginatedConversions" :key="conv.id">
                  <td class="ps-4">
                    <div v-if="conv.item_id && getItem(conv.item_id)" class="d-flex flex-column">
                      <span class="fw-bold text-body fs-8">{{ getItem(conv.item_id).name }}</span>
                      <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fs-9 align-self-start mt-0.5">
                        {{ getItem(conv.item_id).sku }}
                      </span>
                    </div>
                    <span v-else class="badge bg-info-subtle text-info-emphasis border border-info-subtle fs-8 px-2 py-1">
                      Standar Global (Semua Barang)
                    </span>
                  </td>
                  <td>
                    <span class="badge bg-danger-subtle text-danger font-monospace fs-8 border border-danger-subtle">
                      {{ conv.from_uom }}
                    </span>
                  </td>
                  <td>
                    <div class="d-inline-flex align-items-center gap-1.5 py-1 px-2 rounded-2 bg-body-secondary border border-secondary-subtle fs-8">
                      <span class="font-monospace fw-bold text-body">1 {{ conv.from_uom }}</span>
                      <i class="bi bi-arrow-right text-danger"></i>
                      <span class="badge bg-success-subtle text-success-emphasis border border-success-subtle font-monospace fw-bold fs-8">
                        {{ conv.conversion_factor }}
                      </span>
                      <span class="font-monospace fw-bold text-body">{{ conv.to_uom }}</span>
                    </div>
                  </td>
                  <td>
                    <span class="badge bg-primary-subtle text-primary font-monospace fs-8 border border-primary-subtle">
                      {{ conv.to_uom }}
                    </span>
                  </td>
                  <td class="text-secondary fs-8">
                    {{ conv.description || '-' }}
                  </td>
                  <td class="text-center">
                    <span class="badge" :class="conv.is_active ? 'text-bg-success' : 'text-bg-secondary'">
                      {{ conv.is_active ? 'Aktif' : 'Non-Aktif' }}
                    </span>
                  </td>
                  <td class="text-center pe-4">
                    <div class="d-inline-flex align-items-center gap-1">
                      <button type="button" @click="openViewConversionModal(conv)" class="btn-action-icon text-secondary" title="Lihat Detail Konversi">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button type="button" @click="openEditConversionModal(conv)" class="btn-action-icon text-primary" title="Edit Konversi">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button type="button" @click="openDeleteConversionModal(conv)" class="btn-action-icon text-danger" title="Hapus Konversi">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- Empty State -->
                <tr v-if="filteredConversions.length === 0">
                  <td colspan="7" class="text-center py-5 text-secondary">
                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary-subtle"></i>
                    <p class="fw-bold mb-1">Tidak ada aturan konversi satuan yang ditemukan</p>
                    <p class="fs-8 text-muted mb-0">Coba ubah kata kunci pencarian atau sesuaikan filter barang target / status.</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination Footer -->
        <PaginationFooter
          :total="filteredConversions.length"
          v-model:currentPage="convPage"
          v-model:perPage="convPerPage"
        />
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: ITEM (VIEW, CREATE, EDIT, DELETE) -->
    <!-- ========================================================================= -->

    <!-- 1. VIEW ITEM MODAL -->
    <div v-if="viewItemModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Detail Master Barang: <span class="font-monospace text-danger">{{ selectedItem.sku }}</span>
            </h6>
            <div class="d-flex align-items-center gap-2">
              <span class="badge" :class="isItemActive(selectedItem) ? 'text-bg-success' : 'text-bg-secondary'">
                {{ isItemActive(selectedItem) ? 'Aktif' : 'Non-Aktif' }}
              </span>
              <button type="button" @click="viewItemModalOpen = false" class="btn-close ms-2" aria-label="Close"></button>
            </div>
          </div>

          <div class="modal-body p-4 space-y-4" style="max-height: 75vh; overflow-y: auto;">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="row g-3 fs-8">
                <div class="col-12 col-md-6">
                  <span class="text-secondary d-block">Nama Barang:</span>
                  <span class="fw-bold text-body fs-7">{{ selectedItem.name }}</span>
                </div>
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Kategori:</span>
                  <span class="fw-bold text-body">{{ selectedItem.category?.name || getCategoryName(selectedItem.category_id || selectedItem.categoryId) }}</span>
                </div>
                <div class="col-6 col-md-3">
                  <span class="text-secondary d-block">Satuan (UOM):</span>
                  <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace">{{ selectedItem.uom }}</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Kode SKU:</span>
                  <span class="font-monospace fw-bold text-danger">{{ selectedItem.sku }}</span>
                </div>
                <div class="col-6 col-md-4">
                  <span class="text-secondary d-block">Barcode:</span>
                  <span class="font-monospace fw-bold text-body">{{ selectedItem.barcode || '-' }}</span>
                </div>
                <div class="col-12 col-md-4">
                  <span class="text-secondary d-block">Est. Harga Satuan:</span>
                  <span class="font-monospace fw-bold text-success fs-7">{{ formatRupiah(selectedItem.estimated_unit_price ?? selectedItem.estimatedUnitPrice ?? 0) }}</span>
                </div>
                <div class="col-12">
                  <span class="text-secondary d-block">Spesifikasi / Keterangan:</span>
                  <span class="text-body">{{ selectedItem.specification || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- Parameter Kontrol Persediaan -->
            <div class="border rounded-2 overflow-hidden">
              <div class="bg-body-secondary py-1.5 px-3 border-bottom d-flex align-items-center justify-content-between">
                <span class="fs-8 fw-bold text-uppercase text-secondary">
                  Parameter Kontrol Persediaan
                </span>
              </div>
              <div class="table-responsive mb-0">
                <table class="table table-sm table-bordered text-center align-middle mb-0 fs-8">
                  <thead class="bg-body-tertiary text-secondary">
                    <tr>
                      <th class="py-1.5" style="width: 20%;">Min Stock</th>
                      <th class="py-1.5 text-primary" style="width: 20%;">Safety Stock</th>
                      <th class="py-1.5 text-warning-emphasis" style="width: 20%;">Reorder Point (ROP)</th>
                      <th class="py-1.5" style="width: 20%;">Max Stock</th>
                      <th class="py-1.5" style="width: 20%;">Lead Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="fw-bold font-monospace fs-7">
                      <td class="py-2">
                        <span>{{ selectedItem.min_stock ?? selectedItem.minStock ?? 0 }}</span>
                        <span class="fs-9 text-muted fw-normal ms-0.5">{{ selectedItem.uom }}</span>
                      </td>
                      <td class="py-2 text-primary bg-primary-subtle/30">
                        <span>{{ selectedItem.safety_stock ?? selectedItem.safetyStock ?? 0 }}</span>
                        <span class="fs-9 text-primary fw-normal ms-0.5">{{ selectedItem.uom }}</span>
                      </td>
                      <td class="py-2 text-warning-emphasis bg-warning-subtle/30">
                        <span>{{ selectedItem.reorder_point ?? selectedItem.reorderPoint ?? 0 }}</span>
                        <span class="fs-9 text-warning-emphasis fw-normal ms-0.5">{{ selectedItem.uom }}</span>
                      </td>
                      <td class="py-2">
                        <span>{{ selectedItem.max_stock ?? selectedItem.maxStock ?? 0 }}</span>
                        <span class="fs-9 text-muted fw-normal ms-0.5">{{ selectedItem.uom }}</span>
                      </td>
                      <td class="py-2">
                        <span>{{ selectedItem.lead_time_days ?? selectedItem.leadTimeDays ?? 0 }}</span>
                        <span class="fs-9 text-muted fw-normal ms-0.5">Hari</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-between align-items-center py-3 px-4 border-top">
            <button type="button" @click="viewItemModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Tutup
            </button>
            <button type="button" @click="viewItemModalOpen = false; openEditItemModal(selectedItem)" class="btn btn-sm btn-primary px-3">
              Edit Data Barang
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. CREATE ITEM MODAL -->
    <div v-if="createItemModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Master Barang Baru</h6>
            <button type="button" @click="createItemModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitCreateItem">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kategori Barang <span class="text-danger">*</span></label>
                  <select v-model="itemForm.category_id" required class="form-select form-select-sm fs-8">
                    <option value="" disabled>Pilih Kategori...</option>
                    <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode SKU Unik <span class="text-danger">*</span></label>
                  <input type="text" v-model="itemForm.sku" required class="form-control form-control-sm font-monospace fw-bold fs-8" placeholder="Contoh: IT-TNR-002" />
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Barcode (Opsional)</label>
                  <input type="text" v-model="itemForm.barcode" class="form-control form-control-sm font-monospace fs-8" placeholder="Contoh: 8991001025" />
                </div>
                <div class="col-12 col-md-8">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Barang <span class="text-danger">*</span></label>
                  <input type="text" v-model="itemForm.name" required class="form-control form-control-sm fs-8" placeholder="Contoh: Toner HP LaserJet 89A" />
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Satuan (UOM) <span class="text-danger">*</span></label>
                  <select v-model="itemForm.uom" required class="form-select form-select-sm fs-8">
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Estimasi Harga Satuan (Rp) <span class="text-danger">*</span></label>
                  <input type="number" v-model="itemForm.estimated_unit_price" required min="0" step="100" class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Spesifikasi / Keterangan</label>
                <textarea v-model="itemForm.specification" rows="2" class="form-control form-control-sm fs-8" placeholder="Keterangan spesifikasi teknis barang..."></textarea>
              </div>

              <!-- Parameter Persediaan -->
              <div class="border rounded-2 p-2.5 bg-body-tertiary">
                <div class="d-flex align-items-center justify-content-between mb-2 pb-1 border-bottom border-secondary-subtle">
                  <span class="fs-8 fw-bold text-uppercase text-secondary d-flex align-items-center gap-1.5">
                    Parameter Kontrol Persediaan
                  </span>
                </div>
                <div class="row g-2">
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Min Stock <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.min_stock" required min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">{{ itemForm.uom || 'PCS' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-primary mb-1">Safety Stock <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.safety_stock" required min="0" class="form-control text-center font-monospace fw-bold fs-8 text-primary border-primary-subtle" />
                      <span class="input-group-text fs-9 text-primary border-primary-subtle bg-primary-subtle px-2">{{ itemForm.uom || 'PCS' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Max Stock</label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.max_stock" min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">{{ itemForm.uom || 'PCS' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-6">
                    <label class="form-label fs-8 fw-semibold text-warning-emphasis mb-1">Reorder Point (ROP) <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.reorder_point" required min="0" class="form-control text-center font-monospace fw-bold fs-8 text-warning-emphasis border-warning-subtle" />
                      <span class="input-group-text fs-9 text-warning-emphasis border-warning-subtle bg-warning-subtle px-2">{{ itemForm.uom || 'PCS' }}</span>
                    </div>
                  </div>
                  <div class="col-12 col-md-6">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Lead Time Pengadaan</label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.lead_time_days" min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">Hari</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="createItemModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
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

    <!-- 3. EDIT ITEM MODAL -->
    <div v-if="editItemModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Edit Master Barang: <span class="font-monospace text-danger">{{ itemForm.sku }}</span>
            </h6>
            <button type="button" @click="editItemModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <form @submit.prevent="submitEditItem">
            <div class="modal-body p-4 space-y-3" style="max-height: 75vh; overflow-y: auto;">
              <div class="row g-3">
                <div class="col-12 col-md-8">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kategori Barang <span class="text-danger">*</span></label>
                  <select v-model="itemForm.category_id" required class="form-select form-select-sm fs-8">
                    <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Status Barang <span class="text-danger">*</span></label>
                  <select v-model="itemForm.is_active" class="form-select form-select-sm fs-8">
                    <option :value="1">Aktif</option>
                    <option :value="0">Non-Aktif</option>
                  </select>
                </div>
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode SKU <span class="text-danger">*</span></label>
                  <input type="text" v-model="itemForm.sku" required class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Barcode (Opsional)</label>
                  <input type="text" v-model="itemForm.barcode" class="form-control form-control-sm font-monospace fs-8" />
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Barang <span class="text-danger">*</span></label>
                <input type="text" v-model="itemForm.name" required class="form-control form-control-sm fs-8" />
              </div>

              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Satuan (UOM) <span class="text-danger">*</span></label>
                  <select v-model="itemForm.uom" required class="form-select form-select-sm fs-8">
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Estimasi Harga Satuan (Rp) <span class="text-danger">*</span></label>
                  <input type="number" v-model="itemForm.estimated_unit_price" required min="0" step="100" class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Spesifikasi / Keterangan</label>
                <textarea v-model="itemForm.specification" rows="2" class="form-control form-control-sm fs-8"></textarea>
              </div>

              <div class="border rounded-2 p-2.5 bg-body-tertiary">
                <div class="d-flex align-items-center justify-content-between mb-2 pb-1 border-bottom border-secondary-subtle">
                  <span class="fs-8 fw-bold text-uppercase text-secondary d-flex align-items-center gap-1.5">
                    Parameter Kontrol Persediaan
                  </span>
                </div>
                <div class="row g-2">
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Min Stock <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.min_stock" required min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">{{ itemForm.uom || 'UOM' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-primary mb-1">Safety Stock <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.safety_stock" required min="0" class="form-control text-center font-monospace fw-bold fs-8 text-primary border-primary-subtle" />
                      <span class="input-group-text fs-9 text-primary border-primary-subtle bg-primary-subtle px-2">{{ itemForm.uom || 'UOM' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-4">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Max Stock</label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.max_stock" min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">{{ itemForm.uom || 'UOM' }}</span>
                    </div>
                  </div>
                  <div class="col-6 col-md-6">
                    <label class="form-label fs-8 fw-semibold text-warning-emphasis mb-1">Reorder Point (ROP) <span class="text-danger">*</span></label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.reorder_point" required min="0" class="form-control text-center font-monospace fw-bold fs-8 text-warning-emphasis border-warning-subtle" />
                      <span class="input-group-text fs-9 text-warning-emphasis border-warning-subtle bg-warning-subtle px-2">{{ itemForm.uom || 'UOM' }}</span>
                    </div>
                  </div>
                  <div class="col-12 col-md-6">
                    <label class="form-label fs-8 fw-semibold text-secondary mb-1">Lead Time Pengadaan</label>
                    <div class="input-group input-group-sm">
                      <input type="number" v-model="itemForm.lead_time_days" min="0" class="form-control text-center font-monospace fw-bold fs-8" />
                      <span class="input-group-text fs-9 text-secondary px-2">Hari</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
              <button type="button" @click="editItemModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
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

    <!-- 4. DELETE ITEM MODAL -->
    <div v-if="deleteItemModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle d-flex align-items-center justify-content-between py-3 px-4 border-bottom border-danger-subtle">
            <h6 class="mb-0 fw-bold text-danger">Konfirmasi Hapus Master Barang</h6>
            <button type="button" @click="deleteItemModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>

          <div class="modal-body p-4 space-y-3">
            <p class="text-body mb-2 fs-7">
              Apakah Anda yakin ingin menghapus master barang berikut secara permanen?
            </p>

            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-1">Kode SKU:</div>
              <div class="font-monospace fw-bold text-danger fs-6 mb-2">{{ selectedItem.sku }}</div>
              <div class="fs-8 text-secondary mb-1">Nama Barang:</div>
              <div class="fw-bold text-body fs-7">{{ selectedItem.name }}</div>
            </div>

            <div class="alert alert-warning py-2 px-3 fs-8 mb-0 d-flex align-items-start gap-2">
              <i class="bi bi-shield-exclamation text-warning fs-6 flex-shrink-0 mt-0.5"></i>
              <div>
                <strong>Perlindungan Integritas Data:</strong> Barang yang sudah pernah tercatat dalam order atau memiliki alokasi persediaan di gudang tidak dapat dihapus. Anda dapat mengubah status barang menjadi <strong>Non-Aktif</strong>.
              </div>
            </div>
          </div>

          <div class="modal-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteItemModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">
              Batal
            </button>
            <button type="button" @click="submitDeleteItem" class="btn btn-sm btn-danger fw-bold px-3 shadow-xs">
              Ya, Hapus Barang
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: CATEGORY (VIEW, CREATE, EDIT, DELETE) -->
    <!-- ========================================================================= -->

    <!-- CREATE CATEGORY MODAL -->
    <div v-if="createCatModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Kategori Barang Baru</h6>
            <button type="button" @click="createCatModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitCreateCategory">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Kategori <span class="text-danger">*</span></label>
                <input type="text" v-model="catForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8 text-uppercase" placeholder="Contoh: CAT-ATK" />
                <span class="fs-9 text-secondary mt-0.5 d-block">Kode unik kategori, otomatis disimpan dalam format huruf kapital.</span>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Kategori <span class="text-danger">*</span></label>
                <input type="text" v-model="catForm.name" required class="form-control form-control-sm fw-bold fs-8" placeholder="Contoh: Alat Tulis Kantor (ATK)" />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi Pengelompokan</label>
                <textarea v-model="catForm.description" rows="2" class="form-control form-control-sm fs-8" placeholder="Keterangan kategori..."></textarea>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="createCatModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- VIEW CATEGORY MODAL -->
    <div v-if="viewCatModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Detail Kategori: <span class="font-monospace text-danger">{{ selectedCategory.code }}</span>
            </h6>
            <button type="button" @click="viewCatModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-0.5">Nama Kategori:</div>
              <h5 class="fw-bold text-body mb-0">{{ selectedCategory.name }}</h5>
            </div>
            <div class="row g-3 fs-8">
              <div class="col-sm-6">
                <span class="text-secondary d-block">Kode Kategori:</span>
                <span class="font-monospace fw-bold text-danger">{{ selectedCategory.code }}</span>
              </div>
              <div class="col-sm-6">
                <span class="text-secondary d-block">Jumlah Barang Terdaftar:</span>
                <span class="badge bg-secondary-subtle text-secondary-emphasis">{{ getCategoryItemsCount(selectedCategory.id) }} SKU Barang</span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block mb-1">Deskripsi:</span>
                <div class="p-2.5 rounded-2 bg-body border text-body">{{ selectedCategory.description || '- Tidak ada deskripsi -' }}</div>
              </div>
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewCatModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Tutup</button>
          </div>
        </div>
      </div>
    </div>

    <!-- EDIT CATEGORY MODAL -->
    <div v-if="editCatModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Edit Kategori Barang</h6>
            <button type="button" @click="editCatModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitEditCategory">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Kategori <span class="text-danger">*</span></label>
                <input type="text" v-model="catForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8 text-uppercase" />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Kategori <span class="text-danger">*</span></label>
                <input type="text" v-model="catForm.name" required class="form-control form-control-sm fw-bold fs-8" />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi</label>
                <textarea v-model="catForm.description" rows="2" class="form-control form-control-sm fs-8"></textarea>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="editCatModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- DELETE CATEGORY MODAL -->
    <div v-if="deleteCatModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold">Konfirmasi Hapus Kategori</h6>
            <button type="button" @click="deleteCatModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <p class="mb-2 fs-7 text-body">Apakah Anda yakin ingin menghapus kategori:</p>
            <div class="fw-bold fs-6 text-danger mb-2 font-monospace">[{{ selectedCategory.code }}] {{ selectedCategory.name }}</div>
            <div v-if="getCategoryItemsCount(selectedCategory.id) > 0" class="alert alert-warning py-2 px-3 fs-8 text-start mb-0">
              <i class="bi bi-exclamation-circle-fill me-1"></i>
              Kategori ini masih digunakan oleh <strong>{{ getCategoryItemsCount(selectedCategory.id) }}</strong> barang. Anda tidak dapat menghapus kategori yang masih memiliki relasi barang aktif.
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteCatModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
            <button
              type="button"
              :disabled="getCategoryItemsCount(selectedCategory.id) > 0"
              @click="submitDeleteCategory"
              class="btn btn-sm btn-danger fw-bold px-3 shadow-xs"
            >
              Ya, Hapus Kategori
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: UOM (VIEW, CREATE, EDIT, DELETE) -->
    <!-- ========================================================================= -->

    <!-- CREATE UOM MODAL -->
    <div v-if="createUomModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Satuan Ukuran (UOM) Baru</h6>
            <button type="button" @click="createUomModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitCreateUom">
            <div class="modal-body p-4 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Satuan <span class="text-danger">*</span></label>
                  <input type="text" v-model="uomForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8 text-uppercase" placeholder="Contoh: PCS" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Klasifikasi Tipe <span class="text-danger">*</span></label>
                  <select v-model="uomForm.type" required class="form-select form-select-sm fs-8">
                    <option v-for="t in uomTypes" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Satuan <span class="text-danger">*</span></label>
                <input type="text" v-model="uomForm.name" required class="form-control form-control-sm fw-bold fs-8" placeholder="Contoh: Pieces / Satuan Terkecil" />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi & Contoh Penggunaan</label>
                <textarea v-model="uomForm.description" rows="2" class="form-control form-control-sm fs-8" placeholder="Contoh penggunaan satuan ini..."></textarea>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="createUomModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- VIEW UOM MODAL -->
    <div v-if="viewUomModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">
              Detail Satuan Ukuran: <span class="font-monospace text-danger">{{ selectedUom.code }}</span>
            </h6>
            <button type="button" @click="viewUomModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle">
              <div class="fs-8 text-secondary mb-0.5">Nama Satuan:</div>
              <h5 class="fw-bold text-body mb-0">{{ selectedUom.name }}</h5>
            </div>
            <div class="row g-3 fs-8">
              <div class="col-sm-4">
                <span class="text-secondary d-block">Kode Satuan:</span>
                <span class="badge bg-secondary-subtle text-secondary-emphasis font-monospace fs-7 border">{{ selectedUom.code }}</span>
              </div>
              <div class="col-sm-4">
                <span class="text-secondary d-block">Klasifikasi:</span>
                <span class="badge bg-primary-subtle text-primary border">{{ selectedUom.type || 'Kuantitas' }}</span>
              </div>
              <div class="col-sm-4">
                <span class="text-secondary d-block">Barang Pengguna:</span>
                <span class="badge text-bg-light border font-monospace">{{ getUomUsageCount(selectedUom.code) }} Barang</span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block mb-1">Deskripsi:</span>
                <div class="p-2.5 rounded-2 bg-body border text-body">{{ selectedUom.description || '- Tidak ada deskripsi -' }}</div>
              </div>
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewUomModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Tutup</button>
          </div>
        </div>
      </div>
    </div>

    <!-- EDIT UOM MODAL -->
    <div v-if="editUomModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Edit Satuan Ukuran (UOM)</h6>
            <button type="button" @click="editUomModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitEditUom">
            <div class="modal-body p-4 space-y-3">
              <div class="row g-3">
                <div class="col-12 col-md-5">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Kode Satuan <span class="text-danger">*</span></label>
                  <input type="text" v-model="uomForm.code" required class="form-control form-control-sm font-monospace fw-bold fs-8 text-uppercase" />
                </div>
                <div class="col-12 col-md-7">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Klasifikasi Tipe <span class="text-danger">*</span></label>
                  <select v-model="uomForm.type" required class="form-select form-select-sm fs-8">
                    <option v-for="t in uomTypes" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Nama Satuan <span class="text-danger">*</span></label>
                <input type="text" v-model="uomForm.name" required class="form-control form-control-sm fw-bold fs-8" />
              </div>
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi</label>
                <textarea v-model="uomForm.description" rows="2" class="form-control form-control-sm fs-8"></textarea>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="editUomModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- DELETE UOM MODAL -->
    <div v-if="deleteUomModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold">Konfirmasi Hapus Satuan</h6>
            <button type="button" @click="deleteUomModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <p class="mb-2 fs-7 text-body">Apakah Anda yakin ingin menghapus satuan ukuran:</p>
            <div class="fw-bold fs-6 text-danger mb-2 font-monospace">[{{ selectedUom.code }}] {{ selectedUom.name }}</div>
            <div v-if="getUomUsageCount(selectedUom.code) > 0" class="alert alert-warning py-2 px-3 fs-8 text-start mb-0">
              <i class="bi bi-exclamation-circle-fill me-1"></i>
              Satuan ini masih digunakan oleh <strong>{{ getUomUsageCount(selectedUom.code) }}</strong> barang. Anda tidak dapat menghapus satuan yang masih memiliki barang aktif.
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteUomModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
            <button
              type="button"
              :disabled="getUomUsageCount(selectedUom.code) > 0"
              @click="submitDeleteUom"
              class="btn btn-sm btn-danger fw-bold px-3 shadow-xs"
            >
              Ya, Hapus Satuan
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODALS: CONVERSION (VIEW, CREATE, EDIT, DELETE) -->
    <!-- ========================================================================= -->

    <!-- CREATE CONVERSION MODAL -->
    <div v-if="createConvModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Tambah Aturan Konversi Satuan Baru</h6>
            <button type="button" @click="createConvModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitCreateConversion">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Target Barang (Opsional)</label>
                <select v-model="convForm.item_id" class="form-select form-select-sm fs-8">
                  <option value="">-- Standar Global (Berlaku untuk Semua Barang) --</option>
                  <option v-for="it in itemList" :key="it.id" :value="it.id">
                    [{{ it.sku }}] {{ it.name }} (Satuan Dasar: {{ it.uom }})
                  </option>
                </select>
                <div class="form-text fs-9">Pilih barang tertentu atau biarkan kosong untuk konversi umum/global.</div>
              </div>

              <div class="row g-3 align-items-end">
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">1 Satuan Asal <span class="text-danger">*</span></label>
                  <select v-model="convForm.from_uom" required class="form-select form-select-sm font-monospace fw-bold fs-8 text-uppercase">
                    <option value="" disabled>-- Pilih Satuan Asal --</option>
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Setara Dengan (=) <span class="text-danger">*</span></label>
                  <input type="number" v-model="convForm.conversion_factor" step="any" min="0.0001" required class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Satuan Tujuan <span class="text-danger">*</span></label>
                  <select v-model="convForm.to_uom" required class="form-select form-select-sm font-monospace fw-bold fs-8 text-uppercase">
                    <option value="" disabled>-- Pilih Satuan Tujuan --</option>
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi / Keterangan Formula</label>
                <textarea v-model="convForm.description" rows="2" class="form-control form-control-sm fs-8" placeholder="Contoh: 1 Box Kertas berisi 5 Rim"></textarea>
              </div>

              <div class="form-check form-switch pt-1">
                <input class="form-check-input" type="checkbox" v-model="convForm.is_active" id="create_conv_active" />
                <label class="form-check-label fs-8 fw-semibold text-body" for="create_conv_active">Status Konversi Aktif</label>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="createConvModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- VIEW CONVERSION MODAL -->
    <div v-if="viewConvModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Detail Aturan Konversi Satuan</h6>
            <button type="button" @click="viewConvModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 space-y-3">
            <div class="p-3 rounded-3 bg-body-secondary border border-secondary-subtle text-center">
              <span class="text-secondary fs-9 text-uppercase fw-bold d-block mb-1">Formula Rasio Konversi</span>
              <div class="d-flex align-items-center justify-content-center gap-2 font-monospace fs-5 fw-bold text-body">
                <span class="badge bg-danger text-white px-2.5 py-1.5">1 {{ selectedConversion.from_uom }}</span>
                <i class="bi bi-arrow-right text-danger"></i>
                <span class="badge bg-success text-white px-2.5 py-1.5">{{ selectedConversion.conversion_factor }} {{ selectedConversion.to_uom }}</span>
              </div>
            </div>
            <div class="row g-3 fs-8">
              <div class="col-12">
                <span class="text-secondary d-block">Target Barang:</span>
                <div class="fw-bold text-body mt-0.5">
                  <span v-if="selectedConversion.item_id && getItem(selectedConversion.item_id)" class="badge bg-secondary-subtle text-secondary-emphasis font-monospace me-1">
                    {{ getItem(selectedConversion.item_id).sku }}
                  </span>
                  <span>{{ selectedConversion.item_id && getItem(selectedConversion.item_id) ? getItem(selectedConversion.item_id).name : 'Standar Global (Semua Barang)' }}</span>
                </div>
              </div>
              <div class="col-6">
                <span class="text-secondary d-block">Satuan Asal:</span>
                <span class="badge bg-danger-subtle text-danger font-monospace fs-8">{{ selectedConversion.from_uom }}</span>
              </div>
              <div class="col-6">
                <span class="text-secondary d-block">Satuan Tujuan:</span>
                <span class="badge bg-primary-subtle text-primary font-monospace fs-8">{{ selectedConversion.to_uom }}</span>
              </div>
              <div class="col-6">
                <span class="text-secondary d-block">Faktor Pengali:</span>
                <span class="fw-bold font-monospace text-body fs-7">{{ selectedConversion.conversion_factor }}</span>
              </div>
              <div class="col-6">
                <span class="text-secondary d-block">Status:</span>
                <span class="badge" :class="selectedConversion.is_active ? 'text-bg-success' : 'text-bg-secondary'">
                  {{ selectedConversion.is_active ? 'Aktif' : 'Non-Aktif' }}
                </span>
              </div>
              <div class="col-12">
                <span class="text-secondary d-block mb-1">Keterangan / Catatan:</span>
                <div class="p-2.5 rounded-2 bg-body border text-body">{{ selectedConversion.description || '-' }}</div>
              </div>
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end py-3 px-4 border-top">
            <button type="button" @click="viewConvModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Tutup</button>
          </div>
        </div>
      </div>
    </div>

    <!-- EDIT CONVERSION MODAL -->
    <div v-if="editConvModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold text-body">Edit Aturan Konversi Satuan</h6>
            <button type="button" @click="editConvModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitEditConversion">
            <div class="modal-body p-4 space-y-3">
              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Target Barang (Opsional)</label>
                <select v-model="convForm.item_id" class="form-select form-select-sm fs-8">
                  <option value="">-- Standar Global (Berlaku untuk Semua Barang) --</option>
                  <option v-for="it in itemList" :key="it.id" :value="it.id">
                    [{{ it.sku }}] {{ it.name }} (Satuan Dasar: {{ it.uom }})
                  </option>
                </select>
              </div>

              <div class="row g-3 align-items-end">
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">1 Satuan Asal <span class="text-danger">*</span></label>
                  <select v-model="convForm.from_uom" required class="form-select form-select-sm font-monospace fw-bold fs-8 text-uppercase">
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Setara Dengan (=) <span class="text-danger">*</span></label>
                  <input type="number" v-model="convForm.conversion_factor" step="any" min="0.0001" required class="form-control form-control-sm font-monospace fw-bold fs-8" />
                </div>
                <div class="col-12 col-sm-4">
                  <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Satuan Tujuan <span class="text-danger">*</span></label>
                  <select v-model="convForm.to_uom" required class="form-select form-select-sm font-monospace fw-bold fs-8 text-uppercase">
                    <option v-for="u in uomList" :key="u.code" :value="u.code">{{ u.code }} - {{ u.name }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Deskripsi / Keterangan Formula</label>
                <textarea v-model="convForm.description" rows="2" class="form-control form-control-sm fs-8"></textarea>
              </div>

              <div class="form-check form-switch pt-1">
                <input class="form-check-input" type="checkbox" v-model="convForm.is_active" id="edit_conv_active" />
                <label class="form-check-label fs-8 fw-semibold text-body" for="edit_conv_active">Status Konversi Aktif</label>
              </div>
            </div>
            <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
              <button type="button" @click="editConvModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
              <button type="submit" class="btn btn-sm btn-danger fw-bold px-4 shadow-xs">
                Simpan
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- DELETE CONVERSION MODAL -->
    <div v-if="deleteConvModalOpen" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.6); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow-2xl overflow-hidden" style="background-color: var(--bs-body-bg); color: var(--bs-body-color);">
          <div class="modal-header bg-danger-subtle text-danger d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="mb-0 fw-bold">Konfirmasi Hapus Konversi</h6>
            <button type="button" @click="deleteConvModalOpen = false" class="btn-close" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <p class="mb-2 fs-7 text-body">Apakah Anda yakin ingin menghapus aturan konversi satuan ini?</p>
            <div class="fw-bold fs-6 text-danger mb-2 font-monospace">
              1 {{ selectedConversion.from_uom }} = {{ selectedConversion.conversion_factor }} {{ selectedConversion.to_uom }}
            </div>
            <div class="fs-8 text-secondary">
              Target: {{ selectedConversion.item_id && getItem(selectedConversion.item_id) ? getItem(selectedConversion.item_id).name : 'Standar Global' }}
            </div>
          </div>
          <div class="card-footer bg-body-tertiary d-flex justify-content-end gap-2 py-3 px-4 border-top">
            <button type="button" @click="deleteConvModalOpen = false" class="btn btn-sm btn-outline-secondary px-3">Batal</button>
            <button type="button" @click="submitDeleteConversion" class="btn btn-sm btn-danger fw-bold px-3 shadow-xs">
              Ya, Hapus Konversi
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
import { toast } from '@/utils/toast';

const route = useRoute();
const router = useRouter();

// ==================== TABS STATE ====================
// Tabbed: items | categories | uoms | conversions
const validTabs = ['items', 'categories', 'uoms', 'conversions'];
const activeMainTab = ref(
  (route.query.tab && validTabs.includes(route.query.tab))
    ? route.query.tab
    : 'items'
);

const switchTab = (tab) => {
  if (validTabs.includes(tab)) {
    activeMainTab.value = tab;
    router.replace({ query: { ...route.query, tab } });
  }
};

watch(() => route.query.tab, (newTab) => {
  if (newTab && validTabs.includes(newTab)) {
    activeMainTab.value = newTab;
  }
});

// Toast notification helper
const showAlert = (message, type = 'success') => {
  if (type === 'success') {
    toast.success(message, 'Berhasil');
  } else if (type === 'danger' || type === 'error') {
    toast.error(message, 'Gagal');
  } else if (type === 'warning' || type === 'warn') {
    toast.warn(message, 'Peringatan');
  } else {
    toast.info(message, 'Informasi');
  }
};

// ==================== DATA STATE (Pure Backend / No Hardcoded Seed Data) ====================
const itemList = ref([]);
const categoryList = ref([]);
const uomList = ref([
  { code: 'PCS', name: 'Pieces / Satuan Terkecil', type: 'Kuantitas', description: 'Satuan untuk barang eceran atau satuan tunggal (pena, tumbler, spons, dll.)' },
  { code: 'BOX', name: 'Box / Kotak', type: 'Kemasan', description: 'Kemasan boks berisi beberapa unit/roll/lembar (kertas HVS, slip setoran, dll.)' },
  { code: 'PACK', name: 'Pack / Bungkus', type: 'Kemasan', description: 'Kemasan pack atau bungkus kelipatan unit (buku tabungan, bilyet giro, segel, dll.)' },
  { code: 'UNIT', name: 'Unit / Perangkat', type: 'Perangkat', description: 'Satuan untuk perangkat, perlengkapan elektronik, stempel, atau mesin kasir' },
  { code: 'RIM', name: 'Rim (500 Lembar)', type: 'Kertas', description: 'Satuan standar kertas cetak/fotokopi berisi 500 lembar' },
  { code: 'SET', name: 'Set / Pasang', type: 'Paket', description: 'Kumpulan barang yang merupakan satu kesatuan lengkap yang tidak dipisah' },
  { code: 'ROLL', name: 'Roll / Gulung', type: 'Gulungan', description: 'Gulungan kertas seperti roll thermal EDC/ATM, pita banner, atau kabel' },
  { code: 'BUKU', name: 'Buku / Eksemplar', type: 'Publikasi', description: 'Buku administrasi, buku pedoman, atau formulir berformat jilid buku' },
  { code: 'LEMBAR', name: 'Lembar / Sheet', type: 'Kertas', description: 'Satuan per lembar kertas cetak khusus atau dokumen resmi' },
  { code: 'DUS', name: 'Dus / Karton', type: 'Kemasan', description: 'Kemasan master box karton pengiriman grosir dari penyedia' },
  { code: 'BOTOL', name: 'Botol / Bottle', type: 'Cairan', description: 'Kemasan botol cairan tinta refill, cairan pembersih, atau disinfektan' },
  { code: 'LUSIN', name: 'Lusin (12 Pcs)', type: 'Kuantitas', description: 'Kelipatan 12 satuan barang sejenis' },
  { code: 'METER', name: 'Meter (m)', type: 'Panjang', description: 'Satuan ukuran panjang untuk kabel jaringan, pita pembatas, dll.' }
]);
const conversionList = ref([]);
const loading = ref(false);

const uomTypes = [
  'Kuantitas',
  'Kemasan',
  'Kertas',
  'Perangkat',
  'Paket',
  'Gulungan',
  'Cairan',
  'Panjang',
  'Publikasi'
];

// Load from API
const loadData = async () => {
  loading.value = true;
  try {
    const [resItems, resCats] = await Promise.allSettled([
      api.get('/master/items'),
      api.get('/master/categories')
    ]);

    if (resItems.status === 'fulfilled') {
      const items = extractList(resItems.value);
      if (Array.isArray(items)) {
        itemList.value = items;
      }
    }

    if (resCats.status === 'fulfilled') {
      const cats = extractList(resCats.value);
      if (Array.isArray(cats)) {
        categoryList.value = cats;
      }
    }
  } catch (err) {
    console.warn('Gagal memuat master items / categories:', err?.message || err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
  if (route.query.category_id) {
    filterCategoryId.value = String(route.query.category_id);
    activeMainTab.value = 'items';
  }
});

// Helper functions
const formatRupiah = (num) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(num || 0);
};

const formatNumber = (num) => {
  return new Intl.NumberFormat('id-ID').format(num || 0);
};

const isItemActive = (it) => {
  if (it.is_active !== undefined) return Boolean(it.is_active);
  if (it.isActive !== undefined) return Boolean(it.isActive);
  return true;
};

const getCategoryName = (catId) => {
  const c = categoryList.value.find((cat) => String(cat.id) === String(catId));
  return c ? c.name : '-';
};

const getItem = (itemId) => {
  return itemList.value.find((it) => String(it.id) === String(itemId)) || null;
};

const getCategoryItemsCount = (catId) => {
  return itemList.value.filter((it) => String(it.category_id || it.categoryId || it.category?.id) === String(catId)).length;
};

const getUomUsageCount = (code) => {
  return itemList.value.filter((it) => (it.uom || '').toUpperCase() === (code || '').toUpperCase()).length;
};

// ==================== TAB 1: MASTER BARANG LOGIC ====================
const filterCategoryId = ref('ALL');
const filterItemStatus = ref('');
const itemSearch = ref('');
const itemPage = ref(1);
const itemPerPage = ref(10);

const resetItemFilters = () => {
  filterCategoryId.value = 'ALL';
  filterItemStatus.value = '';
  itemSearch.value = '';
  itemPage.value = 1;
};

const filteredItems = computed(() => {
  return itemList.value.filter((it) => {
    // Category Filter
    if (filterCategoryId.value && filterCategoryId.value !== 'ALL') {
      const cId = String(it.category_id || it.categoryId || it.category?.id || '');
      if (cId !== String(filterCategoryId.value)) return false;
    }
    // Status Filter
    if (filterItemStatus.value === 'ACTIVE' && !isItemActive(it)) return false;
    if (filterItemStatus.value === 'INACTIVE' && isItemActive(it)) return false;
    // Search Filter
    if (itemSearch.value) {
      const q = itemSearch.value.toLowerCase().trim();
      const sku = (it.sku || '').toLowerCase();
      const name = (it.name || '').toLowerCase();
      const spec = (it.specification || '').toLowerCase();
      const barcode = (it.barcode || '').toLowerCase();
      if (!sku.includes(q) && !name.includes(q) && !spec.includes(q) && !barcode.includes(q)) {
        return false;
      }
    }
    return true;
  });
});

const paginatedItems = computed(() => {
  const start = (itemPage.value - 1) * itemPerPage.value;
  return filteredItems.value.slice(start, start + itemPerPage.value);
});

const toggleItemStatus = (it) => {
  const current = isItemActive(it);
  it.is_active = !current;
  it.isActive = !current;
  showAlert(`Status barang ${it.name} (${it.sku}) diubah menjadi ${!current ? 'Aktif' : 'Non-Aktif'}.`);
};

const filterItemsByCategory = (catId) => {
  filterCategoryId.value = String(catId);
  switchTab('items');
};

// ==================== TAB 2: KATEGORI BARANG LOGIC ====================
const filterCatRelasi = ref('');
const catSearch = ref('');

const filteredCategories = computed(() => {
  return categoryList.value.filter((cat) => {
    const count = getCategoryItemsCount(cat.id);
    if (filterCatRelasi.value === 'WITH_ITEMS' && count === 0) return false;
    if (filterCatRelasi.value === 'NO_ITEMS' && count > 0) return false;
    if (catSearch.value) {
      const q = catSearch.value.toLowerCase().trim();
      const code = (cat.code || '').toLowerCase();
      const name = (cat.name || '').toLowerCase();
      const desc = (cat.description || '').toLowerCase();
      if (!code.includes(q) && !name.includes(q) && !desc.includes(q)) return false;
    }
    return true;
  });
});

// ==================== TAB 3: SATUAN UNIT (UOM) LOGIC ====================
const filterUomType = ref('ALL');
const filterUomUsage = ref('');
const uomSearch = ref('');

const filteredUoms = computed(() => {
  return uomList.value.filter((u) => {
    if (filterUomType.value && filterUomType.value !== 'ALL' && u.type !== filterUomType.value) {
      return false;
    }
    const count = getUomUsageCount(u.code);
    if (filterUomUsage.value === 'USED' && count === 0) return false;
    if (filterUomUsage.value === 'UNUSED' && count > 0) return false;
    if (uomSearch.value) {
      const q = uomSearch.value.toLowerCase().trim();
      const code = (u.code || '').toLowerCase();
      const name = (u.name || '').toLowerCase();
      const desc = (u.description || '').toLowerCase();
      if (!code.includes(q) && !name.includes(q) && !desc.includes(q)) return false;
    }
    return true;
  });
});

// ==================== TAB 4: KONVERSI SATUAN LOGIC ====================
const filterConvItemId = ref('');
const filterConvStatus = ref('ALL');
const convSearch = ref('');
const convPage = ref(1);
const convPerPage = ref(10);

const resetConvFilters = () => {
  filterConvItemId.value = '';
  filterConvStatus.value = 'ALL';
  convSearch.value = '';
  convPage.value = 1;
};

const filteredConversions = computed(() => {
  return conversionList.value.filter((conv) => {
    if (filterConvItemId.value === 'global' && conv.item_id) return false;
    if (filterConvItemId.value && filterConvItemId.value !== 'global' && String(conv.item_id) !== String(filterConvItemId.value)) return false;
    if (filterConvStatus.value === 'ACTIVE' && !conv.is_active) return false;
    if (filterConvStatus.value === 'INACTIVE' && conv.is_active) return false;
    if (convSearch.value) {
      const q = convSearch.value.toLowerCase().trim();
      const fromU = (conv.from_uom || '').toLowerCase();
      const toU = (conv.to_uom || '').toLowerCase();
      const desc = (conv.description || '').toLowerCase();
      const it = getItem(conv.item_id);
      const itName = it ? it.name.toLowerCase() : '';
      const itSku = it ? it.sku.toLowerCase() : '';
      if (!fromU.includes(q) && !toU.includes(q) && !desc.includes(q) && !itName.includes(q) && !itSku.includes(q)) return false;
    }
    return true;
  });
});

const paginatedConversions = computed(() => {
  const start = (convPage.value - 1) * convPerPage.value;
  return filteredConversions.value.slice(start, start + convPerPage.value);
});

// ==================== ITEM MODAL HANDLERS ====================
const viewItemModalOpen = ref(false);
const createItemModalOpen = ref(false);
const editItemModalOpen = ref(false);
const deleteItemModalOpen = ref(false);

const selectedItem = ref({});
const itemForm = reactive({
  id: null,
  category_id: '',
  sku: '',
  barcode: '',
  name: '',
  uom: 'PCS',
  specification: '',
  estimated_unit_price: 0,
  min_stock: 10,
  max_stock: 500,
  safety_stock: 20,
  reorder_point: 30,
  lead_time_days: 5,
  is_active: 1
});

const openViewItemModal = (it) => {
  selectedItem.value = { ...it };
  viewItemModalOpen.value = true;
};

const openCreateItemModal = () => {
  Object.assign(itemForm, {
    id: null,
    category_id: categoryList.value.length ? categoryList.value[0].id : '',
    sku: '',
    barcode: '',
    name: '',
    uom: 'PCS',
    specification: '',
    estimated_unit_price: 0,
    min_stock: 10,
    max_stock: 500,
    safety_stock: 20,
    reorder_point: 30,
    lead_time_days: 5,
    is_active: 1
  });
  createItemModalOpen.value = true;
};

const openEditItemModal = (it) => {
  selectedItem.value = { ...it };
  Object.assign(itemForm, {
    id: it.id,
    category_id: it.category_id || it.categoryId || (it.category ? it.category.id : ''),
    sku: it.sku || '',
    barcode: it.barcode || '',
    name: it.name || '',
    uom: it.uom || 'PCS',
    specification: it.specification || '',
    estimated_unit_price: it.estimated_unit_price ?? it.estimatedUnitPrice ?? 0,
    min_stock: it.min_stock ?? it.minStock ?? 10,
    max_stock: it.max_stock ?? it.maxStock ?? 500,
    safety_stock: it.safety_stock ?? it.safetyStock ?? 20,
    reorder_point: it.reorder_point ?? it.reorderPoint ?? 30,
    lead_time_days: it.lead_time_days ?? it.leadTimeDays ?? 5,
    is_active: isItemActive(it) ? 1 : 0
  });
  editItemModalOpen.value = true;
};

const openDeleteItemModal = (it) => {
  selectedItem.value = { ...it };
  deleteItemModalOpen.value = true;
};

const submitCreateItem = () => {
  const newId = Date.now();
  const catObj = categoryList.value.find((c) => c.id === itemForm.category_id) || null;
  const newItem = {
    id: newId,
    category_id: itemForm.category_id,
    category: catObj,
    sku: itemForm.sku.toUpperCase(),
    barcode: itemForm.barcode,
    name: itemForm.name,
    uom: itemForm.uom,
    specification: itemForm.specification,
    estimated_unit_price: Number(itemForm.estimated_unit_price),
    min_stock: Number(itemForm.min_stock),
    max_stock: Number(itemForm.max_stock),
    safety_stock: Number(itemForm.safety_stock),
    reorder_point: Number(itemForm.reorder_point),
    lead_time_days: Number(itemForm.lead_time_days),
    is_active: 1
  };
  itemList.value.unshift(newItem);
  createItemModalOpen.value = false;
  showAlert(`Master barang ${newItem.name} (${newItem.sku}) berhasil ditambahkan.`);
};

const submitEditItem = () => {
  const it = itemList.value.find((i) => i.id === itemForm.id);
  if (it) {
    const catObj = categoryList.value.find((c) => c.id === itemForm.category_id) || null;
    it.category_id = itemForm.category_id;
    it.category = catObj;
    it.sku = itemForm.sku.toUpperCase();
    it.barcode = itemForm.barcode;
    it.name = itemForm.name;
    it.uom = itemForm.uom;
    it.specification = itemForm.specification;
    it.estimated_unit_price = Number(itemForm.estimated_unit_price);
    it.min_stock = Number(itemForm.min_stock);
    it.max_stock = Number(itemForm.max_stock);
    it.safety_stock = Number(itemForm.safety_stock);
    it.reorder_point = Number(itemForm.reorder_point);
    it.lead_time_days = Number(itemForm.lead_time_days);
    it.is_active = itemForm.is_active === 1;
    showAlert(`Perubahan data barang ${it.name} berhasil disimpan.`);
  }
  editItemModalOpen.value = false;
};

const submitDeleteItem = () => {
  itemList.value = itemList.value.filter((i) => i.id !== selectedItem.value.id);
  deleteItemModalOpen.value = false;
  showAlert(`Master barang ${selectedItem.value.name} telah berhasil dihapus.`);
};

// ==================== CATEGORY MODAL HANDLERS ====================
const viewCatModalOpen = ref(false);
const createCatModalOpen = ref(false);
const editCatModalOpen = ref(false);
const deleteCatModalOpen = ref(false);

const selectedCategory = ref({});
const catForm = reactive({
  id: null,
  code: '',
  name: '',
  description: ''
});

const openViewCategoryModal = (cat) => {
  selectedCategory.value = { ...cat };
  viewCatModalOpen.value = true;
};

const openCreateCategoryModal = () => {
  Object.assign(catForm, { id: null, code: '', name: '', description: '' });
  createCatModalOpen.value = true;
};

const openEditCategoryModal = (cat) => {
  selectedCategory.value = { ...cat };
  Object.assign(catForm, { id: cat.id, code: cat.code, name: cat.name, description: cat.description || '' });
  editCatModalOpen.value = true;
};

const openDeleteCategoryModal = (cat) => {
  selectedCategory.value = { ...cat };
  deleteCatModalOpen.value = true;
};

const submitCreateCategory = () => {
  const newCat = {
    id: Date.now(),
    code: catForm.code.toUpperCase(),
    name: catForm.name,
    description: catForm.description
  };
  categoryList.value.push(newCat);
  createCatModalOpen.value = false;
  showAlert(`Kategori baru "${newCat.name}" berhasil ditambahkan.`);
};

const submitEditCategory = () => {
  const cat = categoryList.value.find((c) => c.id === catForm.id);
  if (cat) {
    cat.code = catForm.code.toUpperCase();
    cat.name = catForm.name;
    cat.description = catForm.description;
    showAlert(`Perubahan kategori "${cat.name}" berhasil disimpan.`);
  }
  editCatModalOpen.value = false;
};

const submitDeleteCategory = () => {
  if (getCategoryItemsCount(selectedCategory.value.id) > 0) {
    showAlert('Gagal menghapus kategori: Kategori masih memiliki relasi barang.', 'danger');
    deleteCatModalOpen.value = false;
    return;
  }
  categoryList.value = categoryList.value.filter((c) => c.id !== selectedCategory.value.id);
  deleteCatModalOpen.value = false;
  showAlert(`Kategori "${selectedCategory.value.name}" berhasil dihapus.`);
};

// ==================== UOM MODAL HANDLERS ====================
const viewUomModalOpen = ref(false);
const createUomModalOpen = ref(false);
const editUomModalOpen = ref(false);
const deleteUomModalOpen = ref(false);

const selectedUom = ref({});
const uomForm = reactive({
  code: '',
  name: '',
  type: 'Kuantitas',
  description: ''
});

const openViewUomModal = (u) => {
  selectedUom.value = { ...u };
  viewUomModalOpen.value = true;
};

const openCreateUomModal = () => {
  Object.assign(uomForm, { code: '', name: '', type: 'Kuantitas', description: '' });
  createUomModalOpen.value = true;
};

const openEditUomModal = (u) => {
  selectedUom.value = { ...u };
  Object.assign(uomForm, { code: u.code, name: u.name, type: u.type || 'Kuantitas', description: u.description || '' });
  editUomModalOpen.value = true;
};

const openDeleteUomModal = (u) => {
  selectedUom.value = { ...u };
  deleteUomModalOpen.value = true;
};

const submitCreateUom = () => {
  const newUom = {
    code: uomForm.code.toUpperCase(),
    name: uomForm.name,
    type: uomForm.type,
    description: uomForm.description
  };
  uomList.value.push(newUom);
  createUomModalOpen.value = false;
  showAlert(`Satuan ukuran "${newUom.code}" berhasil ditambahkan.`);
};

const submitEditUom = () => {
  const u = uomList.value.find((item) => item.code === selectedUom.value.code);
  if (u) {
    u.code = uomForm.code.toUpperCase();
    u.name = uomForm.name;
    u.type = uomForm.type;
    u.description = uomForm.description;
    showAlert(`Perubahan satuan "${u.code}" berhasil disimpan.`);
  }
  editUomModalOpen.value = false;
};

const submitDeleteUom = () => {
  if (getUomUsageCount(selectedUom.value.code) > 0) {
    showAlert('Gagal menghapus satuan: Satuan masih digunakan oleh barang aktif.', 'danger');
    deleteUomModalOpen.value = false;
    return;
  }
  uomList.value = uomList.value.filter((item) => item.code !== selectedUom.value.code);
  deleteUomModalOpen.value = false;
  showAlert(`Satuan "${selectedUom.value.code}" berhasil dihapus.`);
};

// ==================== CONVERSION MODAL HANDLERS ====================
const viewConvModalOpen = ref(false);
const createConvModalOpen = ref(false);
const editConvModalOpen = ref(false);
const deleteConvModalOpen = ref(false);

const selectedConversion = ref({});
const convForm = reactive({
  id: null,
  item_id: '',
  from_uom: '',
  conversion_factor: 1,
  to_uom: '',
  description: '',
  is_active: true
});

const openViewConversionModal = (conv) => {
  selectedConversion.value = { ...conv };
  viewConvModalOpen.value = true;
};

const openCreateConversionModal = () => {
  Object.assign(convForm, {
    id: null,
    item_id: '',
    from_uom: uomList.value.length ? uomList.value[0].code : 'BOX',
    conversion_factor: 1,
    to_uom: uomList.value.length > 1 ? uomList.value[1].code : 'PCS',
    description: '',
    is_active: true
  });
  createConvModalOpen.value = true;
};

const openEditConversionModal = (conv) => {
  selectedConversion.value = { ...conv };
  Object.assign(convForm, {
    id: conv.id,
    item_id: conv.item_id || '',
    from_uom: conv.from_uom,
    conversion_factor: conv.conversion_factor,
    to_uom: conv.to_uom,
    description: conv.description || '',
    is_active: Boolean(conv.is_active)
  });
  editConvModalOpen.value = true;
};

const openDeleteConversionModal = (conv) => {
  selectedConversion.value = { ...conv };
  deleteConvModalOpen.value = true;
};

const submitCreateConversion = () => {
  const newConv = {
    id: Date.now(),
    item_id: convForm.item_id || null,
    from_uom: convForm.from_uom,
    conversion_factor: Number(convForm.conversion_factor),
    to_uom: convForm.to_uom,
    description: convForm.description,
    is_active: convForm.is_active ? 1 : 0
  };
  conversionList.value.unshift(newConv);
  createConvModalOpen.value = false;
  showAlert(`Aturan konversi 1 ${newConv.from_uom} = ${newConv.conversion_factor} ${newConv.to_uom} berhasil dibuat.`);
};

const submitEditConversion = () => {
  const c = conversionList.value.find((item) => item.id === convForm.id);
  if (c) {
    c.item_id = convForm.item_id || null;
    c.from_uom = convForm.from_uom;
    c.conversion_factor = Number(convForm.conversion_factor);
    c.to_uom = convForm.to_uom;
    c.description = convForm.description;
    c.is_active = convForm.is_active ? 1 : 0;
    showAlert(`Perubahan aturan konversi berhasil disimpan.`);
  }
  editConvModalOpen.value = false;
};

const submitDeleteConversion = () => {
  conversionList.value = conversionList.value.filter((item) => item.id !== selectedConversion.value.id);
  deleteConvModalOpen.value = false;
  showAlert('Aturan konversi satuan telah berhasil dihapus.');
};

// Print
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

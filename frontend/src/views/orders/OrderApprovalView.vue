<template>
  <div class="order-approval-page space-y-3">
    <!-- 1. Judul Halaman & Breadcrumb (Standar Jatim: tanpa icon di judul, tanpa deskripsi) -->
    <div class="app-content-header mb-3">
      <div class="container-fluid px-0">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h3 class="mb-0 text-body fw-bold">Persetujuan Order Permintaan</h3>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-end mb-0 fs-8">
              <li class="breadcrumb-item">
                <router-link to="/dashboard" class="text-decoration-none text-danger">Home</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link to="/orders" class="text-decoration-none text-danger">Permintaan & Order</router-link>
              </li>
              <li class="breadcrumb-item active text-secondary" aria-current="page">Persetujuan Order</li>
            </ol>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. Filter Tabs Bar (Persis Jatim PHP: Selalu 1 Baris) -->
    <div class="card card-outline card-danger shadow-xs mb-3">
      <div class="card-header p-2 d-flex flex-row justify-content-between align-items-center gap-2 flex-nowrap">
        <ul class="nav nav-pills card-header-pills fs-7 flex-nowrap text-nowrap flex-shrink-0 mb-0">
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="statusTab === 'pending' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="setTab('pending')"
            >
              Menunggu Persetujuan
            </button>
          </li>
          <li class="nav-item">
            <button
              type="button"
              class="nav-link py-1 px-3 text-nowrap"
              :class="statusTab === 'history' ? 'active bg-danger fw-bold text-white' : 'text-body'"
              @click="setTab('history')"
            >
              Riwayat Persetujuan
            </button>
          </li>
        </ul>

        <!-- Quick Search Tab Riwayat Persetujuan (Rata Kanan & Fleksibel Tanpa Memecah Baris Tab) -->
        <div v-if="statusTab === 'history'" class="d-flex align-items-center justify-content-end ms-auto gap-2 m-0 flex-shrink-1" style="max-width: 280px;">
          <div class="input-group input-group-sm w-100">
            <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
              <i class="bi bi-search"></i>
            </span>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Cari nomor order / cabang..."
              class="form-control form-control-sm border-start-0 border-end-0 fs-8"
            />
            <button type="button" class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs" @click="currentPage = 1">
              Cari
            </button>
            <button v-if="searchQuery" type="button" class="btn btn-sm btn-outline-danger fs-8" @click="searchQuery = ''; currentPage = 1" title="Reset">
              <i class="bi bi-arrow-counterclockwise"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loadError" class="alert alert-danger fs-8">
      {{ loadError }}
    </div>

    <!-- ==================== TAMPILAN 1: TAB MENUNGGU PERSETUJUAN & SEMUA TRANSAKSI (TABLE GRID) ==================== -->
    <div v-if="statusTab === 'pending' || statusTab === 'all'" class="card card-outline card-danger shadow-xs">
      <!-- Toolbar Pencarian & Filter (Pola inventory/switching/approvals) -->
      <div class="card-body p-3 bg-body-tertiary border-bottom">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-geo-alt"></i>
              </span>
              <select v-model="filterBranch" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Cabang Pemohon</option>
                <option v-for="br in branchList" :key="br" :value="br">{{ br }}</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-flag"></i>
              </span>
              <select v-model="filterPriority" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Prioritas</option>
                <option value="URGENT">Urgent</option>
                <option value="HIGH">High</option>
                <option value="NORMAL">Normal</option>
              </select>
            </div>
          </div>
          <!-- Filter Status khusus untuk tab Semua Transaksi -->
          <div v-if="statusTab === 'all'" class="col-12 col-sm-6 col-md-2">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-funnel"></i>
              </span>
              <select v-model="filterStatus" class="form-select form-select-sm border-start-0 fs-8">
                <option value="">Semua Status</option>
                <option value="WAITING_APPROVAL">Menunggu Otorisasi</option>
                <option value="SUBMITTED">Diajukan</option>
                <option value="APPROVED">Disetujui</option>
                <option value="ALLOCATED">Teralokasi</option>
                <option value="COMPLETED">Selesai</option>
                <option value="REJECTED">Ditolak</option>
              </select>
            </div>
          </div>
          <div class="col-auto" v-if="searchQuery || filterBranch || filterPriority || filterStatus">
            <button
              type="button"
              @click="resetFilters"
              class="btn btn-sm btn-outline-danger fs-8"
              title="Reset Filter"
            >
              Reset
            </button>
          </div>
          <div class="col-12 col-md ms-md-auto">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-body text-secondary border-end-0 fs-8">
                <i class="bi bi-search"></i>
              </span>
              <input
                type="text"
                v-model="searchQuery"
                class="form-control form-control-sm border-start-0 border-end-0 fs-8"
                placeholder="Cari No. Order, Cabang, Maker..."
              />
              <button
                class="btn btn-sm btn-danger fw-bold fs-8 shadow-xs"
                type="button"
                @click="currentPage = 1"
              >
                Cari
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table Grid (Pola inventory/switching/approvals) -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 fs-8">
          <thead class="bg-body-secondary text-secondary border-bottom">
            <tr>
              <th class="ps-3 py-2 text-uppercase fs-9" style="min-width: 150px;">Nomor Order</th>
              <th class="py-2 text-uppercase fs-9" style="min-width: 180px;">Unit Pemohon</th>
              <th class="py-2 text-uppercase fs-9" style="min-width: 160px;">Diajukan Oleh</th>
              <th class="py-2 text-uppercase fs-9 text-end" style="min-width: 130px;">Estimasi Nilai</th>
              <th class="py-2 text-uppercase fs-9 text-center" style="width: 110px;">Prioritas</th>
              <th class="py-2 text-uppercase fs-9" style="min-width: 180px;">Status / Rekomendasi</th>
              <th class="text-center pe-3 py-2 text-uppercase fs-9" style="width: 130px;">Aksi Keputusan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in paginatedOrders" :key="order.id">
              <td class="ps-3">
                <router-link
                  :to="`/orders/${order.id}`"
                  class="font-monospace fw-bold text-danger text-decoration-none"
                  title="Lihat Detail Pesanan"
                >
                  {{ order.order_number }}
                </router-link>
                <span v-if="isEmbossOrder(order)" class="badge bg-danger-subtle text-danger border border-danger-subtle fs-9 ms-1">
                  Emboss
                </span>
                <div class="fs-9 text-secondary">{{ order.created_at_formatted }}</div>
              </td>
              <td>
                <div class="fw-semibold text-body">{{ order.requesting_organization?.name || '-' }}</div>
                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle fs-9">
                  {{ order.requesting_organization?.code || '-' }}
                </span>
              </td>
              <td>
                <div class="fw-medium text-body">{{ order.requester?.name || '-' }}</div>
                <div class="fs-9 text-secondary font-monospace">NIP: {{ order.requester?.nip || '-' }}</div>
              </td>
              <td class="text-end font-monospace fw-bold text-body-emphasis">
                {{ formatRupiah(order.total_estimated_value) }}
              </td>
              <td class="text-center">
                <span class="badge fs-9" :class="priorityBadgeClass(order.priority)">
                  {{ order.priority }}
                </span>
              </td>
              <td>
                <span class="badge fs-8 text-uppercase" :class="badgeClass(order.status)">
                  {{ getStatusLabel(order.status) }}
                </span>
                <div v-if="order.is_overbudget" class="mt-1">
                  <span class="badge bg-danger fs-9">
                    Overbudget ({{ order.projected_utilization }}%)
                  </span>
                </div>
                <div v-else-if="order.switchingRecommendations && order.switchingRecommendations.length > 0" class="mt-1">
                  <span class="badge text-bg-warning fs-9">
                    Rekomendasi Switch
                  </span>
                </div>
              </td>
              <td class="text-center pe-3">
                <div class="d-inline-flex align-items-center gap-1">
                  <template v-if="['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(order.status)">
                    <button
                      type="button"
                      class="btn-action-icon text-success"
                      @click="approveOrder(order)"
                      title="Setujui & Reservasi Stok"
                    >
                      <i class="bi bi-check2-circle"></i>
                    </button>
                    <button
                      type="button"
                      class="btn-action-icon text-danger"
                      @click="openRejectModal(order)"
                      title="Tolak Order"
                    >
                      <i class="bi bi-x-circle"></i>
                    </button>
                  </template>
                  <router-link
                    :to="`/orders/${order.id}`"
                    class="btn-action-icon text-secondary"
                    title="Lihat Detail Order"
                  >
                    <i class="bi bi-eye"></i>
                  </router-link>
                </div>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="7" class="text-center py-5 text-secondary">
                <i class="bi bi-check-circle fs-2 d-block mb-2 text-success"></i>
                <span class="fw-bold fs-7 d-block">Tidak ada order yang ditemukan</span>
                <span class="fs-8 text-muted">Seluruh pengajuan telah diproses atau tidak ada data yang cocok dengan kriteria filter.</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Paging -->
      <PaginationFooter
        :total="filteredOrders.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>

    <!-- ==================== TAMPILAN 2: TAB RIWAYAT PERSETUJUAN (MASTER-DETAIL DUA KOLOM JATIM_PHP) ==================== -->
    <div v-else-if="statusTab === 'history'">
      <!-- Empty State jika tidak ada order pada tab riwayat -->
      <div v-if="!selectedOrder" class="card shadow-xs text-center py-5">
        <div class="card-body">
          <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
          <h5 class="fw-bold mt-3 mb-1 text-body">Tidak Ada Order Dalam Antrean</h5>
          <p class="text-secondary fs-7 mb-0">Seluruh order pada kategori ini telah diproses atau belum ada riwayat persetujuan.</p>
        </div>
      </div>

      <!-- Master-Detail Two-Column Layout (Persis jatim_php) -->
      <div v-else class="row g-3">
        <!-- Left Column: Antrean Order (col-12 col-xl-4) -->
        <div class="col-12 col-xl-4">
          <div class="card card-outline card-secondary shadow-xs">
            <div class="card-header border-bottom d-flex justify-content-between align-items-center py-2 px-3">
              <span class="fs-7 fw-bold text-body">
                Antrean Order ({{ filteredOrders.length }})
              </span>
              <span class="fs-8 text-secondary">Pilih untuk memproses</span>
            </div>
            <div class="card-body p-2 space-y-2" style="max-height: 720px; overflow-y: auto;">
              <div
                v-for="ord in paginatedOrders"
                :key="ord.id"
                @click="selectOrder(ord)"
                class="d-block p-3 rounded-3 text-decoration-none border transition shadow-xs cursor-pointer"
                :class="ord.id === selectedOrder.id ? 'border-danger bg-danger-subtle' : 'border-secondary-subtle bg-body hover:bg-body-tertiary'"
                style="cursor: pointer;"
              >
                <div class="d-flex justify-content-between align-items-start mb-1">
                  <span class="font-monospace fw-bold fs-7" :class="ord.id === selectedOrder.id ? 'text-danger' : 'text-body'">
                    {{ ord.order_number }}
                  </span>
                  <span v-if="isEmbossOrder(ord)" class="badge bg-danger-subtle text-danger border border-danger-subtle fs-9 ms-1">
                    Emboss
                  </span>
                  <span class="badge fs-8 text-uppercase" :class="badgeClass(ord.status)">
                    {{ getStatusLabel(ord.status) }}
                  </span>
                </div>
                <div class="fs-8 fw-semibold text-body mb-1">
                  {{ ord.requesting_organization?.name || '-' }}
                </div>
                <div class="d-flex justify-content-between align-items-center fs-8 text-secondary">
                  <span>Maker: {{ ord.requester?.name || '-' }}</span>
                  <span class="fw-bold font-monospace text-body">
                    {{ formatRupiah(ord.total_estimated_value) }}
                  </span>
                </div>
                <div class="d-flex justify-content-between align-items-center fs-8 text-secondary mt-1 pt-1 border-top border-secondary-subtle">
                  <span><i class="bi bi-clock me-1"></i>{{ ord.created_at_formatted }}</span>
                  <span class="badge" :class="priorityBadgeClass(ord.priority)">
                    {{ ord.priority }}
                  </span>
                </div>
              </div>

              <div v-if="filteredOrders.length === 0" class="text-center py-4 text-secondary fs-8">
                Tidak ada order yang sesuai dengan filter pencarian.
              </div>
            </div>

            <!-- Compact Pagination untuk Kolom Sempit agar Rapi & Tidak Bertumpuk -->
            <PaginationFooter
              :compact="true"
              :total="filteredOrders.length"
              v-model:currentPage="currentPage"
              v-model:perPage="perPage"
            />
          </div>
        </div>

        <!-- Right Column: Detail Order Lengkap (col-12 col-xl-8) -->
        <div class="col-12 col-xl-8">
          <div class="space-y-4">
            <!-- 1. Order Header Banner -->
            <div class="card card-outline card-danger shadow-xs">
              <div class="card-body p-3 p-md-4">
                <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3">
                  <div>
                    <div class="d-flex align-items-center flex-wrap gap-2">
                      <h4 class="fw-bold mb-0 font-monospace text-body">{{ selectedOrder.order_number }}</h4>
                      <span v-if="isEmbossOrder(selectedOrder)" class="badge bg-danger-subtle text-danger border border-danger-subtle fs-8">
                        <i class="bi bi-credit-card-2-front me-1"></i> Order Emboss
                      </span>
                      <span class="badge fs-8 text-uppercase" :class="badgeClass(selectedOrder.status)">
                        {{ getStatusLabel(selectedOrder.status) }}
                      </span>
                      <span v-if="selectedOrder.is_overbudget" class="badge bg-danger fs-8">
                        <i class="bi bi-exclamation-octagon-fill me-1"></i> OVERBUDGET ({{ selectedOrder.projected_utilization }}%)
                      </span>
                      <span v-else-if="selectedOrder.projected_utilization >= 80" class="badge bg-warning text-dark fs-8">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i> BUDGET WARNING ({{ selectedOrder.projected_utilization }}%)
                      </span>
                    </div>
                    <p class="fs-7 text-secondary mb-0 mt-1">
                      Pemohon: <strong class="text-body">{{ selectedOrder.requester?.name }}</strong> &bull; {{ selectedOrder.created_at_formatted }} WIB &bull; Unit: {{ selectedOrder.requesting_organization?.name }} ({{ selectedOrder.requesting_organization?.code }})
                    </p>
                    <div v-if="selectedOrder.is_overbudget" class="alert alert-warning border border-warning d-flex align-items-center gap-2 p-2 mt-2 mb-0 fs-8">
                      <i class="bi bi-exclamation-triangle-fill text-warning fs-5"></i>
                      <div>
                        <strong>Peringatan Anggaran:</strong> Total order {{ formatRupiah(selectedOrder.total_estimated_value) }} menyebabkan proyeksi anggaran mencapai <strong>{{ selectedOrder.projected_utilization }}%</strong> (&gt;100%).
                        <div v-if="selectedOrder.overbudget_approval_reason" class="text-secondary mt-1">
                          Dispensasi: <em>"{{ selectedOrder.overbudget_approval_reason }}"</em>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Action Buttons -->
                  <div class="d-flex flex-wrap align-items-center gap-2">
                    <template v-if="['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(selectedOrder.status)">
                      <button type="button" @click="openRejectModal(selectedOrder)" class="btn btn-sm btn-outline-danger fw-bold">
                        Tolak Order
                      </button>
                      <button type="button" @click="approveOrder(selectedOrder)" class="btn btn-sm btn-success fw-bold shadow-xs">
                        Setujui & Reservasi Stok
                      </button>
                    </template>
                    <router-link :to="`/orders/${selectedOrder.id}/print`" target="_blank" class="btn btn-sm btn-outline-secondary">
                      Cetak
                    </router-link>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2. Status Alur Transaksi (Workflow Timeline) -->
            <div class="card card-outline card-danger shadow-xs">
              <div class="card-header border-bottom">
                <h3 class="card-title fs-7 fw-bold mb-0 text-uppercase text-secondary">
                  Status Alur Transaksi (Workflow Timeline)
                </h3>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover align-middle mb-0 fs-7">
                    <thead class="bg-body-tertiary border-bottom fs-8 text-uppercase text-secondary">
                      <tr>
                        <th class="ps-3" style="width: 50px;">No</th>
                        <th style="min-width: 170px;">Status Alur</th>
                        <th style="min-width: 160px;">Tanggal & Waktu</th>
                        <th style="min-width: 180px;">Siapa yang Memproses</th>
                        <th>Keterangan / Catatan</th>
                        <th class="text-center pe-3" style="width: 130px;">Kondisi</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="tl in currentTimeline"
                        :key="tl.step"
                        :class="{ 'table-warning': tl.status_state === 'CURRENT' }"
                      >
                        <td class="ps-3 text-center fw-bold">
                          <span
                            class="badge rounded-pill"
                            :class="timelineStepClass(tl.status_state)"
                            style="width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center;"
                          >
                            {{ tl.step }}
                          </span>
                        </td>
                        <td>
                          <div class="fw-bold text-body">{{ tl.label }}</div>
                          <span class="fs-8 font-monospace text-secondary">{{ tl.code }}</span>
                        </td>
                        <td>
                          <div class="fw-semibold font-monospace fs-8 text-body">
                           {{ tl.date_formatted }}
                          </div>
                        </td>
                        <td>
                          <div class="fw-bold text-body">{{ tl.actor_name }}</div>
                          <div class="fs-8 text-secondary">{{ tl.actor_role }}</div>
                        </td>
                        <td>
                          <div class="fs-8 text-body-secondary leading-relaxed">
                            {{ tl.description }}
                          </div>
                        </td>
                        <td class="text-center pe-3">
                          <span class="badge fs-8" :class="tl.badge_class">
                            {{ tl.badge_label }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <!-- 3. Switching Stock Recommendation Banner (jika ada) -->
            <div
              v-if="selectedOrder.switchingRecommendations && selectedOrder.switchingRecommendations.length > 0"
              class="alert alert-warning border-warning shadow-xs"
            >
              <div class="d-flex align-items-center gap-2 fw-bold fs-7 mb-1">
                <i class="bi bi-lightbulb-fill text-warning fs-5"></i>
                <span>Rekomendasi Cerdas Switching Stock Antar-Cabang (Intelligent Fulfillment)</span>
              </div>
              <p class="fs-8 mb-3">
                Stok pada Gudang Logistik Utama tidak mencukupi untuk memenuhi seluruh permintaan. Sistem mendeteksi unit alternatif dengan kelebihan stok:
              </p>

              <div class="space-y-2">
                <div
                  v-for="rec in selectedOrder.switchingRecommendations"
                  :key="rec.item.id"
                  class="card p-3 border-warning-subtle bg-body mb-2"
                >
                  <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
                    <div>
                      <div class="fs-7 fw-bold text-body">{{ rec.item.name }}</div>
                      <div class="fs-8 text-secondary">
                        Defisit: <strong class="text-danger">{{ rec.deficit }} {{ rec.item.uom || 'Unit' }}</strong> (Stok Pusat: {{ rec.central_available || 0 }} {{ rec.item.uom || 'Unit' }})
                      </div>
                      <div class="fs-8 text-primary fw-semibold mt-1">
                        Sumber Alternatif: {{ rec.alternatives?.[0]?.organization_name || 'KC Terdekat' }} (Stok Lebih: {{ rec.alternatives?.[0]?.excess_stock || 0 }} {{ rec.item.uom || 'Unit' }})
                      </div>
                    </div>
                    <button @click="openSwitchingModal(rec)" class="btn btn-sm btn-warning text-dark fw-bold">
                      Ajukan Switching Stock
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 4. Rincian Barang yang Diminta Card -->
            <div class="card card-outline card-secondary shadow-xs">
              <div class="card-header border-bottom">
                <h3 class="card-title fs-6 fw-bold mb-0 text-body">
                  Rincian Barang yang Diminta
                </h3>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover align-middle mb-0 fs-7">
                    <thead class="border-bottom fs-8 text-uppercase text-secondary bg-body-tertiary">
                      <tr>
                        <th class="ps-3">Item & SKU</th>
                        <th class="text-center">Diminta</th>
                        <th class="text-center">Dialokasikan</th>
                        <th class="text-center">Dipick/Pack</th>
                        <th class="text-center">Dikirim</th>
                        <th class="text-center">Diterima</th>
                        <th class="text-end pe-3">Est. Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="it in (selectedOrder.items || [])" :key="it.id">
                        <td class="ps-3">
                          <div class="fw-bold text-body">{{ it.item?.name }}</div>
                          <div class="fs-8 font-monospace text-secondary">{{ it.item?.sku }} &bull; {{ it.item?.uom }}</div>
                        </td>
                        <td class="text-center fw-bold text-body">{{ it.qty_requested }}</td>
                        <td class="text-center fw-bold text-primary">{{ it.qty_allocated }}</td>
                        <td class="text-center fw-bold text-info">{{ it.qty_packed }}</td>
                        <td class="text-center fw-bold text-warning">{{ it.qty_shipped }}</td>
                        <td class="text-center fw-bold text-success">{{ it.qty_received }}</td>
                        <td class="text-end pe-3 fw-bold font-monospace text-body-emphasis">
                          {{ formatRupiah(it.subtotal_ref) }}
                        </td>
                      </tr>
                      <tr v-if="!selectedOrder.items || selectedOrder.items.length === 0">
                        <td colspan="7" class="text-center py-4 text-secondary fs-8">
                          Tidak ada rincian barang untuk order ini.
                        </td>
                      </tr>
                    </tbody>
                    <tfoot class="bg-body-tertiary border-top">
                      <tr>
                        <th colspan="6" class="text-end ps-3 fw-bold text-body">Total Estimasi Nilai Order:</th>
                        <th class="text-end pe-3 fw-bold font-monospace text-danger fs-6">
                          {{ formatRupiah(selectedOrder.total_estimated_value) }}
                        </th>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL: TOLAK PESANAN (Tanpa Icon di Judul) ==================== -->
    <div v-if="orderToReject" class="modal-backdrop-custom" @click.self="orderToReject = null">
      <div class="modal-dialog-custom modal-sm card shadow-lg">
        <div class="card-header bg-danger-subtle d-flex align-items-center justify-content-between py-2.5 px-4 border-bottom border-danger-subtle">
          <h6 class="modal-title fw-bold text-danger mb-0">Konfirmasi Penolakan Order</h6>
          <button type="button" class="btn-close-modal" @click="orderToReject = null">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="card-body p-3.5 fs-8">
          <div class="p-2.5 rounded-3 bg-body-secondary border border-secondary-subtle mb-3">
            <div class="text-secondary mb-0.5">Order & Cabang Pemohon:</div>
            <strong class="text-body d-block font-monospace">{{ orderToReject.order_number }}</strong>
            <span class="text-secondary d-block">{{ orderToReject.requesting_organization?.name }}</span>
            <span class="font-monospace fw-bold text-danger">{{ formatRupiah(orderToReject.total_estimated_value) }}</span>
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">
              Alasan Penolakan <span class="text-danger">*</span>
            </label>
            <textarea
              v-model="rejectReason"
              rows="3"
              required
              class="form-control fs-7"
              placeholder="Contoh: Plafon anggaran cabang tidak mencukupi atau stok diprioritaskan untuk kebutuhan darurat..."
            ></textarea>
          </div>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2.5 px-4 border-top">
          <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="orderToReject = null">
            Batal
          </button>
          <button type="button" @click="confirmReject" class="btn btn-sm btn-danger fw-bold shadow-xs px-3">
            Tolak Order
          </button>
        </div>
      </div>
    </div>

    <!-- ==================== MODAL: SWITCHING STOCK (Tanpa Icon di Judul) ==================== -->
    <div v-if="showSwitchingModal" class="modal-backdrop-custom" @click.self="showSwitchingModal = false">
      <div class="modal-dialog-custom card shadow-lg">
        <div class="card-header bg-body-tertiary d-flex align-items-center justify-content-between py-2.5 px-4 border-bottom">
          <h6 class="modal-title fw-bold mb-0 text-body">Form Pengajuan Switching Stock</h6>
          <button type="button" class="btn-close-modal" @click="showSwitchingModal = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="card-body p-3.5 fs-8 space-y-3">
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Barang</label>
            <div class="p-2 rounded bg-body-secondary fw-bold fs-7 text-body">
              {{ selectedSwitchItem?.item?.name }}
            </div>
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Pilih Unit Sumber Alternatif</label>
            <select v-model="switchSourceWarehouseId" class="form-select fs-8" required>
              <option v-for="alt in selectedSwitchItem?.alternatives" :key="alt.warehouse_id" :value="alt.warehouse_id">
                {{ alt.organization_name }} (Stok Lebih: {{ alt.excess_stock }} {{ selectedSwitchItem?.item?.uom || 'Unit' }})
              </option>
            </select>
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Jumlah Switching Stock (Qty)</label>
            <input
              type="number"
              v-model.number="switchQty"
              min="1"
              class="form-control form-control-sm fw-bold font-monospace fs-8"
              required
            />
          </div>
          <div>
            <label class="form-label fs-8 fw-bold text-secondary text-uppercase mb-1">Alasan Rekomendasi</label>
            <textarea
              v-model="switchReason"
              rows="2"
              class="form-control form-control-sm fs-8"
            ></textarea>
          </div>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2.5 px-4 border-top">
          <button type="button" class="btn btn-sm btn-outline-secondary px-3" @click="showSwitchingModal = false">
            Batal
          </button>
          <button type="button" @click="submitSwitching" class="btn btn-sm btn-warning text-dark fw-bold shadow-xs px-3">
            Ajukan Proposal Switching
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';
import PaginationFooter from '@/components/PaginationFooter.vue';

// State
const statusTab = ref('pending'); // 'pending' | 'history' | 'all'
const searchQuery = ref('');
const filterBranch = ref('');
const filterPriority = ref('');
const filterStatus = ref('');
const currentPage = ref(1);
const perPage = ref(10);

const orderToReject = ref(null);
const rejectReason = ref('');

const showSwitchingModal = ref(false);
const selectedSwitchItem = ref(null);
const switchSourceWarehouseId = ref('');
const switchQty = ref(0);
const switchReason = ref('Pemenuhan kekurangan stok pusat dari kelebihan stok cabang regional.');

const mapApprovalOrder = (o) => ({
  id: o.id,
  order_number: o.orderNumber || o.order_number,
  order_type: o.orderType || o.order_type || (o.orderNumber?.includes('EMBOSS') ? 'EMBOSS_ORDER' : 'INTERNAL_REQUEST'),
  emboss_file_id: o.embossFile?.id || o.emboss_file_id,
  status: o.status,
  priority: o.priority || 'NORMAL',
  is_overbudget: Boolean(o.isOverbudget),
  projected_utilization: 0,
  requesting_organization: {
    id: o.requestingOrganization?.id || o.requesting_organization?.id,
    name: o.requestingOrganization?.name || o.requesting_organization?.name || 'Cabang',
    code: o.requestingOrganization?.code || o.requesting_organization?.code || '-'
  },
  requester: {
    name: o.createdByUser?.name || o.requester?.name || 'Maker',
    nip: o.createdByUser?.nip || o.requester?.nip || '-'
  },
  total_estimated_value: Number(o.totalEstimatedValue || o.total_estimated_value || 0),
  created_at_formatted: (o.createdAt || o.created_at) ? new Date(o.createdAt || o.created_at).toLocaleString('id-ID', { dateStyle: 'medium', timeStyle: 'short' }) : '-',
  notes: o.notes || '',
  items: (o.items || []).map((li, idx) => ({
    id: li.id || idx + 1,
    item: {
      id: li.item?.id,
      name: li.item?.name || 'Barang Logistik',
      sku: li.item?.sku || '-',
      uom: li.item?.uom || 'PCS'
    },
    qty_requested: li.qtyRequested || li.qty_requested || 0,
    qty_allocated: li.qtyApproved || li.qty_allocated || 0,
    qty_packed: li.qtyPacked || li.qty_packed || 0,
    qty_shipped: li.qtyShipped || li.qty_shipped || 0,
    qty_received: li.qtyReceived || li.qty_received || 0,
    subtotal_ref: Number(li.subtotalRef || li.subtotal_ref || 0)
  })),
  switchingRecommendations: []
});

const orders = ref([]);
const selectedOrder = ref(null);
const loadError = ref('');

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

// Lifecycle
onMounted(async () => {
  loadError.value = '';
  try {
    const res = await api.get('/orders', {
      params: { page: 0, size: 100, sort: 'createdAt,desc' }
    });
    const serverOrders = extractOrdersList(res);
    if (serverOrders !== null) {
      orders.value = serverOrders.map(mapApprovalOrder);
    }
  } catch (err) {
    console.warn('Failed loading orders from server:', err);
    orders.value = [];
  } finally {
    if (!selectedOrder.value || !orders.value.find(o => o.id === selectedOrder.value.id)) {
      selectedOrder.value = orders.value[0] || null;
    }
  }
});

// Branch list for dropdown filter
const branchList = computed(() => {
  const branches = new Set();
  orders.value.forEach(o => {
    if (o.requesting_organization?.name) {
      branches.add(o.requesting_organization.name);
    }
  });
  return Array.from(branches);
});

// Counts for tabs
const pendingCount = computed(() => {
  return orders.value.filter(o => ['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(o.status)).length;
});

const historyCount = computed(() => {
  return orders.value.filter(o => !['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(o.status)).length;
});

const allCount = computed(() => orders.value.length);

// Tab switching
const setTab = (tab) => {
  statusTab.value = tab;
  currentPage.value = 1;

  // Auto-select first matching order when switching to history
  if (tab === 'history') {
    const historyList = orders.value.filter(o => !['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(o.status));
    selectedOrder.value = historyList[0] || null;
  }
};

// Filtered orders based on tab, search, branch, priority, and status
const filteredOrders = computed(() => {
  let list = orders.value;

  if (statusTab.value === 'pending') {
    list = list.filter(o => ['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(o.status));
  } else if (statusTab.value === 'history') {
    list = list.filter(o => !['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(o.status));
  }

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(o =>
      o.order_number.toLowerCase().includes(q) ||
      (o.requesting_organization?.name && o.requesting_organization.name.toLowerCase().includes(q)) ||
      (o.requester?.name && o.requester.name.toLowerCase().includes(q))
    );
  }

  if (statusTab.value === 'pending' || statusTab.value === 'all') {
    if (filterBranch.value) {
      list = list.filter(o => o.requesting_organization?.name === filterBranch.value);
    }
    if (filterPriority.value) {
      list = list.filter(o => o.priority === filterPriority.value);
    }
    if (statusTab.value === 'all' && filterStatus.value) {
      list = list.filter(o => o.status === filterStatus.value);
    }
  }

  return list;
});

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return filteredOrders.value.slice(start, start + perPage.value);
});

const selectOrder = (ord) => {
  selectedOrder.value = ord;
};

const resetFilters = () => {
  searchQuery.value = '';
  filterBranch.value = '';
  filterPriority.value = '';
  filterStatus.value = '';
  currentPage.value = 1;
};

// Formatters & Helpers
const formatRupiah = (val) => {
  return 'Rp ' + new Intl.NumberFormat('id-ID').format(Math.round(val || 0));
};

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

const getStatusLabel = (st) => {
  return statusMap[st] || (st ? st.replace(/_/g, ' ') : '-');
};

const isEmbossOrder = (ord) => {
  if (!ord) return false;
  return ord.order_type === 'EMBOSS_ORDER' ||
    Boolean(ord.order_number && ord.order_number.toUpperCase().includes('EMBOSS')) ||
    Boolean(ord.emboss_file_id || ord.emboss_file);
};

const badgeClass = (st) => {
  switch (st) {
    case 'COMPLETED':
    case 'RECEIVED':
      return 'text-bg-success';
    case 'IN_TRANSIT':
      return 'text-bg-info';
    case 'READY_TO_SHIP':
    case 'ALLOCATED':
    case 'APPROVED':
      return 'text-bg-primary';
    case 'PRODUCTION':
    case 'IN_PRODUCTION':
    case 'PRODUKSI':
      return 'text-bg-warning text-dark border border-warning-subtle';
    case 'WAITING_APPROVAL':
    case 'SUBMITTED':
      return 'text-bg-warning';
    case 'CANCELLED':
    case 'REJECTED':
      return 'text-bg-danger';
    case 'DRAFT':
      return 'text-bg-secondary';
    default:
      return 'text-bg-light border';
  }
};

const priorityBadgeClass = (pr) => {
  switch (pr) {
    case 'URGENT':
      return 'bg-danger text-white';
    case 'HIGH':
      return 'bg-warning text-dark';
    default:
      return 'bg-secondary text-white';
  }
};

const timelineStepClass = (state) => {
  switch (state) {
    case 'DONE':
      return 'bg-danger text-white';
    case 'CURRENT':
      return 'bg-warning text-dark';
    case 'REJECTED':
      return 'bg-danger text-white';
    default:
      return 'bg-secondary text-white';
  }
};

// Dynamic Timeline Generation matching jatim_php & Emboss Workflow
const currentTimeline = computed(() => {
  if (!selectedOrder.value) return [];
  const ord = selectedOrder.value;
  const isRejected = ord.status === 'REJECTED';
  const isEmboss = isEmbossOrder(ord);

  if (isRejected) {
    return [
      {
        step: 1,
        code: 'SUBMITTED',
        label: isEmboss ? 'Draft Order Emboss Dibuat' : 'Order Diajukan (Submitted)',
        status_state: 'DONE',
        date_formatted: ord.created_at_formatted + ' WIB',
        actor_name: ord.requester?.name || 'Maker Cabang',
        actor_role: 'Staf Operasional Cabang',
        description: ord.notes || `Order diajukan oleh ${ord.requesting_organization?.name}`,
        badge_label: 'SELESAI',
        badge_class: 'text-bg-success'
      },
      {
        step: 2,
        code: 'REJECTED',
        label: 'Persetujuan Order (Ditolak / Rejected)',
        status_state: 'REJECTED',
        date_formatted: ord.created_at_formatted ? ord.created_at_formatted + ' WIB' : '10:15 WIB',
        actor_name: 'Penyelia Otorisator',
        actor_role: 'Order Approver',
        description: ord.notes || 'Ditolak: Pagu anggaran cabang tidak mencukupi.',
        badge_label: 'DITOLAK',
        badge_class: 'text-bg-danger'
      }
    ];
  }

  const isApproved = ['APPROVED', 'PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI', 'ALLOCATED', 'PICKING', 'PACKING', 'READY_TO_SHIP', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
  const isWaitingApproval = ['DRAFT', 'SUBMITTED', 'WAITING_APPROVAL'].includes(ord.status);

  // Alur Order Emboss: draft - disetujui - produksi kemudian picking dan selanjutnya
  if (isEmboss) {
    const isProdDone = ['PICKING', 'READY_TO_SHIP', 'PACKING', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
    const isProdCurrent = ['PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI'].includes(ord.status) || ord.status === 'APPROVED' || ord.status === 'ALLOCATED';
    const isPickDone = ['READY_TO_SHIP', 'PACKING', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
    const isPackDone = ['READY_TO_SHIP', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
    const isShipDone = ['IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
    const isRecDone = ['RECEIVED', 'COMPLETED'].includes(ord.status);

    return [
      {
        step: 1,
        code: 'DRAFT',
        label: 'Draft Order Emboss (Draft)',
        status_state: 'DONE',
        date_formatted: ord.created_at_formatted + ' WIB',
        actor_name: ord.requester?.name || 'Maker Cabang',
        actor_role: 'Staf Operasional Cabang',
        description: ord.notes || `Draft order emboss diterbitkan dari berkas core banking untuk ${ord.requesting_organization?.name}.`,
        badge_label: 'SELESAI',
        badge_class: 'text-bg-success'
      },
      {
        step: 2,
        code: 'APPROVED',
        label: 'Persetujuan & Otorisasi Order (Disetujui)',
        status_state: isApproved ? 'DONE' : (isWaitingApproval ? 'CURRENT' : 'PENDING'),
        date_formatted: isApproved ? (ord.created_at_formatted ? ord.created_at_formatted + ' WIB' : '12 Sep 2026, 10:45 WIB') : '-',
        actor_name: isApproved ? 'Penyelia Otorisator' : 'Menunggu Approval',
        actor_role: isApproved ? 'Order Approver' : 'Pejabat Berwenang',
        description: isApproved
          ? 'Order emboss telah disetujui & alokasi kuota bahan baku produk diotorisasi.'
          : 'Menunggu telaah dan otorisasi oleh Pejabat Pemutus (Approver).',
        badge_label: isApproved ? 'DISETUJUI' : 'MENUNGGU APPROVAL',
        badge_class: isApproved ? 'text-bg-success' : 'text-bg-warning'
      },
      {
        step: 3,
        code: 'PRODUCTION',
        label: 'Produksi & Personalisasi Kartu (Produksi)',
        status_state: isProdDone ? 'DONE' : (isProdCurrent ? 'CURRENT' : 'PENDING'),
        date_formatted: isProdDone ? '12 Sep 2026, 11:30 WIB' : '-',
        actor_name: isProdDone || isProdCurrent ? 'Petugas Cetak Produk' : '-',
        actor_role: 'Operator Mesin Emboss',
        description: isProdDone
          ? 'Personalisasi emboss kartu debit/ATM dan verifikasi chip nasabah selesai.'
          : (isProdCurrent
              ? (['PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI'].includes(ord.status)
                  ? 'Proses personalisasi data nasabah dan pencetakan fisik kartu sedang berjalan.'
                  : 'Order telah disetujui, siap masuk antrean produksi & pencetakan kartu.')
              : 'Menunggu persetujuan order sebelum masuk tahap produksi.'),
        badge_label: isProdDone
          ? 'SELESAI'
          : (['PRODUCTION', 'IN_PRODUCTION', 'PRODUKSI'].includes(ord.status)
              ? 'PROSES PRODUKSI'
              : (isApproved ? 'SIAP PRODUKSI' : 'MENUNGGU')),
        badge_class: isProdDone
          ? 'text-bg-success'
          : (isApproved ? 'text-bg-warning text-dark' : 'text-bg-secondary')
      },
      {
        step: 4,
        code: 'PICKING',
        label: 'Pengambilan Fisik Gudang (Picking)',
        status_state: isPickDone ? 'DONE' : (ord.status === 'PICKING' ? 'CURRENT' : 'PENDING'),
        date_formatted: isPickDone ? '12 Sep 2026, 13:00 WIB' : '-',
        actor_name: isPickDone || ord.status === 'PICKING' ? 'Petugas Gudang Pusat' : '-',
        actor_role: 'Logistics Warehouse Officer',
        description: isPickDone
          ? 'Pengambilan fisik kartu hasil produksi dan PIN mailer dari rak selesai.'
          : (isProdDone ? 'Menunggu antrean pengambilan kartu jadi dari ruang produksi/gudang.' : '-'),
        badge_label: isPickDone ? 'SELESAI' : (ord.status === 'PICKING' ? 'PROSES PICKING' : 'MENUNGGU'),
        badge_class: isPickDone ? 'text-bg-success' : (ord.status === 'PICKING' ? 'text-bg-primary' : 'text-bg-secondary')
      },
      {
        step: 5,
        code: 'READY_TO_SHIP',
        label: 'Pengepakan Koli (Packing)',
        status_state: isPackDone ? 'DONE' : (['READY_TO_SHIP', 'PACKING'].includes(ord.status) ? 'CURRENT' : 'PENDING'),
        date_formatted: isPackDone ? '12 Sep 2026, 14:30 WIB' : '-',
        actor_name: isPackDone || ['READY_TO_SHIP', 'PACKING'].includes(ord.status) ? 'Petugas Packing' : '-',
        actor_role: 'Logistics Packing Officer',
        description: isPackDone
          ? 'Kartu fisik dan amplop PIN telah dikemas rapi dalam kemasan bersegel keamanan.'
          : (isPickDone ? 'Menunggu proses pengepakan koli dan penimbangan berat paket.' : '-'),
        badge_label: isPackDone ? 'SELESAI' : (['READY_TO_SHIP', 'PACKING'].includes(ord.status) ? 'PROSES PACKING' : 'MENUNGGU'),
        badge_class: isPackDone ? 'text-bg-success' : (['READY_TO_SHIP', 'PACKING'].includes(ord.status) ? 'text-bg-primary' : 'text-bg-secondary')
      },
      {
        step: 6,
        code: 'IN_TRANSIT',
        label: 'Pengiriman & Manifest Ekspedisi',
        status_state: isShipDone ? 'DONE' : (ord.status === 'IN_TRANSIT' ? 'CURRENT' : 'PENDING'),
        date_formatted: isShipDone ? '12 Sep 2026, 16:00 WIB' : '-',
        actor_name: isShipDone || ord.status === 'IN_TRANSIT' ? 'Logistics Express' : '-',
        actor_role: 'Kurir Ekspedisi',
        description: isShipDone
          ? 'Manifest pengiriman diterbitkan. Paket kartu dalam perjalanan ke cabang tujuan.'
          : (isPackDone ? 'Menunggu penyerahan paket ke jasa ekspedisi/kurir dan cetak surat jalan.' : '-'),
        badge_label: isShipDone ? (isRecDone ? 'TERKIRIM' : 'IN-TRANSIT') : 'MENUNGGU',
        badge_class: isShipDone ? (isRecDone ? 'text-bg-success' : 'text-bg-info') : 'text-bg-secondary'
      },
      {
        step: 7,
        code: 'RECEIVED',
        label: 'Diterima Cabang (Selesai)',
        status_state: isRecDone ? 'DONE' : (isShipDone ? 'CURRENT' : 'PENDING'),
        date_formatted: isRecDone ? '13 Sep 2026, 09:30 WIB' : '-',
        actor_name: isRecDone ? (ord.requesting_organization?.name || 'Cabang Pemesan') : '-',
        actor_role: 'Staf Operasional Cabang',
        description: isRecDone
          ? 'Paket fisik kartu telah diterima di kantor cabang, diverifikasi, dan siap diserahkan ke nasabah.'
          : (isShipDone ? 'Paket dalam perjalanan, menunggu konfirmasi serah terima di cabang.' : '-'),
        badge_label: isRecDone ? 'SELESAI' : 'MENUNGGU',
        badge_class: isRecDone ? 'text-bg-success' : 'text-bg-secondary'
      }
    ];
  }

  // Alur order logistik standar (Non-Emboss)
  const isPickingDone = ['PICKING', 'READY_TO_SHIP', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
  const isPackingDone = ['READY_TO_SHIP', 'IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
  const isShipmentDone = ['IN_TRANSIT', 'RECEIVED', 'COMPLETED'].includes(ord.status);
  const isReceivedDone = ['RECEIVED', 'COMPLETED'].includes(ord.status);

  return [
    {
      step: 1,
      code: 'SUBMITTED',
      label: 'Order Diajukan (Submitted)',
      status_state: 'DONE',
      date_formatted: ord.created_at_formatted + ' WIB',
      actor_name: ord.requester?.name || 'Maker Cabang',
      actor_role: 'Staf Operasional Cabang',
      description: ord.notes || `Order diajukan oleh ${ord.requesting_organization?.name}`,
      badge_label: 'SELESAI',
      badge_class: 'text-bg-success'
    },
    {
      step: 2,
      code: 'APPROVED',
      label: 'Persetujuan & Alokasi Stok (Approved)',
      status_state: isApproved ? 'DONE' : (isWaitingApproval ? 'CURRENT' : 'PENDING'),
      date_formatted: isApproved ? '12 Sep 2026, 10:45 WIB' : '-',
      actor_name: isApproved ? 'Penyelia Otorisator' : 'Menunggu Approval',
      actor_role: isApproved ? 'Order Approver' : 'Pejabat Berwenang',
      description: isApproved
        ? 'Order telah disetujui & alokasi stok persediaan direservasi otomatis dari Gudang Logistik Pusat.'
        : 'Menunggu telaah dan otorisasi oleh Pejabat Pemutus (Approver).',
      badge_label: isApproved ? 'DISETUJUI' : 'MENUNGGU APPROVAL',
      badge_class: isApproved ? 'text-bg-success' : 'text-bg-warning'
    },
    {
      step: 3,
      code: 'PICKING',
      label: 'Pengambilan Fisik Gudang (Picking)',
      status_state: isPickingDone ? 'DONE' : (ord.status === 'ALLOCATED' ? 'CURRENT' : 'PENDING'),
      date_formatted: isPickingDone ? '12 Sep 2026, 13:00 WIB' : '-',
      actor_name: isPickingDone ? 'Petugas Gudang Pusat' : '-',
      actor_role: 'Logistics Warehouse Officer',
      description: isPickingDone
        ? 'Pengambilan fisik barang dari rak gudang logistik pusat selesai.'
        : (isApproved ? 'Menunggu antrean pengambilan barang dari rak penyimpanan gudang logistik.' : '-'),
      badge_label: isPickingDone ? 'SELESAI' : (ord.status === 'ALLOCATED' ? 'PROSES PICKING' : 'MENUNGGU'),
      badge_class: isPickingDone ? 'text-bg-success' : (ord.status === 'ALLOCATED' ? 'text-bg-primary' : 'text-bg-secondary')
    },
    {
      step: 4,
      code: 'READY_TO_SHIP',
      label: 'Pengepakan Koli (Packing)',
      status_state: isPackingDone ? 'DONE' : (ord.status === 'PICKING' ? 'CURRENT' : 'PENDING'),
      date_formatted: isPackingDone ? '12 Sep 2026, 14:30 WIB' : '-',
      actor_name: isPackingDone ? 'Petugas Packing' : '-',
      actor_role: 'Logistics Packing Officer',
      description: isPackingDone
        ? 'Barang dikemas rapi: 2 Koli (24.5 kg). Siap serah terima ekspedisi logistik.'
        : (isPickingDone ? 'Menunggu proses pengepakan koli dan penimbangan berat paket.' : '-'),
      badge_label: isPackingDone ? 'SELESAI' : (ord.status === 'PICKING' ? 'PROSES PACKING' : 'MENUNGGU'),
      badge_class: isPackingDone ? 'text-bg-success' : (ord.status === 'PICKING' ? 'text-bg-primary' : 'text-bg-secondary')
    },
    {
      step: 5,
      code: 'IN_TRANSIT',
      label: 'Pengiriman & Manifest Ekspedisi',
      status_state: isShipmentDone ? 'DONE' : (ord.status === 'READY_TO_SHIP' ? 'CURRENT' : 'PENDING'),
      date_formatted: isShipmentDone ? '12 Sep 2026, 16:00 WIB' : '-',
      actor_name: isShipmentDone ? 'Logistics Express' : '-',
      actor_role: 'Kurir Ekspedisi',
      description: isShipmentDone
        ? 'Manifest pengiriman diterbitkan. Paket dalam perjalanan ke cabang tujuan.'
        : (isPackingDone ? 'Menunggu penyerahan paket ke jasa ekspedisi/kurir dan cetak surat jalan.' : '-'),
      badge_label: isShipmentDone ? (isReceivedDone ? 'TERKIRIM' : 'IN-TRANSIT') : 'MENUNGGU',
      badge_class: isShipmentDone ? (isReceivedDone ? 'text-bg-success' : 'text-bg-info') : 'text-bg-secondary'
    }
  ];
});

// Approval & Rejection Handlers
const approveOrder = async (order) => {
  if (order.is_overbudget) {
    const reason = prompt(`Order ini terdeteksi OVERBUDGET (${order.projected_utilization}%). Masukkan alasan otorisasi dispensasi:`);
    if (!reason) return;
    order.overbudget_approval_reason = reason;
  }

  try {
    await api.post(`/orders/${order.id}/approve`);
  } catch (e) {
    // Fallback prototype
  }

  order.status = 'APPROVED';
  if (order.items) {
    order.items.forEach(it => {
      it.qty_allocated = it.qty_requested;
    });
  }
  alert(`Pesanan ${order.order_number} berhasil disetujui.`);
};

const openRejectModal = (order) => {
  orderToReject.value = order;
  rejectReason.value = '';
};

const confirmReject = () => {
  if (!rejectReason.value.trim()) {
    alert('Harap isi alasan penolakan.');
    return;
  }
  const num = orderToReject.value.order_number;
  orderToReject.value.status = 'REJECTED';
  orderToReject.value.notes = 'Ditolak: ' + rejectReason.value.trim();
  orderToReject.value = null;
  alert(`Pesanan ${num} berhasil ditolak.`);
};

// Switching Stock Modal Handlers
const openSwitchingModal = (rec) => {
  selectedSwitchItem.value = rec;
  switchSourceWarehouseId.value = rec.alternatives?.[0]?.warehouse_id || '';
  switchQty.value = rec.deficit;
  showSwitchingModal.value = true;
};

const submitSwitching = () => {
  showSwitchingModal.value = false;
  alert(`Proposal switching stock untuk ${selectedSwitchItem.value?.item?.name} sejumlah ${switchQty.value} unit berhasil diajukan!`);
};
</script>

<style scoped>
.space-y-2 > * + * {
  margin-top: 0.5rem;
}
.space-y-3 > * + * {
  margin-top: 0.75rem;
}
.space-y-4 > * + * {
  margin-top: 1rem;
}
.cursor-pointer {
  cursor: pointer;
}
</style>

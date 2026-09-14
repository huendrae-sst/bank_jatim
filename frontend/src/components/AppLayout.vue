<template>
  <div class="app-wrapper" :class="{ 'sidebar-collapsed': !sidebarOpen }">
    <!-- App Header (Navbar) -->
    <nav class="app-header navbar navbar-expand shadow-xs border-bottom">
      <div class="container-fluid">
        <!-- Start Navbar Links -->
        <ul class="navbar-nav align-items-center">
          <!-- Official Logo Bank Jatim + JIMS Badge in Topbar Left -->
          <li class="nav-item me-2 me-md-3">
            <router-link to="/dashboard" class="topbar-brand">
              <img
                v-if="resolvedTheme === 'dark'"
                src="/images/logo-bankjatim-white.png"
                alt="Bank Jatim"
                class="topbar-logo-img"
              />
              <img
                v-else
                src="/images/logo-bankjatim.png"
                alt="Bank Jatim"
                class="topbar-logo-img"
              />
              <span class="badge text-bg-danger px-1.5 py-0.5 fs-8 fw-bold">JIMS</span>
            </router-link>
          </li>
          <li class="nav-item">
            <button
              class="nav-link btn-action-icon"
              @click="sidebarOpen = !sidebarOpen"
              role="button"
              aria-label="Toggle sidebar"
              title="Toggle sidebar"
            >
              <i class="bi bi-list fs-4"></i>
            </button>
          </li>
          <li class="nav-item d-none d-md-block ms-2">
            <router-link
              to="/dashboard"
              class="nav-link"
              :class="{ 'active fw-bold text-danger': $route.path === '/dashboard' || $route.path === '/dashboard/operational' }"
            >
              <i class="bi bi-speedometer2 me-1"></i> Dashboard
            </router-link>
          </li>
          <li class="nav-item d-none d-md-block">
            <router-link
              to="/orders"
              class="nav-link"
              :class="{ 'active fw-bold text-danger': $route.path.startsWith('/orders') }"
            >
              <i class="bi bi-cart3 me-1"></i> Orders
            </router-link>
          </li>
          <li class="nav-item d-none d-md-block">
            <router-link
              to="/inventory/balances"
              class="nav-link"
              :class="{ 'active fw-bold text-danger': $route.path.startsWith('/inventory') }"
            >
              <i class="bi bi-boxes me-1"></i> Stock Balances
            </router-link>
          </li>
        </ul>

        <!-- End Navbar Links -->
        <ul class="navbar-nav ms-auto align-items-center gap-1 gap-sm-2">
          <!-- Quick Barcode Scan Input in Topbar -->
          <li class="nav-item d-none d-lg-block">
            <div class="input-group input-group-sm" style="width: 240px;">
              <input
                type="text"
                v-model="scanInput"
                class="form-control"
                placeholder="Scan Barcode / SKU..."
                @keydown.enter.prevent="openScanModal(scanInput)"
              />
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="openScanModal(scanInput)"
                title="Buka Scanner Barcode"
              >
                <i class="bi bi-upc-scan text-danger"></i>
              </button>
            </div>
          </li>

          <!-- Scanner Trigger on Mobile -->
          <li class="nav-item d-lg-none">
            <button
              class="nav-link btn-action-icon"
              @click="openScanModal('')"
              role="button"
              title="Scanner Barcode"
            >
              <i class="bi bi-upc-scan fs-5 text-danger"></i>
            </button>
          </li>

          <!-- Color Mode Toggle Dropdown -->
          <li class="nav-item dropdown position-relative">
            <button
              class="nav-link btn-action-icon"
              @click="showThemeMenu = !showThemeMenu"
              role="button"
              title="Ubah Tema"
            >
              <i
                :class="[
                  colorMode === 'dark' ? 'bi bi-moon-stars-fill text-warning' : (colorMode === 'auto' ? 'bi bi-circle-half' : 'bi bi-sun-fill text-warning')
                ]"
                class="fs-5"
              ></i>
            </button>
            <div v-show="showThemeMenu" class="dropdown-menu dropdown-menu-end show shadow-sm">
              <button
                type="button"
                class="dropdown-item d-flex align-items-center"
                :class="{ active: colorMode === 'light' }"
                @click="setColorMode('light')"
              >
                <i class="bi bi-sun-fill me-2 opacity-50"></i> Light
              </button>
              <button
                type="button"
                class="dropdown-item d-flex align-items-center"
                :class="{ active: colorMode === 'dark' }"
                @click="setColorMode('dark')"
              >
                <i class="bi bi-moon-stars-fill me-2 opacity-50"></i> Dark
              </button>
              <button
                type="button"
                class="dropdown-item d-flex align-items-center"
                :class="{ active: colorMode === 'auto' }"
                @click="setColorMode('auto')"
              >
                <i class="bi bi-circle-half me-2 opacity-50"></i> Auto
              </button>
            </div>
          </li>

          <!-- Notifications Dropdown Menu -->
          <li class="nav-item dropdown position-relative">
            <button
              class="nav-link btn-action-icon position-relative"
              @click="showNotifMenu = !showNotifMenu"
              role="button"
              title="Notifikasi"
            >
              <i class="bi bi-bell fs-5"></i>
              <span class="navbar-badge badge text-bg-danger">3</span>
            </button>
            <div v-show="showNotifMenu" class="dropdown-menu dropdown-menu-end show shadow notif-dropdown">
              <div class="dropdown-header d-flex justify-content-between align-items-center fw-bold py-2 px-3 border-bottom">
                <span>3 Notifikasi Baru</span>
                <button
                  type="button"
                  class="btn btn-link btn-sm p-0 text-decoration-none fs-8 text-danger fw-normal"
                  @click="showNotifMenu = false"
                >
                  Tandai semua dibaca
                </button>
              </div>
              <div class="notif-list">
                <router-link to="/orders/approvals" class="dropdown-item py-2 border-bottom" @click="showNotifMenu = false">
                  <div class="d-flex align-items-start gap-2">
                    <i class="bi bi-clipboard-check-fill text-warning fs-6 mt-1"></i>
                    <div class="flex-grow-1 text-truncate">
                      <div class="fs-7 fw-bold text-truncate">Order Butuh Persetujuan</div>
                      <div class="fs-8 text-secondary text-truncate">KC Surabaya mengajukan 500 Buku Tabungan</div>
                      <div class="fs-9 text-muted">10 menit yang lalu</div>
                    </div>
                  </div>
                </router-link>
                <router-link to="/inventory/early-warning" class="dropdown-item py-2 border-bottom" @click="showNotifMenu = false">
                  <div class="d-flex align-items-start gap-2">
                    <i class="bi bi-exclamation-triangle-fill text-danger fs-6 mt-1"></i>
                    <div class="flex-grow-1 text-truncate">
                      <div class="fs-7 fw-bold text-truncate">Peringatan EWS Stok Menipis</div>
                      <div class="fs-8 text-secondary text-truncate">KARTU-ATM-SILVER di bawah Safety Stock</div>
                      <div class="fs-9 text-muted">1 jam yang lalu</div>
                    </div>
                  </div>
                </router-link>
                <router-link to="/distribution/shipments" class="dropdown-item py-2 border-bottom" @click="showNotifMenu = false">
                  <div class="d-flex align-items-start gap-2">
                    <i class="bi bi-truck text-info fs-6 mt-1"></i>
                    <div class="flex-grow-1 text-truncate">
                      <div class="fs-7 fw-bold text-truncate">Manifest Ekspedisi Siap Kirim</div>
                      <div class="fs-8 text-secondary text-truncate">Pengiriman ke KC Malang selesai packing</div>
                      <div class="fs-9 text-muted">2 jam yang lalu</div>
                    </div>
                  </div>
                </router-link>
              </div>
              <router-link to="/notifications" class="dropdown-item text-center fs-7 text-danger fw-bold py-2" @click="showNotifMenu = false">
                Lihat Semua Notifikasi
              </router-link>
            </div>
          </li>

          <!-- User Profile Dropdown Menu -->
          <li class="nav-item dropdown position-relative">
            <button
              class="nav-link user-btn d-flex align-items-center gap-2"
              @click="showUserMenu = !showUserMenu"
              role="button"
            >
              <div class="avatar-circle">
                {{ userInitials }}
              </div>
              <span class="d-none d-md-inline fs-7 fw-semibold user-label">{{ authStore.userName }}</span>
            </button>
            <div v-show="showUserMenu" class="dropdown-menu dropdown-menu-end show shadow user-dropdown">
              <div class="user-header bg-danger text-white text-center p-3">
                <div class="avatar-circle-large mx-auto mb-2 shadow-sm">
                  {{ userInitials }}
                </div>
                <p class="mb-0 fw-bold fs-6">{{ authStore.userName }}</p>
                <small class="text-white-50">{{ authStore.userRole }} • {{ authStore.orgName }}</small>
              </div>
              <div class="user-body p-3 fs-7 border-bottom bg-body">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-secondary">Email:</span>
                  <span class="fw-bold font-monospace text-body text-truncate" style="max-width: 150px;">{{ authStore.userEmail }}</span>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-secondary">Unit Kerja:</span>
                  <span class="fw-bold text-body">{{ authStore.orgName }}</span>
                </div>
              </div>
              <div class="user-footer d-flex justify-content-end p-2 bg-body">
                <button type="button" class="btn btn-sm btn-danger" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-1"></i> Sign out
                </button>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Main Container (Sidebar + Content) -->
    <div class="app-body-container">
      <!-- App Sidebar -->
      <aside class="app-sidebar shadow" :data-bs-theme="resolvedTheme">
        <!-- Sidebar Navigation Menu (Complete treeviews matching jatim_php) -->
        <div class="sidebar-wrapper">
          <nav class="mt-2" aria-label="Main navigation">
            <ul class="nav sidebar-menu flex-column">
              
              <!-- 1. Dashboard Overview -->
              <li class="nav-item">
                <router-link
                  to="/dashboard"
                  class="nav-link"
                  :class="{ active: $route.path === '/dashboard' || $route.path === '/dashboard/operational' }"
                >
                  <i class="nav-icon bi bi-speedometer2"></i>
                  <span class="nav-text">Dashboard Overview</span>
                </router-link>
              </li>

              <!-- 2. Permintaan & Order -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.orders }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('orders')" :class="{ active: isOrdersActive }">
                  <i class="nav-icon bi bi-cart3"></i>
                  <span class="nav-text">Permintaan & Order</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.orders }"></i>
                </a>
                <ul v-show="openMenus.orders" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/orders" class="nav-link" :class="{ active: $route.path === '/orders' || $route.path === '/orders/branch' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Order</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/orders/approvals" class="nav-link" :class="{ active: $route.path === '/orders/approvals' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Persetujuan Order</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/emboss" class="nav-link" :class="{ active: $route.path === '/emboss' || $route.path === '/emboss/cards' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Riwayat Berkas</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/emboss/reject-queue" class="nav-link" :class="{ active: $route.path === '/emboss/reject-queue' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Antrean Reject Emboss</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/production" class="nav-link" :class="{ active: $route.path === '/production' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Bon Produksi</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 3. Gudang & Distribusi -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.warehouse }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('warehouse')" :class="{ active: isWarehouseActive }">
                  <i class="nav-icon bi bi-box-seam"></i>
                  <span class="nav-text">Gudang & Distribusi</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.warehouse }"></i>
                </a>
                <ul v-show="openMenus.warehouse" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/warehouse/picking" class="nav-link" :class="{ active: $route.path === '/warehouse/picking' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Antrean Picking</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/warehouse/packing" class="nav-link" :class="{ active: $route.path === '/warehouse/packing' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Antrean Packing</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/distribution/shipments" class="nav-link" :class="{ active: $route.path === '/distribution/shipments' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Pengiriman & Manifest</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 4. Penerimaan -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.receiving }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('receiving')" :class="{ active: isReceivingActive }">
                  <i class="nav-icon bi bi-truck"></i>
                  <span class="nav-text">Penerimaan</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.receiving }"></i>
                </a>
                <ul v-show="openMenus.receiving" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/receiving/po" class="nav-link" :class="{ active: $route.path === '/receiving/po' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Penerimaan PO</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/receiving" class="nav-link" :class="{ active: $route.path === '/receiving' || $route.path.startsWith('/receiving/confirm') }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Penerimaan Cabang</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/receiving/discrepancies" class="nav-link" :class="{ active: $route.path === '/receiving/discrepancies' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Berita Acara Selisih</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 5. Persediaan -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.inventory }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('inventory')" :class="{ active: isInventoryActive }">
                  <i class="nav-icon bi bi-stack"></i>
                  <span class="nav-text">Persediaan</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.inventory }"></i>
                </a>
                <ul v-show="openMenus.inventory" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/inventory/balances" class="nav-link" :class="{ active: $route.path === '/inventory/balances' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Stock Balances</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/reconciliation" class="nav-link" :class="{ active: $route.path === '/inventory/reconciliation' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Rekonsiliasi Stok</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/movement-inquiry" class="nav-link" :class="{ active: $route.path === '/inventory/movement-inquiry' || $route.path === '/inventory/ledger' || $route.path === '/inventory/stock-card' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Inquiry Histori Mutasi</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/initial-stock" class="nav-link" :class="{ active: $route.path === '/inventory/initial-stock' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Saldo Awal Gudang</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/stock-opname" class="nav-link" :class="{ active: $route.path === '/inventory/stock-opname' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Stock Opname Fisik</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/stock-opname/history" class="nav-link" :class="{ active: $route.path === '/inventory/stock-opname/history' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">History Stock Opname</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/early-warning" class="nav-link" :class="{ active: $route.path === '/inventory/early-warning' || $route.path === '/inventory/ews' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Early Warning</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/forecasting" class="nav-link" :class="{ active: $route.path === '/inventory/forecasting' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Forecasting</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/switching" class="nav-link" :class="{ active: $route.path === '/inventory/switching' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Switching Stock</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/inventory/switching/approvals" class="nav-link" :class="{ active: $route.path === '/inventory/switching/approvals' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Persetujuan Switching</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/returns" class="nav-link" :class="{ active: $route.path === '/returns' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Retur Barang</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/destructions" class="nav-link" :class="{ active: $route.path === '/destructions' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Pemusnahan (BA)</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 6. Pengadaan (Procurement) -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.procurement }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('procurement')" :class="{ active: isProcurementActive }">
                  <i class="nav-icon bi bi-bag-check"></i>
                  <span class="nav-text">Pengadaan</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.procurement }"></i>
                </a>
                <ul v-show="openMenus.procurement" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/procurement/pr" class="nav-link" :class="{ active: $route.path === '/procurement/pr' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Purchase Request (PR)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/procurement/approvals/pr" class="nav-link" :class="{ active: $route.path === '/procurement/approvals/pr' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Approval PR</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/procurement/consolidation" class="nav-link" :class="{ active: $route.path === '/procurement/consolidation' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Konsolidasi PR</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/procurement/po" class="nav-link" :class="{ active: $route.path === '/procurement/po' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Purchase Order (PO)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/procurement/approvals/po" class="nav-link" :class="{ active: $route.path === '/procurement/approvals/po' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Approval PO</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 7. Finance -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.finance }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('finance')" :class="{ active: isFinanceActive }">
                  <i class="nav-icon bi bi-cash-stack"></i>
                  <span class="nav-text">Finance</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.finance }"></i>
                </a>
                <ul v-show="openMenus.finance" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/finance/settlements" class="nav-link" :class="{ active: $route.path === '/finance/settlements' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Settlement</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/budgets/early-warning" class="nav-link" :class="{ active: $route.path === '/master/budgets/early-warning' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">EWS Anggaran</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 8. Master Data -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.master }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('master')" :class="{ active: isMasterActive }">
                  <i class="nav-icon bi bi-gear-wide-connected"></i>
                  <span class="nav-text">Master Data</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.master }"></i>
                </a>
                <ul v-show="openMenus.master" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/master/organizations" class="nav-link" :class="{ active: $route.path === '/master/organizations' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Unit Kerja & Gudang</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/items" class="nav-link" :class="{ active: $route.path === '/master/items' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Master Barang (SKU)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/budgets" class="nav-link" :class="{ active: $route.path === '/master/budgets' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Pagu Anggaran</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/accounting" class="nav-link" :class="{ active: $route.path === '/master/accounting' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">COA & Cost Center</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/vendors" class="nav-link" :class="{ active: $route.path === '/master/vendors' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Vendor & Ekspedisi</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/expedition-mappings" class="nav-link" :class="{ active: $route.path === '/master/expedition-mappings' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Pemetaan Ekspedisi</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/users" class="nav-link" :class="{ active: $route.path === '/master/users' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Manajemen Pengguna</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/roles" class="nav-link" :class="{ active: $route.path === '/master/roles' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Manajemen Peran (Role)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/menus" class="nav-link" :class="{ active: $route.path === '/master/menus' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Manajemen Menu</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/master/role-menus" class="nav-link" :class="{ active: $route.path === '/master/role-menus' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Mapping Role & Menu</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 9. Audit Trail Sistem -->
              <li class="nav-item">
                <router-link to="/audit-trail" class="nav-link" :class="{ active: $route.path === '/audit-trail' }">
                  <i class="nav-icon bi bi-shield-check"></i>
                  <span class="nav-text">Audit Trail Sistem</span>
                </router-link>
              </li>

              <!-- 10. Notifikasi -->
              <li class="nav-item">
                <router-link to="/notifications" class="nav-link" :class="{ active: $route.path === '/notifications' }">
                  <i class="nav-icon bi bi-bell"></i>
                  <span class="nav-text">Notifikasi</span>
                  <span class="badge text-bg-danger ms-auto fs-9">3</span>
                </router-link>
              </li>

              <!-- 11. Executive Support System (ESS) -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.ess }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('ess')" :class="{ active: isEssActive }">
                  <i class="nav-icon bi bi-speedometer2 text-danger"></i>
                  <span class="nav-text">Executive Support (ESS)</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.ess }"></i>
                </a>
                <ul v-show="openMenus.ess" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/ess/valuation-budget" class="nav-link" :class="{ active: $route.path === '/ess/valuation-budget' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Valuasi & Anggaran</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/cost-saving" class="nav-link" :class="{ active: $route.path === '/ess/cost-saving' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Efisiensi Biaya Switching</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/inventory-turnover" class="nav-link" :class="{ active: $route.path === '/ess/inventory-turnover' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Perputaran Stok (ITO)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/risk-heatmap" class="nav-link" :class="{ active: $route.path === '/ess/risk-heatmap' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Peta Ketahanan Jaringan</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/service-level" class="nav-link" :class="{ active: $route.path === '/ess/service-level' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Kinerja Layanan (SLA)</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/audit-compliance" class="nav-link" :class="{ active: $route.path === '/ess/audit-compliance' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Akuntabilitas & Audit</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/ess/predictive-budget" class="nav-link" :class="{ active: $route.path === '/ess/predictive-budget' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Proyeksi Belanja Logistik</span>
                    </router-link>
                  </li>
                </ul>
              </li>

              <!-- 12. Laporan & Rekapitulasi -->
              <li class="nav-item has-treeview" :class="{ 'menu-open': openMenus.reports }">
                <a href="#" class="nav-link" @click.prevent="toggleMenu('reports')" :class="{ active: isReportsActive }">
                  <i class="nav-icon bi bi-bar-chart-line"></i>
                  <span class="nav-text">Laporan & Rekapitulasi</span>
                  <i class="nav-arrow bi bi-chevron-right" :class="{ rotated: openMenus.reports }"></i>
                </a>
                <ul v-show="openMenus.reports" class="nav nav-treeview">
                  <li class="nav-item">
                    <router-link to="/reports/stock-valuation" class="nav-link" :class="{ active: $route.path === '/reports/stock-valuation' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Valuasi Persediaan</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/reports/stock-distribution" class="nav-link" :class="{ active: $route.path === '/reports/stock-distribution' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Sebaran Stok Wilayah</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/reports/settlements" class="nav-link" :class="{ active: $route.path === '/reports/settlements' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Rekapitulasi Settlement</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/reports/general-ledger" class="nav-link" :class="{ active: $route.path === '/reports/general-ledger' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Jurnal Buku Besar</span>
                    </router-link>
                  </li>
                  <li class="nav-item">
                    <router-link to="/reports/procurement-coverage" class="nav-link" :class="{ active: $route.path === '/reports/procurement-coverage' }">
                      <i class="nav-icon bi bi-circle"></i>
                      <span class="nav-text">Keterlacakan Pengadaan</span>
                    </router-link>
                  </li>
                </ul>
              </li>

            </ul>
          </nav>
        </div>
      </aside>

      <!-- App Content Area -->
      <main class="app-main">
        <div class="app-content p-3 p-md-4">
          <router-view />
        </div>
      </main>
    </div>

    <!-- Quick Barcode Scanner Modal -->
    <div v-if="showScannerModal" class="scanner-modal-backdrop" @click.self="showScannerModal = false">
      <div class="scanner-modal card shadow-lg">
        <div class="card-header bg-body border-bottom d-flex justify-content-between align-items-center">
          <h5 class="mb-0 fw-bold fs-6">Quick Barcode Scanner (JIMS)</h5>
          <button type="button" class="btn-close-modal" @click="showScannerModal = false" aria-label="Close">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="card-body p-3">
          <p class="fs-8 text-secondary mb-2">
            Scan barcode fisik barang, masukkan SKU atau nomor serial untuk verifikasi instan:
          </p>
          <div class="input-group mb-3">
            <input
              type="text"
              v-model="scanInputModal"
              class="form-control"
              placeholder="Contoh: SKU-KARTU-ATM-001 / BAP-2026-001"
              @keydown.enter.prevent="executeScan"
              autofocus
            />
            <button class="btn btn-danger" type="button" @click="executeScan" :disabled="scanLoading">
              <span v-if="scanLoading" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-search me-1"></i> Scan
            </button>
          </div>

          <!-- Scan Result Alert -->
          <div v-if="scanResult" class="alert alert-success py-2.5 px-3 fs-8 mb-0">
            <div class="fw-bold fs-7 mb-1"><i class="bi bi-check-circle-fill me-1"></i> Item Ditemukan!</div>
            <div><strong>SKU:</strong> {{ scanResult.code }}</div>
            <div><strong>Nama:</strong> {{ scanResult.name }}</div>
            <div><strong>Barcode:</strong> {{ scanResult.barcode || '-' }}</div>
            <div><strong>Kategori:</strong> {{ scanResult.category || '-' }}</div>
            <div class="mt-2">
              <router-link to="/inventory/balances" class="btn btn-sm btn-outline-danger" @click="showScannerModal = false">
                Buka di Stock Balances &rarr;
              </router-link>
            </div>
          </div>
          <div v-else-if="scanError" class="alert alert-danger py-2 px-3 fs-8 mb-0">
            <i class="bi bi-exclamation-octagon-fill me-1"></i> {{ scanError }}
          </div>
        </div>
        <div class="card-footer bg-body-tertiary d-flex justify-content-end align-items-center gap-2 py-2 px-3 border-top">
          <button type="button" class="btn btn-sm btn-secondary" @click="showScannerModal = false">Batal</button>
          <button type="button" class="btn btn-sm btn-danger fw-bold" @click="executeScan" :disabled="scanLoading">
            <span v-if="scanLoading" class="spinner-border spinner-border-sm me-1"></span>
            <i v-else class="bi bi-search me-1"></i> Scan Barcode
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/api/client';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const sidebarOpen = ref(true);
const showThemeMenu = ref(false);
const showNotifMenu = ref(false);
const showUserMenu = ref(false);
const showScannerModal = ref(false);
const scanInput = ref('');
const scanInputModal = ref('');
const scanResult = ref(null);
const scanError = ref(null);
const scanLoading = ref(false);

const colorMode = ref('light');
const resolvedTheme = ref('light');

const openMenus = ref({
  orders: true,
  warehouse: false,
  receiving: false,
  inventory: true,
  procurement: false,
  finance: false,
  master: false,
  ess: false,
  reports: false
});

const isOrdersActive = computed(() => route.path.startsWith('/orders') || route.path.startsWith('/emboss') || route.path.startsWith('/production'));
const isWarehouseActive = computed(() => route.path.startsWith('/warehouse') || route.path.startsWith('/distribution'));
const isReceivingActive = computed(() => route.path.startsWith('/receiving'));
const isInventoryActive = computed(() => route.path.startsWith('/inventory') || route.path.startsWith('/returns') || route.path.startsWith('/destructions'));
const isProcurementActive = computed(() => route.path.startsWith('/procurement'));
const isFinanceActive = computed(() => route.path.startsWith('/finance'));
const isMasterActive = computed(() => route.path.startsWith('/master'));
const isEssActive = computed(() => route.path.startsWith('/ess'));
const isReportsActive = computed(() => route.path.startsWith('/reports'));

const toggleMenu = (key) => {
  openMenus.value[key] = !openMenus.value[key];
};

const userInitials = computed(() => {
  if (!authStore.userName) return 'BJ';
  return authStore.userName.substring(0, 2).toUpperCase();
});

onMounted(() => {
  colorMode.value = localStorage.getItem('lte-theme') || 'light';
  applyTheme(colorMode.value);

  // Auto expand menu based on active route
  if (isOrdersActive.value) openMenus.value.orders = true;
  if (isWarehouseActive.value) openMenus.value.warehouse = true;
  if (isReceivingActive.value) openMenus.value.receiving = true;
  if (isInventoryActive.value) openMenus.value.inventory = true;
  if (isProcurementActive.value) openMenus.value.procurement = true;
  if (isFinanceActive.value) openMenus.value.finance = true;
  if (isMasterActive.value) openMenus.value.master = true;
  if (isEssActive.value) openMenus.value.ess = true;
  if (isReportsActive.value) openMenus.value.reports = true;

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.dropdown')) {
      showThemeMenu.value = false;
      showNotifMenu.value = false;
      showUserMenu.value = false;
    }
  });
});

const setColorMode = (mode) => {
  colorMode.value = mode;
  localStorage.setItem('lte-theme', mode);
  applyTheme(mode);
  showThemeMenu.value = false;
};

const applyTheme = (mode) => {
  let resolved = mode;
  if (mode === 'auto') {
    resolved = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  resolvedTheme.value = resolved;
  document.documentElement.setAttribute('data-bs-theme', resolved);
  document.documentElement.style.colorScheme = resolved;
};

const openScanModal = (code) => {
  scanInputModal.value = code || '';
  scanResult.value = null;
  scanError.value = null;
  showScannerModal.value = true;
  if (code) executeScan();
};

const executeScan = async () => {
  const q = (scanInputModal.value || '').trim();
  if (!q) {
    scanError.value = 'Silakan masukkan kode barcode atau SKU.';
    scanResult.value = null;
    return;
  }

  scanLoading.value = true;
  scanError.value = null;
  scanResult.value = null;

  try {
    const response = await api.get('/master/items');
    const items = response?.data || [];
    const needle = q.toLowerCase();
    const item = items.find((entry) => {
      return [entry.sku, entry.barcode, entry.name]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase() === needle);
    });

    if (!item) {
      scanError.value = 'Barang tidak ditemukan di master data.';
      return;
    }

    scanResult.value = {
      code: item.sku,
      barcode: item.barcode,
      name: item.name,
      category: item.category?.name
    };
  } catch (err) {
    scanError.value = err?.message || err?.error || 'Gagal membaca master data dari server.';
  } finally {
    scanLoading.value = false;
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bs-body-bg, #ffffff);
}

.container-fluid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.navbar-nav {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-link {
  color: var(--bs-body-color, #495057);
  padding: 0.5rem 0.75rem;
  display: flex;
  align-items: center;
  text-decoration: none;
  font-size: 0.8125rem;
  border-radius: 0.25rem;
  transition: color 0.15s ease;
}

.nav-link:hover {
  color: var(--jatim-red, #D9252A);
}

.navbar-badge {
  position: absolute;
  top: 4px;
  right: 2px;
  font-size: 0.625rem;
  padding: 2px 4px;
  border-radius: 9999px;
  line-height: 1;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--jatim-red, #D9252A);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.75rem;
  flex-shrink: 0;
}

.avatar-circle-large {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #ffffff;
  color: var(--jatim-red, #D9252A);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.25rem;
}

.user-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
}

.user-dropdown {
  min-width: 250px;
  padding: 0;
  overflow: hidden;
  position: absolute;
  right: 0;
  top: 100%;
  z-index: 1050;
  background-color: var(--bs-secondary-bg, #ffffff);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-radius: 0.375rem;
}

.notif-dropdown {
  width: 320px;
  padding: 0;
  position: absolute;
  right: 0;
  top: 100%;
  z-index: 1050;
  background-color: var(--bs-secondary-bg, #ffffff);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-radius: 0.375rem;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  background-color: var(--bs-secondary-bg, #ffffff);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-radius: 0.375rem;
  padding: 0.5rem 0;
  z-index: 1050;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.4rem 1rem;
  clear: both;
  font-weight: 400;
  color: var(--bs-body-color, #495057);
  text-align: inherit;
  text-decoration: none;
  background-color: transparent;
  border: 0;
  cursor: pointer;
  font-size: 0.8125rem;
}

.dropdown-item:hover, .dropdown-item.active {
  background-color: var(--bs-tertiary-bg, #eef1f5);
  color: var(--jatim-red, #D9252A);
}

.app-header {
  height: 3.5rem;
  background-color: var(--bs-secondary-bg, #ffffff) !important;
  border-bottom: 1px solid var(--bs-border-color, #e2e8f0);
  display: flex;
  align-items: center;
  padding: 0 1rem;
  position: sticky !important;
  top: 0 !important;
  z-index: 1040 !important;
}

.app-sidebar {
  width: 260px;
  min-width: 260px;
  background-color: #ffffff;
  border-right: 1px solid var(--bs-border-color, #e2e8f0);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 3.5rem) !important;
  max-height: calc(100vh - 3.5rem) !important;
  position: sticky !important;
  top: 3.5rem !important;
  transition: width 0.25s ease;
  z-index: 1030 !important;
}

[data-bs-theme="dark"] .app-sidebar {
  background-color: #17191d !important;
  border-right: 1px solid #23272d;
}

.sidebar-collapsed .app-sidebar {
  width: 70px;
  min-width: 70px;
}

.app-body-container {
  display: flex;
  flex: 1;
}

.app-main {
  flex: 1;
  min-width: 0;
  background-color: var(--bs-body-bg, #ffffff);
}

/* Topbar Brand */
.topbar-brand {
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.topbar-logo-img {
  height: 28px;
  width: auto;
  max-width: 140px;
  object-fit: contain;
}

.sidebar-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-menu .nav-link {
  padding: 0.5rem 0.75rem;
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  border-radius: 0.375rem;
  margin-bottom: 2px;
  color: #334155;
  transition: all 0.15s ease;
}

[data-bs-theme="dark"] .sidebar-menu .nav-link {
  color: #94a3b8;
}

.sidebar-menu .nav-link:hover {
  background-color: #f1f5f9;
  color: #0f172a;
}

[data-bs-theme="dark"] .sidebar-menu .nav-link:hover {
  background-color: rgba(255, 255, 255, 0.06);
  color: #ffffff;
}

.sidebar-menu .nav-link.active {
  background-color: var(--jatim-red, #D9252A) !important;
  color: #ffffff !important;
  font-weight: 700;
}

.nav-icon {
  font-size: 1.05rem;
  width: 1.5rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.5rem;
  flex-shrink: 0;
}

.nav-text {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-arrow {
  font-size: 0.75rem;
  transition: transform 0.2s ease;
}

.nav-arrow.rotated {
  transform: rotate(90deg);
}

.nav-treeview {
  list-style: none;
  padding: 0 0 0 1.25rem;
  margin: 0;
}

.nav-treeview .nav-link {
  font-size: 0.775rem !important;
  padding: 0.35rem 0.5rem !important;
}

.nav-treeview .nav-link.active {
  background-color: rgba(217, 37, 42, 0.12) !important;
  color: var(--jatim-red, #D9252A) !important;
  font-weight: 700;
}

[data-bs-theme="dark"] .nav-treeview .nav-link.active {
  background-color: rgba(217, 37, 42, 0.25) !important;
  color: #ff6b6e !important;
}

/* Scanner Modal */
.scanner-modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.scanner-modal {
  width: 480px;
  max-width: 100%;
}
</style>

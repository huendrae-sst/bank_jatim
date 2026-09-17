<template>
  <div class="app-wrapper" :class="{ 'sidebar-collapsed': !sidebarOpen }">
    <!-- App Header (Navbar) -->
    <nav class="app-header navbar navbar-expand shadow-xs border-bottom">
      <div class="container-fluid">
        <!-- Start Navbar Links -->
        <ul class="navbar-nav align-items-center">
          <!-- Official Logo Bank Jatim + JIMS Badge in Topbar Left -->
          <li class="nav-item me-2 me-md-3">
            <router-link to="/" class="topbar-brand">
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
          <li
            v-for="(shortcut, index) in topbarShortcuts"
            :key="shortcut.code"
            class="nav-item d-none d-md-block"
            :class="{ 'ms-2': index === 0 }"
          >
            <router-link
              :to="shortcut.path"
              class="nav-link"
              :class="{ 'active fw-bold text-danger': ownsCurrentRoute(shortcut) }"
            >
              <i :class="['bi', shortcut.icon, 'me-1']"></i>
              {{ shortcut.title }}
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
        <!-- Sidebar navigation rendered from authenticated role-menu data -->
        <div class="sidebar-wrapper">
          <nav class="mt-2" aria-label="Main navigation">
            <ul class="nav sidebar-menu flex-column">
              
              <li v-if="navigationStore.loading" class="nav-item px-3 py-3 text-secondary fs-8">
                <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                Memuat navigasi...
              </li>

              <li
                v-else-if="navigationGroups.length === 0"
                class="nav-item px-3 py-3 text-secondary fs-8"
              >
                <i class="bi bi-info-circle me-2"></i>
                Tidak ada menu yang diberikan
              </li>

              <template v-else v-for="group in navigationGroups" :key="group.key">
                <li
                  v-if="group.items.length === 1 && group.items[0].children.length === 0"
                  class="nav-item"
                >
                  <router-link
                    :to="group.items[0].path"
                    class="nav-link"
                    :class="{ active: ownsCurrentRoute(group.items[0]) }"
                  >
                    <i :class="['nav-icon', 'bi', group.items[0].icon]"></i>
                    <span class="nav-text">{{ group.items[0].title }}</span>
                  </router-link>
                </li>

                <li
                  v-else
                  class="nav-item has-treeview"
                  :class="{ 'menu-open': openMenus[group.key] }"
                >
                  <a
                    href="#"
                    class="nav-link"
                    :class="{ active: groupOwnsCurrentRoute(group) }"
                    @click.prevent="toggleMenu(group.key)"
                  >
                    <i :class="['nav-icon', 'bi', group.icon]"></i>
                    <span class="nav-text">{{ group.module }}</span>
                    <i
                      class="nav-arrow bi bi-chevron-right"
                      :class="{ rotated: openMenus[group.key] }"
                    ></i>
                  </a>
                  <ul v-show="openMenus[group.key]" class="nav nav-treeview">
                    <template v-for="item in group.items" :key="item.code">
                      <li class="nav-item">
                        <router-link
                          :to="item.path"
                          class="nav-link"
                          :class="{ active: ownsCurrentRoute(item) }"
                        >
                          <i :class="['nav-icon', 'bi', item.icon]"></i>
                          <span class="nav-text">{{ item.title }}</span>
                        </router-link>
                      </li>
                      <li
                        v-for="child in item.children"
                        :key="child.code"
                        class="nav-item ms-3"
                      >
                        <router-link
                          :to="child.path"
                          class="nav-link"
                          :class="{ active: ownsCurrentRoute(child) }"
                        >
                          <i :class="['nav-icon', 'bi', child.icon]"></i>
                          <span class="nav-text">{{ child.title }}</span>
                        </router-link>
                      </li>
                    </template>
                  </ul>
                </li>
              </template>

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
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useNavigationStore } from '@/stores/navigation';
import api from '@/api/client';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const navigationStore = useNavigationStore();

const sidebarOpen = ref(true);
const showThemeMenu = ref(false);
const showUserMenu = ref(false);
const showScannerModal = ref(false);
const scanInput = ref('');
const scanInputModal = ref('');
const scanResult = ref(null);
const scanError = ref(null);
const scanLoading = ref(false);

const colorMode = ref('light');
const resolvedTheme = ref('light');

const openMenus = ref({});
const navigationGroups = computed(() => navigationStore.groups);
const topbarShortcuts = computed(() => navigationStore.accessibleItems.slice(0, 3));
const currentOwner = computed(() => navigationStore.resolveRoute(route.path));

const ownsCurrentRoute = (item) => currentOwner.value?.code === item.code;

const groupOwnsCurrentRoute = (group) => group.items.some((item) =>
  ownsCurrentRoute(item) || item.children.some(ownsCurrentRoute)
);

const toggleMenu = (key) => {
  openMenus.value[key] = !openMenus.value[key];
};

watch(
  [() => route.path, navigationGroups],
  () => {
    const activeGroup = navigationGroups.value.find(groupOwnsCurrentRoute);
    if (activeGroup) openMenus.value[activeGroup.key] = true;
  },
  { immediate: true }
);

const userInitials = computed(() => {
  if (!authStore.userName) return 'BJ';
  return authStore.userName.substring(0, 2).toUpperCase();
});

onMounted(() => {
  colorMode.value = localStorage.getItem('lte-theme') || 'light';
  applyTheme(colorMode.value);

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.dropdown')) {
      showThemeMenu.value = false;
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
  navigationStore.clear();
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

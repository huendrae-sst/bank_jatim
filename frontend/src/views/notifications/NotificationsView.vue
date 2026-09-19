<template>
  <div class="notifications-page">
    <div class="app-content-header py-2 px-3 mb-3 border-bottom bg-body rounded-3 shadow-xs d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
      <div>
        <h3 class="mb-0 text-body fw-bold">Pusat Pemberitahuan & Notifikasi</h3>
      </div>
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="markAllRead">
          Tandai Semua Dibaca
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger fs-8">{{ errorMessage }}</div>

    <!-- Notification List Card -->
    <div class="card card-outline card-danger shadow-xs">
      <div class="card-header border-bottom p-3">
        <h3 class="card-title fw-semibold mb-0 fs-6">Daftar Notifikasi Terbaru</h3>
      </div>

      <div class="list-group list-group-flush">
        <div
          v-for="n in paginatedNotifs"
          :key="n.id"
          class="list-group-item p-3 d-flex align-items-start gap-3 border-bottom"
          :class="{ 'bg-body-tertiary': !n.isRead }"
        >
          <div class="rounded-circle p-2 d-flex align-items-center justify-content-center text-white flex-shrink-0" :class="n.badgeClass" style="width: 36px; height: 36px;">
            <i :class="['bi', n.icon, 'fs-5']"></i>
          </div>
          <div class="flex-grow-1 min-w-0">
            <div class="d-flex justify-content-between align-items-center">
              <h6 class="fw-bold mb-0 text-body fs-7">{{ n.title }}</h6>
              <span class="fs-9 text-secondary font-monospace">{{ n.time }}</span>
            </div>
            <p class="fs-8 text-secondary mb-1 mt-0.5">{{ n.message }}</p>
            <router-link :to="n.link" class="btn btn-sm btn-outline-danger fs-9 fw-bold">
              Buka Transaksi
            </router-link>
          </div>
        </div>
        <div v-if="notifs.length === 0" class="list-group-item p-4 text-center text-secondary fs-8">
          <i class="bi bi-inbox fs-3 d-block mb-1"></i>
          Belum ada notifikasi dari backend.
        </div>
      </div>

      <!-- Standardized Pagination Footer -->
      <PaginationFooter
        :total="notifs.length"
        v-model:currentPage="currentPage"
        v-model:perPage="perPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/api/client';

const currentPage = ref(1);
const perPage = ref(10);
const errorMessage = ref('');

const iconByType = (type) => {
  switch (type) {
    case 'ACTION_REQUIRED':
    case 'APPROVAL_REQUEST':
      return 'bi-clipboard-check-fill';
    case 'ALERT':
    case 'LOW_STOCK_WARNING':
      return 'bi-exclamation-triangle-fill';
    case 'SHIPMENT_DELIVERED':
      return 'bi-truck';
    case 'SETTLEMENT_POSTED':
      return 'bi-cash-coin';
    default:
      return 'bi-info-circle-fill';
  }
};

const badgeByPriority = (priority) => {
  switch (priority) {
    case 'CRITICAL':
    case 'HIGH':
      return 'bg-danger';
    case 'WARNING':
    case 'MEDIUM':
      return 'bg-warning text-dark';
    default:
      return 'bg-info text-dark';
  }
};

const formatTime = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('id-ID', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
};

const mapNotification = (notification) => ({
  id: notification.id,
  title: notification.title || '-',
  message: notification.message || '-',
  icon: iconByType(notification.type),
  badgeClass: badgeByPriority(notification.priority),
  time: formatTime(notification.createdAt),
  link: notification.link || notification.actionUrl || '#',
  isRead: Boolean(notification.isRead)
});

const notifs = ref([]);

const loadNotifications = async () => {
  errorMessage.value = '';
  try {
    const response = await api.get('/notifications');
    const items = response.data?.data || response.data || [];
    if (Array.isArray(items) && items.length > 0) {
      notifs.value = items.map(mapNotification);
    }
  } catch (error) {
    notifs.value = [];
    console.warn('Backend /notifications unavailable:', error);
  }
};

const paginatedNotifs = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return notifs.value.slice(start, start + perPage.value);
});

const markAllRead = async () => {
  errorMessage.value = '';
  try {
    await api.post('/notifications/mark-all-read');
    await loadNotifications();
  } catch (error) {
    errorMessage.value = error?.message || error?.error || 'Gagal menandai notifikasi dibaca.';
  }
};

onMounted(loadNotifications);
</script>

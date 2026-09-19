<template>
  <main class="access-page d-flex align-items-center justify-content-center p-4">
    <section class="card border-0 shadow-sm access-card text-center">
      <div class="card-body p-4 p-md-5">
        <div class="access-icon mx-auto mb-3">
          <i class="bi bi-shield-lock fs-2"></i>
        </div>
        <p class="text-uppercase text-danger fw-bold fs-9 letter-spacing mb-2">Akses dibatasi</p>
        <h1 class="h4 fw-bold mb-2">Halaman tidak dapat dibuka</h1>
        <p class="text-secondary mb-4">{{ message }}</p>
        <router-link
          v-if="navigationStore.firstAccessiblePath"
          :to="navigationStore.firstAccessiblePath"
          class="btn btn-danger"
        >
          Kembali ke menu utama
        </router-link>
        <router-link v-else to="/login" class="btn btn-outline-secondary">
          Kembali ke login
        </router-link>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useNavigationStore } from '@/stores/navigation';

const route = useRoute();
const navigationStore = useNavigationStore();

const messages = {
  unavailable: 'Navigasi tidak dapat dimuat. Silakan coba lagi.',
  empty: 'Belum ada menu yang diberikan untuk akun ini.',
  unmatched: 'Halaman ini belum terdaftar pada konfigurasi menu.',
  forbidden: 'Anda tidak memiliki akses ke halaman ini.'
};

const message = computed(() =>
  messages[route.query.reason] || messages.forbidden
);
</script>

<style scoped>
.access-page {
  min-height: 100vh;
  background: var(--bs-tertiary-bg);
}

.access-card {
  width: min(100%, 440px);
}

.access-icon {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  color: var(--bs-danger);
  background: rgba(var(--bs-danger-rgb), 0.1);
  border-radius: 50%;
}

.letter-spacing {
  letter-spacing: 0.08em;
}
</style>

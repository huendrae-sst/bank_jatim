<template>
  <Transition name="fade-center">
    <div
      v-if="isLoading"
      class="global-loading-indicator"
      role="status"
      aria-live="polite"
      aria-label="Memuat data dari server"
    >
      <div class="loading-pill shadow-lg">
        <span class="spinner-ring" aria-hidden="true"></span>
        <span class="loading-label">{{ message }}</span>
        <span v-if="count > 1" class="request-counter" :title="count + ' permintaan aktif'">
          {{ count }}
        </span>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue';
import { globalLoading } from '@/stores/loading';

const isLoading = computed(() => globalLoading.isLoading);
const message = computed(() => globalLoading.message);
const count = computed(() => globalLoading.count);
</script>

<style scoped>
.global-loading-indicator {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 9999;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  background-color: var(--bs-body-bg, #ffffff);
  color: var(--bs-body-color, #1e293b);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-left: 4px solid var(--jatim-red, #D9252A);
  padding: 0.65rem 1.2rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
  box-shadow: 0 20px 35px -5px rgba(0, 0, 0, 0.15), 0 10px 15px -5px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

[data-bs-theme="dark"] .loading-pill {
  background-color: rgba(26, 29, 33, 0.96);
  border-color: #2e343b;
  border-left-color: #D9252A;
  color: #f8fafc;
}

.spinner-ring {
  width: 18px;
  height: 18px;
  border: 2.5px solid rgba(217, 37, 42, 0.2);
  border-top-color: var(--jatim-red, #D9252A);
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
  display: inline-block;
  flex-shrink: 0;
}

.loading-label {
  letter-spacing: 0.01em;
  font-size: 0.8125rem;
  white-space: nowrap;
}

.request-counter {
  font-size: 0.6875rem;
  font-weight: 700;
  background-color: rgba(217, 37, 42, 0.12);
  color: var(--jatim-red, #D9252A);
  padding: 0.15rem 0.45rem;
  border-radius: 9999px;
  line-height: 1;
}

[data-bs-theme="dark"] .request-counter {
  background-color: rgba(217, 37, 42, 0.25);
  color: #ff6b6e;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Center Scale & Fade Transition */
.fade-center-enter-active,
.fade-center-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-center-enter-from,
.fade-center-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}
</style>

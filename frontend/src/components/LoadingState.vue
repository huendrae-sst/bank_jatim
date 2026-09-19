<template>
  <!-- Wrapper / Overlay Mode (when wrapping default slot content) -->
  <div v-if="$slots.default" class="loading-state-container position-relative">
    <slot />

    <Transition name="fade-overlay">
      <div
        v-if="isActive"
        class="loading-overlay d-flex flex-column align-items-center justify-content-center"
        :class="{ 'backdrop-blur': blur }"
      >
        <div class="loading-spinner-box text-center p-3">
          <div
            class="spinner-border text-danger mb-2"
            :class="spinnerSizeClass"
            role="status"
          >
            <span class="visually-hidden">Loading...</span>
          </div>
          <div class="fw-semibold text-body fs-7">{{ displayMessage }}</div>
          <small v-if="subtext" class="text-secondary fs-8 mt-1 d-block">{{ subtext }}</small>
        </div>
      </div>
    </Transition>
  </div>

  <!-- Fullscreen Mode -->
  <Teleport to="body" v-else-if="mode === 'fullscreen'">
    <Transition name="fade-overlay">
      <div
        v-if="isActive"
        class="loading-fullscreen-backdrop d-flex flex-column align-items-center justify-content-center"
      >
        <div class="loading-dialog card shadow-lg border-0 text-center p-4">
          <div class="spinner-border text-danger mx-auto mb-3" style="width: 2.5rem; height: 2.5rem;" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <div class="fw-bold text-body fs-6 mb-1">{{ displayMessage }}</div>
          <div class="text-secondary fs-8">{{ subtext || 'Mohon tunggu beberapa saat...' }}</div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- Inline / Card Standalone Mode -->
  <div
    v-else
    v-show="isActive"
    class="loading-inline-box d-flex flex-column align-items-center justify-content-center text-center p-4"
    :class="[cardClass]"
    :style="{ minHeight: minHeight }"
  >
    <div
      class="spinner-border text-danger mb-2"
      :class="spinnerSizeClass"
      role="status"
    >
      <span class="visually-hidden">Loading...</span>
    </div>
    <div class="fw-semibold text-body fs-7">{{ displayMessage }}</div>
    <small v-if="subtext" class="text-secondary fs-8 mt-1 d-block">{{ subtext }}</small>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { globalLoading } from '@/stores/loading';

const props = defineProps({
  /**
   * Explicit loading boolean. If not provided or null, can automatically track global server state if autoServer is true.
   */
  loading: {
    type: Boolean,
    default: null
  },
  /**
   * Automatically bind to global server loading state when prop 'loading' is null.
   */
  autoServer: {
    type: Boolean,
    default: false
  },
  /**
   * Main message displayed under the spinner.
   */
  message: {
    type: String,
    default: ''
  },
  /**
   * Additional hint or subtext.
   */
  subtext: {
    type: String,
    default: ''
  },
  /**
   * Layout mode: 'inline' | 'card' | 'fullscreen' | 'overlay'
   */
  mode: {
    type: String,
    default: 'inline'
  },
  /**
   * Spinner size: 'sm' | 'md' | 'lg'
   */
  size: {
    type: String,
    default: 'md'
  },
  /**
   * Minimum height for inline mode.
   */
  minHeight: {
    type: String,
    default: '180px'
  },
  /**
   * Apply backdrop blur effect on overlay.
   */
  blur: {
    type: Boolean,
    default: true
  }
});

const isActive = computed(() => {
  if (props.loading !== null) {
    return Boolean(props.loading);
  }
  if (props.autoServer) {
    return globalLoading.isLoading;
  }
  // Default to true if used as a standalone placeholder component without props
  return true;
});

const displayMessage = computed(() => {
  if (props.message) return props.message;
  if (props.autoServer && globalLoading.message) return globalLoading.message;
  return 'Memuat data dari server...';
});

const spinnerSizeClass = computed(() => {
  if (props.size === 'sm') return 'spinner-border-sm';
  if (props.size === 'lg') return 'spinner-large';
  return '';
});

const cardClass = computed(() => {
  if (props.mode === 'card') {
    return 'card border rounded-3 bg-body-tertiary';
  }
  return '';
});
</script>

<style scoped>
.loading-state-container {
  min-height: 50px;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 20;
  background-color: rgba(255, 255, 255, 0.75);
  border-radius: inherit;
  transition: opacity 0.2s ease;
}

[data-bs-theme="dark"] .loading-overlay {
  background-color: rgba(18, 20, 23, 0.82);
}

.backdrop-blur {
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}

.loading-spinner-box {
  background: var(--bs-body-bg, #ffffff);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-radius: 0.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-width: 220px;
  max-width: 90%;
}

.spinner-large {
  width: 2.5rem;
  height: 2.5rem;
}

.loading-fullscreen-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 10000;
}

.loading-dialog {
  width: 320px;
  max-width: 90%;
  border-radius: 0.75rem;
}

/* Transitions */
.fade-overlay-enter-active,
.fade-overlay-leave-active {
  transition: opacity 0.2s ease;
}

.fade-overlay-enter-from,
.fade-overlay-leave-to {
  opacity: 0;
}
</style>

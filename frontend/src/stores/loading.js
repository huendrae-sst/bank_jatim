import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

const DEFAULT_MESSAGE = 'Memuat data dari server...';

// Reactive singleton state
const activeRequests = ref(0);
const loadingMessage = ref(DEFAULT_MESSAGE);

export const useLoadingStore = defineStore('loading', () => {
  const isLoading = computed(() => activeRequests.value > 0);
  const count = computed(() => activeRequests.value);

  const startLoading = (message) => {
    activeRequests.value++;
    if (message) {
      loadingMessage.value = message;
    } else if (activeRequests.value === 1) {
      loadingMessage.value = DEFAULT_MESSAGE;
    }
  };

  const stopLoading = () => {
    if (activeRequests.value > 0) {
      activeRequests.value--;
    }
    if (activeRequests.value === 0) {
      loadingMessage.value = DEFAULT_MESSAGE;
    }
  };

  const forceStop = () => {
    activeRequests.value = 0;
    loadingMessage.value = DEFAULT_MESSAGE;
  };

  return {
    isLoading,
    count,
    loadingMessage,
    startLoading,
    stopLoading,
    forceStop
  };
});

// Standalone export for non-component or interceptor contexts
export const globalLoading = {
  get isLoading() {
    return activeRequests.value > 0;
  },
  get count() {
    return activeRequests.value;
  },
  get message() {
    return loadingMessage.value;
  },
  start(message) {
    activeRequests.value++;
    if (message) {
      loadingMessage.value = message;
    } else if (activeRequests.value === 1) {
      loadingMessage.value = DEFAULT_MESSAGE;
    }
  },
  stop() {
    if (activeRequests.value > 0) {
      activeRequests.value--;
    }
    if (activeRequests.value === 0) {
      loadingMessage.value = DEFAULT_MESSAGE;
    }
  },
  forceStop() {
    activeRequests.value = 0;
    loadingMessage.value = DEFAULT_MESSAGE;
  },
  activeRequests,
  loadingMessage
};

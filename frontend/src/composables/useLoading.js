import { ref, computed } from 'vue';
import { globalLoading } from '../stores/loading.js';

/**
 * Composable for server loading management.
 * Provides both access to global server request state and local loading utilities.
 */
export function useLoading(initialState = false) {
  const localLoading = ref(initialState);
  const localError = ref(null);

  // Global state access
  const isServerLoading = computed(() => globalLoading.isLoading);
  const activeRequestsCount = computed(() => globalLoading.count);
  const serverLoadingMessage = computed(() => globalLoading.message);

  /**
   * Wraps an async operation with local and/or global loading lifecycle.
   * @param {Function} asyncFn - The async function to execute.
   * @param {Object} [options]
   * @param {string} [options.message] - Custom message to display.
   * @param {boolean} [options.global=true] - Whether to also trigger global server loading indicator.
   * @returns {Promise<any>} Result of the async function.
   */
  const withLoading = async (asyncFn, options = {}) => {
    const { message, global = true } = options;
    localLoading.value = true;
    localError.value = null;

    if (global) {
      globalLoading.start(message);
    }

    try {
      return await asyncFn();
    } catch (err) {
      localError.value = err;
      throw err;
    } finally {
      localLoading.value = false;
      if (global) {
        globalLoading.stop();
      }
    }
  };

  return {
    // Local loading state
    loading: localLoading,
    error: localError,
    withLoading,

    // Global loading state
    isServerLoading,
    activeRequestsCount,
    serverLoadingMessage,

    // Manual controls
    startGlobalLoading: (msg) => globalLoading.start(msg),
    stopGlobalLoading: () => globalLoading.stop(),
    forceStopGlobalLoading: () => globalLoading.forceStop()
  };
}

export default useLoading;

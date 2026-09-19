import ToastEventBus from 'primevue/toasteventbus';

const DEFAULT_LIFE = 4000;

/**
 * Global reusable Toast Notification utility.
 * Can be called anywhere across the application (inside Vue components,
 * Axios interceptors, Vue Router guards, Pinia stores, and utility scripts).
 */
export const toast = {
  /**
   * Display a generic toast message
   * @param {Object} options
   * @param {'success'|'info'|'warn'|'error'} [options.severity='info']
   * @param {string} [options.summary]
   * @param {string} [options.detail]
   * @param {number} [options.life=4000]
   */
  show(options = {}) {
    const {
      severity = 'info',
      summary = '',
      detail = '',
      life = DEFAULT_LIFE,
      ...rest
    } = options;

    ToastEventBus.emit('add', {
      severity,
      summary,
      detail,
      life,
      ...rest
    });
  },

  /**
   * Display a success toast
   * @param {string} detail - Message detail
   * @param {string} [summary='Berhasil'] - Toast title
   * @param {Object} [options]
   */
  success(detail, summary = 'Berhasil', options = {}) {
    this.show({ severity: 'success', summary, detail, ...options });
  },

  /**
   * Display an error toast
   * @param {string} detail - Message detail
   * @param {string} [summary='Gagal'] - Toast title
   * @param {Object} [options]
   */
  error(detail, summary = 'Gagal', options = {}) {
    this.show({ severity: 'error', summary, detail, ...options });
  },

  /**
   * Display a warning toast
   * @param {string} detail - Message detail
   * @param {string} [summary='Peringatan'] - Toast title
   * @param {Object} [options]
   */
  warn(detail, summary = 'Peringatan', options = {}) {
    this.show({ severity: 'warn', summary, detail, ...options });
  },

  /**
   * Display an informational toast
   * @param {string} detail - Message detail
   * @param {string} [summary='Informasi'] - Toast title
   * @param {Object} [options]
   */
  info(detail, summary = 'Informasi', options = {}) {
    this.show({ severity: 'info', summary, detail, ...options });
  },

  /**
   * Clear all active toasts
   */
  clear() {
    ToastEventBus.emit('remove-all-groups');
  }
};

export default toast;

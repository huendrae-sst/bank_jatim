export function createNavigationState() {
  return {
    items: [],
    loading: false,
    loaded: false,
    loadedForUser: null,
    error: null
  };
}

export async function ensureNavigationLoaded(state, loader, userKey) {
  if (state.loaded && state.loadedForUser === userKey) {
    return state.items;
  }

  state.items = [];
  state.loading = true;
  state.loaded = false;
  state.loadedForUser = null;
  state.error = null;

  try {
    const items = await loader();
    if (!Array.isArray(items)) {
      throw new Error('Format respons navigasi tidak valid.');
    }
    state.items = items;
    state.loaded = true;
    state.loadedForUser = userKey;
    return items;
  } catch (error) {
    state.items = [];
    state.error = error?.message || 'Gagal memuat navigasi.';
    throw error;
  } finally {
    state.loading = false;
  }
}

export function clearNavigationState(state) {
  Object.assign(state, createNavigationState());
}

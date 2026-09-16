import { defineStore } from 'pinia';

import api from '@/api/client';
import { createNavigationApi } from '@/services/navigationApi';
import {
  buildNavigationGroups,
  firstAccessiblePath as selectFirstAccessiblePath,
  resolveNavigationItem
} from '@/utils/navigationPolicy';
import {
  clearNavigationState,
  createNavigationState,
  ensureNavigationLoaded
} from '@/stores/navigationCore';

const navigationApi = createNavigationApi(api);

export const useNavigationStore = defineStore('navigation', {
  state: () => createNavigationState(),

  getters: {
    accessibleItems: (state) => state.items
      .filter((item) => item.allowed === true)
      .sort((left, right) => (Number(left.order) || 0) - (Number(right.order) || 0)),
    groups: (state) => buildNavigationGroups(state.items),
    firstAccessiblePath: (state) => selectFirstAccessiblePath(state.items),
    resolveRoute: (state) => (path) => resolveNavigationItem(state.items, path)
  },

  actions: {
    async ensureLoaded(userKey) {
      return ensureNavigationLoaded(
        this,
        async () => {
          const response = await navigationApi.getNavigation();
          return response?.data ?? response;
        },
        userKey
      );
    },

    clear() {
      clearNavigationState(this);
    }
  }
});

import { defineStore } from 'pinia';
import api from '@/api/client';
import { extractList } from '@/utils/responseParser';

export const MODULE_CATEGORIES = [
  'Dashboard',
  'Permintaan & Order',
  'Gudang & Distribusi',
  'Penerimaan',
  'Persediaan',
  'Pengadaan',
  'Finance',
  'Master Data',
  'Audit & Keamanan',
  'Notifikasi',
  'Executive Support (ESS)',
  'Laporan & Rekapitulasi'
];

const responseData = (response) => response?.data?.data ?? response?.data ?? null;

export const useMenuStore = defineStore('menu', {
  state: () => ({ menus: [], loading: false, error: null }),

  getters: {
    totalMenus: (state) => state.menus.length,
    activeMenus: (state) => state.menus.filter((menu) => menu.status === 'AKTIF'),
    moduleList: (state) => [...new Set(state.menus.map((menu) => menu.module).filter(Boolean))],
    menusByModule: (state) => {
      const groups = {};
      state.menus.forEach((item) => {
        const moduleName = item.module || 'Lainnya';
        (groups[moduleName] ||= []).push(item);
      });
      Object.values(groups).forEach((items) => items.sort((a, b) => (a.order || 0) - (b.order || 0)));
      return groups;
    },
    hasAccess: (state) => (menuCode, roleCode) => {
      if (!roleCode || roleCode === 'SUPER_ADMIN') return true;
      const menu = state.menus.find((item) => item.code === menuCode);
      return Boolean(menu && Array.isArray(menu.roles) && menu.roles.includes(roleCode));
    },
    orderedModules: (state) => {
      const groups = {};
      state.menus.forEach((item) => {
        const moduleName = item.module || 'Lainnya';
        if (!groups[moduleName]) {
          groups[moduleName] = { module: moduleName, minOrder: item.order || 0, items: [] };
        }
        groups[moduleName].minOrder = Math.min(groups[moduleName].minOrder, item.order || 0);
        groups[moduleName].items.push(item);
      });
      const modules = Object.values(groups).sort((a, b) => a.minOrder - b.minOrder);
      modules.forEach((group) => group.items.sort((a, b) => (a.order || 0) - (b.order || 0)));
      return modules;
    }
  },

  actions: {
    async fetchMenus() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.get('/master/menus');
        const menus = extractList(response);
        if (!menus) throw new Error('Format respons menu dari backend tidak valid.');
        this.menus = menus;
        return this.menus;
      } catch (error) {
        this.menus = [];
        this.error = error?.message || 'Gagal memuat menu dari backend.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async addMenu(menuData) {
      const response = await api.post('/master/menus', menuData);
      const created = responseData(response);
      if (!created) throw new Error('Backend tidak mengembalikan menu yang dibuat.');
      this.menus.push(created);
      this.menus.sort((a, b) => (a.order || 0) - (b.order || 0));
      return created;
    },

    async updateMenu(idOrCode, menuData) {
      const response = await api.put(`/master/menus/${encodeURIComponent(idOrCode)}`, menuData);
      const updated = responseData(response);
      if (!updated) throw new Error('Backend tidak mengembalikan menu yang diperbarui.');
      const index = this.menus.findIndex((menu) => String(menu.id) === String(idOrCode) || menu.code === idOrCode);
      if (index >= 0) this.menus[index] = updated;
      this.menus.sort((a, b) => (a.order || 0) - (b.order || 0));
      return updated;
    },

    async deleteMenu(idOrCode) {
      await api.delete(`/master/menus/${encodeURIComponent(idOrCode)}`);
      this.menus = this.menus.filter(
        (menu) => String(menu.id) !== String(idOrCode) && menu.code !== idOrCode
      );
    },

    async toggleStatus(idOrCode) {
      const response = await api.patch(`/master/menus/${encodeURIComponent(idOrCode)}/status`);
      const status = responseData(response);
      const menu = this.menus.find((item) => String(item.id) === String(idOrCode) || item.code === idOrCode);
      if (menu) menu.status = status;
      return status;
    },

    async reorderModuleItems(moduleName, newOrderedItems) {
      const existingOrders = this.menus
        .filter((menu) => menu.module === moduleName)
        .map((menu) => Number(menu.order) || 1);
      const firstOrder = existingOrders.length ? Math.min(...existingOrders) : 1;
      const updates = newOrderedItems.map((item, index) => ({
        ...item,
        module: moduleName,
        order: firstOrder + index
      }));
      try {
        for (const item of updates) {
          await this.updateMenu(item.id || item.code, item);
        }
      } finally {
        await this.fetchMenus();
      }
    },

    async updateRoleMenus(roleCode, selectedMenuCodes) {
      const response = await api.put(`/master/roles/${encodeURIComponent(roleCode)}/menus`, selectedMenuCodes);
      const assignedCodes = responseData(response) || [];
      const assigned = new Set(assignedCodes);
      this.menus.forEach((menu) => {
        const roles = new Set(Array.isArray(menu.roles) ? menu.roles : []);
        if (roleCode === 'SUPER_ADMIN' || assigned.has(menu.code)) roles.add(roleCode);
        else roles.delete(roleCode);
        menu.roles = [...roles];
      });
      return assignedCodes;
    }
  }
});

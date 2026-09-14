import { defineStore } from 'pinia';
import api from '@/api/client';
import { extractList } from '@/utils/responseParser';

const normalizeRole = (item, index) => {
  if (typeof item === 'string') {
    return {
      id: item,
      code: item,
      name: item.replace(/_/g, ' '),
      category: 'Sistem',
      description: '',
      status: 'AKTIF'
    };
  }
  return { id: item.id ?? item.code ?? String(index + 1), ...item };
};

export const useRoleStore = defineStore('role', {
  state: () => ({ roles: [], loading: false, error: null }),

  getters: {
    roleCodes: (state) => state.roles.map((role) => role.code),
    activeRoles: (state) => state.roles.filter((role) => role.status === 'AKTIF'),
    categories: (state) => [...new Set(state.roles.map((role) => role.category).filter(Boolean))],
    getRoleByCode: (state) => (code) => state.roles.find((role) => role.code === code)
  },

  actions: {
    async fetchRoles() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.get('/master/roles');
        const roles = extractList(response);
        if (!roles) throw new Error('Format respons role dari backend tidak valid.');
        this.roles = roles.map(normalizeRole);
        return this.roles;
      } catch (error) {
        this.roles = [];
        this.error = error?.message || 'Gagal memuat role dari backend.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async addRole() {
      throw new Error('Backend belum menyediakan endpoint untuk menambah role.');
    },

    async updateRole() {
      throw new Error('Backend belum menyediakan endpoint untuk mengubah role.');
    },

    async deleteRole() {
      throw new Error('Backend belum menyediakan endpoint untuk menghapus role.');
    }
  }
});

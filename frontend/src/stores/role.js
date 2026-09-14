import { defineStore } from 'pinia';
import api from '@/api/client';
import { createRoleApi } from '@/services/roleApi';
import { extractList } from '@/utils/responseParser';
import { normalizeRole } from '@/utils/roleMapper';

const roleApi = createRoleApi(api);

export const useRoleStore = defineStore('role', {
  state: () => ({ roles: [], loading: false, error: null }),

  getters: {
    roleCodes: (state) => state.roles.map((role) => role.code),
    activeRoles: (state) => state.roles,
    getRoleByCode: (state) => (code) => state.roles.find((role) => role.code === code)
  },

  actions: {
    async fetchRoles() {
      this.loading = true;
      this.error = null;
      try {
        const response = await roleApi.list();
        const roles = extractList(response);
        if (!roles) throw new Error('Format respons role dari backend tidak valid.');
        this.roles = roles.map((role) => normalizeRole(role));
        return this.roles;
      } catch (error) {
        this.roles = [];
        this.error = error?.message || 'Gagal memuat role dari backend.';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async addRole(roleData) {
      await roleApi.create({
        code: roleData.code,
        name: roleData.name,
        description: roleData.description || null
      });
      return this.fetchRoles();
    },

    async updateRole(code, roleData) {
      await roleApi.update(code, {
        name: roleData.name,
        description: roleData.description || null
      });
      return this.fetchRoles();
    },

    async deleteRole(code) {
      await roleApi.remove(code);
      return this.fetchRoles();
    }
  }
});

import { defineStore } from 'pinia';
import api from '@/api/client';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('jims_jwt_token') || '',
    user: JSON.parse(localStorage.getItem('jims_user') || 'null'),
  }),
  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    userRole: (state) => state.user?.role || 'GUEST',
    isSuperAdmin: (state) => state.user?.role === 'SUPER_ADMIN',
    userName: (state) => state.user?.name || 'Pengguna JIMS',
    userEmail: (state) => state.user?.email || '-',
    orgName: (state) => state.user?.organizationName || 'Bank Jatim',
  },
  actions: {
    async login(email, password) {
      try {
        const res = await api.post('/auth/login', { email, password });
        if (res.success && res.data) {
          this.setSession(res.data.token, res.data);
          return { success: true };
        }
        return { success: false, message: res.message };
      } catch (err) {
        return {
          success: false,
          message: err?.message || err?.error || 'Gagal menghubungi server API.'
        };
      }
    },
    setSession(token, user) {
      this.token = token;
      this.user = user;
      localStorage.setItem('jims_jwt_token', token);
      localStorage.setItem('jims_user', JSON.stringify(user));
    },
    logout() {
      this.token = '';
      this.user = null;
      localStorage.removeItem('jims_jwt_token');
      localStorage.removeItem('jims_user');
    }
  }
});

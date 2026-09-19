import axios from 'axios';
import { globalLoading } from '../stores/loading.js';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Request Interceptor: Attach JWT Token & Start Loading
api.interceptors.request.use(
  (config) => {
    if (!config.skipLoading && !config.silent) {
      globalLoading.start(config.loadingMessage);
    }

    const token = localStorage.getItem('jims_jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    if (!error?.config?.skipLoading && !error?.config?.silent) {
      globalLoading.stop();
    }
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle Global Errors & Stop Loading
api.interceptors.response.use(
  (response) => {
    if (!response?.config?.skipLoading && !response?.config?.silent) {
      globalLoading.stop();
    }
    return response.data;
  },
  (error) => {
    if (!error?.config?.skipLoading && !error?.config?.silent) {
      globalLoading.stop();
    }

    if (error.response && error.response.status === 401) {
      localStorage.removeItem('jims_jwt_token');
      localStorage.removeItem('jims_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error.response?.data || error.message);
  }
);

export default api;

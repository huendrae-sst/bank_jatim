<template>
  <div class="login-page">
    <main class="login-box">
      <!-- Login Logo -->
      <div class="login-logo text-center mb-3">
        <router-link to="/" class="brand-link-header text-decoration-none d-inline-flex flex-column align-items-center">
          <img
            v-if="currentTheme === 'dark'"
            src="/images/logo-bankjatim-white.png"
            alt="Bank Jatim"
            class="brand-logo"
          />
          <img
            v-else
            src="/images/logo-bankjatim.png"
            alt="Bank Jatim"
            class="brand-logo"
          />
          <div class="system-title mt-1">
            Inventory Management System
            <span class="badge text-bg-danger ms-1">JIMS</span>
          </div>
        </router-link>
      </div>

      <!-- Login Card -->
      <div class="card card-outline card-danger shadow-sm">
        <div class="card-body login-card-body">
          <p class="login-box-msg">Login</p>

          <div v-if="errorMessage" class="alert alert-danger py-2 px-3 fs-7 mb-3" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMessage }}
          </div>

          <form @submit.prevent="handleLogin">
            <div class="input-group mb-3">
              <input
                id="loginEmail"
                type="email"
                v-model="email"
                required
                class="form-control"
                placeholder="Email"
                autocomplete="username"
              />
              <div class="input-group-text">
                <i class="bi bi-envelope"></i>
              </div>
            </div>

            <div class="input-group mb-3">
              <input
                id="loginPassword"
                type="password"
                v-model="password"
                required
                class="form-control"
                placeholder="Password"
                autocomplete="current-password"
              />
              <div class="input-group-text">
                <i class="bi bi-lock-fill"></i>
              </div>
            </div>

            <!-- Row: Remember Me & Sign In Button -->
            <div class="row align-items-center mb-3">
              <div class="col-7">
                <div class="form-check mb-0">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    v-model="rememberMe"
                    id="rememberMe"
                  />
                  <label class="form-check-label fs-7" for="rememberMe"> Remember Me </label>
                </div>
              </div>
              <div class="col-5">
                <div class="d-grid">
                  <button type="submit" class="btn btn-danger fw-bold w-100" :disabled="loading">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                    Sign In
                  </button>
                </div>
              </div>
            </div>
          </form>

          <p class="mb-0 text-center">
            <a href="#" @click.prevent="showForgotMsg = true" class="forgot-link fs-7">
              I forgot my password
            </a>
          </p>
          <div v-if="showForgotMsg" class="alert alert-info py-1.5 px-2.5 fs-8 mt-2 mb-0">
            Silakan hubungi administrator IT Kantor Pusat (ext. 4421).
          </div>
        </div>
      </div>

      <div class="text-center mt-3 text-secondary fs-8">
        &copy; 2026 PT Bank Pembangunan Daerah Jawa Timur Tbk
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('admin@bankjatim.co.id');
const password = ref('');
const rememberMe = ref(true);
const loading = ref(false);
const errorMessage = ref('');
const showForgotMsg = ref(false);
const currentTheme = ref('light');

onMounted(() => {
  const resolved = document.documentElement.getAttribute('data-bs-theme') || localStorage.getItem('lte-theme') || 'light';
  currentTheme.value = resolved;
});

const handleLogin = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const res = await authStore.login(email.value, password.value);
    if (res.success) {
      router.push('/dashboard/operational');
    } else {
      errorMessage.value = res.message || res.error || 'Email atau kata sandi tidak valid.';
    }
  } catch (err) {
    errorMessage.value = 'Gagal menghubungi server API Gateway.';
  } finally {
    loading.value = false;
  }
};

</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bs-body-bg, #ffffff);
  padding: 1.5rem 1rem;
}

.login-box {
  width: 420px;
  max-width: 100%;
}

.brand-link-header {
  color: inherit;
}

.brand-logo {
  height: 48px;
  width: auto;
  max-width: 220px;
  object-fit: contain;
}

.system-title {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card-body {
  padding: 1.5rem;
}

.login-box-msg {
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 1.25rem;
}

.input-group {
  position: relative;
  display: flex;
  align-items: stretch;
  width: 100%;
}

.input-group .form-control {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.input-group-text {
  display: flex;
  align-items: center;
  padding: 0.45rem 0.75rem;
  background-color: var(--bs-tertiary-bg, #eef1f5);
  border: 1px solid var(--bs-border-color, #e2e8f0);
  border-left: 0;
  border-top-right-radius: 0.375rem;
  border-bottom-right-radius: 0.375rem;
  color: #64748b;
}

.row {
  display: flex;
  align-items: center;
}

.col-7 {
  flex: 0 0 58.333333%;
  max-width: 58.333333%;
}

.col-5 {
  flex: 0 0 41.666667%;
  max-width: 41.666667%;
}

.form-check {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.form-check-input {
  cursor: pointer;
  accent-color: var(--jatim-red, #D9252A);
}

.forgot-link {
  color: var(--jatim-red, #D9252A);
  text-decoration: none;
}
.forgot-link:hover {
  text-decoration: underline;
}

.text-body {
  color: var(--bs-body-color, #495057);
}

.text-secondary {
  color: #64748b !important;
}
</style>

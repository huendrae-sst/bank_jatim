<template>
  <div class="login-page bg-body-secondary d-flex align-items-center justify-content-center min-vh-100 py-4">
    <main class="login-box" style="width: 420px; max-width: 92%;">
      <!-- Login Logo -->
      <div class="login-logo text-center mb-3">
        <router-link to="/" class="text-decoration-none d-inline-flex flex-column align-items-center">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" class="brand-logo-light" style="height: 48px; width: auto; max-width: 220px; object-fit: contain;">
          <div class="fs-6 fw-bold text-body-secondary mt-1">
            Inventory Management System
            <span class="badge text-bg-danger ms-1">JIMS</span>
          </div>
        </router-link>
      </div>

      <!-- Card -->
      <div class="card card-outline card-danger shadow-sm">
        <div class="card-body login-card-body p-4">
          <p class="login-box-msg text-body-secondary text-center mb-3">
            Lupa kata sandi Anda? Masukkan alamat email terdaftar untuk menerima tautan reset kata sandi.
          </p>

          <form @submit.prevent="handleForgotPassword">
            <div class="input-group mb-3">
              <input 
                type="email" 
                v-model="email" 
                required 
                class="form-control" 
                placeholder="nama.pegawai@bankjatim.co.id" 
              />
              <span class="input-group-text">
                <i class="bi bi-envelope"></i>
              </span>
            </div>

            <div class="d-grid gap-2">
              <button type="submit" :disabled="loading" class="btn btn-danger fw-bold">
                <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                <span>Kirim Permintaan Reset</span>
              </button>
            </div>
          </form>

          <div class="text-center mt-3">
            <router-link to="/login" class="text-decoration-none fs-7 text-danger fw-semibold">
              <i class="bi bi-arrow-left me-1"></i> Kembali ke Halaman Login
            </router-link>
          </div>
        </div>
      </div>

      <div class="text-center mt-3 text-secondary fs-8">
        &copy; {{ new Date().getFullYear() }} PT Bank Pembangunan Daerah Jawa Timur Tbk
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { toast } from '@/utils/toast';

const email = ref('');
const loading = ref(false);

const handleForgotPassword = () => {
  if (!email.value) return;
  loading.value = true;

  setTimeout(() => {
    loading.value = false;
    toast.success(`Instruksi pemulihan kata sandi telah dikirim ke ${email.value}. Silakan periksa inbox email Anda.`);
  }, 800);
};
</script>

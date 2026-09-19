<template>
  <div class="register-page bg-body-secondary d-flex align-items-center justify-content-center min-vh-100 py-4">
    <main class="register-box" style="width: 440px; max-width: 92%;">
      <!-- Register Logo -->
      <div class="register-logo text-center mb-3">
        <router-link to="/" class="text-decoration-none d-inline-flex flex-column align-items-center">
          <img src="/images/logo-bankjatim.png" alt="Bank Jatim" class="brand-logo-light" style="height: 48px; width: auto; max-width: 220px; object-fit: contain;">
          <div class="fs-6 fw-bold text-body-secondary mt-1">
            Inventory Management System
            <span class="badge text-bg-danger ms-1">JIMS</span>
          </div>
        </router-link>
      </div>

      <!-- Register Card -->
      <div class="card card-outline card-danger shadow-sm">
        <div class="card-body register-card-body p-4">
          <p class="register-box-msg text-body-secondary text-center mb-3">Registrasi Akun Pegawai Baru</p>

          <div v-if="errorMessage" class="alert alert-danger py-2 px-3 fs-7 mb-3" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMessage }}
          </div>

          <form @submit.prevent="handleRegister">
            <!-- Full Name -->
            <div class="input-group mb-3">
              <input type="text" v-model="form.name" required class="form-control" placeholder="Nama Lengkap Pegawai" />
              <span class="input-group-text"><i class="bi bi-person"></i></span>
            </div>

            <!-- NIP / NIK -->
            <div class="input-group mb-3">
              <input type="text" v-model="form.nip" class="form-control" placeholder="NIP / NIK Pegawai" />
              <span class="input-group-text"><i class="bi bi-person-badge"></i></span>
            </div>

            <!-- Email -->
            <div class="input-group mb-3">
              <input type="email" v-model="form.email" required class="form-control" placeholder="Email Resmi Bank Jatim" />
              <span class="input-group-text"><i class="bi bi-envelope"></i></span>
            </div>

            <!-- Unit Kerja / Organization -->
            <div class="input-group mb-3">
              <select v-model="form.organization_id" required class="form-select fs-7">
                <option value="">-- Pilih Unit Kerja / Cabang --</option>
                <option value="1">[KPS] Kantor Pusat Surabaya</option>
                <option value="2">[LOG] Divisi Logistik & Umum</option>
                <option value="3">[CBR-SBY-UT] KC Surabaya Utama</option>
                <option value="4">[CBR-MLG] KC Malang</option>
                <option value="5">[CBR-KDR] KC Kediri</option>
                <option value="6">[CBR-JBR] KC Jember</option>
                <option value="7">[CBR-MDN] KC Madiun</option>
              </select>
              <span class="input-group-text"><i class="bi bi-building"></i></span>
            </div>

            <!-- Password -->
            <div class="input-group mb-3">
              <input type="password" v-model="form.password" required class="form-control" placeholder="Kata Sandi Baru" />
              <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
            </div>

            <!-- Confirm Password -->
            <div class="input-group mb-3">
              <input type="password" v-model="form.password_confirmation" required class="form-control" placeholder="Konfirmasi Kata Sandi" />
              <span class="input-group-text"><i class="bi bi-shield-lock"></i></span>
            </div>

            <!-- Terms Agreement & Register Button -->
            <div class="row align-items-center mb-3">
              <div class="col-8">
                <div class="form-check mb-0">
                  <input class="form-check-input" type="checkbox" v-model="form.agree" id="agreeTerms" required />
                  <label class="form-check-label fs-7" for="agreeTerms">
                    Saya menyetujui <a href="#" class="text-decoration-none text-danger" @click.prevent="showTermsModal = true">ketentuan</a>
                  </label>
                </div>
              </div>
              <div class="col-4">
                <div class="d-grid">
                  <button type="submit" :disabled="loading" class="btn btn-danger fw-bold">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                    <span>Daftar</span>
                  </button>
                </div>
              </div>
            </div>
          </form>

          <p class="mb-0 text-center">
            <router-link to="/login" class="text-center text-decoration-none fs-7 text-danger fw-semibold">
              Sudah memiliki akun? Masuk di sini
            </router-link>
          </p>
        </div>
      </div>

      <div class="text-center mt-3 text-secondary fs-8">
        &copy; {{ new Date().getFullYear() }} PT Bank Pembangunan Daerah Jawa Timur Tbk
      </div>
    </main>

    <!-- Terms Modal -->
    <div v-if="showTermsModal" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-body-tertiary d-flex align-items-center justify-content-between py-3 px-4 border-bottom">
            <h6 class="modal-title fs-6 fw-bold mb-0 text-body">Syarat & Ketentuan Penggunaan JIMS</h6>
            <button type="button" class="btn-close" @click="showTermsModal = false" aria-label="Close"></button>
          </div>
          <div class="modal-body fs-7 text-secondary">
            <p>Sistem Informasi Manajemen Persediaan (JIMS) PT Bank Pembangunan Daerah Jawa Timur Tbk hanya dapat diakses oleh pegawai dan staf resmi Bank Jatim.</p>
            <ul class="ps-3 mb-0">
              <li>Pengguna bertanggung jawab penuh menjaga kerahasiaan kredensial akun.</li>
              <li>Setiap transaksi pengadaan, persediaan, dan permohonan barang dicatat dalam audit trail log (ISO 27001).</li>
              <li>Penyalahgunaan hak akses persediaan dapat dikenakan sanksi sesuai kebijakan internal perbankan.</li>
            </ul>
          </div>
          <div class="modal-footer d-flex justify-content-end align-items-center gap-2 py-2 px-3">
            <button type="button" class="btn btn-sm btn-secondary" @click="showTermsModal = false">Batal</button>
            <button type="button" class="btn btn-sm btn-danger fw-bold" @click="showTermsModal = false">Saya Mengerti</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(false);
const errorMessage = ref('');
const showTermsModal = ref(false);

const form = reactive({
  name: '',
  nip: '',
  email: '',
  organization_id: '',
  password: '',
  password_confirmation: '',
  agree: false
});

const handleRegister = () => {
  if (form.password !== form.password_confirmation) {
    errorMessage.value = 'Konfirmasi kata sandi tidak cocok dengan kata sandi.';
    return;
  }
  loading.value = true;
  errorMessage.value = '';

  setTimeout(() => {
    loading.value = false;
    router.push('/login');
  }, 1000);
};
</script>

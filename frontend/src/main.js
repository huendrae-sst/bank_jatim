import { createApp } from 'vue';
import { createPinia } from 'pinia';
import PrimeVue from 'primevue/config';
import Aura from '@primevue/themes/aura';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import App from './App.vue';
import router from './router';
import PaginationFooter from './components/PaginationFooter.vue';
import LoadingState from './components/LoadingState.vue';
import Toast from 'primevue/toast';
import { toast } from './utils/toast.js';

import 'bootstrap-icons/font/bootstrap-icons.css';
import 'primeicons/primeicons.css';
import './assets/main.css';

const app = createApp(App);

app.component('PaginationFooter', PaginationFooter);
app.component('LoadingState', LoadingState);
app.component('ServerLoading', LoadingState);
app.component('Toast', Toast);

app.config.globalProperties.$toastNotify = toast;

app.use(createPinia());
app.use(router);
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '[data-bs-theme="dark"]'
    }
  }
});
app.use(ConfirmationService);
app.use(ToastService);

app.mount('#app');


<template>
  <div class="card-footer bg-body border-top py-2.5 px-3" :class="{ 'px-md-4': !compact }">
    <nav role="navigation" aria-label="Pagination Navigation" class="w-100">
      <!-- 1. Compact Mode (Untuk kolom sempit / master-detail sidebar agar tidak bertumpuk) -->
      <div v-if="compact" class="d-flex flex-column gap-2">
        <div class="d-flex align-items-center justify-content-between fs-8 text-secondary">
          <span>
            <template v-if="total > 0">
              <strong class="text-body">{{ fromItem }}</strong>-<strong class="text-body">{{ toItem }}</strong> dari <strong class="text-body">{{ total.toLocaleString('id-ID') }}</strong>
            </template>
            <template v-else>0 data</template>
          </span>
          <div class="d-inline-flex align-items-center gap-1">
            <span class="fs-9 text-secondary">Baris:</span>
            <select
              :value="perPage"
              @change="onPerPageSelect($event.target.value)"
              class="form-select form-select-sm fs-9 py-0 px-1 shadow-xs"
              style="width: 58px; height: 26px;"
            >
              <option v-for="sz in allSizes" :key="sz" :value="sz">{{ sz }}</option>
            </select>
          </div>
        </div>
        <div class="d-flex align-items-center justify-content-center gap-1">
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary px-2 py-0.5 fs-8"
            :disabled="currentPage <= 1 || total === 0"
            @click="changePage(1)"
            title="Halaman Pertama"
          >
            <i class="bi bi-chevron-double-left fs-9"></i>
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary px-2 py-0.5 fs-8"
            :disabled="currentPage <= 1 || total === 0"
            @click="changePage(currentPage - 1)"
            title="Halaman Sebelumnya"
          >
            <i class="bi bi-chevron-left fs-9"></i>
          </button>
          <span class="fs-8 text-secondary px-1 font-monospace">
            Hal <strong class="text-body">{{ total === 0 ? 0 : currentPage }}</strong> / {{ totalPages }}
          </span>
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary px-2 py-0.5 fs-8"
            :disabled="currentPage >= totalPages || total === 0"
            @click="changePage(currentPage + 1)"
            title="Halaman Berikutnya"
          >
            <i class="bi bi-chevron-right fs-9"></i>
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary px-2 py-0.5 fs-8"
            :disabled="currentPage >= totalPages || total === 0"
            @click="changePage(totalPages)"
            title="Halaman Terakhir"
          >
            <i class="bi bi-chevron-double-right fs-9"></i>
          </button>
        </div>
      </div>

      <!-- 2. Standard Mode (Untuk kartu lebar penuh / table grid) -->
      <div v-else class="row align-items-center justify-content-between w-100 g-2 my-0">
        <!-- 1. Left: Data Counter Info (Rata Kiri) -->
        <div class="col-12 col-md-4 text-start d-flex align-items-center justify-content-md-start justify-content-center order-3 order-md-1">
          <p class="text-secondary fs-8 mb-0">
            <template v-if="total > 0">
              Menampilkan
              <span class="fw-bold text-body">{{ fromItem }}</span>
              sampai
              <span class="fw-bold text-body">{{ toItem }}</span>
              dari
              <span class="fw-bold text-body">{{ total.toLocaleString('id-ID') }}</span>
              data
            </template>
            <template v-else>
              Menampilkan <span class="fw-bold text-body">0</span> data
            </template>
          </p>
        </div>

        <!-- 2. Center: Baris Per Halaman (Tengah) -->
        <div class="col-12 col-md-4 text-center d-flex align-items-center justify-content-center order-2 order-md-2">
          <div class="d-inline-flex align-items-center gap-2">
            <span class="text-secondary fs-8 fw-semibold text-nowrap">Baris per halaman:</span>
            <select
              :value="perPage"
              @change="onPerPageSelect($event.target.value)"
              class="form-select form-select-sm fs-8 shadow-xs py-1"
              style="width: 75px;"
            >
              <option v-for="sz in allSizes" :key="sz" :value="sz">
                {{ sz }}
              </option>
            </select>
          </div>
        </div>

        <!-- 3. Right: Pagination Controls << < 1 2 .. 10 > >> (Rata Kanan) -->
        <div class="col-12 col-md-4 text-end d-flex align-items-center justify-content-md-end justify-content-center order-1 order-md-3">
          <div class="d-inline-flex align-items-center gap-1">
            <!-- First Page Link (<<) -->
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary px-2 py-1 fs-8"
              :disabled="currentPage <= 1 || total === 0"
              @click="changePage(1)"
              title="Halaman Pertama"
            >
              <i class="bi bi-chevron-double-left fs-9"></i>
            </button>

            <!-- Previous Page Link (<) -->
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary px-2.5 py-1 fs-8"
              :disabled="currentPage <= 1 || total === 0"
              @click="changePage(currentPage - 1)"
              title="Halaman Sebelumnya"
            >
              <i class="bi bi-chevron-left fs-9"></i>
            </button>

            <!-- Page Number Buttons (1 2 .. 10) -->
            <template v-for="(p, index) in visiblePages" :key="index">
              <span v-if="p === '..'" class="px-2 py-1 fs-8 text-secondary font-monospace">..</span>
              <button
                v-else
                type="button"
                class="btn btn-sm px-2.5 py-1 fs-8"
                :class="p === currentPage ? 'btn-danger fw-bold' : 'btn-outline-secondary'"
                :disabled="total === 0"
                @click="changePage(p)"
              >
                {{ p }}
              </button>
            </template>

            <!-- Next Page Link (>) -->
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary px-2.5 py-1 fs-8"
              :disabled="currentPage >= totalPages || total === 0"
              @click="changePage(currentPage + 1)"
              title="Halaman Berikutnya"
            >
              <i class="bi bi-chevron-right fs-9"></i>
            </button>

            <!-- Last Page Link (>>) -->
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary px-2 py-1 fs-8"
              :disabled="currentPage >= totalPages || total === 0"
              @click="changePage(totalPages)"
              title="Halaman Terakhir"
            >
              <i class="bi bi-chevron-double-right fs-9"></i>
            </button>
          </div>
        </div>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  total: {
    type: Number,
    required: true,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 1
  },
  perPage: {
    type: Number,
    default: 10
  },
  sizes: {
    type: Array,
    default: () => [5, 10, 15, 25, 50, 100]
  },
  compact: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:currentPage', 'update:perPage', 'change']);

const totalPages = computed(() => {
  if (props.total <= 0) return 1;
  return Math.ceil(props.total / (props.perPage || 10)) || 1;
});

const fromItem = computed(() => {
  if (props.total === 0) return 0;
  return (props.currentPage - 1) * props.perPage + 1;
});

const toItem = computed(() => {
  if (props.total === 0) return 0;
  return Math.min(props.currentPage * props.perPage, props.total);
});

const allSizes = computed(() => {
  const s = [...props.sizes];
  if (!s.includes(props.perPage) && props.perPage > 0) {
    s.push(props.perPage);
    s.sort((a, b) => a - b);
  }
  return s;
});

const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = props.currentPage;

  if (total <= 1) {
    return [1];
  }

  if (total <= 7) {
    const pages = [];
    for (let i = 1; i <= total; i++) pages.push(i);
    return pages;
  }

  const pages = [];
  pages.push(1);

  if (current > 3) {
    pages.push('..');
  }

  const start = Math.max(2, current - 1);
  const end = Math.min(total - 1, current + 1);
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  if (current < total - 2) {
    pages.push('..');
  }

  pages.push(total);
  return pages;
});

const changePage = (p) => {
  if (p >= 1 && p <= totalPages.value && p !== props.currentPage) {
    emit('update:currentPage', p);
    emit('change', { page: p, perPage: props.perPage });
  }
};

const onPerPageSelect = (val) => {
  const num = parseInt(val, 10);
  emit('update:perPage', num);
  emit('update:currentPage', 1);
  emit('change', { page: 1, perPage: num });
};
</script>

<template>
  <span :class="['badge fs-8 text-uppercase', badgeClass]">
    <i v-if="icon" :class="['bi', icon, 'me-1']"></i>
    {{ label || (status ? status.replace(/_/g, ' ') : '') }}
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: ''
  }
});

const badgeClass = computed(() => {
  const s = props.status?.toUpperCase() || '';
  switch (s) {
    case 'COMPLETED':
    case 'RECEIVED':
    case 'APPROVED':
    case 'POSTED':
    case 'DELIVERED':
    case 'VALID':
      return 'text-bg-success';
    case 'IN_TRANSIT':
      return 'text-bg-info';
    case 'READY_TO_SHIP':
    case 'ALLOCATED':
      return 'text-bg-primary';
    case 'WAITING_APPROVAL':
    case 'SUBMITTED':
    case 'PICKING':
    case 'PACKED':
    case 'PROPOSED':
      return 'text-bg-warning';
    case 'REJECTED':
    case 'CANCELLED':
    case 'DISCREPANCY':
    case 'DAMAGED':
    case 'FAILED':
      return 'text-bg-danger';
    case 'DRAFT':
    case 'UPLOADED':
    case 'ASSIGNED':
    case 'CREATED':
      return 'text-bg-secondary';
    default:
      return 'text-bg-light border';
  }
});

const icon = computed(() => {
  const s = props.status?.toUpperCase() || '';
  if (['APPROVED', 'RECEIVED', 'COMPLETED', 'POSTED', 'DELIVERED'].includes(s)) return 'bi-check-circle';
  if (['WAITING_APPROVAL', 'IN_TRANSIT', 'PICKING'].includes(s)) return 'bi-clock';
  if (['REJECTED', 'DISCREPANCY', 'DAMAGED', 'CANCELLED', 'FAILED'].includes(s)) return 'bi-exclamation-triangle';
  return '';
});
</script>

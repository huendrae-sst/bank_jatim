<template>
  <router-view />
  <Toast position="top-right" />
  <GlobalLoadingToast />
</template>

<script setup>
import Toast from 'primevue/toast';
import GlobalLoadingToast from '@/components/GlobalLoadingToast.vue';
</script>

<style>
</style>

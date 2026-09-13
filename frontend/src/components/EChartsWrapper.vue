<template>
  <div ref="chartRef" :style="{ width: '100%', height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  options: {
    type: Object,
    required: true
  },
  height: {
    type: String,
    default: '350px'
  }
});

const chartRef = ref(null);
let chartInstance = null;
let resizeObserver = null;

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    chartInstance.setOption(props.options);
  }
};

watch(() => props.options, (newOptions) => {
  if (chartInstance) {
    chartInstance.setOption(newOptions, true);
  }
}, { deep: true });

onMounted(() => {
  initChart();
  if (window.ResizeObserver && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      chartInstance?.resize();
    });
    resizeObserver.observe(chartRef.value);
  }
});

onUnmounted(() => {
  resizeObserver?.disconnect();
  chartInstance?.dispose();
});
</script>

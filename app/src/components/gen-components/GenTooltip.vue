<template>
  <span ref="tooltipElement" data-bs-toggle="tooltip" :data-bs-placement="placement" :data-bs-title="tip">
    <slot name="main"></slot>
  </span>
</template>

<script setup lang="ts">
import * as bootstrap from 'bootstrap';
import { onBeforeUpdate, onMounted, onUnmounted, onUpdated, ref } from 'vue'

const props = defineProps<{
  tip: string
  placement: string
}>()

const tooltipElement = ref<HTMLElement | null>(null)
const tooltipInstance = ref<bootstrap.Tooltip | null>(null)

onMounted(() => {
  if (tooltipElement.value) {
    tooltipInstance.value = new bootstrap.Tooltip(tooltipElement.value)
  }
})

onUpdated(() => {
  if (tooltipElement.value) {
    tooltipInstance.value = new bootstrap.Tooltip(tooltipElement.value)
  }
})

onBeforeUpdate(() => {
  if (tooltipElement.value) {
    tooltipInstance.value = bootstrap.Tooltip.getInstance(tooltipElement.value)
  }
  if (tooltipInstance.value) {
    tooltipInstance.value?.dispose()
  }
})

onUnmounted(() => {
  if (tooltipInstance.value) {
    tooltipInstance.value?.dispose()
  }
})


</script>

<style scoped lang="scss">

</style>
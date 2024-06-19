<template>
  <img ref="buffElement" :src="buffIcon" :alt="buffName" data-bs-toggle="tooltip"
  data-bs-placement="bottom" :title="buffName" />
</template>

<script setup lang="ts">
import { defineProps, computed, onMounted, ref, onBeforeUpdate, watch, onUpdated, onUnmounted } from 'vue'
import * as bootstrap from 'bootstrap'

let tooltip: any = null
const buffElement = ref()

const props = defineProps({
  buffId: Number,
  buffName: String,
  urlPath: String,
})

const buffIcon: any = computed(() => {
  return `${props.urlPath}${props.buffId}.png`
})

onUpdated(() => {
  tooltip = new bootstrap.Tooltip(buffElement.value)
})

onMounted(() => {
  tooltip = new bootstrap.Tooltip(buffElement.value)
})

onBeforeUpdate(() => {
  tooltip = bootstrap.Tooltip.getInstance(buffElement.value)
  if (tooltip) {
    tooltip.dispose()
  }
})

onUnmounted(() => {
  if (tooltip) {
    tooltip.dispose()
  }
})

</script>

<style scoped lang="scss">
</style>
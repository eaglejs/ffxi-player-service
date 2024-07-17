<template>
  <img ref="buffElement" :src="buffIcon" :alt="buffName" data-bs-toggle="tooltip"
  data-bs-placement="bottom" :title="buffName" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUpdate, watch, onUpdated, onUnmounted } from 'vue'
import * as bootstrap from 'bootstrap'
import { iconsPath } from '@/helpers/config'

let tooltip: any = null
const buffElement = ref()

const props = defineProps({
  buffId: Number,
  buffName: String,
})

const buffIcon: any = computed(() => {
  return `${iconsPath}${props.buffId}.webp`
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
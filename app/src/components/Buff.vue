<template>
  <img ref="buffElement" :src="buffIcon" :alt="buffName" data-bs-toggle="tooltip"
  data-bs-placement="bottom" :title="buffName" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUpdate, onUpdated, onUnmounted } from 'vue'
import type { ComputedRef } from 'vue'
import * as bootstrap from 'bootstrap'
import { iconsPath } from '@/helpers/config'

let tooltip: bootstrap.Tooltip | null = null
const buffElement = ref()

const props = defineProps({
  buffId: Number,
  buffName: String,
})

const buffIcon: ComputedRef<string> = computed(() => {
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
  tooltip?.dispose()
})

</script>

<style scoped lang="scss">
</style>
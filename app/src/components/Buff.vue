<template>
  <img ref="buffElement" :src="buffIcon" :alt="buffName" data-bs-toggle="tooltip"
  data-bs-placement="bottom" :title="buffName" />
</template>

<script setup lang="ts">
import { defineProps, computed, onMounted, ref, onUnmounted, watch } from 'vue'
import * as bootstrap from 'bootstrap'

const buffElement = ref()
const tooltip = ref()

const props = defineProps({
  buffId: Number,
  buffName: String,
  urlPath: String,
})

const buffIcon: any = computed(() => {
  return `${props.urlPath}${props.buffId}.png`
})

</script>

<style scoped lang="scss">
</style>
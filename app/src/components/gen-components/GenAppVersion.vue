<template>
  <div class="app-version">
    <p class="version-text my-2 mx-0 text-center">Version {{ version }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { uiPackageVersion } from '@/helpers/config'

const props = defineProps<{
  themeColor: string
}>()

const version = computed(() => uiPackageVersion)
</script>

<style scoped lang="scss">
.app-version {
  margin: 0 auto;
  max-width: 1920px;
  width: 100%;
  .version-text {
    font-size: 0.75rem;
    color: color-mix(in srgb, v-bind('props.themeColor') 35%, transparent);
  }
}
</style>

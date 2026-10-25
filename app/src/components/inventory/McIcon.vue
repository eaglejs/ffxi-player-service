<template>
  <svg
    class="mc-icon"
    viewBox="0 0 24 24"
    :width="size"
    :height="size"
    fill="currentColor"
    aria-hidden="true"
    focusable="false"
  >
    <path :d="path" />
  </svg>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ path: string; size?: number }>(), { size: 20 })
</script>

<style scoped lang="scss">
.mc-icon {
  flex: none;
  vertical-align: middle;
}
</style>

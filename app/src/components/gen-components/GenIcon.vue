<template>
  <div class="icon-wrapper">
    <svg
      aria-hidden="true"
      class="iconify iconify--mdi"
      :viewBox="`0 0 25 25`"
      :width="iconSize"
      :height="iconSize"
      fill="currentColor"
      role="img"
      preserveAspectRatio="xMidYMid meet"
    >
      <path class="fill" :d="icon" />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed, type ComputedRef } from 'vue'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  icon: String,
  size: {
    type: String,
    required: true,
    validator: (value: string) => ['sm', 'md', 'lg', 'xl', '2xl'].includes(value)
  }
})
const themeStore = useThemeStore()
const theme: ComputedRef<string> = computed(() => themeStore.theme)
const themeColor: ComputedRef<string> = computed(() => (theme.value === 'dark' ? '#fff' : '#000'))

const iconSize = computed(() => {
  switch (props.size) {
    case 'sm':
      return 16
    case 'md':
      return 20
    case 'lg':
      return 24
    case 'xl':
      return 30
    case '2xl':
      return 40
    default:
      return 20
  }
})
</script>

<style scoped lang="scss">
.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: v-bind(iconSize);
  width: 100%;
}
.fill {
  fill: v-bind(themeColor);
}
</style>

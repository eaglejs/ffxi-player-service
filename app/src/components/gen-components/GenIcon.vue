<template>
  <svg 
    aria-hidden="true"
    class="iconify iconify--mdi"
    :width="iconSize"
    :height="iconSize" 
    fill="currentColor"
    role="img"
    preserveAspectRatio="xMidYMid meet"
  >
    <path class="fill" :d="icon" />
  </svg>
</template>

<script setup lang="ts">
import { computed, type ComputedRef } from 'vue'
import { useThemeStore } from '@/stores/theme';

const props = defineProps({
  icon: String,
  size: {
    type: String,
    required: true,
    validator: (value: string) => ['sm', 'md', 'lg', 'xl'].includes(value)
  },
})
const themeStore = useThemeStore()
const theme: ComputedRef<string> = computed(() => themeStore.theme)
const themeColor: ComputedRef<string> = computed(() => (theme.value === 'dark' ? '#fff' : '#000'))

const iconSize = computed(() => {
  switch (props.size) {
    case 'sm':
      return '16px';
    case 'md':
      return '20px';
    case 'lg':
      return '24px';
    case 'xl':
      return '30px';
    default:
      return '20px';
  }
})
</script>

<style scoped lang="scss">
.fill {
  fill: v-bind(themeColor);
}
</style>
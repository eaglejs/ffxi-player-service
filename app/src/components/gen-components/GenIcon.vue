<template>
  <svg 
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    aria-hidden="true"
    class="iconify iconify--mdi"
    :width="iconSize"
    :height="iconSize" 
    fill="currentColor"
    role="img"
    preserveAspectRatio="xMidYMid meet"
  >
    <path :d="icon" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  icon: String,
  size: {
    type: String,
    required: true,
    validator: (value: string) => ['sm', 'md', 'lg'].includes(value)
  }
})

const iconSize = computed(() => {
  switch (props.size) {
    case 'sm':
      return '16px';
    case 'md':
      return '20px';
    case 'lg':
      return '24px';
    default:
      return '20px';
  }
})
</script>
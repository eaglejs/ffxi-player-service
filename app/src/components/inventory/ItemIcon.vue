<template>
  <span class="item-icon" :class="{ 'is-fallback': showFallback }" :style="{ '--tile': `${tile}px` }">
    <img
      v-if="!showFallback"
      :src="src"
      alt=""
      width="32"
      height="32"
      loading="lazy"
      decoding="async"
      @error="failed = true"
    />
    <McIcon v-else :path="kind === 'key' ? mdiKeyVariant : mdiCubeOutline" :size="20" />
  </span>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { mdiCubeOutline, mdiKeyVariant } from '@mdi/js'
import { fullUrl } from '@/helpers/config'
import McIcon from '@/components/inventory/McIcon.vue'

const props = withDefaults(
  defineProps<{ id: number; kind?: 'item' | 'key'; tile?: number }>(),
  { kind: 'item', tile: 44 }
)

// The backend serves each icon from its permanent store; a 404 means it has not been
// extracted from the game yet, so a neutral placeholder is shown instead.
const src = computed(() => `${fullUrl}/items/icon/${props.id}.png`)
const failed = ref(false)
const showFallback = computed(() => props.kind === 'key' || failed.value)

watch(
  () => props.id,
  () => {
    failed.value = false
  }
)
</script>

<style scoped lang="scss">
.item-icon {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.08);
  color: var(--bs-secondary-color);
  display: inline-flex;
  flex: none;
  height: var(--tile);
  justify-content: center;
  transition:
    border-color 0.15s ease,
    transform 0.15s ease;
  width: var(--tile);

  img {
    image-rendering: pixelated;
    filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.25));
  }
}
</style>

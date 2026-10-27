<template>
  <section class="card window" :aria-label="label">
    <header class="card-header window-head">
      <slot name="header" />
    </header>
    <div class="card-body window-body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="card-footer window-foot">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup lang="ts">
defineProps<{ label: string }>()
</script>

<style scoped lang="scss">
.window {
  height: 100%;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.window-head {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem 1rem;
  justify-content: space-between;
}

.window-body {
  padding: 0.5rem 1rem;
}
</style>

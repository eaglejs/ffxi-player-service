<template>
  <section class="window" :aria-label="label">
    <header class="window-head">
      <slot name="header" />
    </header>
    <slot />
  </section>
</template>

<script setup lang="ts">
// The navy-and-gold panel used for character results, in the spirit of FFXI's menu windows.
defineProps<{ label: string }>()
</script>

<style scoped lang="scss">
.window {
  background: linear-gradient(180deg, #0c2d66 0%, #031633 100%);
  border: 1px solid #c9b27c;
  border-radius: 12px;
  box-shadow:
    inset 0 0 0 3px #031633,
    inset 0 0 0 4px rgb(201 178 124 / 35%),
    0 6px 18px rgb(0 0 0 / 28%);
  color: #fff;
  padding: 0.5rem 1rem 0.75rem;
}

.window-head {
  align-items: baseline;
  border-bottom: 1px solid rgb(201 178 124 / 30%);
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 1rem;
  justify-content: space-between;
  padding: 0.5rem 0.25rem;
}
</style>

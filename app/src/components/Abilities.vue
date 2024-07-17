<template>
  <div class="row">
    <div class="col-12 row" v-if="abilities.length">
      <h2>Abilities</h2>
      <section class="col-2" v-for="ability in abilities" :key="ability.ability">
        <p class="m-0">{{ ability.ability }}</p>
        <p>{{ ability.recast }}</p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Ability } from '@/types/Ability'

const props = defineProps<{
  abilities: Ability[]
}>()

</script>

<style scoped lang="scss">

</style>
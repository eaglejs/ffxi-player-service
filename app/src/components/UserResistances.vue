<template>
  <div class="card">
    <div class="card-header">
      <h3 class="p-0 m-0">Elemental Resistance</h3>
    </div>
    <div class="card-body">
      <div class="row">
        <section class="col-12">
          <span v-for="resistance in resistancesGroupLight" :key="resistance.id">
            <img :src="`${iconsPath}${resistance.id}.webp`" class="me-2" />
            <span class="me-2">
              {{ getValue(resistance.id) }}
            </span>
          </span>
        </section>
        <section class="col-12">
          <span v-for="resistance in resistancesGroupDark" :key="resistance.id">
            <img :src="`${iconsPath}${resistance.id}.webp`" class="me-2" />
            <span class="me-2">
              {{ getValue(resistance.id) }}
            </span>
          </span>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { iconsPath } from '@/helpers/config'

const resistancesGroupLight = [
  { id: 'fire', name: 'Fire' },
  { id: 'wind', name: 'Wind' },
  { id: 'lightning', name: 'Lightning' },
  { id: 'light', name: 'Light' },
]

const resistancesGroupDark = [
  { id: 'ice', name: 'Ice' },
  { id: 'earth', name: 'Earth' },
  { id: 'water', name: 'Water' },
  { id: 'dark', name: 'Dark' },
]

const props = defineProps({
  resistances: Object,
})

const getValue = (resistance: string) => {
  return `${props.resistances?.[resistance + 'Resistance']}`
}

</script>